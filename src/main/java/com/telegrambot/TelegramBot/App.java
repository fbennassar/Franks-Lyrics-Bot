package com.telegrambot.TelegramBot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import static spark.Spark.*;



public class App {
    public static void main(String[] args) {
    	
        try {
            Thread.sleep(5000);  // Espera 5 segundos antes de iniciar el servidor
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String port = System.getenv("WEBSITES_PORT");
        if (port != null) {
            port(Integer.parseInt(port));
        }
        
        get("/hello", (req, res) -> "Hello World");  // Ruta básica
        
        try {
            TelegramBotsApi chatBot = new TelegramBotsApi(DefaultBotSession.class);
            chatBot.registerBot(new TelegramBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
