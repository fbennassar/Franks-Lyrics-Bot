package com.telegrambot.TelegramBot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;



public class App {
    public static void main(String[] args) {
        try {
            TelegramBotsApi chatBot = new TelegramBotsApi(DefaultBotSession.class);
            chatBot.registerBot(new TelegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}