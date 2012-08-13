package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.htb.cnk.data.PadOrder.OrderedDish;
import com.htb.cnk.lib.Http;
import com.htb.constant.Server;

public class PhoneOrder extends PadOrder {
	private int MODE_PHONE = 1;

	public PhoneOrder(Context context) {

		super(context);
	}

	protected static List<OrderedDish> mPhoneOrder = new ArrayList<OrderedDish>();

	public void removePhoneItem() {
		mPhoneOrder.clear();
	}

	public OrderedDish getOrderedDish(int position) {
		return mPhoneOrder.get(position);
	}

	public int addItem(Dish dish, int quantity, int id, int type, int tableId) {
		for (OrderedDish item : mPhoneOrder) {
			if (item.dish.getId() == dish.getId()) {
				item.quantity += quantity;
				return 0;
			}
		}

		mPhoneOrder.add(new OrderedDish(dish, quantity, id, type, tableId));

		return 0;
	}

	public int getTablePhoneFromDB(int tableId) {
		String response = Http.get(Server.GET_GETPHONEORDER, "TID=" + tableId);
		Log.d("resp", "Phone:" + response);
		try {
			JSONArray tableList = new JSONArray(response);
			int length = tableList.length();
			if (count() > 0 && getTableId() != tableId) {
				Log.d("tableId", "tableID:" + getTableId());
				removePadItem();
			}
			if (mPhoneOrder.size() > 0)
				removePhoneItem();
			for (int i = 0; i < length; i++) {
				JSONObject item = tableList.getJSONObject(i);
				int quantity = item.getInt("quantity");
				int dishId = item.getInt("dish_id");
				Cursor cur = getDishNameAndPriceFromDB(dishId);
				String name = cur.getString(0);
				double dishPrice = cur.getDouble(1);
				Dish mDish = new Dish(dishId, name, dishPrice, null);
				addItem(mDish, quantity, tableId, MODE_PHONE, tableId);
				Log.d("phone", mPhoneOrder.get(i).getName() + " "
						+ mPhoneOrder.get(i).getDishId() + " "
						+ mPhoneOrder.get(i).getId());
			}
			return 0;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return -1;
	}

	private void removePadItem() {
		super.clear();
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
		Log.d("Respond", (Server.DELETE_PHONEORDER+"TID="
				+ tableId + "&DID=" + dishId));
		if (tableStatusPkg == null) {
			return -1;
		}
		return 0;
	}

	public int updatePhoneOrder(int tableId, int quantity, int dishId) {
		String phoneOrderPkg = Http.get(Server.UPDATE_PHONE_ORDER, "DID="
				+ dishId + "&DNUM=" + quantity + "&TID=" + tableId);
		Log.d("Respond", (Server.UPDATE_PHONE_ORDER + "DID="
				+ dishId + "&DNUM=" + quantity + "&TID=" + tableId));
		Log.d("resp", "updatePhoneOrder:" + phoneOrderPkg);
		if (phoneOrderPkg == null) {
			return -1;
		}
		return 0;
	}

}
