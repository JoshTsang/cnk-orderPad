package com.htb.cnk.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
	private final String TAG = "tableSetting";
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

	public void add(TableSettingItem item) {
		mTableSettings.add(item);
	}

	public int size() {
		return mTableSettings.size();
	}

	public int getStatus(int index) {
		return mTableSettings.get(index).getStatus();
	}

	public int getStatusTableId(int tableId) {
		int i;
		for (i = 0; i < mTableSettings.size() - 1; i++) {
			if (tableId == mTableSettings.get(i).getId()) {
				break;
			}
		}
		return mTableSettings.get(i).getStatus();
	}

	public int getIdIndex(int index) {
		return mTableSettings.get(index).getId();
	}

	public int getId(int tableId) {
		for (TableSettingItem item : mTableSettings) {
			if (item.getId() == tableId) {
				return item.getId();
			}
		}
		return -1;
	}

	public int getId(String tableName) {
		for (TableSettingItem item : mTableSettings) {
			if (item.getName().equals(tableName)) {
				return item.getId();
			}
		}
		return -1;
	}

	public String getName(int tableId) {
		for (TableSettingItem item : mTableSettings) {
			if (item.getId() == tableId) {
				return item.getName();
			}
		}
		return null;
	}

	public String getNameIndex(int index) {
		return mTableSettings.get(index).getName();
	}

	public String[] getNameAll() {
		String tableName[] = new String[mTableSettings.size()];
		int i = 0;
		for (TableSettingItem item : mTableSettings) {
			tableName[i] = item.getName();
			i++;
		}
		return tableName;
	}

	public int[] getIdAll() {
		int tableId[] = new int[mTableSettings.size()];
		int i = 0;
		for (TableSettingItem item : mTableSettings) {
			tableId[i] = item.getId();
			i++;
		}
		return tableId;
	}

	public ArrayList<HashMap<String, Object>> getCombine() {
		ArrayList<HashMap<String, Object>> combine = new ArrayList<HashMap<String, Object>>();
		for (TableSettingItem item : mTableSettings) {
			if (item.getStatus() == 1) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("name", item.getName());
				map.put("id", item.getId());
				combine.add(map);
			}
		}
		return combine;
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

	public int cleanTalble(List<Integer> tableIdList) throws JSONException {
		String time = getCurrentTime();
		JSONObject orderALL = new JSONObject();
		JSONArray order = new JSONArray();
		for (Integer item : tableIdList) {
			JSONObject tId = new JSONObject();
			tId.put("TID", item.intValue());
			order.put(tId);
		}
		orderALL.put("order", order);
		orderALL.put("timestamp", time);
		Log.d(TAG, orderALL.toString());
		String tableCleanPkg = Http.post(Server.CLEAN_TABLE,
				orderALL.toString());
		if (!ErrorPHP.isSucc(tableCleanPkg, TAG)) {
			return -1;
		}
		return 0;
	}

	public int changeTable(Context context, int srcTId, int destTId,
			String srcName, String destName, int persons) {
		int ret = getOrderFromServer(context, srcTId);
		if (ret == -1) {
			Log.e(TAG, "mOrder.getOrderFromServer.timeout:changeTable");
			return TIME_OUT;
		}

		ret = Http.getPrinterStatus(Server.PRINTER_CONTENT_TYPE_ORDER);
		if (ret < 0) {
			return ret;
		}

		JSONObject order = new JSONObject();
		String time = getCurrentTime();
		orderJson(destTId, order, srcName + "->" + destName, time, persons);
		String tableChangePkg = Http.post(Server.CHANGE_TABLE + "?srcTID="
				+ srcTId + "&destTID=" + destTId, order.toString());
		if (!ErrorPHP.isSucc(tableChangePkg, TAG)) {
			return -2;
		}
		return 0;
	}

	public int copyTable(Context context, int srcTId, int destTId, int persons) {
		int ret = getOrderFromServer(context, srcTId);
		if (ret == -1) {
			Log.e(TAG, "mOrder.getOrderFromServer.timeout:copyTable");
			return TIME_OUT;
		}
		JSONObject order = new JSONObject();
		String time = getCurrentTime();
		orderJson(destTId, order, Info.getTableName(), time, persons);
		String tablecopyPkg = Http.post(Server.COPY_TABLE + "?srcTID=" + srcTId
				+ "&destTID=" + destTId, order.toString());
		if (!ErrorPHP.isSucc(tablecopyPkg, TAG)) {
			return -2;
		}
		return 0;
	}

	public int checkOut(Context context, List<Integer> srcTId,
			List<String> tableName, Double receivable, Double income,
			Double change) {
		StringBuffer nameStrBuf = new StringBuffer();
		JSONObject orderAll = new JSONObject();
		JSONArray orderArrary = new JSONArray();
		int i = 0;
		for (Integer item : srcTId) {
			int ret = getOrderFromServer(context, item.intValue());
			if (ret == -1) {
				Log.e(TAG, "mOrder.getOrderFromServer.timeout:checkOut");
				return TIME_OUT;
			}
			JSONObject orderObject = new JSONObject();
			String time = getCurrentTime();
			orderJson(item, orderObject, tableName.get(i), time, 0);
			nameStrBuf.append(tableName.get(i).toString() + ",");
			orderArrary.put(orderObject.toString());
			i++;
		}
		String flavorStr = nameStrBuf.toString().substring(0,
				nameStrBuf.length() - 1);
		try {
			orderAll.put("waiter", UserData.getUserName());
			orderAll.put("orderAll", orderArrary.toString());
			orderAll.put("receivable", receivable.toString());
			orderAll.put("income", income.toString());
			orderAll.put("change", change.toString());
			orderAll.put("tableName", flavorStr.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String tablecheckOutPkg = Http.post(Server.CHECK_OUT,
				orderAll.toString());
		if (!ErrorPHP.isSucc(tablecheckOutPkg, TAG)) {
			return -2;
		}
		return 0;
	}

	public double getTotalPriceTable(Context context, List<Integer> srcTId) {
		double totalPrice = 0;
		for (Integer item : srcTId) {
			int ret = getOrderFromServer(context, item.intValue());
			if (ret == -1) {
				Log.e(TAG,
						"mOrder.getOrderFromServer.timeout:getTotalPriceTable");
				return TIME_OUT;
			}
			totalPrice = totalPrice + mOrder.getTotalPrice();
		}
		return totalPrice;
	}

	private String getCurrentTime() {
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		return time;
	}

	private void orderJson(int destTId, JSONObject order, String tableName,
			String time, int persons) {
		try {
			order.put("waiter", UserData.getUserName());
			order.put("tableId", destTId);
			order.put("persons", persons);
			order.put("tableName", tableName);
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
	}

	public int getOrderFromServer(Context context, int srcTId) {
		if (mOrder == null) {
			mOrder = new MyOrder(context);
		} else {
			mOrder.clear();
		}
		int ret = mOrder.getOrderFromServer(srcTId);
		return ret;
	}

}
