package com.htb.cnk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.MyOrder.OrderedDish;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.lib.BaseActivity;

/**
 * @author josh
 * 
 */
public class PhoneActivity extends BaseActivity {
	private Button mBackBtn;
	private Button mSubmitBtn;
	private TextView mTableNumTxt;
	private TextView mDishCountTxt;
	private TextView mTotalPriceTxt;
	private ListView mMyOrderLst;
	private MyOrder mMyOrder = new MyOrder();
	private MyOrderAdapter mMyOrderAdapter;
	private ProgressDialog mpDialog;
	private TableSetting mSettings = new TableSetting();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myorder_activity);
		findViews();
		fillData();
		setClickListener();
		updateTabelInfos();
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back_btn);
		mSubmitBtn = (Button) findViewById(R.id.submit);
		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);
	}

	private void fillData() {
		mTableNumTxt.setText(Info.getTableName());
		mMyOrderAdapter = getMyOrderAdapterInstance();
		mMyOrderLst.setAdapter(mMyOrderAdapter);
	}

	private MyOrderAdapter getMyOrderAdapterInstance() {

		return new MyOrderAdapter(this, mMyOrder) {
			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				TextView dishName;
				TextView dishPrice;
				TextView dishQuantity;
				Button plusBtn;
				Button minusBtn;
				Button plus5Btn;
				Button minus5Btn;

				if (convertView == null) {
					convertView = LayoutInflater.from(PhoneActivity.this)
							.inflate(R.layout.item_ordereddish, null);
				}
				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);

				dishName = (TextView) convertView.findViewById(R.id.dishName);
				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
				dishQuantity = (TextView) convertView
						.findViewById(R.id.dishQuantity);
				plusBtn = (Button) convertView.findViewById(R.id.dishPlus);
				minusBtn = (Button) convertView.findViewById(R.id.dishMinus);
				plus5Btn = (Button) convertView.findViewById(R.id.dishPlus5);
				minus5Btn = (Button) convertView.findViewById(R.id.dishMinus5);

				dishName.setText(dishDetail.getName());
				dishPrice.setText(Double.toString(dishDetail.getPrice())
						+ " 元/份");
				dishQuantity
						.setText(Integer.toString(dishDetail.getQuantity()));

				plusBtn.setTag(position);
				plusBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						final int position = Integer.parseInt(v.getTag()
								.toString());
						updateDishQuantity(position, 1);
					}
				});

				minusBtn.setTag(position);
				minusBtn.setOnClickListener(minusClicked);

				plus5Btn.setTag(position);

				plus5Btn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						final int position = Integer.parseInt(v.getTag()
								.toString());
						updateDishQuantity(position, 5);
					}
				});

				minus5Btn.setTag(position);

				minus5Btn.setOnClickListener(minus5Clicked);
				return convertView;
			}
		};
	}

	private void setClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
		mSubmitBtn.setOnClickListener(submitBtnClicked);
	}

	Handler queryHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();

				fillData();
				mDishCountTxt
						.setText(Integer.toString(mMyOrder.totalQuantity())
								+ " 道菜");
				mTotalPriceTxt
						.setText(Double.toString(mMyOrder.getTotalPrice())
								+ " 元");
			} else {
				fillData();
				mDishCountTxt
						.setText(Integer.toString(mMyOrder.totalQuantity())
								+ " 道菜");
				mTotalPriceTxt
						.setText(Double.toString(mMyOrder.getTotalPrice())
								+ " 元");
				mMyOrderAdapter.notifyDataSetChanged();

			}
		}
	};

	class queryThread implements Runnable {
		public void run() {
			Message msg = new Message();
			try {
				int ret = mMyOrder.getTablePhoneFromDB(Info.getTableId());
				msg.what = ret;
				queryHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private void updateDishQuantity(int position, int quantity) {
		if (quantity < 0) {
			mMyOrder.minus(position, -quantity);
		} else {
			mMyOrder.add(position, quantity);
		}

		mMyOrderAdapter.notifyDataSetChanged();
		mDishCountTxt.setText(Integer.toString(mMyOrder.totalQuantity())
				+ " 道菜");
		mTotalPriceTxt
				.setText(Double.toString(mMyOrder.getTotalPrice()) + " 元");
	}

	private void updateTabelInfos() {
		mMyOrder = new MyOrder(PhoneActivity.this);
		mMyOrder.clear();
		new Thread(new queryThread()).start();
	}

	private void minusDishQuantity(final int position, final int quantity) {
		if (mMyOrder.getOrderedDish(position).getQuantity() > quantity) {
			updateDishQuantity(position, -quantity);
			updatePhoneOrder(position);
		} else {
			new AlertDialog.Builder(PhoneActivity.this)
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
									
									delPhoneTableThread(position);
									mpDialog = new ProgressDialog(PhoneActivity.this);
									mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
									mpDialog.setTitle("请稍等");
									mpDialog.setMessage("正在删除菜单...");
									mpDialog.setIndeterminate(false);
									mpDialog.setCancelable(false);
									mpDialog.show();
									updateDishQuantity(position, -quantity);
								}
							})
					.setNegativeButton("取消",null).show();
		}
	}

	private void updatePhoneOrder(final int position) {
		new Thread() {
			public void run() {
				Message msg = new Message();
				int ret = mMyOrder.updatePhoneOrder(Info.getTableId(), mMyOrder
						.getOrderedDish(position).getQuantity(), mMyOrder
						.getDishId(position));
				if (ret < 0) {
					delPhoneOrderhandler.sendEmptyMessage(-1);
				} else {
					msg.what = ret;
					delPhoneOrderhandler.sendMessage(msg);
				}
			}
		}.start();
	}

	private void delPhoneTableThread(final int position) {
		new Thread() {
			public void run() {
				int ret = mMyOrder.delPhoneTable(
						Info.getTableId(),
						mMyOrder.getDishId(position));
				mpDialog.cancel();
				if (ret < 0) {
					delPhoneOrderhandler
							.sendEmptyMessage(-1);
				} else {
					delPhoneOrderhandler
							.sendEmptyMessage(0);
				}
			}
		}.start();
	}

	private OnClickListener backBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PhoneActivity.this.finish();
		}
	};

	private OnClickListener minusClicked = new OnClickListener() {

		public void onClick(View v) {
			final int position = Integer.parseInt(v.getTag().toString());
			minusDishQuantity(position, 1);
		}
	};

	private OnClickListener minus5Clicked = new OnClickListener() {

		public void onClick(View v) {
			final int position = Integer.parseInt(v.getTag().toString());
			minusDishQuantity(position, 5);
		}
	};

	private OnClickListener submitBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mMyOrder.count() <= 0) {
				new AlertDialog.Builder(PhoneActivity.this)
						.setTitle("请注意")
						.setMessage("您还没有点任何东西")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).show();
				return;
			}
			// TODO auth
			mpDialog = new ProgressDialog(PhoneActivity.this);
			mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mpDialog.setTitle("请稍等");
			mpDialog.setMessage("正在提交订单...");
			mpDialog.setIndeterminate(false);
			mpDialog.setCancelable(false);
			mpDialog.show();
			new Thread() {
				public void run() {
					String ret = mMyOrder.submit();
					if (ret == null) {
						handler.sendEmptyMessage(-1);
					} else {
						if ("".equals(ret)) {
							handler.sendEmptyMessage(0);
							mSettings.updatusStatus(Info.getTableId(), 1);
							mMyOrder.delPhoneTable(Info.getTableId(), 0);
						} else {
							handler.sendEmptyMessage(-1);
						}
						Log.d("Respond", ret);
					}
				}
			}.start();
		}
	};

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				new AlertDialog.Builder(PhoneActivity.this)
						.setCancelable(false)
						.setTitle("出错了")
						.setMessage("提交订单失败")
						.setPositiveButton("确定",
								null).show();
			} else {
				new AlertDialog.Builder(PhoneActivity.this)
						.setCancelable(false)
						.setTitle("提示")
						.setMessage("订单已提交")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mMyOrder.clear();
										mMyOrderAdapter.notifyDataSetChanged();
									}
								}).show();
			}
		}
	};

	private Handler delPhoneOrderhandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				new AlertDialog.Builder(PhoneActivity.this)
				.setCancelable(false)
				.setTitle("出错了")
				.setMessage("删除失败")
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						
					}
				}).show();
			} else {
				
			}
		}
	};
	
	

}
