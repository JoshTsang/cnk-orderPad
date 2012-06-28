package com.htb.cnk.data;

/**
 * @author josh
 *
 */
public class Info {
	final static public int WORK_MODE_CUSTOMER = 0;
	final static public int WORK_MODE_WAITER = 1;
	
	static private int mMode;
	static private int mTableId;
	static private String mTableName;
	static private boolean mNewCustomer;
	
	public static int getMode() {
		return mMode;
	}
	public static void setMode(int mode) {
		Info.mMode = mode;
	}
	public static int getTableId() {
		return mTableId;
	}
	public static void setTablekId(int tableId) {
		Info.mTableId = tableId;
	}

	public static String getTableName() {
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
}
