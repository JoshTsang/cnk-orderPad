package com.htb.cnk.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.htb.cnk.lib.Http;
import com.htb.constant.Server;

public class MyOrder extends PhoneOrder {

	public MyOrder(Context context) {
		super(context);
	}

	private static List<OrderedDish> mMyOrder = new ArrayList<OrderedDish>();

	public void addOrder() {
		mMyOrder.clear();

		for (int i = 0; i < mPhoneOrder.size(); i++) {
			add(mPhoneOrder.get(i));
		}
		if (mOrder.size() > 0) {
			for (int i = 0; i < mOrder.size(); i++) {
				add(mOrder.get(i));
			}
		} 

	}

	public OrderedDish getOrderedDish(int position) {
		return mMyOrder.get(position);
	}

	public int count() {
		return mMyOrder.size();
	}

	public int add(Dish dish, int quantity, int tableId) {
		for (OrderedDish item : mMyOrder) {
			if (item.dish.getId() == dish.getId()) {
				item.quantity += quantity;
				return 0;
			}
		}

		mMyOrder.add(new OrderedDish(dish, quantity, tableId));
		return 0;
	}

	public int add(OrderedDish dish) {
		for (OrderedDish item : mMyOrder) {
			if (item.dish.getId() == dish.getId()) {
				item.quantity += dish.quantity;
				return 0;
			}
		}
		mMyOrder.add(dish);
		return 0;
	}

	public int add(int position, int quantity) {
		mMyOrder.get(position).quantity += quantity;
		return 0;
	}

	public int minus(Dish dish, int quantity) {
		for (OrderedDish item : mMyOrder) {
			if (item.dish.getId() == dish.getId()) {
				if (item.quantity > quantity) {
					item.quantity -= quantity;
				} else {
					mMyOrder.remove(item);
				}
				return 0;
			}
		}

		return 0;
	}

	public int minus(int position, int quantity) {
		if (mMyOrder.get(position).quantity > quantity) {
			mMyOrder.get(position).quantity -= quantity;
		} else {
			mMyOrder.remove(position);
		}
		return 0;
	}

	public int totalQuantity() {
		int count = 0;

		for (OrderedDish item : mMyOrder) {
			count += item.quantity;
		}
		return count;
	}

	public double getTotalPrice() {
		double totalPrice = 0;

		for (OrderedDish item : mMyOrder) {
			totalPrice += item.quantity * item.dish.getPrice();
		}

		return totalPrice;
	}

	public int getDishId(int position) {
		Log.d("position", "position:" + position + " size:" + mMyOrder.size());
		if (position < mMyOrder.size()) {
			return mMyOrder.get(position).dish.getId();
		}
		return -1;
	}

	public String submit() {
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);

		if (mMyOrder.size() <= 0) {
			return null;
		}

		try {
			order.put("tableId", Info.getTableId());
			order.put("tableName", Info.getTableName());
			order.put("timestamp", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONArray dishes = new JSONArray();
		try {
			for (int i = 0; i < mMyOrder.size(); i++) {
				JSONObject dish = new JSONObject();
				dish.put("id", mMyOrder.get(i).dish.getId());
				dish.put("name", mMyOrder.get(i).dish.getName());
				dish.put("price", mMyOrder.get(i).dish.getPrice());
				dish.put("quan", mMyOrder.get(i).quantity);
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.d("JSON", order.toString());
		String response = Http.post(Server.SUBMIT_ORDER, order.toString());
		if (response == null) {
			Log.d("Respond", "ok");
		} else {
			Log.d("Respond", response);
		}
		return response;
	}

}
