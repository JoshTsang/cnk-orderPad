package com.htb.constant;

public class Server {
	public final static String SERVER_DOMIN = "http://192.168.1.1";
	
	/* php */
	public final static String PHP_DIR = "orderPad/";
	public final static String GET_DISH_STATUS = PHP_DIR + "getDishStatus.php";
	public final static String SUBMIT_ORDER = PHP_DIR + "submitOrder.php";
	public final static String LATEST_STATISTICS = PHP_DIR + "latestStatistics.php";
	
	/* Database */
	public final static String DB_DIR = "db/";
	public final static String DB_MENU = DB_DIR + "menu.db";
	public final static String DB_SALES = DB_DIR + "sales.db";
	
	/* other */
	public final static String IMG_PATH = Server.SERVER_DOMIN + "/jpeg/";
}
