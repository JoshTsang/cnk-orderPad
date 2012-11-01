package com.htb.cnk;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.htb.cnk.adapter.StatisticsAdapter;
import com.htb.cnk.data.CnkDbHelper;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.lib.Http;
import com.htb.cnk.ui.base.StatisticsBaseActivity;
import com.htb.constant.ErrorNum;
import com.htb.constant.Server;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class StuffPerformanceActivity extends StatisticsBaseActivity {
	final static String TAG = "StuffPerformanceActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		downloadUserInfo();
		setViews();
		setPerClickListener();
		setAdapter();
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
	
	private void setViews() {
		
	}
	
	private void setPerClickListener() {
		mQueryByTimeBtn.setOnClickListener(queryByTimeClicked);
		mQueryTodayBtn.setOnClickListener(queryTodayClicked);
		mPrintBtn.setOnClickListener(printClicked);
	}
	
	private void setAdapter() {
		mStatisticsAdapter = new StatisticsAdapter(getApplicationContext(),
				mStatistics) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ViewHolder viewHolder;

				if (convertView == null) {
					viewHolder = new ViewHolder();
					convertView = LayoutInflater.from(
							StuffPerformanceActivity.this).inflate(
							R.layout.item_statistics, null);
					viewHolder.dishName = (TextView) convertView
							.findViewById(R.id.dishName);
					viewHolder.salesCount = (TextView) convertView
							.findViewById(R.id.salesCount);
					viewHolder.totalAmount = (TextView) convertView
							.findViewById(R.id.totalAmount);
					viewHolder.percentage = (TextView) convertView
							.findViewById(R.id.percentage);
					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ViewHolder) convertView.getTag();
				}

				viewHolder.dishName.setText(mStatistics.getName(position));
				viewHolder.salesCount.setText(MyOrder.convertFloat(mStatistics
						.getQuantity(position)));
				viewHolder.totalAmount.setText(MyOrder.convertFloat(mStatistics
						.getAmount(position)));
				DecimalFormat df = new DecimalFormat("0.00");
				double percent = (mStatistics.getAmount(position) * 100)
						/ mStatistics.getTotalAmount();
				viewHolder.percentage.setText(df.format(percent) + "%");
				return convertView;
			}

			class ViewHolder {
				TextView dishName;
				TextView salesCount;
				TextView totalAmount;
				TextView percentage;
			}
		};
		mSalesData.setAdapter(mStatisticsAdapter);
	}
	
	OnClickListener queryTodayClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date d;
			try {
				d = df.parse(mLatestStatistics);
				mStart.setTime(d);
			} catch (ParseException e) {
				e.printStackTrace();
			}
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
	
	OnClickListener printClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mStatistics.count() <= 0) {
				Toast.makeText(getApplicationContext(), "没有可打印信息",
						Toast.LENGTH_LONG).show();
				return;
			}
			showProgressDlg("正在上传打印信息...");
			new Thread() {
				public void run() {
					int ret = mStatistics.print(mStart, mEnd);
					handlerPrint.sendEmptyMessage(ret);
				}
			}.start();

			if (mQueryMode == QUERY_TODAY) {
				updateLatestStatistics();
			}
		}
	};
}
