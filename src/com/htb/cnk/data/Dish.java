package com.htb.cnk.data;

/**
 * @author josh
 *
 */
public class Dish {
	private int mId;
	private String mName;
	private float mPrice;
	private String mPic;
	private int mPrinter;
	private String mUnit;
	private String mShortcut;
	
	public Dish(int id, String name, float price, String pic, String unit, int printer) {
		mId = id;
		mName = name;
		mPrice = price;
		mPic = pic;
		mPrinter = printer;
		mUnit = unit;
	}
	
	public Dish(int id, String name, float price, String pic, String unit, int printer, String shortcut) {
		mId = id;
		mName = name;
		mPrice = price;
		mPic = pic;
		mPrinter = printer;
		mUnit = unit;
		mShortcut = shortcut;
	}
	
	public Dish(int id, String name, float price, String pic, String unit) {
		mId = id;
		mName = name;
		mPrice = price;
		mPic = pic;
		mUnit = unit;
		mPrinter = 0;
	}

	public int getDishId() {
		return mId;
	}
	
	public String getName() {
		return mName;
	}
	
	public double getPrice() {
		return mPrice;
	}
	
	public String getPic() {
		return mPic;
	}
	
	public String getUnit() {
		return mUnit;
	}
	
	public String getShortcut() {
		return mShortcut;
	}
	
	public int getPrinter() {
		return mPrinter;
	}
}