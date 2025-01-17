package com.trenning.learnlanguagebot.handler;

import com.trenning.learnlanguagebot.bot.BotMessages;
import com.trenning.learnlanguagebot.entity.User;
import com.trenning.learnlanguagebot.entity.UserState;
import com.trenning.learnlanguagebot.entity.Word;
import com.trenning.learnlanguagebot.service.MessageService;
import com.trenning.learnlanguagebot.service.StateService;
import com.trenning.learnlanguagebot.service.UserService;
import com.trenning.learnlanguagebot.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class CommandHandler {
    private final MessageService messageService;
    private final StateService stateService;
    private final UserService userService;
    private final WordService wordService;

    public SendMessage handleStartCommand(Long chatId, Update update) {
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

    public SendMessage handleHelpCommand(Long chatId) {
        return messageService.createSimpleMessage(chatId, BotMessages.HELP_MESSAGE);
    }

    public SendMessage handleInfoCommand(Long chatId) {
        User user = userService.findByChatId(chatId);
        return messageService.createUserInfoMessage(chatId, user);
    }

    public SendMessage handleBackCommand(Long chatId) {
        stateService.resetUserState(chatId);
        return messageService.createMenuToStartLearn(chatId);
    }



    public SendMessage handleDeleteWordCommand(Long chatId) {
        User user = userService.findByChatId(chatId);
        List<Word> words = wordService.getUserWords(user);

        if (words.isEmpty()) {
            return messageService.createSimpleMessage(chatId, BotMessages.NO_WORDS_MESSAGE);
        }

        int currentIndex = user.getCurrentWordIndex() % words.size();
        Word wordToDelete = words.get(currentIndex);
        wordService.deleteWordForUser(wordToDelete, user);

        return handleNextWordCommand(chatId);
    }

    public SendMessage handleShowWordsCommand(Long chatId) {
        User user = userService.findByChatId(chatId);

        if (user.getCurrentLanguage() == null) {
            return messageService.createSimpleMessage(chatId, BotMessages.LANGUAGE_NOT_SELECTED_MESSAGE);
        }

        List<Word> words = wordService.getUserWords(user);

        if (words.isEmpty()) {
            return messageService.createSimpleMessage(chatId, BotMessages.NO_WORDS_MESSAGE);
        }

        int currentIndex = user.getCurrentWordIndex() % words.size();
        Word word = words.get(currentIndex);

        return messageService.createWordInfoMessageWithPagination(chatId, word, currentIndex, words.size());
    }

    public SendMessage handleDefaultCommand(Long chatId, String text) {
        if (stateService.getUserState(chatId) == UserState.WAITING_FOR_WORD) {
            stateService.resetUserState(chatId);
            Optional<Word> word = wordService.searchAndSaveWord(text, userService.findByChatId(chatId));
            return word.map(value -> messageService.createWordInfoMessageWhenAddNew(chatId, value))
                    .orElseGet(() -> messageService.createSimpleMessage(chatId, BotMessages.WORD_NOT_FOUND_MESSAGE));
        }
        return messageService.createSimpleMessage(chatId, BotMessages.UNKNOWN_COMMAND_MESSAGE);
    }

    public SendMessage handleChangeLanguageCommand(Long chatId) {
        User user = userService.findByChatId(chatId);
        return messageService.createLanguageMenu(chatId, user.getCurrentLanguage());
    }

    public SendMessage handleAddWordCommand(Long chatId) {
        User user = userService.findByChatId(chatId);

        if (user == null) {
            return messageService.createSimpleMessage(chatId, BotMessages.USER_NOT_FOUND_MESSAGE);
        }

        if (user.getCurrentLanguage() == null) {
            return messageService.createSimpleMessage(chatId, BotMessages.LANGUAGE_NOT_SELECTED_MESSAGE);
        }

        stateService.setUserState(chatId, UserState.WAITING_FOR_WORD);
        return messageService.createSimpleMessage(chatId, BotMessages.ADD_WORD_PROMPT);
    }


    public SendMessage handleLearnLanguageCommand(Long chatId) {
        return messageService.createMenuToStartLearn(chatId);
    }

    public SendMessage handleNextWordCommand(Long chatId) {
        User user = userService.findByChatId(chatId);
        List<Word> words = wordService.getUserWords(user);

        if (words.isEmpty()) {
            return messageService.createSimpleMessage(chatId, BotMessages.NO_WORDS_MESSAGE);
        }

        int nextIndex = (user.getCurrentWordIndex() + 1) % words.size();
        user.setCurrentWordIndex(nextIndex);
        userService.save(user);

        Word nextWord = words.get(nextIndex);
        return messageService.createWordInfoMessageWithPagination(chatId, nextWord, nextIndex, words.size());
    }

    public SendMessage handlePreviousWordCommand(Long chatId) {
        User user = userService.findByChatId(chatId);
        List<Word> words = wordService.getUserWords(user);

        if (words.isEmpty()) {
            return messageService.createSimpleMessage(chatId, BotMessages.NO_WORDS_MESSAGE);
        }

        int previousIndex = (user.getCurrentWordIndex() - 1 + words.size()) % words.size();
        user.setCurrentWordIndex(previousIndex);
        userService.save(user);

        Word previousWord = words.get(previousIndex);
        return messageService.createWordInfoMessageWithPagination(chatId, previousWord, previousIndex, words.size());
    }

    public SendMessage handleMainMenuCommand(Long chatId) {
        stateService.resetUserState(chatId);
        return messageService.createMainMenu(chatId);
    }
}