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
		int ret = mStatistics.perpareStatisticsByPrinter(mStart, mEnd);
		if (ret < 0) {
			popUpDlg("错误", "销售数据出错,需从新下载!", true);
			return;
		}
		updateData(mStart, mEnd);
		mQueryMode = QUERY_TODAY;
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
