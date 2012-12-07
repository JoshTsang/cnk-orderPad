package com.htb.cnk;

import java.util.Calendar;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.htb.cnk.data.Statistics;

public class StatisticsByCategoryActivity extends StatisticsActivity {
	final static String TAG = "StatisticsByCategoryActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setClickListener();
	}
	
	private void setClickListener() {
		mQueryByTimeBtn.setOnClickListener(queryByTimeClicked);
		mQueryTodayBtn.setOnClickListener(queryTodayClicked);
		mPrintBtn.setOnClickListener(printClicked);
	}
	
	OnClickListener queryTodayClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mStartSet.setTimeInMillis(System.currentTimeMillis());
			mStartSet.set(Calendar.HOUR_OF_DAY, 0);
			mStartSet.set(Calendar.MINUTE, 0);
			mEndSet.setTimeInMillis(System.currentTimeMillis());
			
			getResult(QUERY_TODAY);
		}
	};

	OnClickListener queryByTimeClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isDateTimeSet()) {
				getResult(QUERY_BY_TIME);
			}
		}
	};

	private void getResult(final int queryMode) {
		showProgressDlg("正在查询销售信息...");
		new Thread() {
			public void run(){
				String ret = mStatistics.loadStatisticsResultJson(mStartSet, mEndSet, Statistics.BY_CATEGORY);
				Message msg = new Message();
				msg.what = Statistics.BY_CATEGORY;
				msg.obj = ret;
				handleDataLoad.sendMessage(msg);
				mQueryMode = queryMode;
			}
		}.start();
	}
	
	
}
