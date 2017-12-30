package com.crypto.cryptotrader.calculation;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

public class CurrencyUtils {

	public static CurrencyPair toCurrencyPair(String baseCurrencyCode) {
		Currency baseCurrency = Currency.getInstance(baseCurrencyCode);

		return new CurrencyPair(baseCurrency, Currency.BTC);
	}
}
