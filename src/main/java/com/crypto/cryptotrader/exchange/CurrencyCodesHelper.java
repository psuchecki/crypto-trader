package com.crypto.cryptotrader.exchange;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CurrencyCodesHelper {
	@Autowired
	private CurrencyCodeRepository currencyCodeRepository;

	private Map<String, String> currencyCodesMap;

	public Map<String, String> getCurrencyCodesMap() {
		if (currencyCodesMap == null) {
			currencyCodesMap = currencyCodeRepository.findAll().stream()
					.collect(Collectors.toMap(CurrencyCode::getCode, CurrencyCode::getCode));
		}

		return currencyCodesMap;
	}
}
