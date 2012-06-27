package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.htb.cnk.lib.Http;
import com.htb.constant.ErrorNum;
import com.htb.constant.Server;

public class Dishes {
	
	private List<Dish> mDishes = new ArrayList<Dish>();
	private List<Integer> mSoldOutItemsId = new ArrayList<Integer>();
	private int mCategoryId;
	private String mTableName = "";
	private CnkDbHelper mCnkDbHelper;
	private SQLiteDatabase mDb;
	
	final static int ID_COLUMN = 0;
	final static int NAME_COLUMN = 1;
	final static int PRICE_COLUMN = 2;
	final static int PIC_COLUMN = 3;
	
	public Dishes(Context context) {
		mCnkDbHelper = new CnkDbHelper(context, CnkDbHelper.DATABASE_NAME,
				null, 1);
		mDb = mCnkDbHelper.getReadableDatabase();
	}
	
	public int setCategory(int categoryId, String tableName) {
		mCategoryId = categoryId;
		mTableName = tableName;
		mDishes.clear();
		fillCategoriesData();
		int ret = removeSoldOutItems(mCategoryId);
		return ret;
	}
	
	public int count() {
		return mDishes.size();
	}
	
	public Dish getDish(int position) {
		return mDishes.get(position);
	}
	
	public int getDishId(int position) {
		return mDishes.get(position).getId();
	}
	
	public void clear() {
		mDishes.clear();
	}
	
	private void fillCategoriesData() {
		Cursor dishes = getDishesFromDataBase(mTableName);
		while (dishes.moveToNext()) {
			mDishes.add(new Dish(dishes.getInt(ID_COLUMN),
								dishes.getString(NAME_COLUMN),
								dishes.getDouble(PRICE_COLUMN),
								dishes.getString(PIC_COLUMN)));
		}
	}
	
	private Cursor getDishesFromDataBase(String tableName) {
		return mDb.query(tableName, new String[] {CnkDbHelper.DISH_ID,
												  CnkDbHelper.DISH_NAME,
												  CnkDbHelper.DISH_PRICE,
												  CnkDbHelper.DISH_PIC},
							null, null, null, null, null);
	}
	
	private int removeSoldOutItems(int id) {
		int ret;
		ret = getSoldItemsFromSever(id);
		if (ret < 0) {
			return ret;
		}
		
		for (int i:mSoldOutItemsId) {
			Iterator<Dish> iterator = mDishes.iterator();
			while(iterator.hasNext()) {
				Dish d = iterator.next();
				if (d.getId() == i) {
					iterator.remove();
				}
			}
		}
		return 0;
	}
	
	private int getSoldItemsFromSever(int id) {
		String dishStatusPkg = 
				Http.get(Server.GET_DISH_STATUS,
						"CID=" + Integer.toString(id));
		
		if (dishStatusPkg == null) {
			return ErrorNum.GET_SOLDOUT_ITEM_FAILED;
		}
		Log.d("DISHES", dishStatusPkg);
		int start = dishStatusPkg.indexOf("[");
		int end = dishStatusPkg.indexOf("]");
		if ((start < 0) || (end < 0)) {
			return ErrorNum.GET_SOLDOUT_ITEM_FAILED;
		}

		mSoldOutItemsId.clear();
		
		String dishStatus = dishStatusPkg.subSequence(start+1, end).toString();
		if (dishStatus.length() <= 0) {
			return 0;
		}
		
		String dishStatusArray[] = dishStatus.split(",");
		for (int i=dishStatusArray.length-1; i>=0; i--) {
			mSoldOutItemsId.add(Integer.parseInt(dishStatusArray[i]));
		}
		return 0;
	}

	@Override
	protected void finalize() throws Throwable {
		mDb.close();
		super.finalize();
	}
	
	
}
