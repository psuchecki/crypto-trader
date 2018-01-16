package com.crypto.cryptotrader.calculation;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "singleton")
public class CalculationService {
	@Autowired
	private CalculationConfigRepository calculationConfigRepository;

	private CalculationConfig calculationConfig;

	public BigDecimal calculateOriginalAmount(BigDecimal last) {
		return BigDecimal.valueOf(getCalculationConfig().getTradeVolume()).divide(last, CalculationUtils
				.DEFAULT_CURRENCY_SCALE, BigDecimal.ROUND_CEILING);
	}

	public BigDecimal calculateBidPrice(BigDecimal bid) {
		BigDecimal bidOffsetLimit = CalculationUtils.calculateOffset(bid, getCalculationConfig().getBidOffsetPercentage());
		return bid.add(bidOffsetLimit);
	}

	public BigDecimal calculateAskPrice(BigDecimal originalPrice) {
		BigDecimal askOffsetLimit = CalculationUtils.calculateOffset(originalPrice, getCalculationConfig().getAskOffsetPercentage());
		return originalPrice.add(askOffsetLimit);
	}

	public BigDecimal calculateStopLossThreshold(BigDecimal originalPrice) {
		BigDecimal stopLossOffset = CalculationUtils.calculateOffset(originalPrice, getCalculationConfig()
				.getStopLossThresholdPercentage());
		return originalPrice.subtract(stopLossOffset);
	}

	public BigDecimal calculateStopLossPrice(BigDecimal lastPrice) {
		BigDecimal stopLossOffsetLimit = CalculationUtils.calculateOffset(lastPrice, getCalculationConfig().getStopLossOffsetPercentage());
		return lastPrice.subtract(stopLossOffsetLimit);
	}

	@Scheduled(fixedRate = 30000)
	private void refreshConfig() {
		calculationConfig = calculationConfigRepository.findAll().get(0);
	}

	private CalculationConfig getCalculationConfig() {
		if (calculationConfig == null) {
			refreshConfig();
		}

		return calculationConfig;
	}

}
