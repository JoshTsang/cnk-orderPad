package com.htb.cnk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.htb.cnk.data.Dishes;

public class DishListAdapter extends BaseAdapter {
	private Dishes mDishes;
	
	public DishListAdapter(Context context, Dishes dishes) {
		mDishes = dishes;
	}
	
	@Override
	public int getCount() {
		return mDishes.count();
	}

	@Override
	public Object getItem(int arg0) {
		return mDishes.getDish(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return mDishes.getDishId(arg0);
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		return null;
	}

	

}
