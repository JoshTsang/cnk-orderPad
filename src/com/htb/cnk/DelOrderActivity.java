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

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.OrderedDish;
import com.htb.cnk.ui.base.OrderBaseActivity;

public class DelOrderActivity extends OrderBaseActivity {
	private final int CLEANALL = -1;
	private final int UPDATE_ORDER = 1;
	private final int DEL_ITEM_ORDER = 2;
	private int NETWORK_ARERTDIALOG = 0;
	private MyOrderAdapter mMyOrderAdapter;
	private AlertDialog mNetWrorkcancel;
	private AlertDialog.Builder mNetWrorkAlertDialog;
	private final int UPDATE_ORDER_QUAN = 1;

	@Override
	protected void onResume() {
		if (NETWORK_ARERTDIALOG == 1) {
			mNetWrorkcancel.cancel();
			NETWORK_ARERTDIALOG = 0;
		}
		showProgressDlg("正在获取菜品。。。");
		getOrderThread();
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
		mComment.setVisibility(View.GONE);
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
						+ " 元/" + dishDetail.getUnit());
				dishQuantity.setText(MyOrder.convertFloat(dishDetail
						.getQuantity()));

				delBtn.setTag(position);
				delBtn.setText("-1");
				delBtn.setOnClickListener(delClicked);

				return convertView;
			}
		};
		updateTabelInfos();
		mMyOrderLst.setAdapter(mMyOrderAdapter);

	}

	private void setDelClickListener() {
		mLeftBtn.setOnClickListener(cleanBtnClicked);
	}

	private void delDish(final int position, final float updateOrderQuan,
			final int type) {
		new Thread() {
			public void run() {
				try {
					int ret = mMyOrder.submitDelDish(position, type);
					if (ret < 0) {
						delDishHandler.sendEmptyMessage(ret);
						return;
					}
					mMyOrder.minus(position, updateOrderQuan);
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
			if ("斤".equals(mMyOrder.getOrderedDish(position).getUnit())) {
				new AlertDialog.Builder(DelOrderActivity.this)
						.setCancelable(false).setTitle("提示")
						.setMessage("该菜品无法退回！").setPositiveButton("确定", null)
						.show();
				return;
			}
			messages = "确认退菜：" + mMyOrder.getOrderedDish(position).getName();
		}
		delDishDialog(position, messages);
	}

	protected void delDishDialog(final int position, String messages) {
		DialogInterface.OnClickListener delDishPositiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (position == CLEANALL) {
					showProgressDlg("正在退掉所有菜品");
					cleanAllThread();
				} else {
					showProgressDlg("正在退掉菜品");
					if (converFloat(position)) {
						delDish(position, UPDATE_ORDER_QUAN, UPDATE_ORDER);
					} else {
						delDish(position, mMyOrder.getQuantity(position),
								DEL_ITEM_ORDER);
					}
				}
			}

			private boolean converFloat(final int position) {
				return MyOrder.convertFloat(mMyOrder.getQuantity(position))
						.indexOf(".") == -1;
			}
		};

		mTitleAndMessageDialog.titleAndMessageDialog(false,
				getResources().getString(R.string.pleaseNote), messages,
				getResources().getString(R.string.ok), delDishPositiveListener,
				getResources().getString(R.string.cancel), null).show();
	}

	Handler getOrderHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			switch (msg.what) {
			case -10:
				toastText("本地数据库出错，请从网络重新更新数据库");
				break;
			case -2:
				toastText(R.string.delWarning);
				break;
			case -1:
				if (NETWORK_ARERTDIALOG == 1) {
					mNetWrorkcancel.cancel();
				}
				NETWORK_ARERTDIALOG = 1;
				mNetWrorkcancel = mNetWrorkAlertDialog.show();
				break;
			default:
				mMyOrder.removeServedDishes();
				fillDelData();
				mMyOrderAdapter.notifyDataSetChanged();
				break;
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
				switch (msg.what) {
				case -2:
					toastText(R.string.notClean);
					break;
				case MyOrder.NOTHING_TO_DEL:
					toastText(R.string.nothingToDel);
					break;
				default:
					break;
				}
			} else {
				mMyOrder.clear();
				fillDelData();
				mMyOrderAdapter.notifyDataSetChanged();
				getOrderThread();
			}
		}
	};

	protected void getOrderThread() {
		new Thread() {
			public void run() {
				try {
					int ret = mMyOrder.getOrderFromServer(Info.getTableId());
					getOrderHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	protected void cleanAllThread() {
		new Thread() {
			public void run() {
				try {
					if (mMyOrder.count() == 0) {
						cleanAllHandler.sendEmptyMessage(-2);
						return;
					}
					int result = mMyOrder.submitDelDish(CLEANALL, CLEANALL);
					if (result < 0) {
						cleanAllHandler.sendEmptyMessage(result);
						return;
					}
					cleanAllHandler.sendEmptyMessage(result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected AlertDialog.Builder networkDialog() {
		return mNetworkDialog.networkDialog(networkPositiveListener,
				networkNegativeListener);
	}

	DialogInterface.OnClickListener networkPositiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			NETWORK_ARERTDIALOG = 0;
			showProgressDlg(getResources().getString(R.string.connectServer));
			getOrderThread();
		}
	};
	DialogInterface.OnClickListener networkNegativeListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			NETWORK_ARERTDIALOG = 0;
			finish();
		}
	};

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

	@Override
	public void finish() {
		mMyOrder.clear();
		super.finish();
	}

	private void delHanderError(Message msg) {
		String errMsg = "退菜订单失败";
		if (isPrinterError(msg)) {
			errMsg += ":无法连接打印机或打印机缺纸";
		}
		NETWORK_ARERTDIALOG = 1;
		mNetWrorkAlertDialog.setMessage(errMsg + ","
				+ getResources().getString(R.string.networkErrorWarning));
		mNetWrorkcancel = mNetWrorkAlertDialog.show();
	}

}
