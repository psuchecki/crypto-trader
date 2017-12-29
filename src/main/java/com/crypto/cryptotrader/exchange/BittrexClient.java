package com.crypto.cryptotrader.exchange;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.calculation.CalculationUtils;
import com.crypto.cryptotrader.shortorder.ShortOrder;
import com.crypto.cryptotrader.shortorder.ShortOrderBuilder;
import com.crypto.cryptotrader.shortorder.ShortOrderRepository;

@Component
public class BittrexClient implements ExchangeClient {

	@Autowired
	private ShortOrderRepository shortOrderRepository;
	@Autowired
	private BittrexExchangeHelper bittrexExchangeHelper;

	@Override
	public void executeShortBidOrder(BigDecimal originalAmount) throws IOException {
		Exchange bittrex = bittrexExchangeHelper.getExchange();
		MarketDataService marketDataService = bittrex.getMarketDataService();
		Ticker ticker = marketDataService.getTicker(CalculationUtils.BTC_DOGE);

		BigDecimal bidOffsetLimit = CalculationUtils.calculateOffset(ticker.getBid(), CalculationUtils
				.BID_OFFSET_PERCENTAGE);
		BigDecimal bidPrice = ticker.getBid().add(bidOffsetLimit);
		LimitOrder bidLimitOrder = new LimitOrder.Builder(OrderType.BID, CalculationUtils.BTC_DOGE).originalAmount
				(originalAmount).limitPrice(bidPrice).build();

		String orderRef = bittrex.getTradeService().placeLimitOrder(bidLimitOrder);

		ShortOrder shortOrder = new ShortOrderBuilder().setRef(orderRef).setOrderStatus(Order.OrderStatus.NEW)
				.setOriginalAmount(originalAmount).setOrderType(OrderType.BID).createShortOrder();
		shortOrderRepository.save(shortOrder);
	}

	@Override
	public void executeShortAskOrder(List<ShortOrder> shortOrders) throws IOException {
		for (ShortOrder shortOrder : shortOrders) {
			BigDecimal originalAmount = shortOrder.getOriginalAmount();
			BigDecimal bidPrice = shortOrder.getPrice();
			BigDecimal askOffsetLimit = CalculationUtils.calculateOffset(bidPrice, CalculationUtils
					.ASK_OFFSET_PERCENTAGE);
			BigDecimal askPrice = bidPrice.add(askOffsetLimit);
			LimitOrder askLimitOrder = new LimitOrder.Builder(OrderType.ASK, CalculationUtils.BTC_DOGE).originalAmount
					(originalAmount).limitPrice(askPrice).build();

			String orderRef = bittrexExchangeHelper.getExchange().getTradeService().placeLimitOrder(askLimitOrder);
			ShortOrder askShortOrder = new ShortOrderBuilder().setRef(orderRef).setOrderStatus(Order.OrderStatus.NEW)
					.setOrderType(OrderType.ASK).setOriginalAmount(originalAmount).setPrice(bidPrice)
					.createShortOrder();
			shortOrderRepository.save(askShortOrder);
		}
	}

	@Override
	public void executeStopLoss(ShortOrder pendingAsk, BigDecimal lastPrice) throws IOException {
		BigDecimal originalAmount = pendingAsk.getOriginalAmount();
		BigDecimal stopLossOffsetLimit = CalculationUtils.calculateOffset(lastPrice, CalculationUtils
				.STOP_LOSS_OFFSET_PERCENTAGE);
//		BigDecimal stopLossPrice = lastPrice.subtract(stopLossOffsetLimit);
		BigDecimal stopLossPrice = lastPrice.add(stopLossOffsetLimit); //wrong
		LimitOrder stopLossLimitOrder = new LimitOrder.Builder(OrderType.ASK, CalculationUtils.BTC_DOGE).originalAmount
				(originalAmount).limitPrice(stopLossPrice).build();

		String orderRef = bittrexExchangeHelper.getExchange().getTradeService().placeLimitOrder(stopLossLimitOrder);
		ShortOrder stopLossShortOrder = new ShortOrderBuilder().setRef(orderRef).setOrderStatus(Order.OrderStatus.NEW)
				.setOrderType(OrderType.EXIT_ASK).setOriginalAmount(originalAmount).setPrice(stopLossPrice)
				.createShortOrder();
		shortOrderRepository.save(stopLossShortOrder);
	}
}
