package com.htb.cnk.data;

/**
 * @author josh
 *
 */
public class Dish {
	private int mId;
	private String mName;
	private double mPrice;
	private String mPic;
	
	public Dish(int id, String name, double price, String pic) {
		mId = id;
		mName = name;
		mPrice = price;
		mPic = pic;
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
	
}