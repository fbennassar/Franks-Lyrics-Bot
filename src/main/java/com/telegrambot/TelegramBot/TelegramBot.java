package com.telegrambot.TelegramBot;

import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;

public class TelegramBot extends TelegramLongPollingBot {

	private final OkHttpClient client = new OkHttpClient();
	private final Dotenv dotenv = Dotenv.load();
	
	List<JSONObject> songs = new ArrayList<>();
	
    @Override
    public String getBotUsername() {
        //username bot - Example :"@ChatBot"
        return dotenv.get("BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        //token bot - Example "9999:abcdefghijklmnqzzz3a33"
        return dotenv.get("BOT_TOKEN");
    }

    //we handle the received update and capture the text and id of the conversation
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            
            songs.clear();

            // Search for the song on Genius
            String urlGenius = "https://api.genius.com/search?q=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
            Request requestGeniusOptions = new Request.Builder()
                .url(urlGenius)
                .addHeader("Authorization", "Bearer " + dotenv.get("GENIUS_ACCESS_TOKEN"))
                .build();

            try (Response responseGenius = client.newCall(requestGeniusOptions).execute()) {
                if (!responseGenius.isSuccessful()) throw new IOException("Unexpected code " + responseGenius);

                JSONObject jsonObject = new JSONObject(responseGenius.body().string());
                JSONObject response = jsonObject.getJSONObject("response");
                if (response.getJSONArray("hits").length() == 0) {
                    sendMessage(generateSendMessage(chatId, "No se encontraron resultados para " + message));
                    return;
                }

                List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
                for (int i = 0; i < response.getJSONArray("hits").length(); i++) {
                    JSONObject hit = response.getJSONArray("hits").getJSONObject(i).getJSONObject("result");
                    songs.add(hit);
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(hit.getString("full_title"));
                    button.setCallbackData(String.valueOf(i));  // Use the index as callback data
                    List<InlineKeyboardButton> rowInline = new ArrayList<>();
                    rowInline.add(button);
                    keyboard.add(rowInline);
                }

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                inlineKeyboardMarkup.setKeyboard(keyboard);

                SendMessage messageOptions = new SendMessage();
                messageOptions.setChatId(chatId.toString());
                messageOptions.setText("Por favor, selecciona una opción:");
                messageOptions.setReplyMarkup(inlineKeyboardMarkup);

                try {
                    execute(messageOptions);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        else if (update.hasCallbackQuery()) {
        	
        	//get info from genius API
        	
        	Integer index = Integer.parseInt(update.getCallbackQuery().getData());
        	JSONObject song = songs.get(index);
        	
        	String title = song.getString("title");
        	String artist = song.getJSONObject("primary_artist").getString("name");
        	
        	// get lyrics from Lyrics.ovh API
        	
        	String urlLyrics = "https://api.lyrics.ovh/v1/" + artist + "/" + title;
        	
			Request requestLyricsOptions = new Request.Builder().url(urlLyrics).build();

			try (Response responseLyrics = client.newCall(requestLyricsOptions).execute()) {
				if (!responseLyrics.isSuccessful())
					throw new IOException("Unexpected code " + responseLyrics);

				JSONObject jsonObject = new JSONObject(responseLyrics.body().string());
				String lyrics = jsonObject.getString("lyrics");

				SendMessage sendmessage = new SendMessage(update.getCallbackQuery().getMessage().getChatId().toString(),
						"Title: " + title + "\nArtist: " + artist + "\n\n" + lyrics);
				sendMessage(sendmessage);

			} catch (IOException e) {
				e.printStackTrace();
			}
        	
        	// Send Genius's link
			
			String url = song.getString("url");
			
			SendMessage sendmessage = new SendMessage(update.getCallbackQuery().getMessage().getChatId().toString(), url);
			
			try {
				execute(sendmessage);
			} catch (TelegramApiException e) {
				e.printStackTrace();
			}

		}

        
    }

    //we create a SendMessage with the text we want to send to the chat
    private SendMessage generateSendMessage(Long chatId, String message) {
        return new SendMessage(chatId.toString(), message);
    }
    
    //send the message to Telegram API
    private void sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
