package com.htb.cnk.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.htb.cnk.lib.Http;
import com.htb.constant.Server;

/**
 * @author josh
 *
 */
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
	
	static private List<OrderedDish> mOrder = new ArrayList<OrderedDish>();
	
	public int add(Dish dish, int quantity) {
		for (OrderedDish item:mOrder) {
			if (item.dish.getId() == dish.getId()) {
				item.quantity += quantity;
				return 0;
			}
		}
		
		mOrder.add(new OrderedDish(dish, quantity));
		return 0;
	}
	
	public int add(int position, int quantity) {
		mOrder.get(position).quantity += quantity;
		return 0;
	}
	
	public int minus(Dish dish, int quantity) {
		for (OrderedDish item:mOrder) {
			if (item.dish.getId() == dish.getId()) {
				if (item.quantity > quantity) {
					item.quantity -= quantity;
				} else {
					mOrder.remove(item);
				}
				return 0;
			}
		}
		
		return 0;
	}
	
	public int minus(int position, int quantity) {
		if (mOrder.get(position).quantity > quantity) {
			mOrder.get(position).quantity -= quantity;
		} else {
			mOrder.remove(position);
		}
		return 0;
	}
	
	public int count() {
		return mOrder.size();
	}
	
	public double getTotalPrice() {
		double totalPrice = 0;

		for (OrderedDish item:mOrder) {
			totalPrice += item.quantity * item.dish.getPrice();
		}
		
		return totalPrice;
	}
	
	public OrderedDish getOrderedDish(int position) {
		return mOrder.get(position);
	}
	
	public void clear() {
		mOrder.clear();
	}
	
	public String submit() {
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		
		if (mOrder.size() <= 0) {
			return null;
		}
		
		try {
			order.put("tableId", Info.getTableId());
			order.put("timestamp", time);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONArray dishes = new JSONArray();
		try {
			for (int i=0; i<mOrder.size(); i++) {
				JSONObject dish = new JSONObject();
				dish.put("id", mOrder.get(i).dish.getId());
				dish.put("name", mOrder.get(i).dish.getName());
				dish.put("price", mOrder.get(i).dish.getPrice());
				dish.put("quan", mOrder.get(i).quantity);
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
		}
 
		Log.d("JSON",order.toString());
		String response = Http.post(Server.SUBMIT_ORDER, order.toString());
		if (response == null) {
			Log.d("Respond", "die/ok");
		} else {
			Log.d("Respond",response);
		}
		return response;
	}
}
