package com.trenning.learnlanguagebot.service;

import com.trenning.learnlanguagebot.constants.LanguageConstants;
import com.trenning.learnlanguagebot.entity.User;
import com.trenning.learnlanguagebot.entity.Word;
import com.trenning.learnlanguagebot.keyboard.KeyboardFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final KeyboardFactory keyboardFactory;

    // Основные текстовые сообщения
    private static final String WELCOME_MESSAGE = """
            🎉 *Добро пожаловать в LanguageBot!* 🎉

            Этот бот поможет вам учить новые слова и улучшать ваш словарный запас. Вот что вы можете делать:

            📚 *Учить язык* — начните изучать новые слова.
            👤 *Профиль* — посмотрите ваш прогресс и статус.
            🌍 *Сменить язык* — выберите язык для изучения.
            ❓ *Помощь* — узнайте, как использовать бота.

            Перед использованием обязательно укажите язык, который вы учите 🌍
            """;

    private static final String LEARN_MENU_MESSAGE = "Можете выбрать дальнейшее действие \uD83D\uDCCC";
    private static final String PROFILE_MESSAGE_TEMPLATE = """
            Ваш профиль:
            ID: %d
            Имя: %s
            Рейтинг: %d
            Статус: %s %s
            """;
    private static final String WORD_INFO_TEMPLATE = "Слово: %s\nПеревод: %s\nТранскрипция: %s";
    private static final String WORD_ADDED_MESSAGE = "Слово добавлено в выученные! Так держать \uD83D\uDE18";
    private static final String LANGUAGE_MENU_MESSAGE = "Выберите язык: \uD83C\uDF0F";

    // Методы для создания сообщений

    public SendMessage createMainMenu(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), WELCOME_MESSAGE);
        message.setReplyMarkup(createMainMenuKeyboard());
        message.setParseMode("Markdown");
        return message;
    }

    public SendMessage createMenuToStartLearn(Long chatId) {
        SendMessage message = new SendMessage(chatId.toString(), LEARN_MENU_MESSAGE);
        message.setReplyMarkup(createLearnMenuKeyboard());
        message.setParseMode("HTML");
        return message;
    }
    public SendMessage createWordInfoMessageWithPagination(Long chatId, Word word, int currentIndex, int totalWords) {
        if (word == null) {
            return createSimpleMessage(chatId, "❌ Слово не найдено.");
        }

        String response = String.format(WORD_INFO_TEMPLATE, word.getWord(), word.getTranslation(), word.getTranscription());
        SendMessage message = new SendMessage(chatId.toString(), response);
        message.setReplyMarkup(createWordInfoKeyboardWithPagination(word.getId(), currentIndex, totalWords));
        message.setParseMode("HTML");
        return message;
    }


    private InlineKeyboardMarkup createWordInfoKeyboardWithPagination(Long wordId, int currentIndex, int totalWords) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        // Кнопка "Выучил"
        InlineKeyboardButton learnedButton = new InlineKeyboardButton();
        learnedButton.setText("✅ Выучил");
        learnedButton.setCallbackData("learned_" + wordId);

        // Кнопка "Назад"
        InlineKeyboardButton previousButton = new InlineKeyboardButton();
        previousButton.setText("⬅️ Назад");
        previousButton.setCallbackData("previous_word");

        // Кнопка "Вперед"
        InlineKeyboardButton nextButton = new InlineKeyboardButton();
        nextButton.setText("➡️ Вперед");
        nextButton.setCallbackData("next_word");

        // Добавляем кнопки в строки
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(learnedButton);

        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(previousButton);
        secondRow.add(nextButton);

        inlineRows.add(firstRow);
        inlineRows.add(secondRow);

        inlineKeyboardMarkup.setKeyboard(inlineRows);
        return inlineKeyboardMarkup;
    }

    public SendMessage createUserInfoMessage(Long chatId, User user) {
        if (user == null) {
            return createSimpleMessage(chatId, "❌ Пользователь не найден.");
        }

        String status = determineUserStatus(user.getRating());
        String emoji = determineStatusEmoji(user.getRating());

        // Форматируем сообщение с использованием HTML-тегов
        String text = String.format(
                "✨ <b>Ваш профиль:</b> ✨\n\n" +
                        "🆔 <b>ID:</b> %d\n" +
                        "👤 <b>Имя:</b> %s\n" +
                        "⭐ <b>Рейтинг:</b> %d\n" +
                        "🏅 <b>Статус:</b> %s %s\n\n" +
                        "📚 <b>Выучено слов:</b> %d",
                user.getId(),
                user.getName(),
                user.getRating(),
                status,
                emoji,
                user.getRating()
        );

        SendMessage message = new SendMessage(chatId.toString(), text);
        message.setParseMode("HTML");
        return message;
    }

    public SendMessage createWordInfoMessage(Long chatId, Word word) {
        if (word == null) {
            return createSimpleMessage(chatId, "❌ Слово не найдено.");
        }

        String response = String.format(WORD_INFO_TEMPLATE, word.getWord(), word.getTranslation(), word.getTranscription());
        SendMessage message = new SendMessage(chatId.toString(), response);
        message.setReplyMarkup(createWordInfoKeyboard(word.getId()));
        message.setParseMode("HTML");
        return message;
    }

    public SendMessage createWordInfoMessageWhenAddNew(Long chatId, Word word) {
        if (word == null) {
            return createSimpleMessage(chatId, "❌ Слово не найдено.");
        }

        // Форматируем сообщение с выделением
        String response = String.format(
                "✨ <b>Новое слово добавлено:</b> ✨\n\n" +
                        "📖 <b>Слово:</b> %s\n" +
                        "🌍 <b>Перевод:</b> %s\n" +
                        "🔊 <b>Транскрипция:</b> %s",
                word.getWord(), word.getTranslation(), word.getTranscription()
        );

        SendMessage message = new SendMessage(chatId.toString(), response);
        message.setReplyMarkup(createLearnMenuKeyboard());
        message.setParseMode("HTML");
        return message;
    }

    public SendMessage createSimpleMessage(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        message.setParseMode("HTML");
        return message;
    }

    public SendMessage createLanguageMenu(Long chatId, String currentLanguage) {
        SendMessage message = new SendMessage(chatId.toString(), LANGUAGE_MENU_MESSAGE);
        message.setReplyMarkup(createLanguageMenuKeyboard(currentLanguage));
        message.setParseMode("HTML");
        return message;
    }

    // Вспомогательные методы

    private ReplyKeyboardMarkup createMainMenuKeyboard() {
        return keyboardFactory.createKeyboard(
                "👤 Профиль",
                "❓ Помощь",
                "📚 Учить язык",
                "🌍 Сменить язык"
        );
    }

    private ReplyKeyboardMarkup createLearnMenuKeyboard() {
        return keyboardFactory.createKeyboard(
                "➕ Добавить слово",
                "📖 Показать мои слова",
                "✏️ Добавить свое слово",
                "🏠 Главное меню"
        );
    }

    private InlineKeyboardMarkup createWordInfoKeyboard(Long wordId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        InlineKeyboardButton learnedButton = new InlineKeyboardButton();
        learnedButton.setText("✅ Выучил");
        learnedButton.setCallbackData("learned_" + wordId);

        List<InlineKeyboardButton> inlineRow = new ArrayList<>();
        inlineRow.add(learnedButton);
        inlineRows.add(inlineRow);

        inlineKeyboardMarkup.setKeyboard(inlineRows);
        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup createLanguageMenuKeyboard(String currentLanguage) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Map.Entry<String, String> entry : LanguageConstants.LANGUAGES.entrySet()) {
            String languageCode = entry.getKey();
            String languageName = entry.getValue();

            String buttonText = "🌐 " + languageName;
            if (languageCode.equals(currentLanguage)) {
                buttonText += " ✅";
            }

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonText);
            button.setCallbackData("language_" + languageCode);

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private String determineUserStatus(int rating) {
        if (rating == 0) {
            return "Новичок";
        } else if (rating < 150) {
            return "Ученик";
        } else if (rating < 300) {
            return "Продвинутый";
        } else if (rating < 1000) {
            return "Эксперт";
        } else {
            return "Чемпион";
        }
    }

    private String determineStatusEmoji(int rating) {
        if (rating == 0) {
            return "👶";
        } else if (rating < 150) {
            return "🧑‍🎓";
        } else if (rating < 300) {
            return "🎓";
        } else if (rating < 1000) {
            return "🏆";
        } else {
            return "\uD83D\uDCC8";
        }
    }
}