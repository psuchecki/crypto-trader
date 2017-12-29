package com.crypto.cryptotrader.shortorder;

import java.math.BigDecimal;

import org.knowm.xchange.dto.Order;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shortOrder")
public class ShortOrder {

	@Id
	private String id;
	private String ref;
	private Order.OrderStatus orderStatus;
	private Order.OrderType orderType;
	private BigDecimal originalAmount;
	private BigDecimal price;

	public ShortOrder(String ref, Order.OrderStatus orderStatus, Order.OrderType orderType, BigDecimal originalAmount,
					  BigDecimal price) {
		this.ref = ref;
		this.orderStatus = orderStatus;
		this.orderType = orderType;
		this.originalAmount = originalAmount;
		this.price = price;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public Order.OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public void setOrderStatus(Order.OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public Order.OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(Order.OrderType orderType) {
		this.orderType = orderType;
	}

	public void setOriginalAmount(BigDecimal originalAmount) {
		this.originalAmount = originalAmount;
	}

	public BigDecimal getOriginalAmount() {
		return originalAmount;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getPrice() {
		return price;
	}
}
