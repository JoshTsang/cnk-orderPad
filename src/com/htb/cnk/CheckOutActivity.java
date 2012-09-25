package com.htb.cnk;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.MyOrder;
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
	protected TextView mCheckOutPrinte;
	protected TextView mChangeText;
	protected EditText mIncomeEdit;
	protected ListView mMyOrderLst;
	protected MyOrderAdapter mMyOrderAdapter;
	protected MyOrder mMyOrder;
	private Intent checkOutIntent ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkout_activity);
		checkOutIntent = this.getIntent();
		mCheckOutHandler = checkOutHandler;
		mTableHandler =  tableHandler;
		mMyOrder = new MyOrder(CheckOutActivity.this);
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
		mCheckOutPrinte = (TextView) findViewById(R.id.checkOutPrinter);
	}
	
	private void setCheckOutView(){
		mSubmitBtn.setText("结账");
		mLeftBtn.setVisibility(View.GONE);
		mRefreshBtn.setVisibility(View.GONE);
		mComment.setVisibility(View.GONE);
		mBackBtn.setOnClickListener(backBtnClicked);
		mSubmitBtn.setOnClickListener(submitClicked);
		mIncomeEdit.addTextChangedListener(watcher);
		mCheckOutPrinte.setMovementMethod(ScrollingMovementMethod.getInstance());
		getTableValue();
		updateTabelInfos();
	}
	
	Handler tableHandler = new Handler() {
		public void handleMessage(Message msg) {
		}
	};
	
	private void updateTabelInfos() {
		mCheckOutPrinte.setText(mSettings.checkOutJson());
		mReceivableText.setText(String.valueOf(mTotalPrice));
	}
	
	private void getTableValue(){
		Bundle bundle = checkOutIntent.getExtras();
		mTotalPrice = bundle.getDouble("price");
		selectedTable = bundle.getIntegerArrayList("tableId");
		tableName = bundle.getStringArrayList("tableName");
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
			if(tempStr.indexOf("0") == 0){
				tempStr = tempStr.substring(1,tempStr.length());
				mIncomeEdit.setText(tempStr);
			} 
			if (tempStr.length() > 0) {
				mIncome = Double.valueOf(tempStr).doubleValue();
				mChange = mIncome - mTotalPrice;
				mChangeText.setText(String.valueOf(mChange));
			}
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
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
				}
				toastText(R.string.checkOutSucc);
				finish();
			}
		}
	};
	
}
