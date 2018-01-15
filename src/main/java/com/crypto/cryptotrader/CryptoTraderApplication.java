package com.crypto.cryptotrader;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.util.LoadLibs;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.crypto.cryptotrader.feeds.GmailConfiguration;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.gmail.Gmail;

@EnableScheduling
@SpringBootApplication
public class CryptoTraderApplication {
	@Value("${twitter.consumerKey}")
	private String twitterConsumerKey;
	@Value("${twitter.consumerSecret}")
	private String twitterConsumerSecret;
	@Value("${twitter.accessTokenSecret}")
	public String twitterTokenSecret;
	@Value("${twitter.accessToken}")
	private String twitterToken;

	public static void main(String[] args) {
		SpringApplication.run(CryptoTraderApplication.class, args);
	}

	@Bean
	public Gmail gmail() throws IOException, GeneralSecurityException {
		Credential credential = GmailConfiguration.authorize();
		return new Gmail.Builder(GmailConfiguration.HTTP_TRANSPORT, GmailConfiguration.JSON_FACTORY, credential)
				.setApplicationName(GmailConfiguration.APPLICATION_NAME)
				.build();
	}

	@Bean
	public ITesseract tesseract() {
		ITesseract tesseract =  new Tesseract1();
		tesseract.setDatapath(LoadLibs.extractTessResources("tessdata").getParent());

		return tesseract;
	}

	@Bean
	TwitterStream twitter() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(twitterConsumerKey)
				.setOAuthConsumerSecret(twitterConsumerSecret)
				.setOAuthAccessToken(twitterToken)
				.setOAuthAccessTokenSecret(twitterTokenSecret);

		return new TwitterStreamFactory(cb.build()).getInstance();
	}

}
