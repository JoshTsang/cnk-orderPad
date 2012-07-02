package com.htb.cnk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.MyOrder.OrderedDish;

public class DelOrderActivity extends Activity {
	private Button mBackBtn;
	private Button mDelBtn;
	//private Button mContinueBtn;
	private TextView mTableNumTxt;
	private TextView mDishCountTxt;
	private TextView mTotalPriceTxt;
	private ListView mMyOrderLst;
	private MyOrder mMyOrder;
	private MyOrderAdapter mMyOrderAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.del_activity);
		findViews();
		updateTabelInfos();
		setClickListener();
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back_btn);
		mDelBtn = (Button) findViewById(R.id.cleanAll_btn);
		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);

	}

	private void fillData() {
		mTableNumTxt.setText(Info.getTableName());
		mDishCountTxt.setText(Integer.toString(mMyOrder.count())
				+ " 道菜");
		mTotalPriceTxt
				.setText(Double.toString(mMyOrder.getTotalPrice())
						+ " 元");
		mMyOrderAdapter = new MyOrderAdapter(this, mMyOrder) {
			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				TextView dishName;
				TextView dishPrice;
				TextView dishQuantity;
				Button minusBtn;
				

				if (convertView == null) {
					convertView = LayoutInflater.from(DelOrderActivity.this)
							.inflate(R.layout.item_delorder, null);
				}
				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);

				dishName = (TextView) convertView.findViewById(R.id.dishName);
				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
				dishQuantity = (TextView) convertView
						.findViewById(R.id.dishQuantity);
				minusBtn = (Button) convertView.findViewById(R.id.dishMinus);
				
				dishName.setText(dishDetail.getName());
				dishPrice.setText(Double.toString(dishDetail.getPrice())
						+ " 元/份");
				dishQuantity
						.setText(Integer.toString(dishDetail.getQuantity()));

				minusBtn.setTag(position);
				minusBtn.setOnClickListener(minusClicked);

				return convertView;
			}
		};

		mMyOrderLst.setAdapter(mMyOrderAdapter);

	}

	private void setClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
		mDelBtn.setOnClickListener(cleanBtnClicked);
	
	}

	private void updateDishQuantity(int position, int quantity) {
		final int dId = position;
		new Thread() {
			public void run() {
				try {
					Message msg = new Message();						
					int ret = 1;
					ret = mMyOrder.del(dId);
					if (ret < 0) {
						myHandler.sendEmptyMessage(ret);
						return ;
					}
					msg.what = ret;
					myHandler.sendMessage(msg);
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}.start();
		mMyOrder.minus(position, -quantity);
		fillData();
		mMyOrderAdapter.notifyDataSetChanged();
	}

	private void updateTabelInfos() {
		mMyOrder = new MyOrder(DelOrderActivity.this);
		mMyOrder.clear();
		new Thread(new delThread()).start();
	}

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();
			} else {	
				fillData();
				mMyOrderAdapter.notifyDataSetChanged();
			}
		}
	};

	class delThread implements Runnable {
		public void run() {
			Message msg = new Message();
			try {
				int ret = mMyOrder.getTableFromDB(Info.getTableId());
				msg.what = ret;
				myHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
	

	

	private void minusDishQuantity(final int position, final int quantity) {
		
			new AlertDialog.Builder(DelOrderActivity.this)
					.setTitle("请注意")
					.setMessage(
							"确认删除"
									+ mMyOrder.getOrderedDish(position)
											.getName())
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									updateDishQuantity(position, -quantity);
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							}).show();
		
	}

	private OnClickListener backBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			DelOrderActivity.this.finish();
		}
	};

	private OnClickListener cleanBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mMyOrder.clear();
			mMyOrderAdapter.notifyDataSetChanged();
			fillData();
		}
	};

	

	private OnClickListener minusClicked = new OnClickListener() {

		public void onClick(View v) {
			final int position = Integer.parseInt(v.getTag().toString());
			minusDishQuantity(position, 1);
		}
	};

}