package com.htb.cnk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.htb.cnk.data.PadOrder;

/**
 * @author josh
 *
 */
public class PadOrderAdapter extends BaseAdapter {
	private PadOrder mPadOrder;
	
	public PadOrderAdapter(Context context, PadOrder order) {
		mPadOrder = order;
	}
	
	@Override
	public int getCount() {
		return mPadOrder.count();
	}

	@Override
	public Object getItem(int arg0) {
		return mPadOrder.getOrderedDish(arg0);
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
