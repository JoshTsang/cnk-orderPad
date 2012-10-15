package com.htb.cnk.data;

public class OrderedDish {
	//FIXME find out why orderedDish is needed here 
	//private final MyOrder OrderedDish;
	Dish dish;
	float padQuantity;
	int phoneQuantity;
	int status;
	int tableId;
	boolean []flavor;

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
		return dish.getId();
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
}