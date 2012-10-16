package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.htb.cnk.lib.HanziHelper;
import com.htb.constant.ErrorNum;

public class QuickOrder extends MyOrder{
	final static int ID_COLUMN = 0;
	final static int NAME_COLUMN = 1;
	final static int PRICE_COLUMN = 2;
	final static int PIC_COLUMN = 3;
	final static int PRINTER_COLUMN = 4;
	final static int UNIT_NAME = 5;

	private static List<Map<String, Object>> QuickTotalItems = new ArrayList<Map<String, Object>>();
	private static List<Dish> mDishes = new ArrayList<Dish>();

	public QuickOrder(Context context) {
		super(context);
	}

	public int setQucik() {
		QuickTotalItems.clear();
		int ret = fillQuickOrderData();
		if (ret < 0) {
			return ret;
		}
		return ret;
	}

	public int queryDish(String orderName) {
		int ret = 0;
		orderName= orderName.trim();
		char[] tempName = orderName.toCharArray(); 
		String retName = "";
		mDishes.clear();
		for (int i = 0; i < tempName.length; i++) {
			if (i == tempName.length - 1) {
				retName += tempName[i] + "[\\s\\S]*";
			} else {
				retName += tempName[i] + "[\\s\\S]*";
			}
		}
		ret = match(retName);
		return ret;
	}

	public int match(String orderName) {
		int ret = -1;
		String temp = null;
		for (int i = 0; i < QuickTotalItems.size(); i++) {
			temp = QuickTotalItems
					.get(i)
					.keySet()
					.toString()
					.substring(
							1,
							(QuickTotalItems.get(i).keySet().toString()
									.length() - 1));
			Pattern p = Pattern.compile(orderName);
			Matcher m = p.matcher(temp);
			if (m.matches()) {
				mDishes.add((Dish) QuickTotalItems.get(i).get(temp));
				ret = 0;
			}
		}
		return ret;
	}

	public List<Dish> getListDish() {
		return mDishes;
	}

	private int fillQuickOrderData() {
		if (mCnkDbHelper == null) {
			try {
				mCnkDbHelper = new CnkDbHelper(mContext, CnkDbHelper.DB_MENU,
						null, 1);
				mDb = mCnkDbHelper.getReadableDatabase();
			} catch (Exception e) {
				e.printStackTrace();
				return ErrorNum.DB_BROKEN;
			}
		}
		try {
			Cursor dishes = getDishesFromDB();
			while (dishes.moveToNext()) {
				Map<String, Object> map = new HashMap<String, Object>();
				Dish dish = new Dish(dishes.getInt(ID_COLUMN),
						dishes.getString(NAME_COLUMN),
						dishes.getFloat(PRICE_COLUMN),
						dishes.getString(PIC_COLUMN),
						dishes.getString(UNIT_NAME),
						dishes.getInt(PRINTER_COLUMN));
				map.put(HanziHelper.words2Pinyin(dishes.getString(NAME_COLUMN)),
						dish);
				QuickTotalItems.add(map);
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return ErrorNum.DB_BROKEN;
		}
	}

	private Cursor getDishesFromDB() {
		String sql = String
				.format("SELECT %s.%s, %s, %s, %s, %s, %s FROM %s, %s Where %s.%s=%s",
						CnkDbHelper.TABLE_DISH_INFO, CnkDbHelper.DISH_ID,
						CnkDbHelper.DISH_NAME,
						CnkDbHelper.DISH_PRICE,
						CnkDbHelper.DISH_PIC,
						CnkDbHelper.DISH_PRINTER,
						CnkDbHelper.UNIT_NAME,
						CnkDbHelper.TABLE_DISH_INFO,
						CnkDbHelper.TABLE_UNIT,
						CnkDbHelper.TABLE_UNIT, "id", CnkDbHelper.UNIT_ID);
		return mDb.rawQuery(sql, null);
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (mDb != null) {
			mDb.close();
		}
		super.finalize();
	}
}
