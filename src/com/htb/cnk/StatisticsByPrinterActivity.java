package com.htb.cnk;

import java.util.Calendar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.htb.cnk.lib.Http;
import com.htb.constant.ErrorNum;
import com.htb.constant.Server;

public class StatisticsByPrinterActivity extends StatisticsActivity {
	final static String TAG = "StatisticsByCategoryActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadPrinterSetting();
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

	private void getResult(int queryMode) {
		int ret = mStatistics.perpareStatisticsByPrinter(mStartSet, mEndSet);
		if (ret < 0) {
			popUpDlg("错误", "销售数据出错,需从新下载!", true);
			return;
		}
		updateData(mStartSet, mEndSet);
		mQueryMode = queryMode;
	}
	
	private void loadPrinterSetting() {
		new Thread() {
			public void run() {
				String respond = Http.get(Server.PRINTER_SETTING, "");
				if (respond == null) {
					handler.sendEmptyMessage(ErrorNum.GET_LATEST_STATISTICS_FAILED);
					Log.e(TAG, "get latest statistics time failed");
					return;
				}
				int start = respond.indexOf("[") + 1;
				if (start != 1) {
					handler.sendEmptyMessage(ErrorNum.GET_LATEST_STATISTICS_FAILED);
					return;
				}
				mStatistics.setPrinterInfo(respond);
			}
		}.start();
	}
	
}
