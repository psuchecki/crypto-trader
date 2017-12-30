package com.crypto.cryptotrader.exchange;

import javax.annotation.PostConstruct;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "singleton")
public class BittrexExchangeHelper implements ExchangeHelper {
	@Value("${bittrex.user}")
	private String user;
	@Value("${bittrex.key}")
	private String key;
	@Value("${bittrex.secret}")
	private String secret;
	private Exchange exchange;

	@PostConstruct
	public void init() throws Exception {
		ExchangeSpecification exSpec = new BittrexExchange().getDefaultExchangeSpecification();
		exSpec.setUserName(user);
		exSpec.setApiKey(key);
		exSpec.setSecretKey(secret);
		exchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
	}

	@Override
	public Exchange getExchange() {
		return exchange;
	}
}
