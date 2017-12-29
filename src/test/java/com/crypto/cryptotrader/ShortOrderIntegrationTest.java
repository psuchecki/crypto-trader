package com.crypto.cryptotrader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit4.SpringRunner;

import com.crypto.cryptotrader.calculation.CalculationUtils;
import com.crypto.cryptotrader.exchange.ExchangeHelper;
import com.crypto.cryptotrader.shortorder.ShortOrder;
import com.crypto.cryptotrader.shortorder.ShortOrderBuilder;
import com.crypto.cryptotrader.shortorder.ShortOrderExecutor;
import com.crypto.cryptotrader.shortorder.ShortOrderMonitor;
import com.crypto.cryptotrader.shortorder.ShortOrderRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShortOrderIntegrationTest {

	public static final BigDecimal DOGE_AMOUNT = BigDecimal.valueOf(1500);
	public static final BigDecimal BID_LIMIT_PRICE = BigDecimal.valueOf(0.00000050);
	public static final BigDecimal SELL_LOSS_PRICE = BigDecimal.valueOf(0.00000010);
	public static final BigDecimal BID_ACTUAL_PRICE = BigDecimal.valueOf(0.00000052);
	public static final String BID_REF = "12345";
	public static final String ASK_REF = "54321";
	public static final String STOP_LOSS_REF = "33333";

	@Autowired
	private ShortOrderExecutor shortOrderExecutor;
	@Autowired
	private ShortOrderRepository shortOrderRepository;
	@Autowired
	private ShortOrderMonitor shortOrderMonitor;

	@MockBean
	private ExchangeHelper exchangeHelper;
	@MockBean
	private BittrexExchange bittrexExchange;
	@MockBean
	private MarketDataService marketDataService;
	@MockBean
	private TradeService tradeService;

	@Before
	public void setUp() throws Exception {
		shortOrderRepository.deleteAll();
		given(exchangeHelper.getExchange()).willReturn(bittrexExchange);
		given(bittrexExchange.getMarketDataService()).willReturn(marketDataService);
		given(bittrexExchange.getTradeService()).willReturn(tradeService);
		Ticker ticker = new Ticker.Builder().bid(BID_LIMIT_PRICE).build();
		given(marketDataService.getTicker(CalculationUtils.BTC_DOGE)).willReturn(ticker);
	}

	@Test
	public void shouldExecuteShortBidOrder() throws Exception {
		given(tradeService.placeLimitOrder(any(LimitOrder.class))).willReturn(BID_REF);

		shortOrderExecutor.executeShortBidOrder(DOGE_AMOUNT);

		Example<ShortOrder> newBidExample = Example.of(new ShortOrderBuilder().setRef(BID_REF).setOriginalAmount
				(DOGE_AMOUNT)
				.setOrderType(Order.OrderType.BID).setOrderStatus(Order.OrderStatus.NEW).createShortOrder());
		assertThat(shortOrderRepository.findAll(newBidExample).size()).isEqualTo(1);
	}

	@Test
	public void shouldCompleteBidOrderAndExecuteAsk() throws Exception {
		given(tradeService.placeLimitOrder(any(LimitOrder.class))).willReturn(BID_REF).willReturn(ASK_REF);
		setupUserTrades(BID_REF);

		shortOrderExecutor.executeShortBidOrder(DOGE_AMOUNT);
		shortOrderMonitor.handleCompletedBids();

		Example<ShortOrder> filledBidExample = Example.of(new ShortOrderBuilder().setRef(BID_REF).setOriginalAmount
				(DOGE_AMOUNT).setOrderType(Order.OrderType.BID).setOrderStatus(Order.OrderStatus.FILLED).setOrigrinalPrice
						(BID_ACTUAL_PRICE).createShortOrder());
		assertThat(shortOrderRepository.findAll(filledBidExample).size()).isEqualTo(1);

		Example<ShortOrder> newAskExample = Example.of(new ShortOrderBuilder().setRef(ASK_REF).setOriginalAmount
				(DOGE_AMOUNT).setOrderType(Order.OrderType.ASK).setOrderStatus(Order.OrderStatus.NEW).setOrigrinalPrice
				(BID_ACTUAL_PRICE).createShortOrder());
		assertThat(shortOrderRepository.findAll(newAskExample).size()).isEqualTo(1);
	}

	@Test
	public void shouldCompleteFullShortOrder() throws Exception {
		given(tradeService.placeLimitOrder(any(LimitOrder.class))).willReturn(BID_REF).willReturn(ASK_REF);
		setupUserTrades(BID_REF, ASK_REF);

		shortOrderExecutor.executeShortBidOrder(DOGE_AMOUNT);
		shortOrderMonitor.handleCompletedBids();
		shortOrderMonitor.handleCompletedAsks();

		Example<ShortOrder> filledBidExample = Example.of(new ShortOrderBuilder().setRef(BID_REF).setOriginalAmount
				(DOGE_AMOUNT).setOrderType(Order.OrderType.BID).setOrderStatus(Order.OrderStatus.FILLED).setOrigrinalPrice
						(BID_ACTUAL_PRICE).createShortOrder());
		assertThat(shortOrderRepository.findAll(filledBidExample).size()).isEqualTo(1);

		Example<ShortOrder> filledAskExample = Example.of(new ShortOrderBuilder().setRef(ASK_REF).setOriginalAmount
				(DOGE_AMOUNT).setOrderType(Order.OrderType.ASK).setOrderStatus(Order.OrderStatus.FILLED).setOrigrinalPrice
				(BID_ACTUAL_PRICE).createShortOrder());
		assertThat(shortOrderRepository.findAll(filledAskExample).size()).isEqualTo(1);
	}

	@Test
	public void shouldExecuteStopLossScenario() throws Exception {
		Ticker bidTicker = new Ticker.Builder().bid(BID_LIMIT_PRICE).build();
		Ticker stopLossTicker = new Ticker.Builder().last(SELL_LOSS_PRICE).build();
		given(marketDataService.getTicker(CalculationUtils.BTC_DOGE)).willReturn(bidTicker).willReturn(stopLossTicker);
		given(tradeService.placeLimitOrder(any(LimitOrder.class))).willReturn(BID_REF).willReturn(ASK_REF).willReturn
				(STOP_LOSS_REF);
		setupUserTrades(BID_REF);
		given(tradeService.cancelOrder(ASK_REF)).willReturn(true);

		shortOrderExecutor.executeShortBidOrder(DOGE_AMOUNT);
		shortOrderMonitor.handleCompletedBids();
		shortOrderMonitor.handleCompletedAsks();

		Example<ShortOrder> filledBidExample = Example.of(new ShortOrderBuilder().setRef(BID_REF).setOriginalAmount
				(DOGE_AMOUNT).setOrderType(Order.OrderType.BID).setOrderStatus(Order.OrderStatus.FILLED).setOrigrinalPrice
				(BID_ACTUAL_PRICE).createShortOrder());
		assertThat(shortOrderRepository.findAll(filledBidExample).size()).isEqualTo(1);

		Example<ShortOrder> cancelledAskExample = Example.of(new ShortOrderBuilder().setRef(ASK_REF).setOriginalAmount
				(DOGE_AMOUNT).setOrderType(Order.OrderType.ASK).setOrderStatus(Order.OrderStatus.CANCELED).setOrigrinalPrice
				(BID_ACTUAL_PRICE).createShortOrder());
		assertThat(shortOrderRepository.findAll(cancelledAskExample).size()).isEqualTo(1);

		Example<ShortOrder> stopLossExample = Example.of(new ShortOrderBuilder().setRef(STOP_LOSS_REF).setOriginalAmount
				(DOGE_AMOUNT).setOrderType(Order.OrderType.EXIT_ASK).setOrderStatus(Order.OrderStatus.NEW).createShortOrder());
		assertThat(shortOrderRepository.findAll(stopLossExample).size()).isEqualTo(1);
	}

	private void setupUserTrades(String... refs) throws IOException {
		List<UserTrade> trades = Arrays.stream(refs).map(ref -> new UserTrade.Builder().orderId(ref).price
				(BID_ACTUAL_PRICE).timestamp(new Date()).build()).collect(Collectors.toList());
		UserTrades userTrades = new UserTrades(trades, Trades.TradeSortType.SortByTimestamp);
		given(tradeService.getTradeHistory(any(TradeHistoryParamsAll.class))).willReturn(userTrades);
	}

}
