package com.crypto.cryptotrader.shortorder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.crypto.cryptotrader.calculation.CalculationUtils;
import com.crypto.cryptotrader.exchange.ExchangeHelper;

@Component
public class ShortOrderMonitor {

	@Autowired
	private ShortOrderRepository shortOrderRepository;

	@Autowired
	private ExchangeHelper exchangeHelper;

	@Autowired
	private ShortOrderExecutor shortOrderExecutor;

	@Scheduled(fixedRate = 5000)
	public void handleCompletedBids() throws IOException {
		ShortOrder shortOrderPattern = new ShortOrderBuilder().setOrderStatus(Order.OrderStatus.NEW)
				.setOrderType(Order.OrderType.BID).createShortOrder();
		Example<ShortOrder> pendingNewShortOrdersExample = Example.of(shortOrderPattern);

		List<ShortOrder> pendingNewShortOrders = shortOrderRepository.findAll(pendingNewShortOrdersExample);
		if (CollectionUtils.isEmpty(pendingNewShortOrders)) {
			return;
		}

		TradeHistoryParamsAll tradeHistoryParamsAll = new TradeHistoryParamsAll();
		UserTrades tradeHistory = exchangeHelper.getExchange().getTradeService().getTradeHistory
				(tradeHistoryParamsAll);

		Map<String, BigDecimal> orderIdToPriceMap = tradeHistory.getUserTrades().stream().collect(Collectors.toMap
				(UserTrade::getOrderId, Trade::getPrice));
		List<ShortOrder> completedShortOrders = pendingNewShortOrders.stream()
				.filter(shortOrder -> orderIdToPriceMap.keySet().contains(shortOrder.getRef()))
				.collect(Collectors.toList());


		completedShortOrders.forEach(closedShortOrder -> {
					closedShortOrder.setOrderStatus(Order.OrderStatus.FILLED);
					closedShortOrder.setOrigrinalPrice(orderIdToPriceMap.get(closedShortOrder.getRef()));
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
		Ticker ticker = exchangeHelper.getExchange().getMarketDataService().getTicker(CalculationUtils.BTC_DOGE);

		BigDecimal lastPrice = ticker.getLast();
		BigDecimal stopLossOffset = CalculationUtils.calculateOffset(pendingAsk.getOrigrinalPrice(), CalculationUtils
				.STOP_LOSS_PERCENTAGE);
		BigDecimal stopLossThreshold = pendingAsk.getOrigrinalPrice().subtract(stopLossOffset);
//		BigDecimal stopLossThreshold = pendingAsk.getOrigrinalPrice().add(stopLossOffset); //wrong

		if (lastPrice.compareTo(stopLossThreshold) < 0) {
			boolean cancelOrder = exchangeHelper.getExchange().getTradeService().cancelOrder(pendingAsk.getRef());
			if (cancelOrder) {
				shortOrderExecutor.executeStopLoss(pendingAsk, lastPrice);
				pendingAsk.setOrderStatus(Order.OrderStatus.CANCELED);
				shortOrderRepository.save(pendingAsk);
			}
		}
	}

	private boolean markAskCompleted(ShortOrder pendingAsk) throws IOException {
		TradeHistoryParamsAll tradeHistoryParamsAll = new TradeHistoryParamsAll();
		UserTrades tradeHistory = exchangeHelper.getExchange().getTradeService().getTradeHistory
				(tradeHistoryParamsAll);
		Map<String, BigDecimal> orderIdToPriceMap = tradeHistory.getUserTrades().stream().collect(Collectors.toMap
				(UserTrade::getOrderId, Trade::getPrice));

		Optional<String> completedAskOptional = orderIdToPriceMap.keySet().stream().filter(orderRef -> orderRef.equals
				(pendingAsk.getRef())).findFirst();

		if (completedAskOptional.isPresent()) {
			pendingAsk.setOrderStatus(Order.OrderStatus.FILLED);
			pendingAsk.setOrigrinalPrice(orderIdToPriceMap.get(pendingAsk.getRef()));
			shortOrderRepository.save(pendingAsk);
			return true;
		}

		return false;
	}

}
