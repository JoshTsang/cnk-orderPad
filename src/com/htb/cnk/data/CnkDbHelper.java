package com.htb.cnk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class CnkDbHelper extends SQLiteOpenHelper {
	public final static String DATABASE_NAME = "cnk.db";
	
	public final static String DISH_ID = "id";
	public final static String DISH_NAME = "";
	public final static String DISH_PRICE = "";
	public final static String DISH_PIC = "";
	
	public CnkDbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("create table a (id integer primary key autoincrement);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
}
