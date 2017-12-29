package com.crypto.cryptotrader.calculation;

import java.math.BigDecimal;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

public class CalculationUtils {
	public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
	public static final int BID_OFFSET_PERCENTAGE = 10;
	public static final int ASK_OFFSET_PERCENTAGE = 5;
	public static final int DEFAULT_SCALE = 8;
	public static final CurrencyPair BTC_DOGE = new CurrencyPair(Currency.DOGE, Currency.BTC);
	public static final int STOP_LOSS_PERCENTAGE = 20;
	public static final int STOP_LOSS_OFFSET_PERCENTAGE = 70;

	public static BigDecimal percentage(BigDecimal base, BigDecimal pct) {
		return base.multiply(pct).divide(ONE_HUNDRED);
	}

	public static BigDecimal calculateOffset(BigDecimal baseAmount, int offsetPercentage) {
		return percentage(baseAmount, BigDecimal.valueOf(offsetPercentage)).setScale(DEFAULT_SCALE, BigDecimal
				.ROUND_HALF_UP);
	}
}
