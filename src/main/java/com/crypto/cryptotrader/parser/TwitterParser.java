package com.crypto.cryptotrader.parser;

import net.sourceforge.tess4j.TesseractException;
import twitter4j.Status;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.shortorder.ShortOrderExecutor;

@Component
public class TwitterParser {
	public static final String COIN_OF_THE_WEEK_PATTERN = "COIN OF THE WEEK";

	@Autowired
	private ImageParser imageParser;
	@Autowired
	private ShortOrderExecutor shortOrderExecutor;

	public void parseMessage(Status status) throws TesseractException, IOException {
		String message = status.getText();
		String occurence = TextParserUtils.findFirstOccurenceOfPattern(COIN_OF_THE_WEEK_PATTERN, message
				.toUpperCase());

		if (StringUtils.isNotEmpty(occurence) && status.getMediaEntities().length != 0) {
			String imageUrl = status.getMediaEntities()[0].getMediaURL();
			String currencyCode = imageParser.getCurrencyCodeFromImage(imageUrl);
			shortOrderExecutor.executeShortBidOrder(currencyCode);
		}

	}
}
