package com.crypto.cryptotrader.parser;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.shortorder.ShortOrderExecutor;

@Component
public class GmailAlanParser {

	public static final String ALAN_REGEX_PATTERN = "(.*alanmasters you follow published new idea)(.*)(\\(\\w+\\)).*";
	public static final String REMOVE_PARENTHESIS_REGEX = "[()]";
	@Autowired
	private ShortOrderExecutor shortOrderExecutor;

	public void parseMessage(String message) throws IOException {
		Pattern pattern = Pattern.compile(ALAN_REGEX_PATTERN);
		Matcher matcher = pattern.matcher(message);
		if (matcher.matches()) {
			String baseCurrencyCode = matcher.group(matcher.groupCount());
			if (StringUtils.isNotBlank(baseCurrencyCode)) {
				baseCurrencyCode = baseCurrencyCode.replaceAll(REMOVE_PARENTHESIS_REGEX, StringUtils.EMPTY);
				shortOrderExecutor.executeShortBidOrder(baseCurrencyCode);
			}
		}
	}

}
