package com.htb.cnk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * @author josh
 *
 */
public class CnkDbHelper extends SQLiteOpenHelper {
	public final static String DATABASE_NAME = "cnk.db";
	public final static String DB_MENU = "cnk.db";
	public final static String DB_SALES = "sales.db";
	public final static String DB_USER = "user.db";
	
	public final static String TABLE_CATEGORIES = "category";
	
	public final static String CATEGORY_ID = "categoryID";
	public final static String CATEGORY_NAME = "categoryName";
	
	public final static String TABLE_DISH_INFO = "dishInfo";
	public final static String DISH_ID = "id";
	public final static String DISH_NAME = "name";
	public final static String DISH_PRICE = "price";
	public final static String DISH_PIC = "pictureBUrl";
	public final static String DISH_PRINTER = "sortPrint";
	
	public final static String TABLE_DISH_CATEGORY = "dishCategory";
	public final static String DC_DISH_ID = "dishId";
	public final static String DC_CATEGORY_ID = "categoryID";
	
	public final static String TABLE_INFO = "table_info";
	public final static String TABLE_ID = "table_id";
	public final static String TABLE_NAME = "table_name";
	public final static String TABLE_STATUS = "status";
	
	public final static String USER_TABLE = "userInfo";
	public final static String USER_ID = "id";
	public final static String USER_NAME = "username";
	public final static String SALES_DATA = "sales_data";
	
	public CnkDbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table a (id integer primary key autoincrement);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
	}
}
