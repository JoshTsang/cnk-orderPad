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
	
	public Dish(int id, String name, float price, String pic, int printer) {
		mId = id;
		mName = name;
		mPrice = price;
		mPic = pic;
		mPrinter = printer;
	}
	
	public Dish(int id, String name, float price, String pic) {
		mId = id;
		mName = name;
		mPrice = price;
		mPic = pic;
		mPrinter = 0;
	}

	public int getId() {
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
	
	public int getPrinter() {
		return mPrinter;
	}
}