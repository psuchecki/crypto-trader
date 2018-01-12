package com.crypto.cryptotrader.feeds;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.crypto.cryptotrader.parser.GmailAlanParser;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;

@Component
public class GmailFeeder {
	private static final Logger logger = LoggerFactory.getLogger(GmailFeeder.class);
	public static final String AUTHENTICATED_USER = "me";
	public static final ModifyMessageRequest MARK_AS_READ = new ModifyMessageRequest().setRemoveLabelIds(Collections.singletonList
			("UNREAD"));

	@Autowired
	private Gmail gmailService;
	@Autowired
	private GmailAlanParser gmailAlanParser;

//	@Scheduled(fixedRate = 300)
	public void checkForFeeds() throws IOException {
//		logger.info("Checking gmail feeds");
		ListMessagesResponse primary = gmailService.users().messages().list(AUTHENTICATED_USER)
//				.setQ("from:piotr.jan.suchecki@gmail.com is:unread").execute();
				.setQ("from:noreply@tradingview.com is:unread").execute();
		if (primary == null || CollectionUtils.isEmpty(primary.getMessages())) {
			return;
		}
		for (Message message : primary.getMessages()) {
			Message messageDetail = gmailService.users().messages().get(AUTHENTICATED_USER, message.getId()).execute();
			gmailAlanParser.parseMessage(messageDetail.getSnippet());
			gmailService.users().messages().modify(AUTHENTICATED_USER, message.getId(), MARK_AS_READ).execute();
		}
	}

}
