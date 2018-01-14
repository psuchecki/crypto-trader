package com.crypto.cryptotrader.api;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crypto.cryptotrader.shortorder.ShortOrderExecutor;
import com.crypto.cryptotrader.shortorder.ShortOrderMonitor;

@RestController
public class TradeController {
	@Value("${api.key}")
	private String apiKey;
	@Autowired
	private ShortOrderExecutor shortOrderExecutor;
	@Autowired
	private ShortOrderMonitor shortOrderMonitor;

	@RequestMapping("/trade")
	public HttpStatus greeting(@RequestParam(value="key") String key, @RequestParam(value="ccy") String currencyCode)
			throws IOException {
		if (apiKey.equalsIgnoreCase(key)) {
			shortOrderExecutor.executeShortBidOrder(currencyCode.toUpperCase());
			shortOrderMonitor.handleCompletedBids();
			return HttpStatus.OK;
		} else {
			return HttpStatus.FORBIDDEN;
		}

	}
}
