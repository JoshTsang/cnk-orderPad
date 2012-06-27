package com.htb.cnk.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.htb.cnk.data.Statistics;

public class StatisticsAdapter extends BaseAdapter {
	private Statistics mStatistics;
	
	public StatisticsAdapter(Context context, Statistics statistics) {
		mStatistics = statistics;
	}
	
	@Override
	public int getCount() {
		return mStatistics.count();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
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
