package com.trenning.learnlanguagebot.keyboard;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyboardFactory {

    /**
     * Создаёт клавиатуру с кнопками, переданными в аргументах.
     *
     * @param buttons Названия кнопок (varargs).
     * @return ReplyKeyboardMarkup с кнопками.
     */
    public ReplyKeyboardMarkup createKeyboard(String... buttons) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();

        for (int i = 0; i < buttons.length; i++) {
            currentRow.add(new KeyboardButton(buttons[i]));

            if ((i + 1) % 2 == 0 || i == buttons.length - 1) {
                keyboard.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        return keyboardMarkup;
    }

    /**
     * Создаёт клавиатуру с кнопками, сгруппированными по строкам.
     *
     * @param buttonsPerRow Количество кнопок в одном ряду.
     * @param buttons       Названия кнопок (varargs).
     * @return ReplyKeyboardMarkup с кнопками.
     */
    public ReplyKeyboardMarkup createKeyboard(int buttonsPerRow, String... buttons) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow currentRow = new KeyboardRow();

        for (int i = 0; i < buttons.length; i++) {
            currentRow.add(new KeyboardButton(buttons[i]));

            if ((i + 1) % buttonsPerRow == 0 || i == buttons.length - 1) {
                keyboard.add(currentRow);
                currentRow = new KeyboardRow();
            }
        }

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        return keyboardMarkup;
    }
}