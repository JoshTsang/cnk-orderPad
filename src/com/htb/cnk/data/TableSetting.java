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

	private static final long serialVersionUID = 1L;
	private ErrorPHP mError = new ErrorPHP();
	private MyOrder mOrder;
	private final String TAG = "tableAtivity";

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

	public int getTableStatusFromServer() {
		String tableStatusPkg = Http.get(Server.GET_TABLE_STATUS, "");
		if (tableStatusPkg == null || "null".equals(tableStatusPkg)
				|| "".equals(tableStatusPkg)) {
			Log.e("getTableStatusFromServer", tableStatusPkg);
			return -1;
		}
		try {
			JSONArray tableList = new JSONArray(tableStatusPkg);
			int length = tableList.length();
			TableSettingItem asItem;
			mTableSettings.clear();
			for (int i = 0; i < length; i++) {// 遍历JSONArray
				JSONObject item = tableList.getJSONObject(i);
				int id = item.getInt("id");
				String name = item.getString("name");
				int status = item.getInt("status");
				asItem = new TableSettingItem(status, name, id);
				add(asItem);
			}
			return 0;
		} catch (Exception e) {
			Log.w("getTableStatus.php", tableStatusPkg);
			e.printStackTrace();
		}
		return -1;
	}

	public int getItemTableStatus(int tableId) {
		String tableStatusPkg = Http.get(Server.GET_ITEM_TABLE_STATUS, "TSI="
				+ tableId);
		if (tableStatusPkg == null) {
			return -1;
		}

		int start = tableStatusPkg.indexOf("[");
		int end = tableStatusPkg.indexOf("]");

		if ((start < 0) || (end < 0)) {
			Log.e("getItemTableStatus", tableStatusPkg);
			return -1;
		}
		
		String tableStatus = tableStatusPkg.subSequence(start + 1, end)
				.toString();
		if (tableStatus.length() <= 0) {
			Log.e("getItemTableStatus_tableStatus", tableStatus);
			return -1;
		}
		return Integer.parseInt(tableStatus);
	}

	public int updateStatus(int tableId, int status) {
		String tableStatusPkg = Http.get(Server.UPDATE_TABLE_STATUS, "TID="
				+ tableId + "&TST=" + status);
		if (mError.getErrorStr(tableStatusPkg,"updateStatus") < 0) {
			return -1;
		}
		if (mError.getSucc().equals("true")) {
			return 0;
		}
		Log.e("TableSetting_updateStatus", mError.getErroe());
		return -1;
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
		if (mError.getErrorStr(tableCleanPkg,"cleanTalble") < 0) {
			return -1;
		}
		if (mError.getSucc().equals("true")) {
			return 0;
		}
		Log.e("TableSetting_cleanTable", mError.getErroe());
		return -1;
	}

	public int changeTable(int srcTId, int destTId, Context context) {
		if (mOrder == null) {
			mOrder = new MyOrder(context);
		}
		int ret = mOrder.getOrderFromServer(srcTId);
//		if (ret < 0) {
//			Log.e("TableSetting_changeTable_JR", ":11");
//			return -1;
//		}

		JSONObject order = new JSONObject();
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		if (mOrder.count() <= 0) {
			return -1;
		}
		try {
			order.put("tableId", Info.getTableId());
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
		if (mError.getErrorStr(tableChangePkg,"changeTable") < 0) {
			return -1;
		}
		if ("true".equals(mError.getSucc())) {
			return 0;
		}
		Log.e("TableSetting_changeTable", mError.getErroe());
		return -1;
	}

}
