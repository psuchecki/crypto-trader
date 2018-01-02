package com.crypto.cryptotrader.shortorder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.crypto.cryptotrader.calculation.CalculationService;
import com.crypto.cryptotrader.exchange.ExchangeHelper;

@Component
public class ShortOrderMonitor {
	private static final Logger logger = LoggerFactory.getLogger(ShortOrderMonitor.class);

	@Autowired
	private ShortOrderRepository shortOrderRepository;

	@Autowired
	private ExchangeHelper exchangeHelper;

	@Autowired
	private ShortOrderExecutor shortOrderExecutor;
	@Autowired
	private CalculationService calculationService;

	@Scheduled(fixedRate = 2000)
	public void handleCompletedBids() throws IOException {
		ShortOrder shortOrderPattern = new ShortOrderBuilder().setOrderStatus(Order.OrderStatus.NEW)
				.setOrderType(Order.OrderType.BID).createShortOrder();
		Example<ShortOrder> pendingNewShortOrdersExample = Example.of(shortOrderPattern);

		List<ShortOrder> pendingNewShortOrders = shortOrderRepository.findAll(pendingNewShortOrdersExample);
		if (CollectionUtils.isEmpty(pendingNewShortOrders)) {
			return;
		}

		List<CurrencyPair> currencyPairs = pendingNewShortOrders.stream().map(pendingShortOrder -> pendingShortOrder
				.getCurrencyPair()).collect(Collectors.toList());
		TradeHistoryParamsAll tradeHistoryParamsAll = new TradeHistoryParamsAll();
		tradeHistoryParamsAll.setCurrencyPairs(currencyPairs);
		UserTrades tradeHistory = exchangeHelper.getExchange().getTradeService().getTradeHistory
				(tradeHistoryParamsAll);

		Map<String, BigDecimal> orderIdToPriceMap = tradeHistory.getUserTrades().stream().collect(Collectors.toMap
				(UserTrade::getOrderId, Trade::getPrice));
		List<ShortOrder> completedShortOrders = pendingNewShortOrders.stream()
				.filter(shortOrder -> orderIdToPriceMap.keySet().contains(shortOrder.getRef()))
				.collect(Collectors.toList());

		completedShortOrders.forEach(closedShortOrder -> {
					String orderRef = closedShortOrder.getRef();
					logger.info("BID CLOSE: {}", orderRef);
					closedShortOrder.setOrderStatus(Order.OrderStatus.FILLED);
					closedShortOrder.setOriginalPrice(orderIdToPriceMap.get(orderRef));
				}
		);

		shortOrderExecutor.executeShortAskOrder(completedShortOrders);
		shortOrderRepository.saveAll(completedShortOrders);
	}


	@Scheduled(fixedRate = 5000)
	public void handleCompletedAsks() throws IOException {
		ShortOrder shortOrderPattern = new ShortOrderBuilder().setOrderStatus(Order.OrderStatus.NEW)
				.setOrderType(Order.OrderType.ASK).createShortOrder();
		Example<ShortOrder> pendingAskExample = Example.of(shortOrderPattern);

		List<ShortOrder> pendingAsks = shortOrderRepository.findAll(pendingAskExample);
		if (CollectionUtils.isEmpty(pendingAsks)) {
			return;
		}

		for (ShortOrder pendingAsk : pendingAsks) {
			boolean askCompleted = markAskCompleted(pendingAsk);
			if (!askCompleted) {
				tryStopLoss(pendingAsk);
			}
		}

	}

	private void tryStopLoss(ShortOrder pendingAsk) throws IOException {
		Ticker ticker = exchangeHelper.getExchange().getMarketDataService().getTicker(pendingAsk.getCurrencyPair());

		BigDecimal lastPrice = ticker.getLast();
		BigDecimal stopLossThreshold = calculationService.calculateStopLossThreshold(pendingAsk
				.getOriginalPrice());
//		BigDecimal stopLossThreshold = pendingAsk.getOriginalPrice().add(stopLossOffset); //wrong

		if (lastPrice.compareTo(stopLossThreshold) < 0) {
			boolean cancelOrder = exchangeHelper.getExchange().getTradeService().cancelOrder(pendingAsk.getRef());
			if (cancelOrder) {
				logger.info("ASK CANCEL: {}", pendingAsk.getRef());
				pendingAsk.setOrderStatus(Order.OrderStatus.CANCELED);
				shortOrderRepository.save(pendingAsk);
				shortOrderExecutor.executeStopLoss(pendingAsk, lastPrice);
			}
		}
	}

	private boolean markAskCompleted(ShortOrder pendingAsk) throws IOException {
		TradeHistoryParamsAll tradeHistoryParamsAll = new TradeHistoryParamsAll();
		tradeHistoryParamsAll.setCurrencyPair(pendingAsk.getCurrencyPair());
		UserTrades tradeHistory = exchangeHelper.getExchange().getTradeService().getTradeHistory
				(tradeHistoryParamsAll);
		Map<String, BigDecimal> orderIdToPriceMap = tradeHistory.getUserTrades().stream().collect(Collectors.toMap
				(UserTrade::getOrderId, Trade::getPrice));

		String pendingAskRef = pendingAsk.getRef();
		Optional<String> completedAskOptional = orderIdToPriceMap.keySet().stream().filter(orderRef -> orderRef.equals
				(pendingAskRef)).findFirst();

		if (completedAskOptional.isPresent()) {
			logger.info("ASK CLOSE: {}", pendingAskRef);
			pendingAsk.setOrderStatus(Order.OrderStatus.FILLED);
			pendingAsk.setOriginalPrice(orderIdToPriceMap.get(pendingAskRef));
			shortOrderRepository.save(pendingAsk);
			return true;
		}

		return false;
	}

}
