package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Categories {
	public class Category {
		public String mTableName;
		public String mName;
		
		public Category(String tableName, String name) {
			mTableName = tableName;
			mName = name;
		}
	}
	
	private CnkDbHelper mCnkDbHelper;
	private SQLiteDatabase mDb;
	List<Category> mCategories = new ArrayList<Category>();
	
	public Categories(Context context){
		mCnkDbHelper = new CnkDbHelper(context, CnkDbHelper.DATABASE_NAME, null, 1);
		getCategoriesData();
	}
	
	public int count() {
		return mCategories.size();
	}
	
	public String getName(int position) {
		return mCategories.get(position).mName;
	}
	
	public String getTableName(int position) {
		return mCategories.get(position).mTableName;
	}
	
	private void getCategoriesData() {
		mDb = mCnkDbHelper.getReadableDatabase();
		Cursor categories = mDb.query(CnkDbHelper.TABLE_CATEGORIES, new String[] {CnkDbHelper.CATEGORY_NAME,
				CnkDbHelper.CATEGORY_TABLE_NAME},
				null, null, null, null, null);
		while (categories.moveToNext()) {
			mCategories.add(new Category(categories.getString(1), categories.getString(0)));
		} 
		mDb.close();
	}

	private void test() {
		
	}
}
