package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.htb.cnk.data.Info;
import com.htb.cnk.ui.base.TableGridDeskActivity;

public class TableActivity extends TableGridDeskActivity {

	static final String TAG = "TableActivity";
	protected int NETWORK_ARERTDIALOG = 0;
	protected double mIncome;
	protected double mChange;
	protected double mTotalPrice;
	protected AlertDialog.Builder mChangeDialog;
	protected Handler mTotalPriceTableHandler;
	protected Handler mCheckOutHandler;

	protected Button mBackBtn;
	protected Button mUpdateBtn;
	protected Button mStatisticsBtn;
	protected Button mManageBtn;
	private LinearLayout layoutViewPager;

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_activity);
		findViews();
		mpDialog.show();
		setClickListeners();
		mTotalPriceTableHandler = totalPriceTableHandler;
		// setHandler();

	}

	// private void setHandler() {
	// mNotificationHandler = notificationHandler;
	// mTotalPriceTableHandler = totalPriceTableHandler;
	// mChangeTIdHandler = changeTIdHandler;
	// mCopyTIdHandler = copyTIdHandler;
	// mCombineTIdHandler = combineTIdHandler;
	// }

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back);
		mUpdateBtn = (Button) findViewById(R.id.checkOutTable);
		mStatisticsBtn = (Button) findViewById(R.id.logout);
		mManageBtn = (Button) findViewById(R.id.management);
		layoutViewPager = (LinearLayout) findViewById(R.id.scr);
		mOrderNotification = (Button) findViewById(R.id.orderNotification);
		mStatusBar = (TextView) findViewById(R.id.statusBar);
	}

	protected void setClickListeners() {
		mBackBtn.setOnClickListener(backClicked);
		mUpdateBtn.setOnClickListener(checkOutClicked);
		mStatisticsBtn.setOnClickListener(logoutClicked);
		mManageBtn.setOnClickListener(manageClicked);
		mNetWrorkAlertDialog = networkDialog();
		layoutViewPager.addView(getPageView());
	}

	// TODO define
	Handler changeTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			switch (msg.what) {
			case -10:
				toastText("本地数据库出错，请从网络重新更新数据库");
				break;
			case -2:
				toastText(R.string.changeTIdWarning);
				break;
			case -1:
				showNetworkErrDlg("转台失败，"
						+ getResources()
								.getString(R.string.networkErrorWarning));
				break;
			default:
				if (!isPrinterError(msg)) {
					binderStart();
				}
				toastText(R.string.changeSucc);
				break;
			}
		}
	};

	// TODO define
	Handler copyTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			switch (msg.what) {
			case -10:
				toastText("本地数据库出错，请从网络重新更新数据库");
				break;
			case -2:
				toastText(R.string.copyTIdwarning);
				break;
			case -1:
				showNetworkErrDlg("复制失败，"
						+ getResources()
								.getString(R.string.networkErrorWarning));
				break;
			default:
				intent.setClass(TableActivity.this, MyOrderActivity.class);
				Info.setMode(Info.WORK_MODE_WAITER);
				TableActivity.this.startActivity(intent);
				break;
			}
		}
	};

	Handler combineTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			switch (msg.what) {
			case -10:
				toastText("本地数据库出错，请从网络重新更新数据库");
				break;
			case -2:
				toastText(R.string.checkOutWarning);
				break;
			case -1:
				showNetworkErrDlg("合并出错，"
						+ getResources()
								.getString(R.string.networkErrorWarning));
				break;
			default:
				if (isPrinterError(msg)) {
					toastText(R.string.combineError);
				} else {
					binderStart();
					toastText(R.string.combineSucc);
				}
				break;
			}
		}
	};

	Handler notificationHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				toastText(R.string.notificationWarning);
			} else {
				binderStart();
			}
		}
	};

	Handler notificationTypeHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				toastText(R.string.notificationTypeWarning);
			}
		}
	};

	Handler totalPriceTableHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				showNetworkErrDlg("统计失败，"
						+ getResources()
								.getString(R.string.networkErrorWarning));
			} else {
				if (mTotalPrice <= 0) {
					toastText(R.string.dishNull);
				} else {
					sendPriceToCheckout();
				}
			}
		}
	};

	/**
	 * 
	 */
	private void sendPriceToCheckout() {
		Intent checkOutIntent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putDouble("price", mTotalPrice);
		bundle.putIntegerArrayList("tableId",
				(ArrayList<Integer>) selectedTable);
		bundle.putStringArrayList("tableName", (ArrayList<String>) tableName);
		checkOutIntent.putExtras(bundle);
		checkOutIntent.setClass(TableActivity.this, CheckOutActivity.class);
		TableActivity.this.startActivity(checkOutIntent);
	}

	protected AlertDialog.Builder networkDialog() {
		return mTitleAndMessageDialog.networkDialog(networkPositiveListener,
				networkNegativeListener);
	}

	DialogInterface.OnClickListener networkPositiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			dialog.cancel();
			NETWORK_ARERTDIALOG = 0;
			showProgressDlg(getResources().getString(R.string.getStatus));
			binderStart();
		}
	};

	DialogInterface.OnClickListener networkNegativeListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			NETWORK_ARERTDIALOG = 0;
			dialog.cancel();
			finish();
		}
	};

	protected Builder listTableNameDialog(final int type) {
		final List<Integer> tableId = new ArrayList<Integer>();
		final List<String> tableNameStr = new ArrayList<String>();
		ArrayList<HashMap<String, Object>> checkOut = getSettings()
				.getTableOpen();
		if (checkOut.size() <= 0) {
			return mTitleAndMessageDialog.messageDialog(false, getResources()
					.getString(R.string.tableNotOpen), getResources()
					.getString(R.string.ok), null, null, null);
		}
		setNameAndId(tableId, tableNameStr, checkOut);
		final boolean selected[] = new boolean[getSettings()
				.tableSeetingsSize()];

		DialogInterface.OnClickListener listPositiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which) {
				selectedTable.clear();
				tableName.clear();
				for (int i = 0; i < selected.length; i++) {
					if (selected[i] == true) {
						selectedTable.add(tableId.get(i));
						tableName.add(tableNameStr.get(i));
					}
				}
				if (type == CHECKOUT_LIST) {
					showProgressDlg(getResources().getString(
							R.string.statisticsAmount));
					getTotalPriceTable();
				}

			}
		};

		DialogInterface.OnMultiChoiceClickListener listListener = new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which,
					boolean isChecked) {
				selected[which] = isChecked;
			}
		};

		return mMultiChoiceItemsDialog.titleDialog(false, getResources()
				.getString(R.string.chooseTableId), (String[]) tableNameStr
				.toArray(new String[0]), null, listListener, getResources()
				.getString(R.string.ok), listPositiveListener, getResources()
				.getString(R.string.cancel), null);
	}

	private void setNameAndId(final List<Integer> tableId,
			final List<String> tableNameStr,
			ArrayList<HashMap<String, Object>> checkOut) {
		for (HashMap<String, Object> item : checkOut) {
			tableNameStr.add(item.get("name").toString());
			tableId.add(item.get("id").hashCode());
		}
	}

	void getTotalPriceTable() {
		new Thread() {
			public void run() {
				try {
					double ret = getSettings().getTotalPriceTable(
							selectedTable, tableName);
					mTotalPrice = ret;
					mTotalPriceTableHandler.sendEmptyMessage((int) ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void checkOut(final List<Integer> destIId,
			final List<String> tableName, final Double receivable,
			final Double income, final Double change) {
		new Thread() {
			public void run() {
				try {
					int ret = getSettings().checkOut(destIId, tableName,
							receivable, income, change);
					mCheckOutHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected OnClickListener checkOutClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			listTableNameDialog(CHECKOUT_LIST).show();
		}
	};

	protected OnClickListener manageClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setClassToActivity(ManageActivity.class);
		}
	};

	protected OnClickListener backClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	protected OnClickListener logoutClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mTitleAndMessageDialog.titleAndMessageDialog(false,
					getResources().getString(R.string.notice),
					getResources().getString(R.string.islogOut),
					getResources().getString(R.string.ok),
					logoutPositiveListener,
					getResources().getString(R.string.cancel), null).show();
		}
	};

	DialogInterface.OnClickListener logoutPositiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			Info.logout();
			finish();
		}
	};
}