package com.crypto.cryptotrader.shortorder;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.calculation.CalculationUtils;
import com.crypto.cryptotrader.calculation.CurrencyUtils;
import com.crypto.cryptotrader.exchange.ExchangeHelper;

@Component
public class ShortOrderExecutor {

	@Autowired
	private ShortOrderRepository shortOrderRepository;
	@Autowired
	private ExchangeHelper exchangeHelper;

	public void executeShortBidOrder(String baseCurrencyCode) throws IOException {
		Exchange bittrex = exchangeHelper.getExchange();
		CurrencyPair currencyPair = CurrencyUtils.toCurrencyPair(baseCurrencyCode);
		MarketDataService marketDataService = bittrex.getMarketDataService();
		Ticker ticker = marketDataService.getTicker(currencyPair);
		BigDecimal originalAmount = CurrencyUtils.calculateOriginalAmount(ticker.getLast());

		BigDecimal bidOffsetLimit = CalculationUtils.calculateOffset(ticker.getBid(), CalculationUtils
				.BID_OFFSET_PERCENTAGE);
//		BigDecimal bidPrice = ticker.getBid().subtract(bidOffsetLimit); //wrong
		BigDecimal bidPrice = ticker.getBid().add(bidOffsetLimit);
		LimitOrder bidLimitOrder = new LimitOrder.Builder(OrderType.BID, currencyPair).originalAmount
				(originalAmount).limitPrice(bidPrice).build();

		String orderRef = bittrex.getTradeService().placeLimitOrder(bidLimitOrder);

		ShortOrder shortOrder = new ShortOrderBuilder().setRef(orderRef).setOrderStatus(Order.OrderStatus.NEW)
				.setOriginalAmount(originalAmount).setOrderType(OrderType.BID).setBaseCurrencyCode(baseCurrencyCode)
				.createShortOrder();
		shortOrderRepository.save(shortOrder);
	}

	public void executeShortAskOrder(List<ShortOrder> shortOrders) throws IOException {
		for (ShortOrder shortOrder : shortOrders) {
			BigDecimal originalAmount = shortOrder.getOriginalAmount();
			BigDecimal bidPrice = shortOrder.getOriginalPrice();
			BigDecimal askOffsetLimit = CalculationUtils.calculateOffset(bidPrice, CalculationUtils
					.ASK_OFFSET_PERCENTAGE);
			BigDecimal askPrice = bidPrice.add(askOffsetLimit);
			LimitOrder askLimitOrder = new LimitOrder.Builder(OrderType.ASK, shortOrder.getCurrencyPair()).originalAmount
					(originalAmount).limitPrice(askPrice).build();

			String orderRef = exchangeHelper.getExchange().getTradeService().placeLimitOrder(askLimitOrder);
			ShortOrder askShortOrder = new ShortOrderBuilder().setRef(orderRef).setOrderStatus(Order.OrderStatus.NEW)
					.setOrderType(OrderType.ASK).setOriginalAmount(originalAmount).setOriginalPrice(bidPrice)
					.setBaseCurrencyCode(shortOrder.getBaseCurrencyCode()).createShortOrder();
			shortOrderRepository.save(askShortOrder);
		}
	}

	public void executeStopLoss(ShortOrder pendingAsk, BigDecimal lastPrice) throws IOException {
		BigDecimal originalAmount = pendingAsk.getOriginalAmount();
		BigDecimal stopLossOffsetLimit = CalculationUtils.calculateOffset(lastPrice, CalculationUtils
				.STOP_LOSS_OFFSET_PERCENTAGE);
		BigDecimal stopLossPrice = lastPrice.subtract(stopLossOffsetLimit);
//		BigDecimal stopLossPrice = lastPrice.add(stopLossOffsetLimit); //wrong
		LimitOrder stopLossLimitOrder = new LimitOrder.Builder(OrderType.ASK, pendingAsk.getCurrencyPair()).originalAmount
				(originalAmount).limitPrice(stopLossPrice).build();

		String orderRef = exchangeHelper.getExchange().getTradeService().placeLimitOrder(stopLossLimitOrder);
		ShortOrder stopLossShortOrder = new ShortOrderBuilder().setRef(orderRef).setOrderStatus(Order.OrderStatus.NEW)
				.setOrderType(OrderType.EXIT_ASK).setOriginalAmount(originalAmount).setOriginalPrice(stopLossPrice)
				.setBaseCurrencyCode(pendingAsk.getBaseCurrencyCode()).createShortOrder();
		shortOrderRepository.save(stopLossShortOrder);
	}
}
