package com.crypto.cryptotrader.exchange;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.crypto.cryptotrader.shortorder.ShortOrder;

public interface ExchangeClient {
	void executeShortBidOrder(BigDecimal originalAmount) throws IOException;

	void executeShortAskOrder(List<ShortOrder> completedShortOrders) throws IOException;

	void executeStopLoss(ShortOrder pendingAsk, BigDecimal lastPrice) throws IOException;
}
