package com.crypto.cryptotrader;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.crypto.cryptotrader.calculation.CalculationConfig;
import com.crypto.cryptotrader.calculation.CalculationConfigRepository;
import com.crypto.cryptotrader.feeds.GmailFeeder;
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
	private CalculationConfigRepository calculationConfigRepository;

	@PostConstruct
	public void init() throws Exception {
		initCalculationConfig();
//		gmailFeeder.checkForFeeds();
//		shortOrderExecutor.executeShortBidOrder("DOGE");
//		shortOrderMonitor.handleCompletedBids();
//		shortOrderMonitor.handleCompletedAsks();

//		Optional<ShortOrder> shortOrder = shortOrderRepository.findById("5a45527c8a7cc3069cce151f");
//		bittrexClient.executeShortAskOrder(Collections.singletonList(shortOrder.get()));
//		shortOrderMonitor.handleCompletedAsks();
	}

	private void initCalculationConfig() {
		if (CollectionUtils.isEmpty(calculationConfigRepository.findAll())) {
			calculationConfigRepository.save(CalculationConfig.getDefault());
		}
	}


}
