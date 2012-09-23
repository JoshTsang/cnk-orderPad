package com.htb.cnk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.MyOrder.OrderedDish;
import com.htb.cnk.data.Setting;

public class CheckOutActivity extends TableActivity{
	protected Button mBackBtn;
	protected Button mSubmitBtn;
	protected Button mLeftBtn;
	protected Button mRefreshBtn;
	protected Button mComment;
	protected TextView mTableNumTxt;
	protected TextView mDishCountTxt;
	protected TextView mTotalPriceTxt;
	protected TextView mReceivableText;
	protected TextView mChangeText;
	protected EditText mIncomeEdit;
	protected ListView mMyOrderLst;
	protected MyOrderAdapter mMyOrderAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkout_activity);
		mCheckOutHandler = checkOutHandler;
		mTableHandler =  tableHandler;
		checkfindViews();
		setCheckOutView();
	}

	private void checkfindViews() {
		mBackBtn = (Button) findViewById(R.id.back_btn);
		mSubmitBtn = (Button) findViewById(R.id.submit);
		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);
		mLeftBtn = (Button) findViewById(R.id.left_btn);
		mRefreshBtn = (Button) findViewById(R.id.refresh);
		mComment = (Button) findViewById(R.id.comment);
		mReceivableText = (TextView)findViewById(R.id.receivableQuan);
		mIncomeEdit = (EditText)findViewById(R.id.incomeEdit);
		mChangeText = (TextView)findViewById(R.id.changeQuan);
	}
	
	private void setCheckOutView(){
		getTableValue();
		mSubmitBtn.setText("结账");
		mLeftBtn.setVisibility(View.GONE);
		mRefreshBtn.setVisibility(View.GONE);
		mComment.setVisibility(View.GONE);
		mBackBtn.setOnClickListener(backBtnClicked);
		mSubmitBtn.setOnClickListener(submitClicked);
		mIncomeEdit.addTextChangedListener(watcher);
		updateTabelInfos();
	}
	
	Handler tableHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
		}
	};
	
	private void setAdapter() {
		mMyOrderAdapter = new MyOrderAdapter(this, mMyOrder) {
			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				TextView dishName;
				TextView dishPrice;
				TextView dishQuantity;
				TextView dishServedQuantity;
				if (convertView == null) {
					convertView = LayoutInflater.from(CheckOutActivity.this)
							.inflate(R.layout.item_checkout, null);
				}
				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);

				dishName = (TextView) convertView.findViewById(R.id.dishName);
				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
				dishServedQuantity = (TextView) convertView
						.findViewById(R.id.dishServedQuantity);
				dishQuantity = (TextView) convertView
						.findViewById(R.id.dishQuantity);

				dishName.setText(dishDetail.getName());

				dishPrice.setText(Double.toString(dishDetail.getPrice())
						+ " 元/份");
				dishQuantity
						.setText(MyOrder.convertFloat(dishDetail.getQuantity()));
				dishServedQuantity.setText(Integer.toString(dishDetail.getServedQuantity()));
				return convertView;
			}
		};
		mMyOrderLst.setAdapter(mMyOrderAdapter);
	}
	
	private void updateTabelInfos() {
		Log.d("price", "price:"+mTotalPrice);
		mReceivableText.setText(String.valueOf(mTotalPrice));
	}
	
	private void getTableValue(){
		getIntent().getDoubleExtra("price", mTotalPrice);
		Log.d("price", "price:"+mTotalPrice);
		selectedTable = intent.getIntegerArrayListExtra("tableId");
		tableName = intent.getStringArrayListExtra("tableName");
//		Log.d("getTableValue", "id0: "+selectedTable.toString()  +" tableName0: "+tableName.toString());
	}

	private OnClickListener backBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};
	
	private OnClickListener submitClicked = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			checkOut(selectedTable, tableName, mTotalPrice,
					mIncome, mChange);
		}

	};
	TextWatcher watcher = new TextWatcher() {
		String tempStr;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			tempStr = s.toString();
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			if (tempStr.length() > 0) {
				mIncome = Double.valueOf(tempStr).doubleValue();
				mChange = mIncome - mTotalPrice;
				mChangeText.setText(String.valueOf(mChange));
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

	};
	
	protected void queryThread() {
		new Thread() {
			public void run() {
				try {
					int ret = mMyOrder.getOrderFromServer(Info.getTableId());
					queryHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	Handler queryHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what == -2) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();
			} else if (msg.what == -1) {
				netWorkDialogShow("查询菜品失败，请检查连接网络重试");
			} else {
				setAdapter();
				mMyOrderAdapter.notifyDataSetChanged();
				updateTabelInfos();
			}
			
		}
	};
	
	Handler checkOutHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == -2) {
				toastText(R.string.checkOutWarning);
			} else if (msg.what == -1) {
				netWorkDialogShow("收银出错，请检查连接网络重试");
			} else if (isPrinterError(msg)) {
				toastText("退菜订单失败");
			} else {
				if (Setting.enabledCleanTableAfterCheckout()) {
					cleanTableThread(selectedTable);
				} else {
					binderStart();
					toastText(R.string.checkOutSucc);
				}
			}
		}
	};
	
}
