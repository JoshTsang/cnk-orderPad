package com.htb.cnk;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder.OrderedDish;
import com.htb.cnk.lib.OrderBaseActivity;

public class QueryOrderActivity extends OrderBaseActivity {
	private MyOrderAdapter mMyOrderAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setQueryViews();
		showProgressDlg("请稍候...");
		new Thread(new queryThread()).start();
	}

	private void setQueryViews() {
		mSubmitBtn.setText("上菜");
		mLeftBtn.setVisibility(View.GONE);
		mRefreshBtn.setVisibility(View.GONE);
		
		mSubmitBtn.setOnClickListener(submitClicked);
	}

	private void setAdapter() {
		mMyOrderAdapter = new MyOrderAdapter(this, mMyOrder) {
			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				TextView dishName;
				TextView dishPrice;
				TextView dishQuantity;
				if (convertView == null) {
					convertView = LayoutInflater.from(QueryOrderActivity.this)
							.inflate(R.layout.item_queryorder, null);
				}
				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);

				dishName = (TextView) convertView.findViewById(R.id.dishName);
				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
				dishQuantity = (TextView) convertView
						.findViewById(R.id.dishQuantity);

				dishName.setText(dishDetail.getName());
				
				dishPrice.setText(Double.toString(dishDetail.getPrice())
						+ " 元/份");
				dishQuantity
						.setText(Integer.toString(dishDetail.getQuantity()));
				if (dishDetail.getStatus() == 2) {
					int color = Color.rgb(255, 0, 0);
					dishName.setTextColor(color);
					dishPrice.setTextColor(color);
					dishQuantity.setTextColor(color);
				} else {
					int color = Color.rgb(0, 0, 0);
					dishName.setTextColor(color);
					dishPrice.setTextColor(color);
					dishQuantity.setTextColor(color);
				}
				return convertView;
			}
		};
		mMyOrderLst.setAdapter(mMyOrderAdapter);
	}


	Handler queryHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();
			} else {
				setAdapter();
				mMyOrderAdapter.notifyDataSetChanged();
				updateTabelInfos();
			}
		}
	};

	class queryThread implements Runnable {
		public void run() {
			try {
				int ret = mMyOrder.getOrderFromServer(Info.getTableId());
				queryHandler.sendEmptyMessage(ret);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void finish() {
		mMyOrder.clear();
		super.finish();
	}
	
	private OnClickListener submitClicked = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(QueryOrderActivity.this, ServeOrderActivity.class);
				startActivity(intent);
				finish();
		}

	};
}
