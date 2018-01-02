package com.crypto.cryptotrader.calculation;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "calculationConfig")
public class CalculationConfig {

	private static final int BID_OFFSET_PERCENTAGE = 10;
	private static final int ASK_OFFSET_PERCENTAGE = 5;
	private static final int STOP_LOSS_THRESHOLD_PERCENTAGE = 20;
	private static final int STOP_LOSS_OFFSET_PERCENTAGE = 70;
	private static final double TRADE_VOLUME = 0.00125;

	@Id
	private String id;
	private int bidOffsetPercentage;
	private int askOffsetPercentage;
	private int stopLossThresholdPercentage;
	private int stopLossOffsetPercentage;
	private double tradeVolume;

	public CalculationConfig(int bidOffsetPercentage, int askOffsetPercentage, int stopLossThresholdPercentage, int
			stopLossOffsetPercentage, double tradeVolume) {
		this.bidOffsetPercentage = bidOffsetPercentage;
		this.askOffsetPercentage = askOffsetPercentage;
		this.stopLossThresholdPercentage = stopLossThresholdPercentage;
		this.stopLossOffsetPercentage = stopLossOffsetPercentage;
		this.tradeVolume = tradeVolume;
	}

	public static CalculationConfig getDefault() {
		return new CalculationConfig(BID_OFFSET_PERCENTAGE, ASK_OFFSET_PERCENTAGE,
				STOP_LOSS_THRESHOLD_PERCENTAGE, STOP_LOSS_OFFSET_PERCENTAGE, TRADE_VOLUME);
	}

	public int getBidOffsetPercentage() {
		return bidOffsetPercentage;
	}

	public void setBidOffsetPercentage(int bidOffsetPercentage) {
		this.bidOffsetPercentage = bidOffsetPercentage;
	}

	public int getAskOffsetPercentage() {
		return askOffsetPercentage;
	}

	public void setAskOffsetPercentage(int askOffsetPercentage) {
		this.askOffsetPercentage = askOffsetPercentage;
	}

	public int getStopLossThresholdPercentage() {
		return stopLossThresholdPercentage;
	}

	public void setStopLossThresholdPercentage(int stopLossThresholdPercentage) {
		this.stopLossThresholdPercentage = stopLossThresholdPercentage;
	}

	public int getStopLossOffsetPercentage() {
		return stopLossOffsetPercentage;
	}

	public void setStopLossOffsetPercentage(int stopLossOffsetPercentage) {
		this.stopLossOffsetPercentage = stopLossOffsetPercentage;
	}

	public double getTradeVolume() {
		return tradeVolume;
	}

	public void setTradeVolume(double tradeVolume) {
		this.tradeVolume = tradeVolume;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
