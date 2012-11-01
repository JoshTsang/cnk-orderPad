package com.htb.cnk;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.htb.cnk.adapter.StatisticsAdapter;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.ui.base.StatisticsBaseActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;


/**
 * @author josh
 * 
 */
public class StatisticsActivity extends StatisticsBaseActivity {
	final String TAG = "StatisticsActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setStatisticClickListener();
		setAdapter();
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
							StatisticsActivity.this).inflate(
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
	
	private void setStatisticClickListener() {
		mQueryByTimeBtn.setOnClickListener(queryByTimeClicked);
		mQueryTodayBtn.setOnClickListener(queryTodayClicked);
		mPrintBtn.setOnClickListener(printClicked);
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
			
			int ret = mStatistics.perpareResult(mStart, mEnd);
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
