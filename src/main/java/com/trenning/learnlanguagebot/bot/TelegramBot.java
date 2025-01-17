package com.trenning.learnlanguagebot.bot;

import com.trenning.learnlanguagebot.handler.CallbackHandler;
import com.trenning.learnlanguagebot.handler.CommandHandler;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public class TelegramBot extends TelegramLongPollingBot {

    private final CommandHandler commandHandler;
    private final CallbackHandler callbackHandler;

    public TelegramBot(DefaultBotOptions options,
                       String botToken,
                       CommandHandler commandHandler,
                       CallbackHandler callbackHandler) {
        super(options, botToken);
        this.commandHandler = commandHandler;
        this.callbackHandler = callbackHandler;
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
            case "/start" -> commandHandler.handleStartCommand(chatId, update);
            case "👤 Профиль" -> commandHandler.handleInfoCommand(chatId);
            case "❓ Помощь" -> commandHandler.handleHelpCommand(chatId);
            case "📚 Учить язык" -> commandHandler.handleLearnLanguageCommand(chatId);
            case "➕ Добавить слово" -> commandHandler.handleAddWordCommand(chatId);
            case "🔙 Назад" -> commandHandler.handleBackCommand(chatId);
            case "📖 Показать мои слова" -> commandHandler.handleShowWordsCommand(chatId);
            case "➡️ Следующее слово" -> commandHandler.handleNextWordCommand(chatId);
            case "❌ Удалить" -> commandHandler.handleDeleteWordCommand(chatId);
            case "🌍 Сменить язык" -> commandHandler.handleChangeLanguageCommand(chatId);
            case "🏠 Главное меню" -> commandHandler.handleMainMenuCommand(chatId);
            default -> commandHandler.handleDefaultCommand(chatId, text);
        };

        if (response != null) {
            execute(response);
        }
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
                SendMessage response = callbackHandler.handleLanguageSelection(chatId, selectedLanguageCode);
                if (response != null) {
                    execute(response);
                }
            } else if (callbackData.startsWith("learned_")) {
                Long wordId = Long.parseLong(callbackData.split("_")[1]);
                SendMessage response = callbackHandler.handleLearnedWord(chatId, wordId);
                if (response != null) {
                    execute(response);
                }
                // После обработки слова показываем следующее слово
                SendMessage nextWordMessage = commandHandler.handleNextWordCommand(chatId);
                if (nextWordMessage != null) {
                    execute(nextWordMessage);
                }
            } else if (callbackData.equals("next_word")) {
                SendMessage nextWordMessage = commandHandler.handleNextWordCommand(chatId);
                if (nextWordMessage != null) {
                    execute(nextWordMessage);
                }
            } else if (callbackData.equals("previous_word")) {
                SendMessage previousWordMessage = commandHandler.handlePreviousWordCommand(chatId);
                if (previousWordMessage != null) {
                    execute(previousWordMessage);
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "LanguageBot";
    }
}