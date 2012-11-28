package com.htb.cnk;

import java.util.Calendar;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

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
			mStart.setTimeInMillis(System.currentTimeMillis());
			mStart.set(Calendar.HOUR_OF_DAY, 0);
			mStart.set(Calendar.MINUTE, 0);
			mEnd.setTimeInMillis(System.currentTimeMillis());
			
			getResult();
		}
	};

	OnClickListener queryByTimeClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isDateTimeSet()) {
				getResult();
			}
		}
	};

	private void getResult() {
		int ret = mStatistics.perpareStatisticsByCategory(mStart, mEnd);
		if (ret < 0) {
			popUpDlg("错误", "销售数据出错,需从新下载!", true);
			return;
		}
		updateData(mStart, mEnd);
		mQueryMode = QUERY_TODAY;
	}
	
	
}
