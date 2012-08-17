package com.htb.cnk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
		new Thread(new queryThread()).start();
	}

	private void setQueryViews() {
		mSubmitBtn.setVisibility(View.GONE);
		mLeftBtn.setVisibility(View.GONE);
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

				return convertView;
			}
		};
		mMyOrderLst.setAdapter(mMyOrderAdapter);
	}


	Handler queryHandler = new Handler() {
		public void handleMessage(Message msg) {
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
			Message msg = new Message();
			try {
				int ret = mMyOrder.getTableFromDB(Info.getTableId());
				msg.what = ret;
				queryHandler.sendMessage(msg);
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

}
