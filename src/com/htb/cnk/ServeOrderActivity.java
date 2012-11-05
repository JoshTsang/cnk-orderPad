package com.htb.cnk;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.OrderedDish;
import com.htb.cnk.ui.base.OrderBaseActivity;

public class ServeOrderActivity extends OrderBaseActivity {
	private MyOrderAdapter mMyOrderAdapter;
	private PositionIndexMaping postionToIndex = new PositionIndexMaping();
	
	class PositionIndexMaping {
		List<Integer> map = new ArrayList<Integer>();
		
		void setup() {
			map.clear();
			OrderedDish dishDetail;
			for (int i=0; i<mMyOrder.count(); i++) {
				dishDetail = mMyOrder.getOrderedDish(i);
				if ((dishDetail.getQuantity()-dishDetail.getServedQuantity()) > 0) {
					map.add(i);	
				}
			}
		}
		
		int getIndex(int position) {
			return map.get(position);
		}
		
		int count() {
			return map.size();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setQueryViews();
		showProgressDlg("请稍候...");
		new Thread(new queryThread()).start();
	}

	private void setQueryViews() {
		mSubmitBtn.setVisibility(View.GONE);
		mLeftBtn.setVisibility(View.GONE);
		mRefreshBtn.setVisibility(View.GONE);
		mComment.setVisibility(View.GONE);
	}
	
	private void setAdapter() {
		postionToIndex.setup();
		mMyOrderAdapter = new MyOrderAdapter(this, mMyOrder) {
			
			@Override
			public int getCount() {
				return postionToIndex.count();
			}

			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				TextView dishName;
				TextView dishPrice;
				TextView dishQuantity;
				TextView dishServedQuantity;
				
				if (convertView == null) {
					convertView = LayoutInflater.from(ServeOrderActivity.this)
							.inflate(R.layout.item_queryorder, null);
				}
				OrderedDish dishDetail = mMyOrder.getOrderedDish(postionToIndex.getIndex(position));

				dishName = (TextView) convertView.findViewById(R.id.dishName);
				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
				dishQuantity = (TextView) convertView
						.findViewById(R.id.dishQuantity);
				dishServedQuantity = (TextView) convertView
						.findViewById(R.id.dishServedQuantity);
				
				dishName.setText(dishDetail.getName());
				dishPrice.setText(Double.toString(dishDetail.getPrice())
						+ " 元/" + dishDetail.getUnit());
				dishServedQuantity.setText(Integer.toString(dishDetail.getServedQuantity()));
				dishQuantity
						.setText(MyOrder.convertFloat(dishDetail.getQuantity()));

				return convertView;
			}
		};
		mMyOrderLst.setAdapter(mMyOrderAdapter);
		mMyOrderLst.setOnItemClickListener(servedClicked);
	}

	private Handler queryHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();
			} else {
				setAdapter();
				updateTabelInfos();
				mMyOrderAdapter.notifyDataSetChanged();
			}
		}
	};
	
	private Handler markServedHandle = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						"无法更新菜品状态",
						Toast.LENGTH_SHORT).show();
			} else {
				postionToIndex.setup();
				mMyOrderAdapter.notifyDataSetChanged();
			}
		}
	};
	
	private OnItemClickListener servedClicked = new OnItemClickListener() {
	
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
				long arg3) {
			showProgressDlg("更新服务器状态...");
			new Thread() {
				public void run() {
					int ret = mMyOrder.setDishStatus(postionToIndex.getIndex(position));
					markServedHandle.sendEmptyMessage(ret);
				}
			}.start();
			
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
		Intent intent = new Intent();
		intent.setClass(ServeOrderActivity.this, QueryOrderActivity.class);
		startActivity(intent);
		super.finish();
	}

}
