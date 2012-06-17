package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;


public class MyOrder {
	public class OrderedDish {
		Dish dish;
		int quantity;
		
		public OrderedDish(Dish dish, int quantity) {
			this.dish = dish;
			this.quantity = quantity;
		}
		
		public String getName() {
			return dish.getName();
		}
		
		public int getQuantity() {
			return quantity;
		}
		
		public double getPrice() {
			return dish.getPrice();
		}
	}
	
	static private List<OrderedDish> order = new ArrayList<OrderedDish>();
	
	public int add(Dish dish, int quantity) {
		for (OrderedDish item:order) {
			if (item.dish.getId() == dish.getId()) {
				item.quantity += quantity;
				return 0;
			}
		}
		
		order.add(new OrderedDish(dish, quantity));
		return 0;
	}
	
	public int add(int position, int quantity) {
		order.get(position).quantity += quantity;
		return 0;
	}
	
	public int minus(Dish dish, int quantity) {
		for (OrderedDish item:order) {
			if (item.dish.getId() == dish.getId()) {
				if (item.quantity > quantity) {
					item.quantity -= quantity;
				} else {
					order.remove(item);
				}
				return 0;
			}
		}
		
		return 0;
	}
	
	public int minus(int position, int quantity) {
		if (order.get(position).quantity > quantity) {
			order.get(position).quantity -= quantity;
		} else {
			order.remove(position);
		}
		return 0;
	}
	
	public int count() {
		return order.size();
	}
	
	public double getTotalPrice() {
		double totalPrice = 0;

		for (OrderedDish item:order) {
			totalPrice += item.quantity * item.dish.getPrice();
		}
		
		return totalPrice;
	}
	
	public OrderedDish getOrderedDish(int position) {
		return order.get(position);
	}
	
	public void clear() {
		order.clear();
	}
}
