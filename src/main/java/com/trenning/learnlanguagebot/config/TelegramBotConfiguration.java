package com.trenning.learnlanguagebot.config;

import com.trenning.learnlanguagebot.bot.TelegramBot;
import com.trenning.learnlanguagebot.handler.CallbackHandler;
import com.trenning.learnlanguagebot.handler.CommandHandler;
import com.trenning.learnlanguagebot.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfiguration {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramBot telegramBot) throws Exception {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(telegramBot);
        return botsApi;
    }

    @Bean
    public TelegramBot telegramBot(
            @Value("${bot.token}") String botToken,
            CommandHandler commandHandler,
            CallbackHandler callbackHandler) {
        return new TelegramBot(new DefaultBotOptions(), botToken, commandHandler,callbackHandler);
    }
}