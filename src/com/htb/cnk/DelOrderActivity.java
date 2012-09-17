package com.htb.cnk;

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
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.MyOrder.OrderedDish;
import com.htb.cnk.lib.OrderBaseActivity;
import com.htb.constant.ErrorNum;

public class DelOrderActivity extends OrderBaseActivity {
	private final int CLEANALL = -1;
	private int ARERTDIALOG = 0;
	private MyOrderAdapter mMyOrderAdapter;
	private AlertDialog mNetWrorkcancel;
	private AlertDialog.Builder mNetWrorkAlertDialog;
	private final int UPDATE_ORDER_QUAN = 1;
	private final int DEL_ORDER_QUAN = 0;

	@Override
	protected void onResume() {
		if (ARERTDIALOG == 1) {
			mNetWrorkcancel.cancel();
			ARERTDIALOG = 0;
		}
		showProgressDlg("正在获取菜品。。。");
		new Thread(new getOrderThread()).start();
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDelViews();
		setDelClickListener();
		mNetWrorkAlertDialog = networkDialog();
	}

	private void setDelViews() {
		mSubmitBtn.setVisibility(View.GONE);
		mLeftBtn.setText(R.string.cleanAll);
		mRefreshBtn.setVisibility(View.GONE);
	}

	private void fillDelData() {
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
				delBtn.setText("-1");
				delBtn.setOnClickListener(delClicked);

				return convertView;
			}
		};
		mTableNumTxt.setText(Info.getTableName());
		mDishCountTxt.setText(Integer.toString(mMyOrder.totalQuantity())
				+ " 道菜");
		mTotalPriceTxt
				.setText(Double.toString(mMyOrder.getTotalPrice()) + " 元");
		mMyOrderLst.setAdapter(mMyOrderAdapter);

	}

	private void setDelClickListener() {
		mLeftBtn.setOnClickListener(cleanBtnClicked);
	}

	public void showProgressDlg(String msg) {
		mpDialog.setMessage(msg);
		mpDialog.show();
	}

	private void delDish(final int position) {
		new Thread() {
			public void run() {
				try {
					int ret = mMyOrder.submitDelDish(position,
							UPDATE_ORDER_QUAN);
					if (ret < 0) {
						delDishHandler.sendEmptyMessage(ret);
						return;
					}
					mMyOrder.minus(position, 1);
					delDishHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	private void delDishAlert(final int position) {
		String messages;
		if (position == CLEANALL) {
			messages = "确认退掉所有菜品";
		} else {
			messages = "确认退菜：" + mMyOrder.getOrderedDish(position).getName();
		}
		new AlertDialog.Builder(DelOrderActivity.this).setTitle("请注意")
				.setMessage(messages)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (position == CLEANALL) {
							showProgressDlg("正在推掉所有菜品");
							new Thread(new cleanAllThread()).start();
						} else {
							showProgressDlg("正在推掉菜品");
							delDish(position);
						}
					}
				}).setNegativeButton("取消", null).show();

	}

	private OnClickListener cleanBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {

			delDishAlert(CLEANALL);
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

	Handler getOrderHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what == -2) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();
			} else if (msg.what == -1) {
				ARERTDIALOG = 1;
				mNetWrorkcancel = mNetWrorkAlertDialog.show();
			} else {
				mMyOrder.nullServing();
				fillDelData();
				mMyOrderAdapter.notifyDataSetChanged();
			}
		}
	};

	Handler delDishHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				delHanderError(msg);
			} else {
				fillDelData();
				mMyOrderAdapter.notifyDataSetChanged();
			}
		}
	};

	Handler cleanAllHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				if (msg.what == -2) {
					Toast.makeText(getApplicationContext(), "菜都上完，不能再退了",
							Toast.LENGTH_SHORT).show();
				} else {
					delHanderError(msg);
				}
			} else {
				mMyOrder.clear();
				fillDelData();
				mMyOrderAdapter.notifyDataSetChanged();
			}
		}
	};

	class getOrderThread implements Runnable {
		public void run() {
			try {
				int ret = mMyOrder.getOrderFromServer(Info.getTableId());
				getOrderHandler.sendEmptyMessage(ret);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class cleanAllThread implements Runnable {
		public void run() {
			try {
				if (mMyOrder.count() == 0) {
					cleanAllHandler.sendEmptyMessage(-2);
					return;
				}
				int result = mMyOrder.submitDelDish(MyOrder.DEL_ALL_ORDER,
						DEL_ORDER_QUAN);
				// int ret = mSettings.cleanTalble(Info.getTableId());
				if (result < 0) {
					cleanAllHandler.sendEmptyMessage(result);
					return;
				}
				cleanAllHandler.sendEmptyMessage(result);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private AlertDialog.Builder networkDialog() {
		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
				DelOrderActivity.this);
		mAlertDialog.setTitle("错误");// 设置对话框标题
		mAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
		mAlertDialog.setCancelable(false);
		mAlertDialog.setPositiveButton("重试",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						ARERTDIALOG = 0;
						showProgressDlg("正在连接服务器...");
						new Thread(new getOrderThread()).start();
					}
				});
		mAlertDialog.setNegativeButton("退出",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						finish();
						ARERTDIALOG = 0;
					}
				});

		return mAlertDialog;
	}

	@Override
	public void finish() {
		mMyOrder.clear();
		super.finish();
	}

	private void delHanderError(Message msg) {
		String errMsg = "退菜订单失败";
		if (msg.what == ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT
				|| msg.what == ErrorNum.PRINTER_ERR_NO_PAPER) {
			errMsg += ":无法连接打印机或打印机缺纸";
		}
		ARERTDIALOG = 1;
		mNetWrorkAlertDialog.setMessage(errMsg + ",请检查连接网络重试");
		mNetWrorkcancel = mNetWrorkAlertDialog.show();
	}

}
