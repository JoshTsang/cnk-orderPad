package com.htb.cnk;

import java.util.Calendar;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

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
			
			int ret = mStatistics.perpareResult(mStart, mEnd);
			if (ret < 0) {
				popUpDlg("错误", "销售数据出错,需从新下载!", true);
				return;
			}
			updateData(mStart, mEnd);
			mQueryMode = QUERY_TODAY;
		}
	};

	private OnClickListener queryByTimeClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (isDateTimeSet()) {
				int ret = mStatistics.perpareResult(mStartSet, mEndSet);
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
