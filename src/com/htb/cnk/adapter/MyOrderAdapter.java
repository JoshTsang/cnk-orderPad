package com.htb.cnk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.htb.cnk.R;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.MyOrder.OrderedDish;

public class MyOrderAdapter extends BaseAdapter {
	private Context mContext;
	private MyOrder mMyOrder;
	
	public MyOrderAdapter(Context context, MyOrder order) {
		mContext = context;
		mMyOrder = order;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mMyOrder.count();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mMyOrder.getOrderedDish(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		return null;
	}
}
