package com.crypto.cryptotrader.calculation;

import java.math.BigDecimal;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

public class CalculationUtils {
	public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	public static final int DEFAULT_SCALE = 8;
	public static final int DEFAULT_CURRENCY_SCALE = 2;

	public static BigDecimal percentage(BigDecimal base, BigDecimal pct) {
		return base.multiply(pct).divide(ONE_HUNDRED);
	}

	public static BigDecimal calculateOffset(BigDecimal baseAmount, int offsetPercentage) {
		return percentage(baseAmount, BigDecimal.valueOf(offsetPercentage)).setScale(DEFAULT_SCALE, BigDecimal
				.ROUND_HALF_UP);
	}

	public static CurrencyPair toCurrencyPair(String baseCurrencyCode) {
		Currency baseCurrency = Currency.getInstance(baseCurrencyCode);

		return new CurrencyPair(baseCurrency, Currency.BTC);
	}
}
