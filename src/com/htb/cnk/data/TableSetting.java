package com.htb.cnk.data;

import java.io.Serializable;
import java.text.NumberFormat;
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
import com.htb.constant.Table;

public class TableSetting implements Serializable {
	private static final int TIME_OUT = -1;
	private final String TAG = "tableSetting";
	private static final long serialVersionUID = 1L;
	private MyOrder mOrder;
	boolean phoneOrderPending;
	public static final int PHONE_ORDER = 1;
	public static final int MY_ORDER = 2;
	public static final int SUBMIT = 0;

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
	private static List<String> checkOutPrinter = new ArrayList<String>();

	public void add(TableSettingItem item) {
		mTableSettings.add(item);
	}

	public int size() {
		return mTableSettings.size();
	}

	public void clear() {
		mTableSettings.clear();
	}

	public int getStatusIndex(int index) {
		if (isIndexBoundary(index)) {
			return mTableSettings.get(index).getStatus();
		}
		return -1;
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
		if (isIndexBoundary(index)) {
			return mTableSettings.get(index).getId();
		}
		return -1;
	}

	/**
	 * @param index
	 * @return
	 */
	private boolean isIndexBoundary(int index) {
		return index >= 0 && index < mTableSettings.size();
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
		if (isIndexBoundary(index)) {
			return mTableSettings.get(index).getName();
		}
		return null;
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

	public ArrayList<HashMap<String, Object>> getTableOpen() {
		ArrayList<HashMap<String, Object>> tableOpen = new ArrayList<HashMap<String, Object>>();
		for (TableSettingItem item : mTableSettings) {
			if (item.getStatus() == 1) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("name", item.getName());
				map.put("id", item.getId());
				tableOpen.add(map);
			}
		}
		return tableOpen;
	}

	public void setStatus(int index, int n) {
		if (isIndexBoundary(index)) {
			mTableSettings.get(index).setStatus(n);
		}
	}

	public int getTableStatusFromServer() {
		String tableStatusPkg = Http.get(Server.GET_TABLE_STATUS, null);
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
			for (int i = 0; i < length; i++) {
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

	public int updateStatus(int tableId, int type) {
		int status = getItemTableStatus(tableId);
		if (status < 0) {
			return status;
		}
		if (type == PHONE_ORDER) {
			if (status >= Table.PHONE_STATUS) {
				status = status - Table.PHONE_STATUS;
			} else {
				status = 1;
			}
		} else if (type == MY_ORDER) {
			if (status < Table.PHONE_STATUS) {
				status = 1;
			}
		} else if (type == SUBMIT) {
			status = 1;
		}
		String tableStatusPkg = Http.get(Server.UPDATE_TABLE_STATUS, "TID="
				+ tableId + "&TST=" + status);
		if (!ErrorPHP.isSucc(tableStatusPkg, TAG)) {
			return -1;
		}
		return 0;
	}

	public int cleanTalble(List<Integer> tableIdList) {
		String time = getCurrentTime();
		JSONObject orderALL = new JSONObject();
		JSONArray order = new JSONArray();
		for (Integer item : tableIdList) {
			JSONObject tId = new JSONObject();
			try {
				tId.put("TID", item.intValue());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			order.put(tId);
		}
		try {
			orderALL.put("order", order);
			orderALL.put("timestamp", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}

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
		mOrder.clear();
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
		mOrder.clear();
		return 0;
	}

	public int checkOut(Context context, List<Integer> srcTId,
			List<String> tableName, Double receivable, Double income,
			Double change) {
		int ret = Http.getPrinterStatus(Server.PRINTER_CONTENT_TYPE_ORDER);
		if (ret < 0) {
			return ret;
		}
		StringBuffer nameStrBuf = new StringBuffer();
		JSONObject orderAll = new JSONObject();
		JSONArray orderArrary = new JSONArray();
		int i = 0;
		for (Integer item : srcTId) {
			ret = getOrderFromServer(context, item.intValue());
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
		mOrder.clear();
		return 0;
	}

	public int combineTable(Context context, int srcTId, int destTId,
			String srcName, String destName, int persons) {
		int ret = getOrderFromServer(context, srcTId);
		if (ret == -1) {
			Log.e(TAG, "mOrder.getOrderFromServer.timeout:combineTable");
			return TIME_OUT;
		}

		ret = Http.getPrinterStatus(Server.PRINTER_CONTENT_TYPE_ORDER);
		if (ret < 0) {
			return ret;
		}
		JSONObject order = new JSONObject();
		String time = getCurrentTime();
		int destPersons = MyOrder.loodPersons(destTId);
		if (destPersons < 0) {
			Log.e(TAG, "mOrder.getPersonsFromServer");
			return destPersons;
		}
		persons = destPersons + persons;
		orderJson(destTId, order, srcName + "->" + destName, time, persons);
		String tablecombinePkg = Http.post(Server.COMBINE_TABLE + "?srcTID="
				+ srcTId + "&destTID=" + destTId, order.toString());
		if (!ErrorPHP.isSucc(tablecombinePkg, TAG)) {
			return -2;
		}
		mOrder.clear();
		return 0;
	}

	public double getTotalPriceTable(Context context, List<Integer> srcTId,
			List<String> tableName) {
		double totalPrice = 0;
		String time = getCurrentTime();
		int i = 0;
		checkOutPrinter.clear();
		for (Integer item : srcTId) {
			int ret = getOrderFromServer(context, item.intValue());
			if (ret == -1) {
				Log.e(TAG,
						"mOrder.getOrderFromServer.timeout:getTotalPriceTable");
				return TIME_OUT;
			}
			JSONObject order = new JSONObject();
			orderJson(item.intValue(), order, tableName.get(i), time, 0);
			checkOutPrinter.add(order.toString());
			i++;
			totalPrice = totalPrice + mOrder.getTotalPrice();
		}

		return totalPrice;
	}

	public String checkOutJson() {
		String checkOutJson = new String();
		checkOutJson = String.format("\b%s%24s%9s%8s%10s", "品名", "", "单价",
				"数量", "小计");
		int k = 0;
		List<String> tableName = new ArrayList<String>();
		List<Float> totalPrice = new ArrayList<Float>();
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);

		for (String item : checkOutPrinter) {
			try {
				JSONObject json = new JSONObject(item.toString());
				tableName.add(json.getString("tableName"));
				JSONArray jsonArrary = json.getJSONArray("order");
				checkOutJson = String.format("%s\n\r\b%s", checkOutJson,
						tableName.get(k) + "桌");
				float tatolPrice = 0;
				for (int i = 0; i < jsonArrary.length(); i++) {
					JSONObject jsonItem = (JSONObject) jsonArrary.get(i);
					float quan = jsonItem.getInt("quan");
					float price = jsonItem.getInt("price");
					String name = jsonItem.getString("name");
					String dish;
					tatolPrice = tatolPrice + (quan * price);
					byte[] strByte = name.getBytes();
					int strlen = name.length();
					int strByteLen = strByte.length;
					int zhLen = (strByteLen - strlen) / 2;
					int enLen = strlen - zhLen;
					int spaceLen = zhLen * 2 + enLen;
					spaceLen = 19 - spaceLen;
					spaceLen *= 2;
					if (enLen > 0) {
						spaceLen += 1;
					}

					// Log.d(TAG, "dishName:" + name + " zhLen:" + zhLen
					// + " enLen:" + enLen + " spaceLen:" + spaceLen);
					// Log.d(TAG, "dishName:" + name + " strLen:" + strlen
					// + " strByteLen:" + strByteLen);

					String priceStr = nf.format(price);
					String quanStr = nf.format(quan);
					String itemTotalPrice = nf.format(quan * price);
					int priceSpaceLen = getSpaceLen(7, priceStr.length());
					int quanSpaceLen = getSpaceLen(5, quanStr.length());
					int totalPriceSpaceLen = getSpaceLen(7,
							itemTotalPrice.length());
					if (spaceLen > 2) {
						dish = String.format("%s%" + spaceLen + "s%s%"
								+ priceSpaceLen + "s%s%" + quanSpaceLen + "s%"
								+ totalPriceSpaceLen + "s%s", name, "",
								priceStr, "", quanStr, "", "", itemTotalPrice);
					} else {
						// Log.d(TAG, "%s\r\n%36s%s%" + priceSpaceLen + "s%s%"
						// + quanSpaceLen + "s%" + totalPriceSpaceLen
						// + "s%s");
						dish = String.format("%s\r\n%42s%s%" + priceSpaceLen
								+ "s%s%" + quanSpaceLen + "s%"
								+ totalPriceSpaceLen + "s%s", name, "",
								priceStr, "", quanStr, "", "", itemTotalPrice);
					}
					checkOutJson = String.format("%s\n\r\b%s", checkOutJson,
							dish);
				}
				totalPrice.add(tatolPrice);
				k++;
			} catch (JSONException e) {
				Log.e(TAG, "checkOutJson.error");
				e.printStackTrace();
			}
		}
		checkOutJson = String.format("%s\n\r%s", checkOutJson,
				"------------------------------------------");
		float tableAllPrice = 0;
		for (int i = 0; i < k; i++) {
			String tName = tableName.get(i).toString();
			String totalPriceStr = nf.format(totalPrice.get(i));
			int len = 38 - tName.length() - totalPriceStr.length();
			checkOutJson += String.format("\r\n %s%" + len * 2 + "s%s", tName
					+ "桌", "", totalPriceStr);
			tableAllPrice = tableAllPrice + totalPrice.get(i);
		}
		String endPriceStr = nf.format(tableAllPrice);
		checkOutJson = String.format("%s\n\r %s%"
				+ ((36 - endPriceStr.length()) * 2 - 1) + "s%s", checkOutJson,
				"合计", "", endPriceStr);
		return checkOutJson;
	}

	private int getSpaceLen(int expect, int actal) {
		int ret = (expect - actal) * 2;
		return ret > 0 ? ret : 1;
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
				dish.put("printer", mOrder.getPrinter(i));
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
