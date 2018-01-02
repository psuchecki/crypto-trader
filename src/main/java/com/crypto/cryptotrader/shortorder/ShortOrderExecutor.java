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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.calculation.CalculationService;
import com.crypto.cryptotrader.calculation.CalculationUtils;
import com.crypto.cryptotrader.exchange.ExchangeHelper;

@Component
public class ShortOrderExecutor {
	private static final Logger logger = LoggerFactory.getLogger(ShortOrderExecutor.class);

	@Autowired
	private ShortOrderRepository shortOrderRepository;
	@Autowired
	private ExchangeHelper exchangeHelper;
	@Autowired
	private CalculationService calculationService;

	public void executeShortBidOrder(String baseCurrencyCode) throws IOException {
		Exchange bittrex = exchangeHelper.getExchange();
		CurrencyPair currencyPair = CalculationUtils.toCurrencyPair(baseCurrencyCode);
		MarketDataService marketDataService = bittrex.getMarketDataService();
		Ticker ticker = marketDataService.getTicker(currencyPair);
		BigDecimal originalAmount = calculationService.calculateOriginalAmount(ticker.getLast());

		BigDecimal bidPrice = calculationService.calculateBidPrice(ticker.getBid());
		LimitOrder bidLimitOrder = new LimitOrder.Builder(OrderType.BID, currencyPair).originalAmount
				(originalAmount).limitPrice(bidPrice).build();

		logger.info("BID ORDER: {}", bidLimitOrder);
		String orderRef = bittrex.getTradeService().placeLimitOrder(bidLimitOrder);

		ShortOrder shortOrder = new ShortOrderBuilder().setRef(orderRef).setOrderStatus(Order.OrderStatus.NEW)
				.setOriginalAmount(originalAmount).setOrderType(OrderType.BID).setBaseCurrencyCode(baseCurrencyCode)
				.createShortOrder();
		shortOrderRepository.save(shortOrder);
	}

	public void executeShortAskOrder(List<ShortOrder> shortOrders) throws IOException {
		for (ShortOrder shortOrder : shortOrders) {
			BigDecimal originalAmount = shortOrder.getOriginalAmount();
			BigDecimal originalPrice = shortOrder.getOriginalPrice();
			BigDecimal askPrice = calculationService.calculateAskPrice(originalPrice);
			LimitOrder askLimitOrder = new LimitOrder.Builder(OrderType.ASK, shortOrder.getCurrencyPair()).originalAmount
					(originalAmount).limitPrice(askPrice).build();

			logger.info("ASK ORDER: {}", askLimitOrder);
			String orderRef = exchangeHelper.getExchange().getTradeService().placeLimitOrder(askLimitOrder);
			ShortOrder askShortOrder = new ShortOrderBuilder().setRef(orderRef).setOrderStatus(Order.OrderStatus.NEW)
					.setOrderType(OrderType.ASK).setOriginalAmount(originalAmount).setOriginalPrice(originalPrice)
					.setBaseCurrencyCode(shortOrder.getBaseCurrencyCode()).createShortOrder();
			shortOrderRepository.save(askShortOrder);
		}
	}

	public void executeStopLoss(ShortOrder pendingAsk, BigDecimal lastPrice) throws IOException {
		BigDecimal originalAmount = pendingAsk.getOriginalAmount();
		BigDecimal stopLossPrice = calculationService.calculateStopLossPrice(lastPrice);
		LimitOrder stopLossLimitOrder = new LimitOrder.Builder(OrderType.ASK, pendingAsk.getCurrencyPair()).originalAmount
				(originalAmount).limitPrice(stopLossPrice).build();
		logger.info("STOP LOSS ORDER: {}", stopLossLimitOrder);

		String orderRef = exchangeHelper.getExchange().getTradeService().placeLimitOrder(stopLossLimitOrder);
		ShortOrder stopLossShortOrder = new ShortOrderBuilder().setRef(orderRef).setOrderStatus(Order.OrderStatus.NEW)
				.setOrderType(OrderType.EXIT_ASK).setOriginalAmount(originalAmount).setOriginalPrice(stopLossPrice)
				.setBaseCurrencyCode(pendingAsk.getBaseCurrencyCode()).createShortOrder();
		shortOrderRepository.save(stopLossShortOrder);
	}

}
