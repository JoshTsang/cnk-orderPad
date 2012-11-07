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
import android.widget.FrameLayout;

import com.htb.cnk.data.Info;
import com.htb.cnk.ui.base.TableGridDeskActivity;

public class TableActivity extends TableGridDeskActivity {

	private static final String TAG = "TableActivity";
	private final int CHECKOUT_LIST = 1;

	private int NETWORK_ARERTDIALOG = 0;
	private double mTotalPrice;
	private Handler mCheckOutHandler;
	private List<String> tableName = new ArrayList<String>();
	private List<Integer> selectedTable = new ArrayList<Integer>();
	private Button mBackBtn;
	private Button mUpdateBtn;
	private Button mStatisticsBtn;
	private Button mManageBtn;
	private FrameLayout layoutViewPager;
	private AlertDialog.Builder mNetWrorkAlertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_activity);
		findViews();
		setClickListeners();
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back);
		mUpdateBtn = (Button) findViewById(R.id.checkOutTable);
		mStatisticsBtn = (Button) findViewById(R.id.logout);
		mManageBtn = (Button) findViewById(R.id.management);
		layoutViewPager = (FrameLayout) findViewById(R.id.scr);
	}

	private void setClickListeners() {
		mBackBtn.setOnClickListener(backClicked);
		mUpdateBtn.setOnClickListener(checkOutClicked);
		mStatisticsBtn.setOnClickListener(logoutClicked);
		mManageBtn.setOnClickListener(manageClicked);
		mNetWrorkAlertDialog = networkDialog();
		layoutViewPager.addView(getPageView());
	}

	private Handler totalPriceTableHandler = new Handler() {
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

	private AlertDialog.Builder networkDialog() {
		return mTitleAndMessageDialog.networkDialog(networkPositiveListener,
				networkNegativeListener);
	}

	private DialogInterface.OnClickListener networkPositiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			dialog.cancel();
			NETWORK_ARERTDIALOG = 0;
			showProgressDlg(getResources().getString(R.string.getStatus));
			binderStart();
		}
	};

	private DialogInterface.OnClickListener networkNegativeListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			NETWORK_ARERTDIALOG = 0;
			dialog.cancel();
			finish();
		}
	};

	private Builder listTableNameDialog(final int type) {
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

	private void getTotalPriceTable() {
		new Thread() {
			public void run() {
				try {
					double ret = getSettings().getTotalPriceTable(
							selectedTable, tableName);
					mTotalPrice = ret;
					totalPriceTableHandler.sendEmptyMessage((int) ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void checkOut(final List<Integer> destIId,
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

	private OnClickListener checkOutClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			listTableNameDialog(CHECKOUT_LIST).show();
		}
	};

	private OnClickListener manageClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setClassToActivity(ManageActivity.class);
		}
	};

	private OnClickListener backClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	private OnClickListener logoutClicked = new OnClickListener() {
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

	private DialogInterface.OnClickListener logoutPositiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			Info.logout();
			finish();
		}
	};
}