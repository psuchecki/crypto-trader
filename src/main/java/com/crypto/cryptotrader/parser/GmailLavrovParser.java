package com.crypto.cryptotrader.parser;

import static com.crypto.cryptotrader.parser.TextParserUtils.hasPattern;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.shortorder.ShortOrderExecutor;
import com.crypto.cryptotrader.shortorder.ShortOrderMonitor;

@Component
public class GmailLavrovParser {
	private static final Logger logger = LoggerFactory.getLogger(GmailLavrovParser.class);

	public static final String PREMIUM_SINGAL_REGEX = "TRADUNITY PREMIUM SIGNAL";
	public static final String BITTREX_REGEX = "BITTREX";
	public static final String CURRENCY_SECTION_REGEX = "– \\w+ –";
	public static final Pattern CURRENCY_SECTION_PATTERN = Pattern.compile(CURRENCY_SECTION_REGEX);
	public static final String BTC_CODE = "BTC";

	@Autowired
	private ShortOrderExecutor shortOrderExecutor;
	@Autowired
	private ShortOrderMonitor shortOrderMonitor;

	public void parseMessage(String message) throws IOException {
//		if (hasPattern(PREMIUM_SINGAL_REGEX, message) && hasPattern(BITTREX_REGEX, message)) {
		if (hasPattern(BITTREX_REGEX, message)) {
			logger.info("GMAIL LAVROV: {}", message);
			Matcher matcher = CURRENCY_SECTION_PATTERN.matcher(message);
			if (matcher.find()) {
				String currencySection = matcher.group(matcher.groupCount()).toUpperCase();
				currencySection = currencySection.replaceAll("\\s|–", EMPTY);
				if (currencySection.contains(BTC_CODE)) {
					String currency = currencySection.replaceAll(BTC_CODE, EMPTY);
					shortOrderExecutor.executeShortBidOrder(currency);
					shortOrderMonitor.handleCompletedBids();
				}
			}
		}

	}
}
