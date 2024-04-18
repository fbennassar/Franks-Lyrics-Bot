package com.telegrambot.TelegramBot;

import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.github.cdimascio.dotenv.Dotenv;

public class TelegramBot extends TelegramLongPollingBot {

	private final OkHttpClient client = new OkHttpClient();
	private final Dotenv dotenv = Dotenv.load();
	
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
        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        
        // format "Artist - Song"
        String[] messageArray = message.split(" - ", 2);
        
		if (messageArray.length != 2) {
			sendMessage(generateSendMessage(chatId, "Invalid format. Please use \"Artist - Song\""));
			return;
		}
        
        String artist = messageArray[0];
        String song = messageArray[1];
        
        // get lyrics with lyrics.ovh API
        
        Request requestOvh = new Request.Builder()
				.url("https://api.lyrics.ovh/v1/" + URLEncoder.encode(artist, StandardCharsets.UTF_8) + "/"
						+ URLEncoder.encode(song, StandardCharsets.UTF_8))
				.build();
		
		try (Response response = client.newCall(requestOvh).execute()) {
		    String responseBody = response.body().string();
		    if (response.isSuccessful()) {
		        // Parse the JSON response to get the lyrics
		        JSONObject jsonResponse = new JSONObject(responseBody);
		        String lyrics = jsonResponse.getString("lyrics");

		        // Replace "\r\n" with "\n" to fix the formatting
		        lyrics = lyrics.replace("\r\n", "\n");
		        
		        // Split the lyrics into lines
		        String[] lines = lyrics.split("\n");

		        // Skip the first line and join the rest back together
		        lyrics = String.join("\n", Arrays.copyOfRange(lines, 1, lines.length));

		        sendMessage(generateSendMessage(chatId, lyrics));
		    } else {
		        sendMessage(generateSendMessage(chatId, "\n\nLyrics not found"));
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		}

		
        // get lyrics
        String url = "https://api.genius.com/search?q=" + URLEncoder.encode(artist + " " + song, StandardCharsets.UTF_8);
        Request requestGenius = new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + dotenv.get("GENIUS_ACCESS_TOKEN")) // replace with your Genius access token
            .build();

        try (Response response = client.newCall(requestGenius).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                // Parse the JSON response to get the song information
                JSONObject jsonResponse = new JSONObject(responseBody);
                JSONObject songInfo = jsonResponse.getJSONObject("response").getJSONArray("hits").getJSONObject(0).getJSONObject("result");

                // Get the song title and artist name
                String songTitle = songInfo.getString("title");
                String songArtist = songInfo.getJSONObject("primary_artist").getString("name");

                // Get the song URL
                String songUrl = songInfo.getString("url");

                sendMessage(generateSendMessage(chatId, "Artist: " + songArtist + "\nSong: " + songTitle + "\n\n" + songUrl));
            } else {
                sendMessage(generateSendMessage(chatId, "\n\nSong not found"));
            }
        } catch (IOException e) {
            e.printStackTrace();
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
