package com.crypto.cryptotrader.parser;

import net.sourceforge.tess4j.TesseractException;
import twitter4j.Status;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.exchange.CurrencyCodesHelper;
import com.crypto.cryptotrader.shortorder.ShortOrderExecutor;

@Component
public class TwitterParser {
	private static final Logger logger = LoggerFactory.getLogger(TwitterParser.class);
	public static final String COIN_OF_THE_WEEK_PATTERN = "COIN OF THE WEEK";

	@Autowired
	private ImageParser imageParser;
	@Autowired
	private ShortOrderExecutor shortOrderExecutor;
	@Autowired
	private CurrencyCodesHelper currencyCodesHelper;

	public void parseMessage(Status status) throws TesseractException, IOException {
		String message = status.getText();
		String occurence = TextParserUtils.findFirstOccurenceOfPattern(COIN_OF_THE_WEEK_PATTERN, message);

		boolean hasCoinOfTheWeek = StringUtils.isNotEmpty(occurence);
		String currencyCode = null;
		if (hasCoinOfTheWeek && status.getMediaEntities().length != 0) {
			String imageUrl = status.getMediaEntities()[0].getMediaURL();
			currencyCode = imageParser.getCurrencyCodeFromImage(imageUrl);
		} else if (hasCoinOfTheWeek) {
			Map<String, String> currencyCodesMap = currencyCodesHelper.getCurrencyCodesMap();
			currencyCode = TextParserUtils.getCurrencyCodeFromMessage(message, currencyCodesMap);
		}

		if (StringUtils.isNotEmpty(currencyCode)) {
			shortOrderExecutor.executeShortBidOrder(currencyCode);
		}
	}
}
