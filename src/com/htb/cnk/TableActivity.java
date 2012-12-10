package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.ui.base.TableGridActivity;

public class TableActivity extends TableGridActivity {

	public static final String TAG = "TableActivity";
	private final int CHECKOUT_LIST = 1;

	private int NETWORK_ARERTDIALOG = 0;
	private double mTotalPrice;
	private List<String> tableName = new ArrayList<String>();
	private List<Integer> selectedTable = new ArrayList<Integer>();
	private Button mBackBtn;
	private Button mUpdateBtn;
	private Button mStatisticsBtn;
	private Button mManageBtn;
	private FrameLayout layoutViewPager;
	private AlertDialog.Builder mNetWrorkAlertDialog;

	@Override
	protected void onResume() {
		super.onResume();
		checkAndUpdateMenu();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.table_activity);
		findViews();
		setClickListeners();
	}

	
	private void checkAndUpdateMenu() {
		new Thread() {
			public void run() {
				int menuVer = getCurrentMenuVer();
				if (UpdateMenuActivity.isUpdateNeed(menuVer)) {
					Log.d(TAG, "update Menu needed");
					handlerMenuUpdate.sendEmptyMessage(1);
				} else {
					Log.d(TAG, "no new menu founded, currentMenuVer:" + menuVer);
					handlerMenuUpdate.sendEmptyMessage(0);
				}
			}
		}.start();
	}
	
	private int getCurrentMenuVer() {
		SharedPreferences sharedPre = getSharedPreferences("menuDB",
				Context.MODE_PRIVATE);
		return sharedPre.getInt("ver", -1);
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
		mStatisticsBtn.setOnClickListener(multiOrderClicked);
		mManageBtn.setOnClickListener(manageClicked);
		mNetWrorkAlertDialog = networkDialog();
		layoutViewPager.addView(getPageView());
	}

	private Handler handlerMenuUpdate = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what > 0) {
				Intent intent = new Intent();
				intent.setClass(TableActivity.this,
						UpdateMenuActivity.class);
				startActivity(intent);
			}
		}
	};
	
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

	private OnClickListener checkOutClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			listTableNameDialog(CHECKOUT_LIST).show();
		}
	};

	private OnClickListener multiOrderClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Info.setTableId(MyOrder.MULTI_ORDER);
			Info.setTableName("合点");
			luanchActivity(MenuActivity.class);
		}
	};
	
	private OnClickListener manageClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupMenu popup = new PopupMenu(getBaseContext(), v);
	        popup.getMenuInflater().inflate(R.menu.table_activity_more, popup.getMenu());

	        popup.setOnMenuItemClickListener(popupMenuClicked);

	        popup.show();
		}
	};

	private OnClickListener backClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	private void logoutClicked() {
		mTitleAndMessageDialog.titleAndMessageDialog(false,
				getResources().getString(R.string.notice),
				getResources().getString(R.string.islogOut),
				getResources().getString(R.string.ok),
				logoutPositiveListener,
				getResources().getString(R.string.cancel), null).show();
	};
	
	private PopupMenu.OnMenuItemClickListener popupMenuClicked = new PopupMenu.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
			case R.id.manage:
				luanchActivity(ManageActivity.class);
				break;
			case R.id.logout:
				logoutClicked();
			default:
				break;
            }
            return true;
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