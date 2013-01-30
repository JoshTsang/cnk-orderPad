package com.htb.cnk.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.htb.cnk.lib.ErrorPHP;
import com.htb.cnk.lib.Http;
import com.htb.constant.Server;
import com.htb.constant.Table;

public class MyOrder {
	public final static String TAG = "MyOrder";

	/* Error Code */
	public final static int ERR_GET_PHONE_ORDER_FAILED = -10;
	public final static int RET_NULL_PHONE_ORDER = 1;
	public final static int TIME_OUT = -1;
	public final static int NOTHING_TO_DEL = -4;
	public final static int ERR_DB = -10;

	/* Del order mode */
	public final static int DEL_ALL_ORDER = 0;
	public final static int DEL_ITEM_ORDER = 2;
	public final static int MULTI_ORDER = -100;
	public final static int PERSERVE_ORDER = -200;

	/* current order mode */
	public final static int MODE_PAD = 0;
	public final static int MODE_PHONE = 1;

	/* DataBase query columns */
	public static int NAME_COLUMN = 0;
	public static int PRICE_COLUMN = 1;
	public static int PIC_COLUMN = 2;
	public static int PRINTER_COLUMN = 3;
	public final static int UNIT_NAME = 4;
	
	public final static int ORDER_INSTANT = 0;
	public final static int ORDER_PEND = 1;
	public final static int ORDER_REPRINT = 2;

	protected int mOrderType;
	protected int mOrderTimeType = ORDER_INSTANT;

	protected CnkDbHelper mCnkDbHelper;
	protected SQLiteDatabase mDb;
	protected Context mContext;
	protected static List<OrderedDish> mOrder = new ArrayList<OrderedDish>();
	protected static List<OrderedDish> mDelOrder = new ArrayList<OrderedDish>();
	protected int mPersons;
	protected float mAdvPayment;
	protected static String[] mFlavorName;
	protected static String mComment = "";

	public MyOrder(Context context) {
		mCnkDbHelper = new CnkDbHelper(context, CnkDbHelper.DATABASE_NAME,
				null, 1);
		mDb = mCnkDbHelper.getReadableDatabase();
		mContext = context;
	}

	public void setOrderType(int type) {
		mOrderType = type;
	}
	
	public void setOrderTimeType(int type) {
		mOrderTimeType = type;
	}
	
	public int getOrderTimeType() {
		return mOrderTimeType;
	}

	public void setPersons(int persons) {
		this.mPersons = persons;
	}

	public int getPersons() {
		return mPersons;
	}

	protected int addOrder(Dish dish, float quantity, int tableId, int status,
			int type, List<OrderedDish> order) {
		for (OrderedDish item : order) {
			if (item.dish.getDishId() == dish.getDishId()) {
				if (type == MODE_PAD) {
					item.padQuantity += quantity;
					item.status += status;
				} else if (type == MODE_PHONE) {
					item.phoneQuantity += quantity;
				}
				return 0;
			}
		}
		order.add(new OrderedDish(this, dish, quantity, tableId, status, type));
		return 0;
	}

	public void addDelOrder(int position,float quantity) {
		Dish dish = getDish(position);
		int tableId = getDishId(position);
		int status = getStatus(position);
		addOrder(dish, quantity, tableId, status, MODE_PAD, mDelOrder);
	}

	public List<OrderedDish> getDelOrder() {
		return mDelOrder;
	}

	public int add(Dish dish, float quantity, int tableId, int type) {
		for (OrderedDish item : mOrder) {
			if (item.dish.getDishId() == dish.getDishId()) {
				item.padQuantity += quantity;
				return 0;
			}
		}

		mOrder.add(new OrderedDish(this, dish, quantity, tableId, 1, type));
		return 0;
	}

	public int add(int position, float quantity) {
		mOrder.get(position).padQuantity += quantity;
		return 0;
	}

	public int count() {
		return mOrder.size();
	}

	public int getTotalQuantity() {
		return count();
	}

	public int getStatus(int position) {
		if (position < mOrder.size()) {
			return mOrder.get(position).status;
		}
		return -1;
	}

	public Dish getDish(int position) {
		if (position < mOrder.size()) {
			Log.d(TAG, "postion:" + position + "mOrder.size：" + mOrder.size());
			return mOrder.get(position).dish;
		}
		return null;
	}

	public int getDishId(int position) {
		if (position < mOrder.size()) {
			return mOrder.get(position).dish.getDishId();
		}
		return -1;
	}

	public float getTotalPrice() {
		float totalPrice = 0;

		for (OrderedDish item : mOrder) {
			totalPrice += (item.padQuantity + item.phoneQuantity)
					* item.dish.getPrice();
		}

		return totalPrice;
	}

	public int getTableId() {
		return mOrder.get(0).getTableId();
	}

	public int getDishStatus(int index) {
		return mOrder.get(index).getStatus();
	}

	public String getName(int index) {
		return mOrder.get(index).getName();
	}

	public float getQuantity(int index) {
		return mOrder.get(index).getQuantity();
	}

	public int getPrinter(int index) {
		return mOrder.get(index).getPrinter();
	}

	public double getPrice(int index) {
		return mOrder.get(index).getPrice();
	}

	public int getTableId(int index) {
		return mOrder.get(index).getTableId();
	}

	public int getIndex(int dishId) {
		for (int i = 0; i < mOrder.size(); i++) {
			if (dishId == mOrder.get(i).getDishId())
				return i;
		}
		return -1;
	}

	public static String[] getFlavor() {
		return mFlavorName;
	}

	public void setComment(String str) {
		mComment = str;
	}

	public String getComment() {
		return mComment;
	}

	public int setDishStatus(int index) {
		int status = 0;
		String quanStr = convertFloat(mOrder.get(index).getQuantity());
		String quantity[] = quanStr.split("\\.");
		if (quantity.length > 1) {
			status = Integer.parseInt(quantity[0]) + 1;
		}
		int ret = updateServerServedDish(Info.getTableId(), mOrder.get(index)
				.getDishId(), status);
		if (ret < 0) {
			return ret;
		} else {
			mOrder.get(index).addStatus(status == 0 ? 1 : status);
		}
		return 0;
	}

	public int setFlavor(boolean[] flavor, int index) {
		if (mOrder.size() < index) {
			return -1;
		}
		mOrder.get(index).setFlavor(flavor);
		return 0;
	}

	public boolean[] slectedFlavor(int index) {
		boolean[] slected = new boolean[mFlavorName.length];
		if (mOrder.get(index).getFlavor() == null) {
			return slected;
		}
		slected = mOrder.get(index).getFlavor();
		return slected;
	}

	public OrderedDish getOrderedDish(int position) {
		return mOrder.get(position);
	}

	public void removeItem(int position) {
		mOrder.remove(position);
	}

	public int remove(int dishId) {
		for (OrderedDish item : mOrder) {
			if (item.dish.getDishId() == dishId) {
				mOrder.remove(item);
				return 0;
			}
		}
		return -1;
	}

	public void clear() {
		mOrder.clear();
		mComment = "";
	}

	public void talbeClear() {
		if (count() > 0 && getTableId() != Info.getTableId()) {
			mOrder.clear();
		}
	}

	public float getOrderedCount(int did) {
		for (OrderedDish dish : mOrder) {
			if (dish.getDishId() == did) {
				return (dish.padQuantity + dish.phoneQuantity);
			}
		}
		return 0;
	}

	public int submit(int tableStatus) {
		if (mOrder.size() <= 0) {
			return -1;
		}

		int ret = Http.getPrinterStatus(Server.PRINTER_CONTENT_TYPE_ORDER);
		if (ret < 0) {
			return ret;
		}

		String orderJson = getOrderJson();
		if (orderJson == null) {
			return -1;
		}

		String response;
		if (tableStatus == Table.OPEN_TABLE_STATUS) {
			response = Http
					.post(Server.SUBMIT_ORDER + "?action=add", orderJson);
		} else {
			response = Http.post(Server.SUBMIT_ORDER, orderJson);
		}
		if (!ErrorPHP.isSucc(response, TAG)) {
			return -1;
		}
		return 0;

	}

	public String getMultiOrderJson(List<Integer> ids, String names) {
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);

		try {
			order.put("waiter", UserData.getUserName());
			order.put("waiterId", UserData.getUID());
			JSONArray tableIds = new JSONArray();
			for (Integer id:ids) {
				tableIds.put(id);
			}
			order.put("tableId", tableIds);
			order.put("persons", mPersons);
			order.put("tableName", names);
			order.put("multi", 1);
			if (mAdvPayment > 0) {
				order.put("advPayment", mAdvPayment);
			}
			order.put("type", mOrderType == MODE_PAD ? "pad":"phone");
			order.put("timeType", orderTimeTypeToString(mOrderTimeType, date.getHours()));
			if (!"".equals(mComment.trim())) {
				order.put("comment", mComment);
			}
			order.put("timestamp", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Collections.sort(mOrder, mOrder.get(0));
		JSONArray dishes = new JSONArray();
		try {
			for (int i = 0; i < mOrder.size(); i++) {
				String flavorStr = null;
				flavorStr = getFlavorNameToSubmit(i, flavorStr);
				JSONObject dish = new JSONObject();
				dish.put("dishId", mOrder.get(i).dish.getDishId());
				dish.put("name", mOrder.get(i).dish.getName());
				dish.put("price", mOrder.get(i).dish.getPrice());
				dish.put("quan", mOrder.get(i).getQuantity());
				dish.put("printer", mOrder.get(i).getPrinter());
				dish.put("flavor", flavorStr);
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return order.toString();
	}
	
	public static int submitPendedOrder(String order, int tableStatus,
			String MD5) {
		String response = null;

		if (tableStatus == Table.OPEN_TABLE_STATUS) {
			response = Http.post(
					Server.SUBMIT_ORDER + "?action=add&MD5=" + MD5, order);
		} else {
			response = Http.post(Server.SUBMIT_ORDER + "?MD5=" + MD5, order);
		}
		if (!ErrorPHP.isSucc(response, TAG)) {
			return -1;
		}
		return 0;
	}

	public String getOrderJson() {
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);

		try {
			order.put("waiter", UserData.getUserName());
			order.put("waiterId", UserData.getUID());
			order.put("tableId", Info.getTableId());
			order.put("persons", mPersons);
			order.put("tableName", Info.getTableName());
			order.put("type", mOrderType == MODE_PAD ? "pad" : "phone");
			order.put("timeType", orderTimeTypeToString(mOrderTimeType, date.getHours()));
			if (mAdvPayment > 0) {
				order.put("advPayment", mAdvPayment);
			}
			if (!"".equals(mComment.trim())) {
				order.put("comment", mComment);
			}
			order.put("timestamp", time);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		Collections.sort(mOrder, mOrder.get(0));
		JSONArray dishes = new JSONArray();
		try {
			for (int i = 0; i < mOrder.size(); i++) {
				String flavorStr = null;
				flavorStr = getFlavorNameToSubmit(i, flavorStr);
				JSONObject dish = new JSONObject();
				dish.put("dishId", mOrder.get(i).dish.getDishId());
				dish.put("name", mOrder.get(i).dish.getName());
				dish.put("price", mOrder.get(i).dish.getPrice());
				dish.put("quan", mOrder.get(i).getQuantity());
				dish.put("printer", mOrder.get(i).getPrinter());
				dish.put("flavor", flavorStr);
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return order.toString();
	}

	public String getReservationJson(Reservation reservation) {
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);

		try {
			order.put("waiter", UserData.getUserName());
			order.put("waiterId", UserData.getUID());
			order.put("persons", reservation.getPersons());
			if (!"".equals(reservation.getTableNames()) && reservation.getTableNames() != null) {
				order.put("tableName", reservation.getTableNames());
				order.put("tableId", reservation.getTableIds());
			} 
			order.put("type", "pad");
			if (!"".equals(reservation.getComment()) && reservation.getComment() != null) {
				order.put("comment", getComment());
			}
			order.put("reservation", reservation.getDatetime());
			order.put("timestamp", time);
			order.put("tel", reservation.getTel());
			order.put("name", reservation.getName());
			order.put("deposit", reservation.getDeposit());
			if (!"".equals(reservation.getAddr()) && reservation.getAddr() != null) {
				order.put("addr", reservation.getAddr());
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		Collections.sort(mOrder, mOrder.get(0));
		JSONArray dishes = new JSONArray();
		try {
			for (int i = 0; i < mOrder.size(); i++) {
				String flavorStr = null;
				flavorStr = getFlavorNameToSubmit(i, flavorStr);
				JSONObject dish = new JSONObject();
				dish.put("dishId", mOrder.get(i).dish.getDishId());
				dish.put("name", mOrder.get(i).dish.getName());
				dish.put("price", mOrder.get(i).dish.getPrice());
				dish.put("quan", mOrder.get(i).getQuantity());
				dish.put("printer", mOrder.get(i).getPrinter());
				dish.put("flavor", flavorStr);
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return order.toString();
	}
	
	public int submitReservation(Reservation reservation) {
		String json = getReservationJson(reservation);
		if (json == null) {
			return -1;
		}
		
		String ret = Http.submitReservation(reservation, json);
		if (ret == null) {
			return -1;
		} else if (!"".equals(ret)) {
			return -1;
		}
		
		ret = Http.post(Server.PRINT + "?action=reservation", json);
		if (ret == null) {
			Log.d(TAG, "print return null");
			return -1;
		} else {
			if (ErrorPHP.isSucc(ret, TAG)) {
				return 0;
			} else {
				return -1;
			}
		}
	}
	
	public static int loodPersons(int tableId) {
		String response = Http.get(Server.GET_PERSONS, "TID=" + tableId);
		if ("null".equals(response)) {
			Log.w(TAG, "getPersonsFromServer.null");
			return -2;
		} else if (response == null) {
			Log.e(TAG, "getPersonsFromServer.timeOut");
			return TIME_OUT;
		}
		try {
			int start = response.indexOf('[');
			int end = response.indexOf(']');
			if (start < 0 || end < 0 || (end - start) > 4) {
				Log.e(TAG, "getPersons failed:" + response);
				return -1;
			} else {
				String persons = response.substring(start + 1, end);
				return Integer.valueOf(persons);
			}

		} catch (Exception e) {
			Log.e(TAG, response);
			e.printStackTrace();
		}
		return -1;
	}

	public static int getFLavorFromServer() {
		String response = Http.get(Server.GET_FLAVOR, "");
		return getFlavorName(response);
	}

	public static int getFlaovorFromSetting() {
		String response = Setting.getFlavor();
		return getFlavorName(response);
	}

	public static int saveFlavorToSetting() {
		JSONArray flavor = new JSONArray();
		for (int i = 0; i < mFlavorName.length; i++) {
			flavor.put(mFlavorName[i].toString());
		}
		Setting.saveFlavor(flavor.toString());
		return 0;
	}
	
	public int print() {
		if (mOrder.size() <= 0) {
			return -1;
		}

		mOrderTimeType = ORDER_REPRINT;
		String orderJson = getOrderJson();
		if (orderJson == null) {
			return -1;
		}

		String response;
		
		response = Http.post(Server.PRINT + "?action=order", orderJson);
		
		if (!ErrorPHP.isSucc(response, TAG)) {
			return -1;
		}
		return 0;
	}

	/**
	 * @param response
	 */
	private static int getFlavorName(String response) {
		if ("null".equals(response)) {
			Log.w(TAG, "getFLavorFromServer.null");
			return -2;
		} else if (response == null) {
			Log.e(TAG, "getFLavorFromServer.timeOut");
			return TIME_OUT;
		}
		try {
			JSONArray flavor = new JSONArray(response);
			mFlavorName = new String[flavor.length()];
			int length = flavor.length();
			for (int i = 0; i < length; i++) {
				mFlavorName[i] = flavor.getString(i);
			}
			return 0;
		} catch (Exception e) {
			Log.e(TAG, response);
			e.printStackTrace();
		}
		return -1;
	}

	public int getOrderFromServer(int tableId) {
		mDelOrder.clear();
		String response = Http.get(Server.GET_MYORDER, "TID=" + tableId);
		if ("null".equals(response)) {
			Log.w(TAG, "getOrderFromServer.null");
			mOrder.clear();
			return -2;
		} else if (response == null) {
			Log.e(TAG, "getOrderFromServer.timeOut");
			return TIME_OUT;
		}
		try {
			JSONArray tableList = new JSONArray(response);
			int length = tableList.length();
			clear();
			for (int i = 0; i < length; i++) {
				JSONObject item = tableList.getJSONObject(i);
				float quantity = (float) item.getDouble("quantity");
				int dishId = item.getInt("dish_id");
				int status = item.getInt("status");
				Float dishPrice = (float) item.getDouble("price");
				Cursor cur = getDishInfoFromDB(dishId);
				if (cur == null) {
					return ERR_DB;
				}
				String name = cur.getString(NAME_COLUMN);
				String pic = cur.getString(PIC_COLUMN);
				int printer = cur.getInt(PRINTER_COLUMN);
				String unit = cur.getString(UNIT_NAME);
				Dish mDish = new Dish(dishId, name, dishPrice, pic, unit,
						printer);
				addOrder(mDish, quantity, tableId, status, MODE_PAD, mOrder);
			}
			return 0;
		} catch (Exception e) {
			Log.e(TAG, response);
			e.printStackTrace();
		}
		return -1;
	}
	
	public int getOrderFromServerWithoutClear(int tableId) {
		String response = Http.get(Server.GET_MYORDER, "TID=" + tableId);
		if ("null".equals(response)) {
			Log.w(TAG, "getOrderFromServer.null");
			mOrder.clear();
			return -2;
		} else if (response == null) {
			Log.e(TAG, "getOrderFromServer.timeOut");
			return TIME_OUT;
		}
		try {
			JSONArray tableList = new JSONArray(response);
			int length = tableList.length();
			for (int i = 0; i < length; i++) {
				JSONObject item = tableList.getJSONObject(i);
				float quantity = (float) item.getDouble("quantity");
				int dishId = item.getInt("dish_id");
				int status = item.getInt("status");
				Float dishPrice = (float) item.getDouble("price");
				Cursor cur = getDishInfoFromDB(dishId);
				if (cur == null) {
					return ERR_DB;
				}
				String name = cur.getString(NAME_COLUMN);
				String pic = cur.getString(PIC_COLUMN);
				int printer = cur.getInt(PRINTER_COLUMN);
				String unit = cur.getString(UNIT_NAME);
				Dish mDish = new Dish(dishId, name, dishPrice, pic, unit,
						printer);
				addOrder(mDish, quantity, tableId, status, MODE_PAD, mOrder);
			}
			return 0;
		} catch (Exception e) {
			Log.e(TAG, response);
			e.printStackTrace();
		}
		return -1;
	}

	public void updateQuantity(int index, float quantity) {
		mOrder.get(index).phoneQuantity = 0;
		mOrder.get(index).padQuantity = quantity;
	}

	public static String convertFloat(float quantity) {
		DecimalFormat format = new DecimalFormat("0.00");
		String quantityStr[] = format.format(quantity).split("\\.");
		if (quantityStr[1].equals("00")) {
			return quantityStr[0];
		} else {
			return quantityStr[0] + "." + quantityStr[1];
		}
	}

	public String getDishName(int index) {
		String name = getDishNameFromDB(index);
		if (name == null) {
			return "菜名为空";
		}
		return name;
	}

	public int submitDelDish(int type) {
		String response;
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		int ret = Http.getPrinterStatus(Server.PRINTER_CONTENT_TYPE_ORDER);
		if (ret < 0) {
			return ret;
		}
		try {
			order.put("waiter", UserData.getUserName());
			order.put("tableId", Info.getTableId());
			order.put("tableName", Info.getTableName());
			order.put("timestamp", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONArray dishes = new JSONArray();
		try {
			if (type == DEL_ALL_ORDER) {
				for (int i = 0; i < mOrder.size(); i++) {
//					if ("斤".equals(mOrder.get(i).dish.getUnit())) {
//						continue;
//					}
					JSONObject dish = new JSONObject();
					dish.put("dishId", mOrder.get(i).dish.getDishId());
					dish.put("name", mOrder.get(i).dish.getName());
					dish.put("price", mOrder.get(i).dish.getPrice());
					dish.put(
							"quan",
							(mOrder.get(i).padQuantity + mOrder.get(i).phoneQuantity));
					dish.put("printer", mOrder.get(i).getPrinter());
					dishes.put(dish);
				}
				if (dishes.length() <= 0) {
					return NOTHING_TO_DEL;
				}
			} else {
				if (mDelOrder.size() <= 0) {
					return -1;
				}
				for (int i = 0; i < mDelOrder.size(); i++) {
					JSONObject dish = new JSONObject();
					dish.put("dishId", mDelOrder.get(i).dish.getDishId());
					dish.put("name", mDelOrder.get(i).dish.getName());
					dish.put("price", mDelOrder.get(i).dish.getPrice());
					dish.put("quan", mDelOrder.get(i).getQuantity());
					dish.put("printer", mDelOrder.get(i).dish.getPrinter());
					if ("斤".equals(mDelOrder.get(i).dish.getUnit())) {
						dish.put("unit", true);
					}else{
						dish.put("unit", false);
					}
					dishes.put(dish);
				}
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d(TAG, order.toString());
		response = Http.post(Server.DEL_ORDER + "?TYPE=" + type,
				order.toString());
		mDelOrder.clear();
		if (!ErrorPHP.isSucc(response, TAG)) {
			return -2;
		}
		return 0;

	}

	public void showServerDelProgress() {
		Method showDelProcessDlg;
		try {
			showDelProcessDlg = mContext.getClass().getMethod(
					"showDeletePhoneOrderProcessDlg", new Class[0]);
			showDelProcessDlg.invoke(mContext, new Object[0]);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public void removeServedDishes() {
		for (int i = 0; i < mOrder.size(); i++) {
			OrderedDish item = (OrderedDish) mOrder.get(i);
			mOrder.get(i).padQuantity = item.getQuantity() - item.status;
			if (mOrder.get(i).padQuantity <= 0) {
				mOrder.remove(i);
				i--;
			}
		}
	}
	
	public void setAdvPayment(float pay) {
		mAdvPayment = pay;
	}
	
	public float getAdvPayment() {
		return mAdvPayment;
	}

	protected Cursor getDishInfoFromDB(int id) {
		String sql = String.format(Locale.CHINESE, "SELECT %s, %s, %s, %s, %s FROM %s,%s,%s Where %s.%s=%s and %s.%s=%d",
						CnkDbHelper.DISH_NAME, CnkDbHelper.DISH_PRICE,
						CnkDbHelper.DISH_PIC, CnkDbHelper.DISH_PRINTER,
						CnkDbHelper.UNIT_NAME, CnkDbHelper.TABLE_DISH_INFO,
						CnkDbHelper.TABLE_DISH_CATEGORY,
						CnkDbHelper.TABLE_UNIT, CnkDbHelper.TABLE_UNIT, "id",
						CnkDbHelper.UNIT_ID, CnkDbHelper.TABLE_DISH_INFO,
						CnkDbHelper.DISH_ID, id);
		Cursor cur = mDb.rawQuery(sql, null);

		if (cur.moveToNext()) {
			return cur;
		}
		return null;
	}

	@Override
	protected void finalize() throws Throwable {
		if (mDb != null) {
			mDb.close();
		}
		super.finalize();
	}

	private String getFlavorNameToSubmit(int i, String flavorStr) {
		if (mOrder.get(i).flavor != null) {
			StringBuffer flavorStrBuf = new StringBuffer();
			for (int k = 0; k < mOrder.get(i).flavor.length; k++) {
				if (mOrder.get(i).flavor[k] == true) {
					flavorStrBuf.append(MyOrder.mFlavorName[k] + ",");
				}
			}
			if (!flavorStrBuf.toString().equals("")) {
				flavorStr = flavorStrBuf.toString().substring(0,
						flavorStrBuf.length() - 1);
			}
		}
		return flavorStr;
	}

	private int updateServerServedDish(int tableId, int dishId, int status) {
		String dishStatusPkg = Http.get(Server.SERVE_ORDER, "TID=" + tableId
				+ "&DID=" + dishId + "&STATUS=" + status);
		if (ErrorPHP.isSucc(dishStatusPkg, TAG)) {
			return 0;
		} else {
			return -1;
		}
	}

	private String getDishNameFromDB(int id) {
		Cursor cur = mDb.query(CnkDbHelper.TABLE_DISH_INFO,
				new String[] { CnkDbHelper.DISH_NAME }, CnkDbHelper.DISH_ID
						+ "=" + id, null, null, null, null);

		if (cur.moveToNext()) {
			return cur.getString(0);
		}
		return null;
	}
	
	private String orderTimeTypeToString(int type, int hourOfDay) {
		String time = "午";
		if (hourOfDay >= 14) {
			time = "晚";
		}
		switch (type) {
		case ORDER_INSTANT:
			return "即" + time;
		case ORDER_PEND:
			return "叫" + time;
		case ORDER_REPRINT:
			return "重打";
		default:
			return "即单";
		}
	}
	
	public String toString() {
		StringBuffer myOrder = new StringBuffer();
		for (int i = 0; i < mOrder.size(); i++) {
			myOrder.append("|" + mOrder.get(i).getName() + "*" + mOrder.get(i).getQuantity());
		}
		return myOrder.toString();
	}

}
