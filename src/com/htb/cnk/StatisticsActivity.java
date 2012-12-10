package com.htb.cnk;

import java.util.Calendar;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.htb.cnk.data.Statistics;
import com.htb.cnk.ui.base.StatisticsBaseActivity;


/**
 * @author josh
 * 
 */
public class StatisticsActivity extends StatisticsBaseActivity {
	public final static String TAG = "StatisticsActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setStatisticClickListener();
		
	}
	
	
	
	private void setStatisticClickListener() {
		mQueryByTimeBtn.setOnClickListener(queryByTimeClicked);
		mQueryTodayBtn.setOnClickListener(queryTodayClicked);
		mPrintBtn.setOnClickListener(printClicked);
	}
	
	private OnClickListener queryTodayClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mStart.setTimeInMillis(System.currentTimeMillis());
			mStart.set(Calendar.HOUR_OF_DAY, 0);
			mStart.set(Calendar.MINUTE, 0);
			mEnd.setTimeInMillis(System.currentTimeMillis());
			showProgressDlg("正在查询销售信息...");
			new Thread() {
				public void run(){
					String ret = mStatistics.loadStatisticsResultJson(mStart, mEnd, Statistics.BY_DISH);
					Message msg = new Message();
					msg.what = Statistics.BY_DISH;
					msg.obj = ret;
					handleDataLoad.sendMessage(msg);
					mQueryMode = QUERY_TODAY;
				}
			}.start();
		}
	};

	private OnClickListener queryByTimeClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isDateTimeSet()) {
				showProgressDlg("正在查询销售信息...");
				new Thread() {
					public void run(){
						String ret = mStatistics.loadStatisticsResultJson(mStartSet, mEndSet, Statistics.BY_DISH);
						Message msg = new Message();
						msg.what = Statistics.BY_DISH;
						msg.obj = ret;
						handleDataLoad.sendMessage(msg);
						mQueryMode = QUERY_BY_TIME;
					}
				}.start();
			}
		}
	};
	
	
}
