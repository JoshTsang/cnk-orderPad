package com.htb.cnk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.OrderedDish;
import com.htb.cnk.ui.base.OrderBaseActivity;

/**
 * @author josh
 * 
 */
public class MyOrderActivity extends OrderBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setOrderViews();
		fillOrderData();
		setOrderClickListener();
		mMyOrder.setOrderType(MyOrder.MODE_PAD);
	}

	private void setOrderViews() {
		mLeftBtn.setText(mMyOrder.getOrderTimeType()==MyOrder.ORDER_INSTANT?"即单":"叫单");
		mRefreshBtn.setText("预付款");
	}

	private void fillOrderData() {
		mMyOrderAdapter = getMyOrderAdapterInstance();
		mMyOrderLst.setAdapter(mMyOrderAdapter);
	}

	private MyOrderAdapter getMyOrderAdapterInstance() {
		return new MyOrderAdapter(this, mMyOrder) {
			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);
				ItemViewHolder itemViewHolder;
				if (convertView == null) {
					convertView = LayoutInflater.from(MyOrderActivity.this)
							.inflate(R.layout.item_ordereddish, null);
					itemViewHolder = new ItemViewHolder(convertView);
					itemViewHolder.setOnClickListener();
					convertView.setTag(itemViewHolder);
				} else {
					itemViewHolder = (ItemViewHolder) convertView.getTag();
				}
				itemViewHolder.setTag(position);
				itemViewHolder.fillData(dishDetail);
				return convertView;
			}
		};
	}

	private void setOrderClickListener() {
		mBackBtn.setOnClickListener(backClicked);
		mLeftBtn.setOnClickListener(orderTimeTypeClicked);
		mRefreshBtn.setOnClickListener(advPaymentClicked);
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

	private void minusDishQuantity(final int position, final int quantity) {
		if (mMyOrder.getOrderedDish(position).getQuantity() > quantity) {
			updateDishQuantity(position, -quantity);
		} else {
			new AlertDialog.Builder(MyOrderActivity.this)
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
							}).setNegativeButton("取消", null).show();
		}
	}

	private OnClickListener backClicked = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent();
			if (Info.getMode() == Info.WORK_MODE_CUSTOMER || Info.getMenu() == Info.ORDER_LIST_MENU) {
				intent.setClass(MyOrderActivity.this, MenuActivity.class);
			} else {
				intent.setClass(MyOrderActivity.this, QuickMenuActivity.class);
			}
			startActivity(intent);
			finish();
		}
	};

	private OnClickListener minusClicked = new OnClickListener() {

		public void onClick(View v) {
			final int position = Integer.parseInt(v.getTag().toString());
			minusDishQuantity(position, 1);
		}
	};



	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean ret = super.onKeyDown(keyCode, event);
		Intent intent = new Intent();
		intent.setClass(MyOrderActivity.this, MenuActivity.class);
		startActivity(intent);
		return ret;
	}

	class ItemViewHolder {
		TextView dishName;
		TextView dishPrice;
		TextView dishQuantity;
		Button plusBtn;
		Button minusBtn;
		Button flavorBtn;

		public ItemViewHolder(View convertView) {
			dishName = (TextView) convertView.findViewById(R.id.dishName);
			dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
			dishQuantity = (TextView) convertView
					.findViewById(R.id.dishQuantity);
			plusBtn = (Button) convertView.findViewById(R.id.dishPlus);
			minusBtn = (Button) convertView.findViewById(R.id.dishMinus);
			flavorBtn = (Button) convertView.findViewById(R.id.flavor);
		}

		public void setOnClickListener() {
			plusBtn.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					final int position = Integer
							.parseInt(v.getTag().toString());
					updateDishQuantity(position, 1);
				}
			});
			minusBtn.setOnClickListener(minusClicked);

			flavorBtn.setOnClickListener(flavorClicked);
			//dishQuantity.setOnLongClickListener(quantityClicked);
			dishQuantity.setOnClickListener(quantityClicked);
		}

		public void setTag(int position) {
			plusBtn.setTag(position);
			minusBtn.setTag(position);
			flavorBtn.setTag(position);
			dishQuantity.setTag(position);
		}

		public void fillData(OrderedDish dishDetail) {
			dishName.setText(dishDetail.getName());
			dishPrice.setText(Double.toString(dishDetail.getPrice()) + " 元/" + dishDetail.getUnit());
			dishQuantity
					.setText(MyOrder.convertFloat(dishDetail.getQuantity()));

		}
	}
}