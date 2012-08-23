package com.htb.cnk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.data.MyOrder.OrderedDish;
import com.htb.cnk.lib.OrderBaseActivity;


public class DelOrderActivity extends OrderBaseActivity {
	private MyOrderAdapter mMyOrderAdapter;
	private TableSetting mSettings = new TableSetting();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMyOrder.clear();
		setDelViews();
		setDelClickListener();
		new Thread(new delThread()).start();
	}

	private void setDelViews() {
		mSubmitBtn.setVisibility(View.GONE);
		mLeftBtn.setText(R.string.cleanAll);
		mRefreshBtn.setVisibility(View.GONE);
	}

	private void fillDelData() {
		mTableNumTxt.setText(Info.getTableName());
		mDishCountTxt.setText(Integer.toString(mMyOrder.totalQuantity()) + " 道菜");
		mTotalPriceTxt
				.setText(Double.toString(mMyOrder.getTotalPrice()) + " 元");
		mMyOrderAdapter = new MyOrderAdapter(this, mMyOrder) {
			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				TextView dishName;
				TextView dishPrice;
				 TextView dishQuantity;
				Button delBtn;

				if (convertView == null) {
					convertView = LayoutInflater.from(DelOrderActivity.this)
							.inflate(R.layout.item_delorder, null);
				}
				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);

				dishName = (TextView) convertView.findViewById(R.id.dishName);
				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
				 dishQuantity = (TextView) convertView
				 .findViewById(R.id.dishQuantity);
				delBtn = (Button) convertView.findViewById(R.id.dishMinus);

				dishName.setText(dishDetail.getName());
				dishPrice.setText(Double.toString(dishDetail.getPrice())
						+ " 元/份");
				 dishQuantity
				 .setText(Integer.toString(dishDetail.getQuantity()));


				delBtn.setTag(position);
				delBtn.setOnClickListener(delClicked);

				return convertView;
			}
		};

		mMyOrderLst.setAdapter(mMyOrderAdapter);

	}

	private void setDelClickListener() {
		mLeftBtn.setOnClickListener(cleanBtnClicked);
	}


	private void delDish(final int position) {
		new Thread() {
			public void run() {
				try {
					Message msg = new Message();
					int ret = 1;
					ret = mMyOrder.submitDelDish(position);
					if (ret < 0) {
						delHandler.sendEmptyMessage(ret);
						return;
					}
					mMyOrder.removeItem(position);
					msg.what = ret;
					delHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	private void delDishAlert(final int position) {
		new AlertDialog.Builder(DelOrderActivity.this)
				.setTitle("请注意")
				.setMessage(
						"确认删除" + mMyOrder.getOrderedDish(position).getName())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						delDish(position);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();

	}

	private OnClickListener cleanBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.d("TableId", "tableId+" + Info.getTableId());
			new Thread() {
				public void run() {
					try {
						Message msg = new Message();
						int ret = 1;
						int result = 1;
						result = mMyOrder.submitDelDish(-1);
						ret = mSettings.cleanTalble(Info.getTableId());
						if (ret < 0 || result < 0) {
							cleanAllHandler.sendEmptyMessage(ret);
							return;
						}
						msg.what = ret;
						cleanAllHandler.sendMessage(msg);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();

			
			fillDelData();
			mMyOrderAdapter.notifyDataSetChanged();

		}
	};

	private OnClickListener delClicked = new OnClickListener() {
	
		public void onClick(View v) {
			final int position = Integer.parseInt(v.getTag().toString());
			delDishAlert(position);
	
		}
	};
	
	Handler delHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();
			} else {
				fillDelData();
				mMyOrderAdapter.notifyDataSetChanged();
			}
		}
	};
	
	Handler cleanAllHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();
			} else {
				mMyOrder.clear();
				mMyOrderAdapter.notifyDataSetChanged();
				fillDelData();
			}
		}
	};
	
	class delThread implements Runnable {
		public void run() {
			Message msg = new Message();
			try {
				int ret = mMyOrder.getTableFromDB(Info.getTableId());
				if (ret < 0) {
					delHandler.sendEmptyMessage(ret);
					return;
				}
				msg.what = ret;
				delHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
	
		}
	
	}

}
