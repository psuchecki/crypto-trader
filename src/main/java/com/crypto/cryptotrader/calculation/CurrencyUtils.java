package com.crypto.cryptotrader.calculation;

import java.math.BigDecimal;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

public class CurrencyUtils {

	public static final int INT_SCALE = 0;

	public static CurrencyPair toCurrencyPair(String baseCurrencyCode) {
		Currency baseCurrency = Currency.getInstance(baseCurrencyCode);

		return new CurrencyPair(baseCurrency, Currency.BTC);
	}

	public static BigDecimal calculateOriginalAmount(BigDecimal last) {
		return BigDecimal.valueOf(0.0011).divide(last, INT_SCALE, BigDecimal.ROUND_CEILING);
	}
}
