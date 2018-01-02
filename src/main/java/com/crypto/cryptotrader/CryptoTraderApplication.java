package com.crypto.cryptotrader;

import java.io.IOException;
import java.security.GeneralSecurityException;

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

	public static void main(String[] args) {
		SpringApplication.run(CryptoTraderApplication.class, args);
	}

	@Bean
	public static Gmail gmail() throws IOException, GeneralSecurityException {
		Credential credential = GmailConfiguration.authorize();
		return new Gmail.Builder(GmailConfiguration.HTTP_TRANSPORT, GmailConfiguration.JSON_FACTORY, credential)
				.setApplicationName(GmailConfiguration.APPLICATION_NAME)
				.build();
	}

}
