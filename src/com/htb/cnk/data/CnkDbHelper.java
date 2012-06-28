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
	
	public final static String TABLE_CATEGORIES = "table_show";
	
	public final static String CATEGORY_ID = "id";
	public final static String CATEGORY_NAME = "class";
	public final static String CATEGORY_TABLE_NAME = "tablename";
	
	public final static String DISH_TABLE_NAME = "total_menu";
	public final static String DISH_ID = "id";
	public final static String DISH_NAME = "name";
	public final static String DISH_PRICE = "price";
	public final static String DISH_PIC = "pictureBUrl";
	
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
		// TODO Auto-generated method stub--
		
	}
}
