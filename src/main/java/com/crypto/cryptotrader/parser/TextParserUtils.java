package com.crypto.cryptotrader.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class TextParserUtils {
	public static final String REMOVE_PARENTHESIS_REGEX = "[()]";
	public static final String REMOVE_WHITESPACES_REGEX = "\\s+";

	public static String extractCurrencyCode(String currencyCode) {
		return currencyCode.replaceAll(REMOVE_PARENTHESIS_REGEX + "|" + REMOVE_WHITESPACES_REGEX, StringUtils.EMPTY)
				.toUpperCase();
	}

	public static String findFirstOccurenceOfPattern(String regex, String message) {
		Matcher matcher = Pattern.compile(regex).matcher(message);

		return matcher.find() ? matcher.group(matcher.groupCount()) : StringUtils.EMPTY;
	}


}
