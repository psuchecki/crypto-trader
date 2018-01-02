package com.crypto.cryptotrader;

import static com.crypto.cryptotrader.feeds.GmailFeeder.AUTHENTICATED_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.UserTrade;
import org.knowm.xchange.dto.trade.UserTrades;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.knowm.xchange.service.trade.params.TradeHistoryParamsAll;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit4.SpringRunner;

import com.crypto.cryptotrader.exchange.ExchangeHelper;
import com.crypto.cryptotrader.feeds.GmailFeeder;
import com.crypto.cryptotrader.shortorder.ShortOrder;
import com.crypto.cryptotrader.shortorder.ShortOrderBuilder;
import com.crypto.cryptotrader.shortorder.ShortOrderExecutor;
import com.crypto.cryptotrader.shortorder.ShortOrderMonitor;
import com.crypto.cryptotrader.shortorder.ShortOrderRepository;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShortOrderIntegrationTest {

	public static final BigDecimal BID_LIMIT_PRICE = BigDecimal.valueOf(0.00000050);
	public static final BigDecimal BID_LAST_PRICE = BigDecimal.valueOf(0.00000045);
	public static final BigDecimal SELL_LOSS_PRICE = BigDecimal.valueOf(0.00000010);
	public static final BigDecimal BID_ACTUAL_PRICE = BigDecimal.valueOf(0.00000052);
	public static final String BID_REF = "12345";
	public static final String ASK_REF = "54321";
	public static final String STOP_LOSS_REF = "33333";
	public static final String DOGE_CODE = "DOGE";
	public static final String MESSAGE_ID = "message1";
	public static final String ALAN_BODY_SNIPPET = "Dear ilinx99, alanmasters you follow published new idea " +
			"Aurum (AUR) - 2018 " +
			"Opportunity (1230%+ Profits Potential). TradingView Team www.tradingview.com";
	public static final CurrencyPair BTC_DOGE = new CurrencyPair(Currency.DOGE, Currency.BTC);
	public static final CurrencyPair BTC_AUR = new CurrencyPair(Currency.AUR, Currency.BTC);

	@Autowired
	private ShortOrderExecutor shortOrderExecutor;
	@Autowired
	private ShortOrderRepository shortOrderRepository;
	@Autowired
	private ShortOrderMonitor shortOrderMonitor;
	@Autowired
	private GmailFeeder gmailFeeder;

	@MockBean
	private ExchangeHelper exchangeHelper;
	@MockBean
	private BittrexExchange bittrexExchange;
	@MockBean
	private MarketDataService marketDataService;
	@MockBean
	private TradeService tradeService;
	@MockBean(answer = Answers.RETURNS_DEEP_STUBS)
	private Gmail gmailService;

	@Before
	public void setUp() throws Exception {
		given(exchangeHelper.getExchange()).willReturn(bittrexExchange);
		given(bittrexExchange.getMarketDataService()).willReturn(marketDataService);
		given(bittrexExchange.getTradeService()).willReturn(tradeService);
		Ticker ticker = new Ticker.Builder().bid(BID_LIMIT_PRICE).last(BID_LAST_PRICE).build();
		given(marketDataService.getTicker(BTC_DOGE)).willReturn(ticker);
		given(marketDataService.getTicker(BTC_AUR)).willReturn(ticker);
	}

	@After
	public void tearDown() throws Exception {
		shortOrderRepository.deleteAll();
	}

	@Test
	public void shouldExecuteShortBidOrder() throws Exception {
		given(tradeService.placeLimitOrder(any(LimitOrder.class))).willReturn(BID_REF);

		shortOrderExecutor.executeShortBidOrder(DOGE_CODE);

		Example<ShortOrder> newBidExample = new ShortOrderBuilder().setRef(BID_REF)
				.setOrderType(Order.OrderType.BID).setOrderStatus(Order.OrderStatus.NEW).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(newBidExample).size()).isEqualTo(1);
	}

	@Test
	public void shouldCompleteBidOrderAndExecuteAsk() throws Exception {
		given(tradeService.placeLimitOrder(any(LimitOrder.class))).willReturn(BID_REF).willReturn(ASK_REF);
		setupUserTrades(BID_REF);

		shortOrderExecutor.executeShortBidOrder(DOGE_CODE);
		shortOrderMonitor.handleCompletedBids();

		Example<ShortOrder> filledBidExample = new ShortOrderBuilder().setRef(BID_REF).setOrderType(Order.OrderType.BID).setOrderStatus(Order.OrderStatus.FILLED).setOriginalPrice
						(BID_ACTUAL_PRICE).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(filledBidExample).size()).isEqualTo(1);

		Example<ShortOrder> newAskExample = new ShortOrderBuilder().setRef(ASK_REF).setOrderType(Order.OrderType.ASK).setOrderStatus(Order.OrderStatus.NEW).setOriginalPrice
				(BID_ACTUAL_PRICE).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(newAskExample).size()).isEqualTo(1);
	}

	@Test
	public void shouldCompleteFullShortOrder() throws Exception {
		given(tradeService.placeLimitOrder(any(LimitOrder.class))).willReturn(BID_REF).willReturn(ASK_REF);
		setupUserTrades(BID_REF, ASK_REF);

		shortOrderExecutor.executeShortBidOrder(DOGE_CODE);
		shortOrderMonitor.handleCompletedBids();
		shortOrderMonitor.handleCompletedAsks();

		Example<ShortOrder> filledBidExample = new ShortOrderBuilder().setRef(BID_REF).setOrderType(Order.OrderType.BID).setOrderStatus(Order.OrderStatus.FILLED).setOriginalPrice
						(BID_ACTUAL_PRICE).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(filledBidExample).size()).isEqualTo(1);

		Example<ShortOrder> filledAskExample = new ShortOrderBuilder().setRef(ASK_REF).setOrderType(Order.OrderType.ASK).setOrderStatus(Order.OrderStatus.FILLED).setOriginalPrice
				(BID_ACTUAL_PRICE).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(filledAskExample).size()).isEqualTo(1);
	}

	@Test
	public void shouldCompleteFullShortOrderFromAlanGmail() throws IOException {
		setupGmailMessages();
		given(tradeService.placeLimitOrder(any(LimitOrder.class))).willReturn(BID_REF).willReturn(ASK_REF);
		setupUserTrades(BID_REF, ASK_REF);

		gmailFeeder.checkForFeeds();
		shortOrderMonitor.handleCompletedBids();
		shortOrderMonitor.handleCompletedAsks();

		Example<ShortOrder> filledBidExample = new ShortOrderBuilder().setRef(BID_REF).setOrderType(Order.OrderType.BID).setOrderStatus(Order.OrderStatus.FILLED).setOriginalPrice
				(BID_ACTUAL_PRICE).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(filledBidExample).size()).isEqualTo(1);

		Example<ShortOrder> filledAskExample = new ShortOrderBuilder().setRef(ASK_REF).setOrderType(Order.OrderType.ASK).setOrderStatus(Order.OrderStatus.FILLED).setOriginalPrice
				(BID_ACTUAL_PRICE).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(filledAskExample).size()).isEqualTo(1);
	}


	@Test
	public void shouldExecuteStopLossScenario() throws Exception {
		Ticker bidTicker = new Ticker.Builder().bid(BID_LIMIT_PRICE).last(BID_LAST_PRICE).build();
		Ticker stopLossTicker = new Ticker.Builder().last(SELL_LOSS_PRICE).build();
		given(marketDataService.getTicker(BTC_DOGE)).willReturn(bidTicker).willReturn(stopLossTicker);
		given(tradeService.placeLimitOrder(any(LimitOrder.class))).willReturn(BID_REF).willReturn(ASK_REF).willReturn
				(STOP_LOSS_REF);
		setupUserTrades(BID_REF);
		given(tradeService.cancelOrder(ASK_REF)).willReturn(true);

		shortOrderExecutor.executeShortBidOrder(DOGE_CODE);
		shortOrderMonitor.handleCompletedBids();
		shortOrderMonitor.handleCompletedAsks();

		Example<ShortOrder> filledBidExample = new ShortOrderBuilder().setRef(BID_REF).setOrderType(Order.OrderType.BID).setOrderStatus(Order.OrderStatus.FILLED).setOriginalPrice
				(BID_ACTUAL_PRICE).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(filledBidExample).size()).isEqualTo(1);

		Example<ShortOrder> cancelledAskExample = new ShortOrderBuilder().setRef(ASK_REF).setOrderType(Order.OrderType.ASK).setOrderStatus(Order.OrderStatus.CANCELED).setOriginalPrice
				(BID_ACTUAL_PRICE).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(cancelledAskExample).size()).isEqualTo(1);

		Example<ShortOrder> stopLossExample = new ShortOrderBuilder().setRef(STOP_LOSS_REF).setOrderType(Order
				.OrderType.EXIT_ASK).setOrderStatus(Order.OrderStatus.NEW).createShortOrderExample();
		assertThat(shortOrderRepository.findAll(stopLossExample).size()).isEqualTo(1);
	}

	private void setupUserTrades(String... refs) throws IOException {
		List<UserTrade> trades = Arrays.stream(refs).map(ref -> new UserTrade.Builder().orderId(ref).price
				(BID_ACTUAL_PRICE).timestamp(new Date()).build()).collect(Collectors.toList());
		UserTrades userTrades = new UserTrades(trades, Trades.TradeSortType.SortByTimestamp);
		given(tradeService.getTradeHistory(any(TradeHistoryParamsAll.class))).willReturn(userTrades);
	}

	private void setupGmailMessages() throws IOException {
		Message message = new Message().setId(MESSAGE_ID);
		ListMessagesResponse listMessagesResponse = new ListMessagesResponse().setMessages(Collections.singletonList(message));
		given(gmailService.users().messages().list(AUTHENTICATED_USER).setQ(anyString()).execute())
				.willReturn(listMessagesResponse);
		Message messageDetail = new Message().setSnippet(ALAN_BODY_SNIPPET);
		given(gmailService.users().messages().get(AUTHENTICATED_USER, MESSAGE_ID).execute()).willReturn(messageDetail);
	}

}
