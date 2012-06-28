package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * @author josh
 *
 */
public class Categories {
	public class Category {
		public int mCategoryId;
		public String mTableName;
		public String mName;
		
		public Category(String tableName, String name, int id) {
			mTableName = tableName;
			mName = name;
			mCategoryId = id;
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
	
	public int getCategoryId(int position) {
		return mCategories.get(position).mCategoryId;
	}
	
	private void getCategoriesData() {
		mDb = mCnkDbHelper.getReadableDatabase();
		final int CATEGORY_ID = 0;
		final int CATEGORY_NAME = 1;
		final int CATEGORY_TABLE_NAME = 2;
		Cursor categories = mDb.query(CnkDbHelper.TABLE_CATEGORIES, 
				new String[] {CnkDbHelper.CATEGORY_ID, CnkDbHelper.CATEGORY_NAME,
				CnkDbHelper.CATEGORY_TABLE_NAME},
				null, null, null, null, null);
		while (categories.moveToNext()) {
			mCategories.add(new Category(categories.getString(CATEGORY_TABLE_NAME),
					categories.getString(CATEGORY_NAME),
					categories.getInt(CATEGORY_ID)));
		} 
		mDb.close();
	}
}
