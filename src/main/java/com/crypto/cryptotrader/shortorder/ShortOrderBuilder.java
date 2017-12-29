package com.crypto.cryptotrader.shortorder;

import java.math.BigDecimal;

import org.knowm.xchange.dto.Order;

public class ShortOrderBuilder {
	private String ref;
	private Order.OrderStatus orderStatus;
	private Order.OrderType orderType;
	private BigDecimal originalAmount;
	private BigDecimal origrinalPrice;

	public ShortOrderBuilder setRef(String ref) {
		this.ref = ref;
		return this;
	}

	public ShortOrderBuilder setOrderStatus(Order.OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
		return this;
	}

	public ShortOrderBuilder setOrderType(Order.OrderType orderType) {
		this.orderType = orderType;
		return this;
	}

	public ShortOrderBuilder setOriginalAmount(BigDecimal originalAmount) {
		this.originalAmount = originalAmount;
		return this;
	}

	public ShortOrderBuilder setOrigrinalPrice(BigDecimal origrinalPrice) {
		this.origrinalPrice = origrinalPrice;
		return this;
	}

	public ShortOrder createShortOrder() {
		return new ShortOrder(ref, orderStatus, orderType, originalAmount, origrinalPrice);
	}
}