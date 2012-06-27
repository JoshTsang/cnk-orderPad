package com.htb.cnk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.htb.cnk.data.MyOrder;

public class MyOrderAdapter extends BaseAdapter {
	private MyOrder mMyOrder;
	
	public MyOrderAdapter(Context context, MyOrder order) {
		mMyOrder = order;
	}
	
	@Override
	public int getCount() {
		return mMyOrder.count();
	}

	@Override
	public Object getItem(int arg0) {
		return mMyOrder.getOrderedDish(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		return null;
	}
}
