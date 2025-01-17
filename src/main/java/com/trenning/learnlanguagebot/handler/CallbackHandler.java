package com.trenning.learnlanguagebot.handler;

import com.trenning.learnlanguagebot.bot.BotMessages;
import com.trenning.learnlanguagebot.constants.LanguageConstants;
import com.trenning.learnlanguagebot.entity.User;
import com.trenning.learnlanguagebot.entity.Word;
import com.trenning.learnlanguagebot.service.MessageService;
import com.trenning.learnlanguagebot.service.UserService;
import com.trenning.learnlanguagebot.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class CallbackHandler {
    private final MessageService messageService;
    private final UserService userService;
    private final WordService wordService;

    public SendMessage handleLanguageSelection(Long chatId, String languageCode) {
        User user = userService.findByChatId(chatId);

        if (user != null) {
            user.setCurrentLanguage(languageCode);
            userService.save(user);
            return messageService.createSimpleMessage(chatId, "✅ Язык выбран: " + LanguageConstants.LANGUAGES.get(languageCode));
        }

        return messageService.createSimpleMessage(chatId, BotMessages.USER_NOT_FOUND_MESSAGE);
    }

    public SendMessage handleLearnedWord(Long chatId, Long wordId) {
        User user = userService.findByChatId(chatId);
        Word word = wordService.findById(wordId).orElse(null);

        if (word != null && user != null) {
            user.setRating(user.getRating() + 1);
            userService.save(user);

            wordService.deleteWordForUser(word, user);

            return messageService.createSimpleMessage(chatId, BotMessages.WORD_ADDED_MESSAGE);
        }

        return messageService.createSimpleMessage(chatId, BotMessages.WORD_NOT_FOUND_MESSAGE);
    }
}