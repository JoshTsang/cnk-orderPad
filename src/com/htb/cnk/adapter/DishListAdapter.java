package com.htb.cnk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.htb.cnk.data.Dishes;

public class DishListAdapter extends BaseAdapter {
	private Context mContext;
	private Dishes mDishes;
	
	public DishListAdapter(Context context, Dishes dishes) {
		mContext = context;
		mDishes = dishes;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDishes.count();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mDishes.getDish(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return mDishes.getDishId(arg0);
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
