package com.htb.cnk.data;

/**
 * @author josh
 *
 */
public class Info {
	public final static int WORK_MODE_CUSTOMER = 0;
	public final static int WORK_MODE_WAITER = 1;
	public final static int WORK_MODE_PHONE = 2;
	public final static int ORDER_LIST_MENU = 0;
	public final static int ORDER_QUCIK_MENU = 1;
	
	static private int mMode;
	static private int mTableId;
	static private String mTableName;
	static private boolean mNewCustomer;
	static private int mMenu;

	public static int getMode() {
		return mMode;
	}
	public static void setMode(int mode) {
		Info.mMode = mode;
	}
	public static int getTableId() {
		return mTableId;
	}
	public static void setTableId(int tableId) {
		Info.mTableId = tableId;
	}

	public static String getTableName() {
		if (mTableName == null) {
			return "菜谱模式";
		}
		return mTableName;
	}

	public static void setTableName(String tableName) {
		Info.mTableName = tableName;
	}

	public static boolean isNewCustomer() {
		return mNewCustomer;
	}

	public static void setNewCustomer(boolean newCustomer) {
		Info.mNewCustomer = newCustomer;
	}
	
	public static int getMenu(){
		return mMenu;
	}
	public static void setMenu(int menu){
		Info.mMenu = menu;
	}
	
	public static void logout() {
		setMode(Info.WORK_MODE_CUSTOMER);
		setTableId(-1);
		setTableName(null);
	}
}
