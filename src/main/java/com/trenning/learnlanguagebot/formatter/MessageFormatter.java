package com.trenning.learnlanguagebot.formatter;


import com.trenning.learnlanguagebot.entity.Word;

public class MessageFormatter {

    /**
     * Форматирует текст с HTML-разметкой и смайликами.
     */
    public static String formatMessage(String text) {
        return "<b>📌 " + text + "</b>";
    }

    /**
     * Форматирует информацию о слове.
     */
    public static String formatWordInfo(Word word) {
        return "<b>📖 Слово:</b> " + word.getWord() + "\n" +
                "<b>🔤 Перевод:</b> " + word.getTranslation() + "\n" +
                "<b>📝 Транскрипция:</b> " + word.getTranscription() + "\n\n";
    }

    /**
     * Форматирует сообщение об успешном добавлении слова.
     */
    public static String formatWordAddedSuccessfully() {
        return "🎉 <i>Слово успешно добавлено!</i>";
    }

    /**
     * Форматирует сообщение об ошибке.
     */
    public static String formatErrorMessage(String message) {
        return "❌ " + message;
    }
}