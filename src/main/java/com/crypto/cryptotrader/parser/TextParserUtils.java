package com.crypto.cryptotrader.parser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class TextParserUtils {
	public static final String REMOVE_PARENTHESIS_REGEX = "[()]";
	public static final String REMOVE_WHITESPACES_REGEX = "\\s+";
	public static final String CURRENCY_CODE_REGEX = "\\(\\w+\\)";
	public static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile(CURRENCY_CODE_REGEX);

	public static String extractCurrencyCode(String currencyCode) {
		return currencyCode.replaceAll(REMOVE_PARENTHESIS_REGEX + "|" + REMOVE_WHITESPACES_REGEX, StringUtils.EMPTY)
				.toUpperCase();
	}

	public static String findFirstOccurenceOfPattern(String regex, String message) {
		Matcher matcher = Pattern.compile(regex.toUpperCase()).matcher(message.toUpperCase());

		return matcher.find() ? matcher.group(matcher.groupCount()) : StringUtils.EMPTY;
	}

	public static boolean hasPattern(String regex, String pattern) {
		return StringUtils.isNotEmpty(findFirstOccurenceOfPattern(regex, pattern));
	}


	public static String getCurrencyCodeFromMessage(String message, Map<String, String> currencyCodesMap) {
		Matcher matcher = CURRENCY_CODE_PATTERN.matcher(message);

		while (matcher.find()) {
			String currencyCode = matcher.group(matcher.groupCount());
			currencyCode = extractCurrencyCode(currencyCode);
			if (currencyCodesMap.get(currencyCode) != null) {
				return currencyCode;
			}
		}
		return null;
	}
}
