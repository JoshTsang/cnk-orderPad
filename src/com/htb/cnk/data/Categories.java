package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;

import com.htb.constant.ErrorNum;

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
		public String mName;
		
		public Category(String name, int id) {
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
	
	public int getCategoryId(int position) {
		return mCategories.get(position).mCategoryId;
	}
	
	private int getCategoriesData() {
		mDb = mCnkDbHelper.getReadableDatabase();
		final int CATEGORY_ID = 0;
		final int CATEGORY_NAME = 1;
		
		try {
			Cursor categories = mDb.query(CnkDbHelper.TABLE_CATEGORIES, 
					new String[] {CnkDbHelper.CATEGORY_ID, CnkDbHelper.CATEGORY_NAME
					}, null, null, null, null, null);
			while (categories.moveToNext()) {
				mCategories.add(new Category(categories.getString(CATEGORY_NAME),
						categories.getInt(CATEGORY_ID)));
			} 
		} catch (Exception e) {
			e.printStackTrace();
			return ErrorNum.DB_BROKEN;
		}
		mDb.close();
		return 0;
	}
}
