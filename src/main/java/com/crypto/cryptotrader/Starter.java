package com.crypto.cryptotrader;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.crypto.cryptotrader.calculation.CalculationConfig;
import com.crypto.cryptotrader.calculation.CalculationConfigRepository;
import com.crypto.cryptotrader.exchange.CurrencyCode;
import com.crypto.cryptotrader.exchange.CurrencyCodeRepository;
import com.crypto.cryptotrader.exchange.ExchangeHelper;
import com.crypto.cryptotrader.feeds.GmailFeeder;
import com.crypto.cryptotrader.feeds.TwitterFeeder;
import com.crypto.cryptotrader.parser.ImageParser;
import com.crypto.cryptotrader.shortorder.ShortOrderExecutor;
import com.crypto.cryptotrader.shortorder.ShortOrderMonitor;
import com.crypto.cryptotrader.shortorder.ShortOrderRepository;

@Component
public class Starter {
	@Autowired
	private ShortOrderExecutor shortOrderExecutor;
	@Autowired
	private ShortOrderMonitor shortOrderMonitor;
	@Autowired
	private ShortOrderRepository shortOrderRepository;
	@Autowired
	private GmailFeeder gmailFeeder;
	@Autowired
	private ImageParser imageParser;
	@Autowired
	private TwitterFeeder twitterFeeder;

	@Autowired
	private CalculationConfigRepository calculationConfigRepository;
	@Autowired
	private CurrencyCodeRepository currencyCodeRepository;
	@Autowired
	private ExchangeHelper exchangeHelper;

	@PostConstruct
	public void init() throws Exception {
		initCurrencyCodes();
		initCalculationConfig();
//		imageParser.getCurrencyCodeFromImage("C:\\Users\\psuchecki\\Desktop\\images\\unnamed.gif");
//		gmailFeeder.checkForFeeds();
//		shortOrderExecutor.executeShortBidOrder("DOGE");
//		shortOrderMonitor.handleCompletedBids();
//		shortOrderMonitor.handleCompletedAsks();

//		Optional<ShortOrder> shortOrder = shortOrderRepository.findById("5a45527c8a7cc3069cce151f");
//		bittrexClient.executeShortAskOrder(Collections.singletonList(shortOrder.get()));
//		shortOrderMonitor.handleCompletedAsks();
	}

	private void initCurrencyCodes() {
		if (CollectionUtils.isEmpty(currencyCodeRepository.findAll())) {
			ExchangeMetaData exchangeMetaData = exchangeHelper.getExchange().getExchangeMetaData();
			List<CurrencyCode> currencyCodes = exchangeMetaData.getCurrencies().keySet().stream().map(currency -> new
					CurrencyCode(currency.getCurrencyCode())).collect(Collectors.toList());

			currencyCodeRepository.saveAll(currencyCodes);
		}

	}

	private void initCalculationConfig() {
		if (CollectionUtils.isEmpty(calculationConfigRepository.findAll())) {
			calculationConfigRepository.save(CalculationConfig.getDefault());
		}
	}


}
