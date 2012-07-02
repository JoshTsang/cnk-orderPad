package com.htb.constant;

public class Server {
	public final static String SERVER_DOMIN = "http://192.168.1.1";
	
	/* php */
	public final static String PHP_DIR = "orderPad/";
	public final static String GET_DISH_STATUS = PHP_DIR + "getDishStatus.php";
	public final static String SUBMIT_ORDER = PHP_DIR + "submitOrder.php";
	public final static String LATEST_STATISTICS = PHP_DIR + "latestStatistics.php";
	public final static String GET_MYORDER = PHP_DIR + "getToDelOrder.php";
	public final static String DEL_ORDER = PHP_DIR + "DelOrder.php";
	public final static String GET_TABLE_STATUS = PHP_DIR + "getTableStatus.php";
	public final static String UPDATE_TABLE_STATUS = PHP_DIR + "updateTableStatus.php";
	public final static String CLEAN_TABLE= PHP_DIR + "cleanTable.php";
	public final static String GET_PWD = PHP_DIR + "getPwd.php";
	/* Database */
	public final static String DB_DIR = "db/";
	public final static String DB_MENU = DB_DIR + "menu.db";
	public final static String DB_SALES = DB_DIR + "sales.db";
	
	/* other */
	public final static String IMG_PATH = Server.SERVER_DOMIN + "/jpeg/";
}