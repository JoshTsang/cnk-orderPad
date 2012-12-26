package com.htb.cnk.data;

import java.util.Comparator;

public class OrderedDish implements Comparator<OrderedDish>{
	protected Dish dish;
	protected float padQuantity;
	protected float phoneQuantity;
	protected int status;
	protected int tableId;
	protected boolean[] flavor;
	
	public OrderedDish(MyOrder myOrder, Dish dish, float quantity, int tableId, int status,
			int type) {
		//OrderedDish = myOrder;
		this.dish = dish;
		this.tableId = tableId;
		this.status = status;
		if (type == MyOrder.MODE_PAD) {
			this.padQuantity = quantity;
			this.phoneQuantity = 0;
		} else if (type == MyOrder.MODE_PHONE) {
			this.phoneQuantity = (int) quantity;
			this.padQuantity = 0;
		}

	}

	public String getName() {
		return dish.getName();
	}

	public String getUnit() {
		return dish.getUnit();
	}
	
	public int getServedQuantity() {
		return status;
	}

	public float getQuantity() {
		return padQuantity + phoneQuantity;
	}
	
	public int getPrinter() {
		return dish.getPrinter();
	}

	public double getPrice() {
		return dish.getPrice();
	}

	public int getDishId() {
		return dish.getDishId();
	}

	public int getStatus() {
		return status;
	}

	public void addStatus(int add) {
		this.status += add;
	}

	public int getTableId() {
		return this.tableId;
	}

	public boolean[] getFlavor() {
		return this.flavor;
	}

	public void setFlavor(boolean[] flavor) {
		this.flavor = flavor;
	}
	
	public int compare(OrderedDish lhs, OrderedDish rhs) {
		OrderedDish l = (OrderedDish) lhs;
		OrderedDish r = (OrderedDish) rhs;
		
		return l.getPrinter()-r.getPrinter();
	}
}