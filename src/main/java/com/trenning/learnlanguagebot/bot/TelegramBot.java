package com.trenning.learnlanguagebot.bot;

import com.trenning.learnlanguagebot.constants.LanguageConstants;
import com.trenning.learnlanguagebot.entity.User;
import com.trenning.learnlanguagebot.entity.UserState;
import com.trenning.learnlanguagebot.entity.Word;
import com.trenning.learnlanguagebot.service.*;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

public class TelegramBot extends TelegramLongPollingBot {

    private final MessageService messageService;
    private final StateService stateService;
    private final UserService userService;
    private final WordService wordService;

    public TelegramBot(DefaultBotOptions options,
                       String botToken,
                       MessageService messageService,
                       StateService stateService,
                       UserService userService,
                       WordService wordService) {
        super(options, botToken);
        this.messageService = messageService;
        this.stateService = stateService;
        this.userService = userService;
        this.wordService = wordService;
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleTextMessage(Update update) throws TelegramApiException {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        SendMessage response = switch (text) {
            case "/start" -> handleStartCommand(chatId, update);
            case "👤 Профиль" -> handleInfoCommand(chatId);
            case "❓ Помощь" ->handleHelpCommand(chatId);
            case "📚 Учить язык" -> messageService.createMenuToStartLearn(chatId);
            case "➕ Добавить слово" -> handleAddWordCommand(chatId);
            case "🔙 Назад" -> handleBackCommand(chatId);
            case "📖 Показать мои слова" -> handleShowWordsCommand(chatId);
            case "➡️ Следующее слово" -> handleNextWordCommand(chatId);
            case "❌ Удалить" -> handleDeleteWordCommand(chatId);
            case "🌍 Сменить язык" -> handleChangeLanguageCommand(chatId);
            case "🏠 Главное меню" -> handleMainMenu(chatId);
            default -> handleDefaultCommand(chatId, text);
        };

        if (response != null) {
            execute(response);
        }
    }

    private SendMessage handleHelpCommand(Long chatId) {
        String helpMessage = """
            🆘 *Помощь по использованию LanguageBot* 🆘

            Вот основные команды и возможности бота:

            📚 *Учить язык* — начните изучать новые слова. Вы можете:
               - Добавлять слова вручную.
               - Просматривать свои слова.
               - Отмечать слова как выученные.

            👤 *Профиль* — посмотрите ваш прогресс:
               - Количество выученных слов.
               - Ваш текущий статус (новичок, ученик, эксперт и т.д.).

            🌍 *Сменить язык* — выберите язык, который вы хотите изучать.

            ❌ *Удалить слово* — удалите слово из вашего списка.

            ➡️ *Следующее слово* — перейдите к следующему слову.

            🔙 *Назад* — вернитесь в предыдущее меню.

            🏠 *Главное меню* — вернитесь в главное меню.

            Если у вас есть вопросы, напишите мне: @BodyaPryadko .
            """;

        SendMessage message = new SendMessage(chatId.toString(), helpMessage);
        message.setParseMode("Markdown");
        return message;
    }

    private SendMessage handleStartCommand(Long chatId, Update update) {
        var name = update.getMessage().getFrom().getUserName();
        var userFromDb = userService.findByChatId(chatId);

        if (userFromDb == null) {
            User user = new User();
            user.setName(name);
            user.setChatId(chatId);
            user.setUserState(UserState.DEFAULT);
            user.setRating(0);
            userService.save(user);
        }
        return messageService.createMainMenu(chatId);
    }



    private SendMessage handleInfoCommand(Long chatId) {
        User user = userService.findByChatId(chatId);
        return messageService.createUserInfoMessage(chatId, user);
    }

    private SendMessage handleBackCommand(Long chatId) {
        stateService.resetUserState(chatId);
        return messageService.createMenuToStartLearn(chatId);
    }

    private SendMessage handleNextWordCommand(Long chatId) {
        User user = userService.findByChatId(chatId);
        List<Word> words = wordService.getUserWords(user);

        if (words.isEmpty()) {
            return messageService.createSimpleMessage(chatId, "📭 У вас пока нет добавленных слов.");
        }

        int nextIndex = (user.getCurrentWordIndex() + 1) % words.size();
        user.setCurrentWordIndex(nextIndex);
        userService.save(user);

        Word nextWord = words.get(nextIndex);
        return messageService.createWordInfoMessage(chatId, nextWord);
    }

    private SendMessage handleDeleteWordCommand(Long chatId) {
        User user = userService.findByChatId(chatId);
        List<Word> words = wordService.getUserWords(user);

        if (words.isEmpty()) {
            return messageService.createSimpleMessage(chatId, "📭 Нет слов для удаления.");
        }

        int currentIndex = user.getCurrentWordIndex() % words.size();
        Word wordToDelete = words.get(currentIndex);
        wordService.deleteWordForUser(wordToDelete, user);

        return handleNextWordCommand(chatId);
    }

    private SendMessage handleDefaultCommand(Long chatId, String text) {
        if (stateService.getUserState(chatId) == UserState.WAITING_FOR_WORD) {
            stateService.resetUserState(chatId);
            Optional<Word> word = wordService.searchAndSaveWord(text, userService.findByChatId(chatId));
            return word.map(value -> messageService.createWordInfoMessageWhenAddNew(chatId, value))
                    .orElseGet(() -> messageService.createSimpleMessage(chatId, "❌ Слово не найдено."));
        }
        return messageService.createSimpleMessage(chatId, "❓ Неизвестная команда.");
    }

    private SendMessage handleChangeLanguageCommand(Long chatId) {
        User user = userService.findByChatId(chatId);
        return messageService.createLanguageMenu(chatId, user.getCurrentLanguage());
    }

    private SendMessage handleAddWordCommand(Long chatId) {
        User user = userService.findByChatId(chatId);

        if (user == null) {
            return messageService.createSimpleMessage(chatId, "❌ Пользователь не найден. Пожалуйста, начните с команды /start.");
        }

        if (user.getCurrentLanguage() == null) {
            return messageService.createSimpleMessage(chatId, "🌍 Сначала выберите язык.");
        }

        stateService.setUserState(chatId, UserState.WAITING_FOR_WORD);
        return messageService.createSimpleMessage(chatId, "✍️ Теперь вы можете ввести слово, которое будет добавлено в бд.");
    }

    private SendMessage handleShowWordsCommand(Long chatId) throws TelegramApiException {
        User user = userService.findByChatId(chatId);

        if (user.getCurrentLanguage() == null) {
            return messageService.createSimpleMessage(chatId, "🌍 Сначала выберите язык.");
        }

        List<Word> words = wordService.getUserWords(user);

        if (words.isEmpty()) {
            return messageService.createSimpleMessage(chatId, "📭 У вас пока нет добавленных слов.");
        }

        Word word = words.get(0);
        return messageService.createWordInfoMessage(chatId, word);
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackQueryId = callbackQuery.getId();
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQueryId);
            execute(answerCallbackQuery);

            if (callbackData.startsWith("language_")) {
                String selectedLanguageCode = callbackData.split("_")[1];
                SendMessage response = handleLanguageSelection(chatId, selectedLanguageCode);
                if (response != null) {
                    execute(response);
                }
            } else if (callbackData.startsWith("learned_")) {
                Long wordId = Long.parseLong(callbackData.split("_")[1]);
                SendMessage response = handleLearnedWord(chatId, wordId);
                if (response != null) {
                    execute(response);
                }
            }
        }
    }

    private SendMessage handleLearnedWord(Long chatId, Long wordId) throws TelegramApiException {
        User user = userService.findByChatId(chatId);
        Word word = wordService.findById(wordId).orElse(null);

        if (word != null && user != null) {

            user.setRating(user.getRating()+1);
            userService.save(user);

            wordService.deleteWordForUser(word, user);

            SendMessage confirmationMessage = messageService.createSimpleMessage(chatId, "Слово добавлено в выученные! Так держать \uD83D\uDE18");
            execute(confirmationMessage);

            return handleNextWordCommand(chatId);
        }

        return messageService.createSimpleMessage(chatId, "❌ Ошибка при обработке слова.");
    }


    private SendMessage handleLanguageSelection(Long chatId, String languageCode) {
        User user = userService.findByChatId(chatId);

        if (user != null) {
            user.setCurrentLanguage(languageCode);
            userService.save(user);
            return messageService.createSimpleMessage(chatId, "✅ Язык выбран: " + LanguageConstants.LANGUAGES.get(languageCode));
        }

        return messageService.createSimpleMessage(chatId, "❌ Пользователь не найден.");
    }

    private SendMessage handleMainMenu(Long chatId) {
        stateService.resetUserState(chatId);
        return messageService.createMainMenu(chatId);
    }

    @Override
    public String getBotUsername() {
        return "LanguageBot";
    }
}