package com.crypto.cryptotrader;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//import com.crypto.cryptotrader.feeds.GmailFeeder;
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

	@PostConstruct
	public void init() throws Exception {
//		gmailFeeder.checkForFeeds();
//		shortOrderExecutor.executeShortBidOrder(BigDecimal.valueOf(1500), "DOGE");
//		shortOrderMonitor.handleCompletedBids();
//		shortOrderMonitor.handleCompletedAsks();

//		Optional<ShortOrder> shortOrder = shortOrderRepository.findById("5a45527c8a7cc3069cce151f");
//		bittrexClient.executeShortAskOrder(Collections.singletonList(shortOrder.get()));
//		shortOrderMonitor.handleCompletedAsks();

	}
}
