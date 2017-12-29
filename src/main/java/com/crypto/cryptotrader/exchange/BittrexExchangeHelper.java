package com.crypto.cryptotrader.exchange;

import javax.annotation.PostConstruct;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "singleton")
public class BittrexExchangeHelper {

	private Exchange exchange;

	@PostConstruct
	public void init() throws Exception {
		ExchangeSpecification exSpec = new BittrexExchange().getDefaultExchangeSpecification();
		exSpec.setUserName("piotr.jan.suchecki@gmail.com");
		exSpec.setApiKey("a69e3e5924b14be291741f8aa102d216");
		exSpec.setSecretKey("abc2ee264f764e94b75a0dc3c97cfde8");
		exchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
	}

	public Exchange getExchange() {
		return exchange;
	}
}
