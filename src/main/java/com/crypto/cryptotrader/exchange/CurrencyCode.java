package com.crypto.cryptotrader.exchange;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "currencyCode")
public class CurrencyCode {
	private String code;

	public CurrencyCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
