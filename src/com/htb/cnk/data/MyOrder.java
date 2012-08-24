package com.htb.cnk.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

public class MyOrder {
	public final static int ERR_GET_PHONE_ORDER_FAILED = -10;
	public final static int RET_NULL_PHONE_ORDER = 1;
	public final static int RET_MINUS_SUCC = -2;

	private final static int MODE_PAD = 0;
	private final static int MODE_PHONE = 1;

	public class OrderedDish {
		Dish dish;
		int padQuantity;
		int phoneQuantity;
		int orderDishId;
		int tableId;

		public OrderedDish(Dish dish, int quantity, int tableId, int type) {
			this.dish = dish;
			this.tableId = tableId;
			if (type == MODE_PAD) {
				this.padQuantity = quantity;
				this.phoneQuantity = 0;
			} else if (type == MODE_PHONE) {
				this.phoneQuantity = quantity;
				this.padQuantity = 0;
			}

		}

		public OrderedDish(Dish dish, int quantity, int id, int tableId,
				int type) {
			this.dish = dish;
			this.orderDishId = id;
			this.tableId = tableId;
			if (type == MODE_PAD) {
				this.padQuantity = quantity;
				this.phoneQuantity = 0;
			} else if (type == MODE_PHONE) {
				this.phoneQuantity = quantity;
				this.padQuantity = 0;
			}
		}

		public String getName() {
			return dish.getName();
		}

		public int getQuantity() {
			return padQuantity + phoneQuantity;
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

		public int getTableId() {
			return this.tableId;
		}
	}

	private CnkDbHelper mCnkDbHelper;
	protected SQLiteDatabase mDb;
	private Context mDelDlgActivity;
	protected static List<OrderedDish> mOrder = new ArrayList<OrderedDish>();

	public MyOrder(Context context) {
		mCnkDbHelper = new CnkDbHelper(context, CnkDbHelper.DATABASE_NAME,
				null, 1);
		mDb = mCnkDbHelper.getReadableDatabase();
		mDelDlgActivity = context;
	}

	public int addOrder(Dish dish, int quantity, int tableId, int type) {
		for (OrderedDish item : mOrder) {
			if (item.dish.getId() == dish.getId()) {
				if (type == MODE_PAD) {
					item.padQuantity += quantity;
				} else if (type == MODE_PHONE) {
					item.phoneQuantity += quantity;
				}
				return 0;
			}
		}
		mOrder.add(new OrderedDish(dish, quantity, tableId, type));
		return 0;
	}

	public int add(Dish dish, int quantity, int tableId, int type) {
		for (OrderedDish item : mOrder) {
			if (item.dish.getId() == dish.getId()) {
				item.padQuantity += quantity;
				return 0;
			}
		}

		mOrder.add(new OrderedDish(dish, quantity, tableId, type));
		return 0;
	}

	public int addItem(Dish dish, int quantity, int id, int tableId) {
		for (OrderedDish item : mOrder) {
			if (item.dish.getId() == dish.getId()) {
				item.padQuantity += quantity;
				return 0;
			}
		}

		mOrder.add(new OrderedDish(dish, quantity, id, tableId));
		return 0;
	}

	public int add(int position, int quantity) {
		mOrder.get(position).padQuantity += quantity;
		return 0;
	}

	public int minus(Dish dish, int quantity) {
		for (OrderedDish item : mOrder) {
			if (item.dish.getId() == dish.getId()) {
				return minus(item, quantity);
			}
		}

		return 0;
	}

	public int minus(int position, int quantity) {
		OrderedDish item = mOrder.get(position);
		return minus(item, quantity);
	}

	public int count() {
		return mOrder.size();
	}

	public int totalQuantity() {
		int count = 0;

		for (OrderedDish item : mOrder) {
			count += (item.padQuantity + item.phoneQuantity);
		}
		return count;
	}

	public int getDishId(int position) {
		if (position < mOrder.size()) {
			return mOrder.get(position).dish.getId();
		}
		return -1;
	}

	public double getTotalPrice() {
		double totalPrice = 0;

		for (OrderedDish item : mOrder) {
			totalPrice += (item.padQuantity + item.phoneQuantity)
					* item.dish.getPrice();
		}

		return totalPrice;
	}

	public int getTableId() {
		return mOrder.get(0).getTableId();
	}

	public OrderedDish getOrderedDish(int position) {
		return mOrder.get(position);
	}

	public int getPhoneQuantity(int position) {
		return mOrder.get(position).phoneQuantity;
	}

	public void removeItem(int position) {
		mOrder.remove(position);
	}

	public int remove(int dishId) {
		for (OrderedDish item : mOrder) {
			if (item.dish.getId() == dishId) {
				mOrder.remove(item);
				return 0;
			}
		}
		return -1;
	}
	public void clear() {
		mOrder.clear();
	}

	public void phoneClear() {
		for (int i = 0; i < mOrder.size(); i++) {
			OrderedDish item = (OrderedDish) mOrder.get(i);
			if (item.padQuantity == 0) {
				mOrder.remove(item);
				i--;
			} else {
				item.phoneQuantity = 0;
			}
		}
	}

	public void talbeClear() {
		if (count() > 0 && getTableId() != Info.getTableId()) {
			mOrder.clear();
			Log.d("talbeClear", "clear");
		}
	}

	public int getOrderedCount(int did) {
		for (OrderedDish dish : mOrder) {
			if (dish.getId() == did) {
				return (dish.padQuantity + dish.phoneQuantity);
			}
		}
		return 0;
	}

	public int submit() {
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);

		if (mOrder.size() <= 0) {
			return -1;
		}

		int ret = Http.getPrinterStatus();
		if (ret < 0) {
			return ret;
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
				dish.put(
						"quan",
						(mOrder.get(i).padQuantity + mOrder.get(i).phoneQuantity));
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
			return -1;
		}

		Log.d("JSON", order.toString());
		String response = Http.post(Server.SUBMIT_ORDER, order.toString());
		if ("".equals(response)) {
			Log.d("Respond", "ok");
			return 0;
		} else {
			Log.d("Respond", response);
			return -1;
		}
	}

	public int getOrderFromServer(int tableId) {
		String response = Http.get(Server.GET_MYORDER, "TID=" + tableId);
//		Log.d("resp", response);
		if("null".equals(response)){
			return -2;
		}else if(response == null){
			return -1;
		}
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
				addOrder(mDish, quantity, tableId, MODE_PAD);
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int getPhoneOrderFromServer(int tableId) {
		talbeClear();
		String response = Http.get(Server.GET_GETPHONEORDER, "TID=" + tableId);
		Log.d("resp", "Phone:" + response);
		if (response == null) {
			return -1;
		} else if ("null".equals(response)) {
			return RET_NULL_PHONE_ORDER;
		}

		try {
			JSONArray tableList = new JSONArray(response);
			int length = tableList.length();
			phoneClear();

			for (int i = 0; i < length; i++) {
				JSONObject item = tableList.getJSONObject(i);
				int quantity = item.getInt("quantity");
				int dishId = item.getInt("dish_id");
				Cursor cur = getDishNameAndPriceFromDB(dishId);
				String name = cur.getString(0);
				double dishPrice = cur.getDouble(1);
				Dish mDish = new Dish(dishId, name, dishPrice, null);
				addOrder(mDish, quantity, tableId, MODE_PHONE);
			}
			return 0;

		} catch (Exception e) {
			e.printStackTrace();
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

	//TODO handle err
	public int cleanServerPhoneOrder(int tableId) {
		String tableStatusPkg = Http.get(Server.DELETE_PHONEORDER, "TID="
				+ tableId);
		if (tableStatusPkg == null) {
					return -1;
		}
		phoneClear();
		return 0;
	}

	public int submitDelDish(int position) {
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
	
			if (position == -1) {
				for (int i = 0; i < mOrder.size(); i++) {
					JSONObject dish = new JSONObject();
					dish.put("dishId", mOrder.get(i).dish.getId());
					dish.put("name", mOrder.get(i).dish.getName());
					dish.put("price", mOrder.get(i).dish.getPrice());
					dish.put(
							"quan",
							(mOrder.get(i).padQuantity + mOrder.get(i).phoneQuantity));
					dish.put("id", mOrder.get(i).getDishId());
					dishes.put(dish);
				}
			} else {
				JSONObject dish = new JSONObject();
				dish.put("dishId", mOrder.get(position).dish.getId());
				dish.put("name", mOrder.get(position).dish.getName());
				dish.put("price", mOrder.get(position).dish.getPrice());
				dish.put("quan", (mOrder.get(position).padQuantity + mOrder
						.get(position).phoneQuantity));
				dish.put("id", mOrder.get(position).getDishId());
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
		Log.d("JSON", order.toString());
	
		String response = Http.post(Server.DEL_ORDER, order.toString());
		Log.d("post", "response:" + response);
		if ("".equals(response)) {
			return 0;
		} else {
			return -1;
		}
	}

	private int minus(OrderedDish item, int quantity) {
		if ((item.padQuantity + item.phoneQuantity) > quantity) {
			if (item.padQuantity > quantity) {
				item.padQuantity -= quantity;
				return 0;
			} else {
				quantity -= item.padQuantity;
				
				if (minusPhoneOrderOnServer(Info.getTableId(), item.phoneQuantity - quantity, item.getId()) < 0) {
					return -1;
				} else {
					item.phoneQuantity -= quantity;
					item.padQuantity = 0;
					return 0;
				}
				
			}
		} else {
			if(item.phoneQuantity > 0) {
				if (minusPhoneOrderOnServer(Info.getTableId(), 0, item.getId()) < 0) {
					return -1;
				}
			}
			mOrder.remove(item);
			return 0;
		}
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


	//TODO handle err
	private int delPhoneOrderedDish(int tableId, int dishId) {
		showServerDelProgress();
		String tableStatusPkg = Http.get(Server.DELETE_PHONEORDER, "TID="
				+ tableId + "&DID=" + dishId);
		if (tableStatusPkg == null) {
			return -1;
		}
		remove(dishId);
		return 0;
	}

	//TODO
	private int minusPhoneOrderOnServer(int tableId, int quantity, int dishId) {
		if (quantity != 0) {
			showServerDelProgress();
			String phoneOrderPkg = Http.get(Server.UPDATE_PHONE_ORDER, "DID="
					+ dishId + "&DNUM=" + quantity + "&TID=" + tableId);
			Log.d("resp", "resp:" + phoneOrderPkg);
			if (phoneOrderPkg == null || !"\r\n".equals(phoneOrderPkg)) {
				return -1;
			}
		} else {
			return delPhoneOrderedDish(tableId, dishId);
		}
		return 0;
	}

	public void showServerDelProgress() {
		Method showDelProcessDlg;
		try {
			showDelProcessDlg = mDelDlgActivity.getClass().getMethod("showDeletePhoneOrderProcessDlg", new Class[0]);
			showDelProcessDlg.invoke(mDelDlgActivity, new Object[0]);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	protected void finalize() throws Throwable {
		if (mDb != null) {
			mDb.close();
		}
		super.finalize();
	}

}
