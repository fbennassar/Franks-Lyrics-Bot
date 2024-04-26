package com.telegrambot.TelegramBot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

public class WebScraper {
	
    public SongData getSongData(String url) throws Exception {
        Document document = Jsoup.connect(url)
        		.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36")
                .get();

        String title = document.select("span.SongHeaderdesktop__HiddenMask-sc-1effuo1-11").text();
        String artist = document.select("a.song_header-primary_info-primary_artist").text();
        String lyricsHtml = document.select("div.Lyrics__Container-sc-1ynbvzw-1").html();
        String lyrics = Jsoup.clean(lyricsHtml, "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));
        lyrics = lyrics.replace("<br>", "\n");

        return new SongData(title, artist, lyrics);
    }
}

class SongData {
	
    private String title;
    private String artist;
    private String lyrics;

    public SongData(String title, String artist, String lyrics) {
        this.setTitle(title);
        this.setArtist(artist);
        this.setLyrics(lyrics);
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getLyrics() {
		return lyrics;
	}

	public void setLyrics(String lyrics) {
		this.lyrics = lyrics;
	}


}