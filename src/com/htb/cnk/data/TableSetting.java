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
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseArray;

import com.htb.cnk.lib.ErrorPHP;
import com.htb.cnk.lib.Http;
import com.htb.constant.Server;
import com.htb.constant.Table;

public class TableSetting implements Serializable {
	private static final int TIME_OUT = -1;
	private static final String TAG = "tableSetting";
	private static final long serialVersionUID = 1L;
	private MyOrder mOrder;
	boolean phoneOrderPending = false;
	public static final int PHONE_ORDER = 1;
	public static final int MY_ORDER = 2;
	public static final int SUBMIT = 0;
	private int floorNum;
	// private int FLOOR_NUM_CURRENT = 10;
	private Context mContext;

	public class TableSettingItem {
		protected int mStatus;
		protected String mName;
		protected int mId;
		protected int mCategory;
		protected int mIndex;
		protected int mArea;
		protected int mFloor;

		public TableSettingItem(int status, String name, int id, int category,
				int index, int area, int floor) {
			mStatus = status;
			mName = name;
			mId = id;
			mCategory = category;
			mIndex = index;
			mArea = area;
			mFloor = floor;
		}

		public void update(int status) {
			mStatus = status;
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

		public int getCategory() {
			return mCategory;
		}

		public int getIndex() {
			return mIndex;
		}

		public int getArea() {
			return mArea;
		}

		public int getFloor() {
			return mFloor;
		}
	}

	private static List<TableSettingItem> mTableSettings = new ArrayList<TableSettingItem>();
	private static ArrayList<List<TableSettingItem>> mTableFloor = new ArrayList<List<TableSettingItem>>();
	private static SparseArray<TableSettingItem> mTableIndexForId = new SparseArray<TableSetting.TableSettingItem>();
	private static SparseArray<TableSettingItem> mChargedAreaIndex = new SparseArray<TableSetting.TableSettingItem>();
	private static HashMap<String, TableSettingItem> mTableIndexForName = new HashMap<String, TableSetting.TableSettingItem>();
	private static List<TableSettingItem> mTableScope = new ArrayList<TableSettingItem>();
	private static List<String> checkOutPrinter = new ArrayList<String>();

	public TableSetting(Context context) {
		mContext = context;
	}

	public void add(TableSettingItem item, List<TableSettingItem> tableItem) {
		tableItem.add(item);
	}

	public int tableSeetingsSize() {
		return mTableSettings.size();
	}

	public void tableSeetingsclear() {
		mTableSettings.clear();
	}

	public void addFloor() {
		mTableFloor.clear();
		for (int i = 1; i < floorNum + 1; i++) {
			List<TableSettingItem> map = new ArrayList<TableSettingItem>();
			for (int k = 0; k < mTableSettings.size(); k++) {
				if (i == mTableSettings.get(k).getFloor()) {
					map.add(mTableSettings.get(k));
				}
			}
			mTableFloor.add(map);
		}
	}

	public int getFloorNum() {
		return floorNum;
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

	public int getId(String tableName) {
		TableSettingItem item = mTableIndexForName.get(tableName);
		return item == null ? (-1) : item.getId();
	}

	public int getIdByAllIndex(int index) {
		TableSettingItem item = mTableSettings.get(index);
		return item == null ? (-1) : item.getId();
	}

	public String getName(int tableId) {
		TableSettingItem item = mTableIndexForId.get(tableId);
		return item == null ? null : item.getName();
	}

	public String getNameByAllIndex(int index) {
		TableSettingItem item = mTableSettings.get(index);
		return item == null ? null : item.getName();
	}

	public int getStatusById(int tableId) {
		TableSettingItem item = mTableIndexForId.get(tableId);

		return item == null ? 0 : item.getStatus();
	}

	public int getIndexByAllId(int tableId) {
		int index = 0;
		for (TableSettingItem item : mTableSettings) {
			if (item.getId() == tableId) {
				return index;
			}
			index++;
		}
		return -1;
	}

	public ArrayList<HashMap<String, Object>> getTableOpen() {
		ArrayList<HashMap<String, Object>> tableOpen = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < mTableSettings.size(); i++) {
			if (mTableSettings.get(i).getStatus() == 1) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("name", mTableSettings.get(i).getName());
				map.put("id", mTableSettings.get(i).getId());
				tableOpen.add(map);
			}
		}
		return tableOpen;
	}

	public int getTableStatusFromServerActivity() {
		String tableStatusPkg = Http.get(Server.GET_TABLE_STATUS, null);
		if (tableStatusPkg == null) {
			Log.e(TAG, "getTableStatusFromServer.timeout");
			return TIME_OUT;
		}
		return parseTableSetting(tableStatusPkg);
	}

	public static String getTableStatusFromServer() {
		String tableStatusPkg = Http.get(Server.GET_TABLE_STATUS, "UUID="
				+ Lisence.getDeviceId());
		if (tableStatusPkg == null) {
			Log.e(TAG, "getTableStatusFromServer.timeout");
			return null;
		}
		return tableStatusPkg;
	}

	/**
	 * @param tableStatusPkg
	 * @return
	 */
	public int parseTableSetting(String tableStatusPkg) {
		if (tableStatusPkg == null || tableStatusPkg == "")
			return -1;
		// Log.d(TAG, tableStatusPkg);
		try {
			JSONArray tableList = new JSONArray(tableStatusPkg);
			// TODO find good solution for table status update
			createTables(tableList);
			// /////////////////////////////////////
			return 0;
		} catch (Exception e) {
			Log.e(TAG, "tableStatusResponse:" + tableStatusPkg);
			e.printStackTrace();
		}
		return -1;
	}

	private void createTables(JSONArray tableList) {
		mTableSettings.clear();
		TableSettingItem asItem;
		phoneOrderPending = false;
		int length = tableList.length();

		try {
			for (int i = 0; i < length; i++) {
				JSONObject item = tableList.getJSONObject(i);
				int id = item.getInt("id");
				String name = item.getString("name");
				int status = item.getInt("status");
				if (status == 50 || status == 51) {
					phoneOrderPending = true;
				}
				int category;
				category = item.getInt("category");
				int index = item.getInt("index");
				int area = item.getInt("area");
				int floor = item.getInt("floor");
				asItem = new TableSettingItem(status, name, id, category,
						index, area, floor);
				add(asItem, mTableSettings);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		floorCategory();
		addFloor();
		updateIndex();
	}

	private void updateIndex() {
		mChargedAreaIndex.clear();
		mTableIndexForId.clear();
		mTableIndexForName.clear();
		int size = mTableSettings.size();
		TableSettingItem item;
		for (int i = 0; i < size; i++) {
			item = mTableSettings.get(i);
			mTableIndexForId.put(item.getId(), item);
			mTableIndexForName.put(item.getName(), item);
		}
	}

	private boolean isTableInCharge(int area) {
		SharedPreferences sharedPre = mContext.getSharedPreferences(
				"areaSetting", Context.MODE_PRIVATE);
		String areaString = sharedPre.getString("areaName", "");
		String stringTemp[] = null;
		boolean flag = true;
		stringTemp = areaString.split(",");
		if (!areaString.equals("")) {
			for (int i = 0; i < stringTemp.length; i++) {
				if (stringTemp[i].equals(String.valueOf(area))) {
					return true;
				}
			}
			flag = false;
		}
		return flag;
	}

	public String[] getchargedAreaName() {
		String[] areaString = null;
		String response = Http.get(Server.GET_AREA, "");
		Log.d(TAG, response);
		if ("null".equals(response)) {
			Log.w(TAG, "getOrderFromServer.null");
			return null;
		} else if (response == null) {
			Log.e(TAG, "getOrderFromServer.timeOut");
			return null;
		}
		try {
			JSONArray tableList = new JSONArray(response);
			int length = tableList.length();
			areaString = new String[length];
			for (int i = 0; i < length; i++) {
				JSONObject item = tableList.getJSONObject(i);
				int area = item.getInt("area");
				areaString[i] = String.valueOf(area);
			}
		} catch (Exception e) {
			Log.e(TAG, response);
			e.printStackTrace();
		}
		return areaString;
	}

	private void updateTables(JSONArray tableList) throws JSONException {
		TableSettingItem asItem;
		phoneOrderPending = false;
		int length = tableList.length();
		boolean isIndexUpdateNeed = false;

		for (int i = 0; i < length; i++) {
			JSONObject item = tableList.getJSONObject(i);
			int id = item.getInt("id");
			String name = item.getString("name");
			int status = item.getInt("status");
			if (status == 50 || status == 51) {
				phoneOrderPending = true;
			}
			int category = item.getInt("category");
			int index = item.getInt("index");
			int area = item.getInt("area");
			int floor = item.getInt("floor");
			asItem = findTableItemById(id);
			if (asItem != null) {
				asItem.update(status);
			} else {
				asItem = new TableSettingItem(status, name, id, category,
						index, area, floor);
				add(asItem, mTableSettings);
				isIndexUpdateNeed = true;
			}
		}

		floorCategory();
		if (isIndexUpdateNeed) {
			updateIndex();
		}
	}

	private TableSettingItem findTableItemById(int id) {
		return mTableIndexForId.get(id);
	}

	public boolean hasPendedPhoneOrder() {
		return phoneOrderPending;
	}

	public List<TableSettingItem> getTables() {
		return mTableSettings;
	}

	private TableSettingItem getItem(int tableId) {
		for (TableSettingItem item : mTableSettings) {
			if (item.getId() == tableId) {
				return item;
			}
		}
		return null;
	}

	public List<TableSettingItem> getTablesByFloor(int floor) {
		return mTableFloor.get(floor);
	}

	public List<TableSettingItem> getTablesByArea() {
		List<TableSettingItem> item = new ArrayList<TableSetting.TableSettingItem>();
		for (int i = 0; i < mChargedAreaIndex.size(); i++) {
			item.add(mChargedAreaIndex.valueAt(i));
		}
		return item;
	}

	// TODO seems like there are some multi thread prombles, modified mSettings
	// when accessing it
	public List<TableSettingItem> getTablesByScope() {
		mTableScope.clear();
		SharedPreferences sharedPre = mContext.getSharedPreferences(
				"waiterSetting", Context.MODE_PRIVATE);
		String waiterScopeString = sharedPre.getString("waiterScope", "");
		String stringTemp[] = null;
		stringTemp = waiterScopeString.split(",");
		if (!waiterScopeString.equals("")) {
			for (int i = 0; i < stringTemp.length; i++) {
				if (getItem(Integer.parseInt(stringTemp[i])) != null) {
					mTableScope.add(getItem(Integer.parseInt(stringTemp[i])));
				}
			}
		}
		return mTableScope;
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
		Log.d(TAG, orderALL.toString());
		String tableCleanPkg = Http.post(Server.CLEAN_TABLE,
				orderALL.toString());
		if (!ErrorPHP.isSucc(tableCleanPkg, TAG)) {
			return -1;
		}
		return 0;
	}

	public int changeTable(int srcTId, int destTId, String srcName,
			String destName, int persons) {
		int ret = getOrderFromServer(srcTId);
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

	public int copyTable(int srcTId, int destTId, int persons) {
		int ret = getOrderFromServer(srcTId);
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

	public int checkOut(List<Integer> srcTId, List<String> tableName,
			Double receivable, Double income, Double change) {
		int ret = Http.getPrinterStatus(Server.PRINTER_CONTENT_TYPE_ORDER);
		if (ret < 0) {
			return ret;
		}
		StringBuffer nameStrBuf = new StringBuffer();
		JSONObject orderAll = new JSONObject();
		JSONArray orderArrary = new JSONArray();
		int i = 0;
		for (Integer item : srcTId) {
			ret = getOrderFromServer(item.intValue());
			if (ret == -1) {
				Log.e(TAG, "mOrder.getOrderFromServer.timeout:checkOut");
				return TIME_OUT;
			}
			JSONObject orderObject = new JSONObject();
//			String time = getCurrentTime();
			orderJson(item, orderObject, tableName.get(i), null, 0);
			nameStrBuf.append(tableName.get(i).toString() + ",");
			orderArrary.put(orderObject.toString());
			i++;
		}
		String flavorStr = nameStrBuf.toString().substring(0,
				nameStrBuf.length() - 1);
		String time = getCurrentTime();
		try {
			orderAll.put("waiter", UserData.getUserName());
			orderAll.put("orderAll", orderArrary.toString());
			orderAll.put("receivable", receivable.toString());
			orderAll.put("income", income.toString());
			orderAll.put("change", change.toString());
			orderAll.put("tableName", flavorStr.toString());
			orderAll.put("timestamp", time);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d(TAG, orderAll.toString());
		String tablecheckOutPkg = Http.post(Server.CHECK_OUT,
				orderAll.toString());
		if (!ErrorPHP.isSucc(tablecheckOutPkg, TAG)) {
			return -2;
		}
		mOrder.clear();
		return 0;
	}

	public int combineTable(int srcTId, int destTId, String srcName,
			String destName, int persons) {
		int ret = getOrderFromServer(srcTId);
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

	public static boolean hasPhoneOrderPendingForChargedTables() {
		int status;
		for (int i = mChargedAreaIndex.size() - 1; i >= 0; i--) {
			status = mChargedAreaIndex.valueAt(i).getStatus();
			if (status == Table.PHONE_STATUS
					|| status == Table.PHONE_STATUS + Table.OPEN_TABLE_STATUS) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTableUnderCharge(int tableId) {
		if (mChargedAreaIndex.get(tableId) != null) {
			return true;
		} else {
			return false;
		}
	}

	public double getTotalPriceTable(List<Integer> srcTId,
			List<String> tableName) {
		double totalPrice = 0;
		String time = getCurrentTime();
		int i = 0;
		checkOutPrinter.clear();
		for (Integer item : srcTId) {
			int ret = getOrderFromServer(item.intValue());
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
					float quan = (float) jsonItem.getDouble("quan");
					float price = (float) jsonItem.getDouble("price");
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

	public int getOrderFromServer(int srcTId) {
		if (mOrder == null) {
			mOrder = new MyOrder(mContext);
		} else {
			mOrder.clear();
		}
		int ret = mOrder.getOrderFromServer(srcTId);
		return ret;
	}

	public static int getLocalTableStatusById(int tid) {
		return mTableIndexForId.get(tid).getStatus();
	}

	public static void setLocalTableStatusById(int tid, int status) {
		mTableIndexForId.get(tid).setStatus(status);
	}

	public int floorCategory() {
		String floorCategoryPkg = Http.get(Server.GET_FLOORNUM, null);
		if (floorCategoryPkg == null) {
			return -1;
		}
		floorNum = Integer.parseInt(floorCategoryPkg);
		return 0;
	}

}
