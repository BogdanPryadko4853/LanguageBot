package com.trenning.learnlanguagebot.service;

import com.trenning.learnlanguagebot.constants.LanguageConstants;
import com.trenning.learnlanguagebot.entity.User;
import com.trenning.learnlanguagebot.entity.Word;
import com.trenning.learnlanguagebot.formatter.MessageFormatter;
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


    public SendMessage createMainMenu(Long chatId) {

        String text = MessageFormatter.formatMessage("Выберите действие:");
        String welcomeMessage = """
            🎉 *Добро пожаловать в LanguageBot!* 🎉

            Этот бот поможет вам учить новые слова и улучшать ваш словарный запас. Вот что вы можете делать:

            📚 *Учить язык* — начните изучать новые слова.
            👤 *Профиль* — посмотрите ваш прогресс и статус.
            🌍 *Сменить язык* — выберите язык для изучения.
            ❓ *Помощь* — узнайте, как использовать бота.

            Перед использование обязательно укажите язык, который вы учите 🌍
            """;
        SendMessage message = new SendMessage(chatId.toString(), welcomeMessage);


        message.setReplyMarkup(keyboardFactory.createKeyboard(
                "👤 Профиль",
                "❓ Помощь",
                "📚 Учить язык",
                "🌍 Сменить язык"
        ));
        message.setParseMode("HTML");
        return message;
    }

    public SendMessage createMenuToStartLearn(Long chatId) {
        String text = MessageFormatter.formatMessage("Можете выбрать дальнейшее действие");
        SendMessage message = new SendMessage(chatId.toString(), text);
        message.setReplyMarkup(keyboardFactory.createKeyboard(
                "➕ Добавить слово",
                "📖 Показать мои слова",
                "✏️ Добавить свое слово",
                "🏠 Главное меню"
        ));
        message.setParseMode("HTML");
        return message;
    }

    public SendMessage createUserInfoMessage(Long chatId, User user) {
        SendMessage sendMessage;
        if (user != null) {
            int rating = user.getRating();

            String status;
            String emoji;
            if (rating == 0) {
                status = "Новичок";
                emoji = "👶";
            } else if (rating < 150) {
                status = "Ученик";
                emoji = "🧑‍🎓";
            } else if (rating < 300) {
                status = "Продвинутый";
                emoji = "🎓";
            } else if(rating<1000) {
                status = "Эксперт";
                emoji = "🏆";
            } else {
                status = "Чемпион";
                emoji = "\uD83D\uDCC8";
            }

            // Формируем текст сообщения
            String text = MessageFormatter.formatMessage(
                    "Ваш профиль:\n" +
                            "ID: " + user.getId() + "\n" +
                            "Имя: " + user.getName() + "\n" +
                            "Рейтинг: " + rating + "\n" +
                            "Статус: " + status + " " + emoji
            );

            sendMessage = new SendMessage(chatId.toString(), text);
            sendMessage.setParseMode("HTML");
        } else {
            sendMessage = new SendMessage(chatId.toString(), MessageFormatter.formatErrorMessage("Пользователь не найден."));
            sendMessage.setParseMode("HTML");
        }
        return sendMessage;
    }
    public SendMessage createWordInfoMessage(Long chatId, Word word) {
        SendMessage sendMessage;
        if (word != null) {
            String response = MessageFormatter.formatWordInfo(word);

            // Inline-кнопка "Выучил"
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

            InlineKeyboardButton learnedButton = new InlineKeyboardButton();
            learnedButton.setText("✅ Выучил");
            learnedButton.setCallbackData("learned_" + word.getId());

            List<InlineKeyboardButton> inlineRow = new ArrayList<>();
            inlineRow.add(learnedButton);
            inlineRows.add(inlineRow);

            inlineKeyboardMarkup.setKeyboard(inlineRows);

            ReplyKeyboardMarkup replyKeyboardMarkup = keyboardFactory.createKeyboard(
                    "❌ Удалить",
                    "➡️ Следующее слово",
                    "🔙 Назад",
                    "🏠 Главное меню"
            );


            sendMessage = new SendMessage(chatId.toString(), response);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            sendMessage.setParseMode("HTML");

        } else {
            sendMessage = new SendMessage(chatId.toString(), MessageFormatter.formatErrorMessage("Слово не найдено."));
            sendMessage.setParseMode("HTML");
        }
        return sendMessage;
    }

    public SendMessage createWordInfoMessageWhenAddNew(Long chatId, Word word) {
        SendMessage sendMessage;
        if (word != null) {
            String response = MessageFormatter.formatWordInfo(word) + MessageFormatter.formatWordAddedSuccessfully();
            ReplyKeyboardMarkup keyboard = keyboardFactory.createKeyboard(
                    "➕ Добавить слово",
                    "📖 Показать мои слова",
                    "✏️ Добавить свое слово",
                    "🏠 Главное меню"
            );
            SendMessage message = new SendMessage(chatId.toString(), response);
            message.setReplyMarkup(keyboard);
            message.setParseMode("HTML");
            return message;
        } else {
            sendMessage = new SendMessage(chatId.toString(), MessageFormatter.formatErrorMessage("Слово не найдено."));
            sendMessage.setParseMode("HTML");
            return sendMessage;
        }
    }


    public SendMessage createSimpleMessage(Long chatId, String text) {
        String formattedText = MessageFormatter.formatMessage(text);
        SendMessage sendMessage = new SendMessage(chatId.toString(), formattedText);
        sendMessage.setParseMode("HTML");
        return sendMessage;
    }

    public SendMessage createLanguageMenu(Long chatId, String currentLanguage) {
        String text = MessageFormatter.formatMessage("Выберите язык:");
        SendMessage message = new SendMessage(chatId.toString(), text);
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Map.Entry<String, String> entry : LanguageConstants.LANGUAGES.entrySet()) {
            String languageCode = entry.getKey();
            String languageName = entry.getValue();

            String buttonText = "🌐 " + languageName;

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(buttonText);
            button.setCallbackData("language_" + languageCode);

            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            rows.add(row);
        }

        keyboardMarkup.setKeyboard(rows);
        message.setReplyMarkup(keyboardMarkup);
        message.setParseMode("HTML");
        return message;
    }
}