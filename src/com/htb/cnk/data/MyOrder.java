package com.htb.cnk.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
		int orderDishId;

		public OrderedDish(Dish dish, int quantity) {
			this.dish = dish;
			this.quantity = quantity;
		}

		public OrderedDish(Dish dish, int quantity, int id) {
			this.dish = dish;
			this.quantity = quantity;
			this.orderDishId = id;
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

		public int getDishId() {
			return this.orderDishId;
		}

		public int getId() {
			return dish.getId();
		}
	}

	private CnkDbHelper mCnkDbHelper;
	private SQLiteDatabase mDb;
	private static List<OrderedDish> mOrder = new ArrayList<OrderedDish>();

	public MyOrder(Context context) {
		mCnkDbHelper = new CnkDbHelper(context, CnkDbHelper.DATABASE_NAME,
				null, 1);
		mDb = mCnkDbHelper.getReadableDatabase();
	}

	public MyOrder() {
	}

	public int add(Dish dish, int quantity) {
		for (OrderedDish item : mOrder) {
			if (item.dish.getId() == dish.getId()) {
				item.quantity += quantity;
				return 0;
			}
		}

		mOrder.add(new OrderedDish(dish, quantity));
		return 0;
	}

	public int addItem(Dish dish, int quantity, int id) {
		for (OrderedDish item : mOrder) {
			if (item.dish.getId() == dish.getId()) {
				item.quantity += quantity;
				return 0;
			}
		}

		mOrder.add(new OrderedDish(dish, quantity, id));
		return 0;
	}

	public int add(int position, int quantity) {
		mOrder.get(position).quantity += quantity;
		return 0;
	}

	public int minus(Dish dish, int quantity) {
		for (OrderedDish item : mOrder) {
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

	public int totalQuantity() {
		int count = 0;

		for (OrderedDish item : mOrder) {
			count += item.quantity;
		}
		return count;
	}

	public int getDishId(int position) {
		Log.d("position", "position:" + position + " size:" + mOrder.size());
		if (position < mOrder.size()) {
			return mOrder.get(position).dish.getId();
		}
		return -1;
	}

	public double getTotalPrice() {
		double totalPrice = 0;

		for (OrderedDish item : mOrder) {
			totalPrice += item.quantity * item.dish.getPrice();
		}

		return totalPrice;
	}

	public OrderedDish getOrderedDish(int position) {
		return mOrder.get(position);
	}

	public void removeItem(int position) {
		mOrder.remove(position);

	}

	public void clear() {
		mOrder.clear();
	}

	public int getOrderedCount(int did) {
		for (OrderedDish dish : mOrder) {
			if (dish.getId() == did) {
				Log.d("ordered dish",
						"name:" + dish.getName() + "did:" + dish.getId());
				return dish.quantity;
			}
		}
		return 0;
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
			order.put("tableName", Info.getTableName());
			order.put("timestamp", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONArray dishes = new JSONArray();
		try {
			for (int i = 0; i < mOrder.size(); i++) {
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

		Log.d("JSON", order.toString());
		String response = Http.post(Server.SUBMIT_ORDER, order.toString());
		if (response == null) {
			Log.d("Respond", "ok");
		} else {
			Log.d("Respond", response);
		}
		return response;
	}

	public int getTableFromDB(int tableId) {
		String response = Http.get(Server.GET_MYORDER, "TID=" + tableId);
		Log.d("resp", response);
		try {
			JSONArray tableList = new JSONArray(response);
			int length = tableList.length();
			clear();
			for (int i = 0; i < length; i++) {
				JSONObject item = tableList.getJSONObject(i);
				int quantity = item.getInt("quantity");
				int dishId = item.getInt("dish_id");
				double dishPrice = item.getInt("price");
				Log.d("tableFromDB", "quantity" + quantity + "dishId" + dishId
						+ "dishPrice" + dishPrice);
				String name = getDishName(dishId);
				Dish mDish = new Dish(dishId, name, dishPrice, null);
				addItem(mDish, quantity, tableId);
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

		return -1;
	}

	public int getTablePhoneFromDB(int tableId) {
		String response = Http.get(Server.GET_GETPHONEORDER, "TID=" + tableId);
		try {
			JSONArray tableList = new JSONArray(response);
			int length = tableList.length();
			clear();
			for (int i = 0; i < length; i++) {
				JSONObject item = tableList.getJSONObject(i);
				int quantity = item.getInt("quantity");
				int dishId = item.getInt("dish_id");
				Cursor cur = getDishNameAndPriceFromDB(dishId);
				String name = cur.getString(0);
				double dishPrice = cur.getDouble(1);
				Dish mDish = new Dish(dishId, name, dishPrice, null);
				addItem(mDish, quantity, tableId);
			}
			return 0;

		} catch (Exception e) {

			// TODO: handle exception
		}

		return -1;
	}

	public String getDishName(int index) {
		String name = getDishNameFromDB(index);
		if (name == null) {
			return "菜名为空";
		}
		return name;
	}

	private String getDishNameFromDB(int id) {
		Cursor cur = mDb.query(CnkDbHelper.DISH_TABLE_NAME,
				new String[] { CnkDbHelper.DISH_NAME }, CnkDbHelper.DISH_ID
						+ "=" + id, null, null, null, null);

		if (cur.moveToNext()) {
			return cur.getString(0);
		}
		return null;
	}

	private Cursor getDishNameAndPriceFromDB(int id) {
		Cursor cur = mDb.query(CnkDbHelper.DISH_TABLE_NAME, new String[] {
				CnkDbHelper.DISH_NAME, CnkDbHelper.DISH_PRICE },
				CnkDbHelper.DISH_ID + "=" + id, null, null, null, null);

		if (cur.moveToNext()) {
			return cur;
		}
		return null;
	}

	public int delPhoneTable(int tableId, int dishId) {
		String tableStatusPkg;
		if (dishId == 0) {
			tableStatusPkg = Http.get(Server.DELETE_PHONEORDER, "TID="
					+ tableId);
		} else {
			tableStatusPkg = Http.get(Server.DELETE_PHONEORDER, "TID="
					+ tableId + "&DID=" + dishId);
		}
		if (tableStatusPkg == null) {
			return -1;
		}
		return 0;
	}

	public int updatePhoneOrder(int tableId, int quantity, int dishId) {
		String phoneOrderPkg = Http.get(Server.UPDATE_PHONE_ORDER, "DID="
				+ dishId + "&DNUM=" + quantity + "&TID=" + tableId);
		if (phoneOrderPkg == null) {
			return -1;
		}
		return 0;
	}

	public int delDish(int dishId) {
		Log.d("DID", "" + dishId);
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		if (mOrder.size() <= 0) {
			return -1;
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

			if (dishId == -1) {
				for (int i = 0; i < mOrder.size(); i++) {
					JSONObject dish = new JSONObject();
					dish.put("disId", mOrder.get(i).dish.getId());
					dish.put("name", mOrder.get(i).dish.getName());
					dish.put("price", mOrder.get(i).dish.getPrice());
					dish.put("quan", mOrder.get(i).quantity);
					dish.put("id", mOrder.get(i).getDishId());
					dishes.put(dish);
				}
			} else {
				JSONObject dish = new JSONObject();
				dish.put("dishId", mOrder.get(dishId).dish.getId());
				dish.put("name", mOrder.get(dishId).dish.getName());
				dish.put("price", mOrder.get(dishId).dish.getPrice());
				dish.put("quan", mOrder.get(dishId).quantity);
				dish.put("id", mOrder.get(dishId).getDishId());
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Log.d("JSON", order.toString());

		String response = Http.post(Server.DEL_ORDER, order.toString());
		if (response == null) {
			return -1;
		}
		return 0;
	}

	@Override
	protected void finalize() throws Throwable {
		if (mDb != null) {
			mDb.close();
		}
		super.finalize();
	}

}
