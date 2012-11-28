package com.htb.cnk;

import java.util.Calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.htb.cnk.data.CnkDbHelper;
import com.htb.cnk.ui.base.StatisticsBaseActivity;
import com.htb.constant.Server;

public class StuffPerformanceActivity extends StatisticsBaseActivity {
	final static String TAG = "StuffPerformanceActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		downloadUserInfo();
		setPerClickListener();
	}

	private void downloadUserInfo() {
		showProgressDlg("正在加载销售数据...");
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				int ret = mStatistics.downloadDB(Server.SERVER_DB_USER, CnkDbHelper.DB_USER);
				if (ret < 0) {
					handler.sendEmptyMessage(ret);
					Log.e(TAG, "Download sales db failed:" + ret);
					return;
				}
			}
		}.start();
	}
	
	private void setPerClickListener() {
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
			
			int ret = mStatistics.perparePerformanceResult(mStart, mEnd);
			if (ret < 0) {
				popUpDlg("错误", "销售数据出错,需从新下载!", true);
				return;
			}
			updateData(mStart, mEnd);
			mQueryMode = QUERY_TODAY;
		}
	};

	OnClickListener queryByTimeClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isDateTimeSet()) {
				int ret = mStatistics.perparePerformanceResult(mStartSet, mEndSet);
				if (ret < 0) {
					popUpDlg("错误", "销售数据出错,需从新下载!", true);
					return;
				}
				updateData(mStartSet, mEndSet);
				mQueryMode = QUERY_BY_TIME;
			}
		}
	};
		
}
