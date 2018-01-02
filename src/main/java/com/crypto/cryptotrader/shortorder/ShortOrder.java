package com.crypto.cryptotrader.shortorder;

import java.math.BigDecimal;
import java.util.Date;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.crypto.cryptotrader.calculation.CalculationUtils;

@Document(collection = "shortOrder")
public class ShortOrder {

	@Id
	private String id;
	private String ref;
	private Order.OrderStatus orderStatus;
	private Order.OrderType orderType;
	private BigDecimal originalAmount;
	private BigDecimal originalPrice;
	private String baseCurrencyCode;
	private Date createDate;

	public ShortOrder(String ref, Order.OrderStatus orderStatus, Order.OrderType orderType, BigDecimal originalAmount,
					  BigDecimal originalPrice, String baseCurrencyCode, Date createDate) {
		this.ref = ref;
		this.orderStatus = orderStatus;
		this.orderType = orderType;
		this.originalAmount = originalAmount;
		this.originalPrice = originalPrice;
		this.baseCurrencyCode = baseCurrencyCode;
		this.createDate = createDate;
	}

	public String getBaseCurrencyCode() {
		return baseCurrencyCode;
	}

	public void setBaseCurrencyCode(String baseCurrencyCode) {
		this.baseCurrencyCode = baseCurrencyCode;
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

	public void setOriginalPrice(BigDecimal originalPrice) {
		this.originalPrice = originalPrice;
	}

	public BigDecimal getOriginalPrice() {
		return originalPrice;
	}

	public CurrencyPair getCurrencyPair() {
		return CalculationUtils.toCurrencyPair(baseCurrencyCode);
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
}
