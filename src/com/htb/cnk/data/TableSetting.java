package com.htb.cnk.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.htb.cnk.lib.ErrorPHP;
import com.htb.cnk.lib.Http;
import com.htb.constant.Server;

public class TableSetting implements Serializable {
	private static final int TIME_OUT = -1;
	private final String TAG = "tableAtivity";
	private static final long serialVersionUID = 1L;
	private MyOrder mOrder;
	boolean phoneOrderPending;

	public class TableSettingItem {
		protected int mStatus;
		protected String mName;
		protected int mId;

		public TableSettingItem(int status, String name, int id) {
			mStatus = status;
			mName = name;
			mId = id;
		}

		public void setStatus(int status) {
			mStatus = status;
		}

		public int getStatus() {
			return mStatus;
		}

		public int getId() {
			return mId;
		}

		public String getName() {
			return mName;
		}
	}

	private static List<TableSettingItem> mTableSettings = new ArrayList<TableSettingItem>();

	public TableSetting() {

	}

	public void add(TableSettingItem item) {
		mTableSettings.add(item);
	}

	public int size() {
		return mTableSettings.size();
	}

	public int getStatus(int index) {
		return mTableSettings.get(index).getStatus();
	}

	public int getStatusTableId(int index) {
		int i;
		for (i = 0; i < mTableSettings.size() - 1; i++) {
			if (index == mTableSettings.get(i).getId()) {
				break;
			}
		}
		return mTableSettings.get(i).getStatus();
	}

	public int getId(int index) {
		return mTableSettings.get(index).getId();
	}

	public String getName(int index) {
		return mTableSettings.get(index).getName();
	}

	public void setStatus(int index, int n) {
		mTableSettings.get(index).setStatus(n);
	}

	// TODO define
	public int getTableStatusFromServer() {
		String tableStatusPkg = Http.get(Server.GET_TABLE_STATUS, "");
		if (tableStatusPkg == null) {
			Log.e(TAG, "getTableStatusFromServer.timeout");
			return TIME_OUT;
		}
		try {
			JSONArray tableList = new JSONArray(tableStatusPkg);
			int length = tableList.length();
			TableSettingItem asItem;
			mTableSettings.clear();
			phoneOrderPending = false;
			for (int i = 0; i < length; i++) {// 遍历JSONArray
				JSONObject item = tableList.getJSONObject(i);
				int id = item.getInt("id");
				String name = item.getString("name");
				int status = item.getInt("status");
				if (status == 50 || status == 51) {
					phoneOrderPending = true;
				}
				asItem = new TableSettingItem(status, name, id);
				add(asItem);
			}
			return 0;
		} catch (Exception e) {
			Log.e(TAG, tableStatusPkg);
			e.printStackTrace();
		}
		return -1;
	}

	public boolean hasPendedPhoneOrder() {
		return phoneOrderPending;
	}

	public int getItemTableStatus(int tableId) {
		String tableStatusPkg = Http.get(Server.GET_ITEM_TABLE_STATUS, "TSI="
				+ tableId);
		if (tableStatusPkg == null) {
			Log.e(TAG, "getItemTableStatus:tableStatusPkg is null");
			return TIME_OUT;
		}

		int start = tableStatusPkg.indexOf("[");
		int end = tableStatusPkg.indexOf("]");

		if ((start < 0) || (end < 0)) {
			Log.e(TAG, "getItemTableStatus:tableStatusPkg is " + tableStatusPkg);
			return -1;
		}

		String tableStatus = tableStatusPkg.subSequence(start + 1, end)
				.toString();
		if (tableStatus.length() <= 0) {
			Log.e(TAG, "getItemTableStatus:tableStatusPkg length  < 0");
			return -1;
		}
		return Integer.parseInt(tableStatus);
	}

	public int updateStatus(int tableId, int status) {
		String tableStatusPkg = Http.get(Server.UPDATE_TABLE_STATUS, "TID="
				+ tableId + "&TST=" + status);
		if (!ErrorPHP.isSucc(tableStatusPkg, TAG)) {
			return -1;
		}
		return 0;
	}

	public int cleanTalble(int tableId) {
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		try {
			order.put("timestamp", time);
			order.put("TID", tableId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String tableCleanPkg = Http.post(Server.CLEAN_TABLE, order.toString());
		if (!ErrorPHP.isSucc(tableCleanPkg, TAG)) {
			return -1;
		}
		return 0;
	}

	public int changeTable(int srcTId, int destTId, String destName, Context context) {
		if (mOrder == null) {
			mOrder = new MyOrder(context);
		} else {
			mOrder.clear();
		}
		int ret = mOrder.getOrderFromServer(srcTId);
		if (ret == -1) {
			Log.e(TAG, "mOrder.getOrderFromServer.timeout");
			return TIME_OUT;
		}
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		try {
			order.put("waiter", UserData.getUserName());
			order.put("tableName", destName);
			order.put("tableId", destTId);
			order.put("timestamp", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONArray dishes = new JSONArray();
		try {
			for (int i = 0; i < mOrder.count(); i++) {
				JSONObject dish = new JSONObject();
				dish.put("dishId", mOrder.getDishId(i));
				dish.put("name", mOrder.getName(i));
				dish.put("price", mOrder.getPrice(i));
				dish.put("quan", mOrder.getQuantity(i));
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String tableChangePkg = Http.post(Server.CHANGE_TABLE + "?srcTID="
				+ srcTId + "&destTID=" + destTId, order.toString());
		if (!ErrorPHP.isSucc(tableChangePkg, TAG)) {
			return -1;
		}
		return 0;
	}

	public int copyTable(int srcTId, int destTId, Context context) {
		if (mOrder == null) {
			mOrder = new MyOrder(context);
		} else {
			mOrder.clear();
		}
		int ret = mOrder.getOrderFromServer(srcTId);
		if (ret == -1) {
			Log.e(TAG, "mOrder.getOrderFromServer.timeout");
			return TIME_OUT;
		}
		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		try {
			order.put("waiter", UserData.getUserName());
			order.put("tableId", destTId);
			order.put("tableName", Info.getTableName());
			order.put("timestamp", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JSONArray dishes = new JSONArray();
		try {
			for (int i = 0; i < mOrder.count(); i++) {
				JSONObject dish = new JSONObject();
				dish.put("dishId", mOrder.getDishId(i));
				dish.put("name", mOrder.getName(i));
				dish.put("price", mOrder.getPrice(i));
				dish.put("quan", mOrder.getQuantity(i));
				dishes.put(dish);
			}
			order.put("order", dishes);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String tablecopyPkg = Http.post(Server.COPY_TABLE + "?srcTID="
				+ srcTId + "&destTID=" + destTId, order.toString());
		if (!ErrorPHP.isSucc(tablecopyPkg, TAG)) {
			return -2;
		}
		return 0;
	}
	
}
