package com.crypto.cryptotrader;

import java.util.Collections;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.exchange.BittrexClient;
import com.crypto.cryptotrader.shortorder.ShortOrder;
import com.crypto.cryptotrader.shortorder.ShortOrderMonitor;
import com.crypto.cryptotrader.shortorder.ShortOrderRepository;

@Component
public class Starter {
	@Autowired
	private BittrexClient bittrexClient;
	@Autowired
	private ShortOrderMonitor shortOrderMonitor;
	@Autowired
	private ShortOrderRepository shortOrderRepository;

	@PostConstruct
	public void init() throws Exception {
//		bittrexClient.executeShortBidOrder(BigDecimal.valueOf(1500));
//		shortOrderMonitor.handleCompletedBids();
//		shortOrderMonitor.handleCompletedAsks();

		Optional<ShortOrder> shortOrder = shortOrderRepository.findById("5a45527c8a7cc3069cce151f");
		bittrexClient.executeShortAskOrder(Collections.singletonList(shortOrder.get()));
		shortOrderMonitor.handleCompletedAsks();

	}
}
