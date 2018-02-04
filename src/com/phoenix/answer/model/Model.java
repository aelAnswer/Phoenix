package com.phoenix.answer.model;

import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

public class Model {

	private LongAdder quantity;
	private double unitPrice;
	private final String productId;
	
	private Supplier<Long> lazyQuantityGetter = () -> {
		long val = quantity.sum();
		lazyQuantityGetter = () -> val;
		return val;
	};

	public Model(String productId) {
		super();
		this.quantity = new LongAdder();
		this.productId = productId;
	}

	public void addQuantity(int quantity) {
		this.quantity.add(quantity);
	}
	
	public long getQuantity() {
		return lazyQuantityGetter.get();
	}

	public String getProductId() {
		return productId;
	}
	
	public double getCa() {
		return unitPrice * getQuantity(); 
	}

	public double getUnitPrice() {
		return unitPrice;
	}
	
	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}
	
	public String toString() {
		return String.format("(productId: %s-quantity: %d-unitPrice: %.2f)",
				productId, getQuantity(), getUnitPrice());
	}
	
}
