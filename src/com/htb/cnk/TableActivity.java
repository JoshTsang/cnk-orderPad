package com.htb.cnk;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.htb.cnk.data.Info;
import com.htb.cnk.ui.base.TableBaseActivity;

public class TableActivity extends TableBaseActivity {

	static final String TAG = "TablesActivity";

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
		setHandler();
		
	}

	private void setHandler() {
		mNotificationHandler = notificationHandler;
		mTotalPriceTableHandler = totalPriceTableHandler;
		mChangeTIdHandler = changeTIdHandler;
		mCopyTIdHandler = copyTIdHandler;
		mCombineTIdHandler = combineTIdHandler;
	}

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

}