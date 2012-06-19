package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Dishes {
	private List<Dish> mDishes = new ArrayList<Dish>();
	private int mSoldOutItemsId[];
	private String mTableName = "";
	private CnkDbHelper mCnkDbHelper;
	private SQLiteDatabase mDb;
	
	final static int ID_COLUMN = 0;
	final static int NAME_COLUMN = 1;
	final static int PRICE_COLUMN = 2;
	final static int PIC_COLUMN = 3;
	
	public Dishes(Context context) {
		mCnkDbHelper = new CnkDbHelper(context, CnkDbHelper.DATABASE_NAME, null, 1);
		mDb = mCnkDbHelper.getReadableDatabase();
	}
	
	public int setCategory(String tableName) {
		if (mTableName.equals(tableName)) {
			return 0;
		}
		mTableName = tableName;
		mDishes.clear();
		fillCategoriesData();
		return 0;
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
//		test(id);
		Cursor dishes = getDishesFromDataBase(mTableName);
		while (dishes.moveToNext()) {
			mDishes.add(new Dish(dishes.getInt(ID_COLUMN),
								dishes.getString(NAME_COLUMN),
								dishes.getDouble(PRICE_COLUMN),
								dishes.getString(PIC_COLUMN)));
		}
//		removeSoldOutItems(id);
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
			for (Dish item:mDishes) {
				if (item.getId() == i) {
					mDishes.remove(item);
				}
			}
		}
		return 0;
	}
	
	private int getSoldItemsFromSever(int id) {
		return 0;
	}
	
	private void test(int id) {
		switch(id) {
		case 0:
			mDishes.add(new Dish(0,"菜1",1.0,"1"));
			mDishes.add(new Dish(1,"菜2",1.0,"2"));
			mDishes.add(new Dish(2,"菜3",1.0,"3"));
			mDishes.add(new Dish(3,"菜4",1.0,"4"));
			mDishes.add(new Dish(4,"菜5",1.0,"5"));
			break;
		case 1:
			mDishes.add(new Dish(5,"菜6",1.0,"6"));
			mDishes.add(new Dish(6,"菜7",1.0,"7"));
			mDishes.add(new Dish(7,"菜8",1.0,"8"));
			mDishes.add(new Dish(8,"菜9",1.0,"9"));
			mDishes.add(new Dish(9,"菜10",1.0,"0"));
			break;
		case 2:
			mDishes.add(new Dish(10,"菜11",1.0,"a"));
			mDishes.add(new Dish(11,"菜12",1.0,"a"));
			mDishes.add(new Dish(12,"菜13",1.0,"a"));
			mDishes.add(new Dish(13,"菜14",1.0,"a"));
			mDishes.add(new Dish(14,"菜15",1.0,"a"));
			break;
		case 3:
			mDishes.add(new Dish(15,"菜16",1.0,"a"));
			mDishes.add(new Dish(16,"菜17",1.0,"a"));
			mDishes.add(new Dish(17,"菜18",1.0,"a"));
			mDishes.add(new Dish(18,"菜19",1.0,"a"));
			mDishes.add(new Dish(19,"菜20",1.0,"a"));
			break;
		case 4:
			mDishes.add(new Dish(20,"菜21",1.0,"a"));
			mDishes.add(new Dish(21,"菜22",1.0,"a"));
			mDishes.add(new Dish(22,"菜23",1.0,"a"));
			mDishes.add(new Dish(23,"菜24",1.0,"a"));
			mDishes.add(new Dish(24,"菜25",1.0,"a"));
			break;
		case 5:
			mDishes.add(new Dish(25,"菜26",1.0,"a"));
			mDishes.add(new Dish(26,"菜27",1.0,"a"));
			mDishes.add(new Dish(27,"菜28",1.0,"a"));
			mDishes.add(new Dish(28,"菜29",1.0,"a"));
			mDishes.add(new Dish(29,"菜30",1.0,"a"));
			break;
		default:
			mDishes.add(new Dish(30,"菜31",1.0,"a"));
			mDishes.add(new Dish(31,"菜32",1.0,"a"));
			mDishes.add(new Dish(32,"菜33",1.0,"a"));
			mDishes.add(new Dish(33,"菜34",1.0,"a"));
			mDishes.add(new Dish(34,"菜35",1.0,"a"));
			break;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		mDb.close();
		super.finalize();
	}
	
	
}
