package com.htb.cnk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.MyOrder.OrderedDish;

public class MyOrderActivity extends Activity {
	private Button mBackBtn;
	private TextView mTableNumTxt;
	private TextView mDishCountTxt;
	private TextView mTotalPriceTxt;
	private ListView mMyOrderLst;
	private MyOrder mMyOrder = new MyOrder();
	private MyOrderAdapter mMyOrderAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myorder_activity);
		findViews();
		fillData();
		setClickListener();
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back_btn);
		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);
	}
	
	private void fillData() {
		mTableNumTxt.setText("100");
		updateTabelInfos();
		
		mMyOrderAdapter = new MyOrderAdapter(this, mMyOrder) {
			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				if(convertView==null)
				{
					convertView=LayoutInflater.from(MyOrderActivity.this).inflate(R.layout.item_ordereddish, null);
				}
				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);
				
				TextView dishName = (TextView) convertView.findViewById(R.id.dishName);
				TextView dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
				TextView dishQuantity = (TextView) convertView.findViewById(R.id.dishQuantity);
				Button plusBtn = (Button) convertView.findViewById(R.id.dishPlus);
				Button minusBtn = (Button) convertView.findViewById(R.id.dishMinus);
				Button plus5Btn = (Button) convertView.findViewById(R.id.dishPlus5);
				Button minus5Btn = (Button) convertView.findViewById(R.id.dishMinus5);
				
				dishName.setText(dishDetail.getName());
				dishPrice.setText(Double.toString(dishDetail.getPrice()) + " 元/份");
				dishQuantity.setText(Integer.toString(dishDetail.getQuantity()));
				
				plusBtn.setTag(position);

				plusBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						final int position = Integer.parseInt(v.getTag().toString());
						updateDishQuantity(position, 1);
					}
				});
				
				minusBtn.setTag(position);

				minusBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						final int position = Integer.parseInt(v.getTag().toString());
						if (mMyOrder.getOrderedDish(position).getQuantity() > 1) {
							updateDishQuantity(position, -1);
						} else {
							new AlertDialog.Builder(MyOrderActivity.this)
							.setTitle("请注意")
							.setMessage("确认删除" + mMyOrder.getOrderedDish(position).getName())
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											updateDishQuantity(position, -1);
										}
									})
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									
								}
							}).show();
						}
					}
				});
				
				plus5Btn.setTag(position);

				plus5Btn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						final int position = Integer.parseInt(v.getTag().toString());
						updateDishQuantity(position, 5);
					}
				});
				
				minus5Btn.setTag(position);

				minus5Btn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						final int position = Integer.parseInt(v.getTag().toString());
						if (mMyOrder.getOrderedDish(position).getQuantity() > 1) {
							updateDishQuantity(position, -5);
						} else {
							new AlertDialog.Builder(MyOrderActivity.this)
							.setTitle("请注意")
							.setMessage("确认删除" + mMyOrder.getOrderedDish(position).getName())
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog,
												int which) {
											updateDishQuantity(position, -5);
										}
									})
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									
								}
							}).show();
						}
					}
				});
				return convertView;
			}
		};
		mMyOrderLst.setAdapter(mMyOrderAdapter);
	}
	
	private void setClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
	}
	
	private void updateDishQuantity(int position, int quantity) {
		if (quantity < 0) {
			mMyOrder.minus(position, -quantity);
		} else {
			mMyOrder.add(position, quantity);
		}

		mMyOrderAdapter.notifyDataSetChanged();
		updateTabelInfos();
	}

	private void updateTabelInfos() {
		mDishCountTxt.setText(Integer.toString(mMyOrder.count()) + " 道菜");
		mTotalPriceTxt.setText(Double.toString(mMyOrder.getTotalPrice()) + " 元");
	}

	private OnClickListener backBtnClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			MyOrderActivity.this.finish();
		}
	};
}
