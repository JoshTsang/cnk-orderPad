package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;

import com.htb.cnk.lib.HanziHelper;
import com.htb.constant.ErrorNum;

public class QuickOrder extends MyOrder{

	private static List<Map<String, Object>> QuickTotalItems = new ArrayList<Map<String, Object>>();
	private static List<Dish> mMatchedDishes = new ArrayList<Dish>();
	private Dishes mDishes;

	public QuickOrder(Context context) {
		super(context);
		mDishes = new Dishes(context);
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
		mMatchedDishes.clear();
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
				mMatchedDishes.add((Dish) QuickTotalItems.get(i).get(temp));
				ret = 0;
			}
		}
		return ret;
	}

	public List<Dish> getListDish() {
		return mMatchedDishes;
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
			Cursor dishes = mDishes.getDishesFromDB();
			while (dishes.moveToNext()) {
				Map<String, Object> map = new HashMap<String, Object>();
				Dish dish = new Dish(dishes.getInt(Dishes.ID_COLUMN),
						dishes.getString(Dishes.NAME_COLUMN),
						dishes.getFloat(Dishes.PRICE_COLUMN),
						dishes.getString(Dishes.PIC_COLUMN),
						dishes.getString(Dishes.UNIT_NAME),
						dishes.getInt(Dishes.PRINTER_COLUMN),
						dishes.getString(Dishes.SHORTCUT));
				map.put(HanziHelper.words2Pinyin(dishes.getString(Dishes.NAME_COLUMN)),
						dish);
				QuickTotalItems.add(map);
			}
			return 0;
		} catch (Exception e) {
			e.printStackTrace();
			return ErrorNum.DB_BROKEN;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (mDb != null) {
			mDb.close();
		}
		super.finalize();
	}
}
