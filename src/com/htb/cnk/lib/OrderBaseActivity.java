package com.htb.cnk.lib;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.htb.cnk.R;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.PadOrder;
import com.htb.cnk.lib.BaseActivity;

/**
 * @author josh
 *
 */
public class OrderBaseActivity extends BaseActivity {
	protected Button mBackBtn;
	protected Button mSubmitBtn;
	protected Button mLeftBtn;
	protected TextView mTableNumTxt;
	protected TextView mDishCountTxt;
	protected TextView mTotalPriceTxt;
	protected ListView mMyOrderLst;
	protected PadOrder mMyOrder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myorder_activity);
		mMyOrder = new PadOrder(OrderBaseActivity.this);
		findViews();
		fillData();
		setClickListener();
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back_btn);
		mSubmitBtn = (Button) findViewById(R.id.submit);
		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);
		mLeftBtn = (Button) findViewById(R.id.left_btn);
	}

	private void fillData() {
		mTableNumTxt.setText(Info.getTableName());
		updateTabelInfos();
	}

	private void setClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
	}

	protected void updateTabelInfos() {
		mDishCountTxt.setText(Integer.toString(mMyOrder.totalQuantity()) + " 道菜");
		mTotalPriceTxt.setText(Double.toString(mMyOrder.getTotalPrice()) + " 元");
	}

	private OnClickListener backBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			OrderBaseActivity.this.finish();
		}
	};

}
