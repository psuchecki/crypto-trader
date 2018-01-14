package com.crypto.cryptotrader.parser;

import net.sourceforge.tess4j.TesseractException;
import twitter4j.Status;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.shortorder.ShortOrderExecutor;

@Component
public class TwitterParser {
	private static final Logger logger = LoggerFactory.getLogger(TwitterParser.class);
	public static final String COIN_OF_THE_WEEK_PATTERN = "COIN OF THE WEEK";

	@Autowired
	private ImageParser imageParser;
	@Autowired
	private ShortOrderExecutor shortOrderExecutor;

	public void parseMessage(Status status) throws TesseractException, IOException {
		String message = status.getText();
		String occurence = TextParserUtils.findFirstOccurenceOfPattern(COIN_OF_THE_WEEK_PATTERN, message);

		if (StringUtils.isNotEmpty(occurence) && status.getMediaEntities().length != 0) {
			String imageUrl = status.getMediaEntities()[0].getMediaURL();
			String currencyCode = imageParser.getCurrencyCodeFromImage(imageUrl);
			if (StringUtils.isNotEmpty(currencyCode)) {
				shortOrderExecutor.executeShortBidOrder(currencyCode);
			} else {
				logger.warn("TWITTER: Cannot recognize currency code in image {}" + imageUrl);
			}
		}
	}
}
