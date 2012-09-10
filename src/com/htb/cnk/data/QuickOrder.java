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
import android.util.Log;

import com.htb.cnk.lib.HanziHelper;
import com.htb.constant.ErrorNum;

public class QuickOrder {
	private CnkDbHelper mCnkDbHelper;
	private SQLiteDatabase mDb;
	private Context mContext;

	final static int ID_COLUMN = 0;
	final static int NAME_COLUMN = 1;
	final static int PRICE_COLUMN = 2;
	final static int PIC_COLUMN = 3;

	// public class DishesNameItem {
	// private String mName;
	// private String mNamePinyin;
	// private int mId;
	//
	// public DishesNameItem(String namePinyin, String name, int id) {
	// this.mName = name;
	// this.mNamePinyin = HanziHelper.words2Pinyin(name);
	// mId = id;
	// }
	//
	// public String getName() {
	// return mName;
	// }
	//
	// public int getId() {
	// return mId;
	// }
	//
	// public String getNamePinYin() {
	// return mNamePinyin;
	// }
	// }

	private static List<Map<String, Object>> QuickTotalItems = new ArrayList<Map<String, Object>>();
	private static List<Dish> mDishes = new ArrayList<Dish>();

	public QuickOrder(Context context) {
		mContext = context;
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
				Log.d("QuickTotalItem", "1");
			} else {
				retName += tempName[i] + "[\\s\\S]*";
				Log.d("QuickTotalItem", "2");
			}
		}
		ret = match(retName);
		Log.d("retName", retName);
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
				Log.d("QuickTotalItem.keySet", "aa"
						+ QuickTotalItems.get(i).keySet().toString());
				mDishes.add((Dish) QuickTotalItems.get(i).get(temp));
				ret = 0;
			}
		}
		return ret;
	}

	public List<Dish> getListDish() {
		return mDishes;
	}

	// public String getName(int posi{}tion) {
	// return items.get("a");
	// }
	//
	// public void setName(String name) {
	// this.mName = name;
	// this.mNamePinyin = HanziHelper.words2Pinyin(name);
	// }
	//
	// public String getNamePinyin() {
	// return mNamePinyin;
	// }
	//
	// public void setNamePinyin(String namePinyin) {
	// this.mNamePinyin = namePinyin;
	// }
	//
	// public int compareTo(DishesName another) {
	// return mNamePinyin.compareToIgnoreCase(another.mNamePinyin);
	// }
	//
	// public String toString() {
	// return mName;
	// }

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
			Cursor dishes = getDishesFromDataBase(CnkDbHelper.DISH_TABLE_NAME);
			while (dishes.moveToNext()) {
				Map<String, Object> map = new HashMap<String, Object>();
				Dish dish = new Dish(dishes.getInt(ID_COLUMN),
						dishes.getString(NAME_COLUMN),
						dishes.getDouble(PRICE_COLUMN),
						dishes.getString(PIC_COLUMN));
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

	private Cursor getDishesFromDataBase(String tableName) {
		return mDb.query(tableName, new String[] { CnkDbHelper.DISH_ID,
				CnkDbHelper.DISH_NAME, CnkDbHelper.DISH_PRICE,
				CnkDbHelper.DISH_PIC }, null, null, null, null, null);
	}

	@Override
	protected void finalize() throws Throwable {
		if (mDb != null) {
			mDb.close();
		}
		super.finalize();
	}
}