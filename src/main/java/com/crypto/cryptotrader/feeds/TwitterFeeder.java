package com.crypto.cryptotrader.feeds;

import net.sourceforge.tess4j.TesseractException;
import twitter4j.FilterQuery;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.TwitterStream;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.parser.TwitterParser;

@Component
public class TwitterFeeder extends StatusAdapter {
	private static final Logger logger = LoggerFactory.getLogger(TwitterFeeder.class);

	public static final int FALSE = -1;
	public static final long ILINX_TEST_ID = 951293828903809024l;
	public static final long MCAFEE_ID = 961445378l;
	@Autowired
	private TwitterStream twitterStream;
	@Autowired
	private TwitterParser twitterParser;

	@PostConstruct
	public void initializeListener() {
		twitterStream.addListener(this);
		FilterQuery filterQuery = new FilterQuery();
		filterQuery.follow(ILINX_TEST_ID);
//		filterQuery.follow(MCAFEE_ID);
		twitterStream.filter(filterQuery);
	}

	@Override
	public void onStatus(Status status) {
		if (isNewUserTweet(status)) {
			try {
				logger.info("TWITTER MCAFEE: {}", status.getText());
				twitterParser.parseMessage(status);
			} catch (TesseractException|IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isNewUserTweet(Status status) {
		return status.getInReplyToStatusId() == FALSE && status.getInReplyToUserId() == FALSE && status
				.getRetweetedStatus() == null && status.getQuotedStatus() == null;
	}

}
