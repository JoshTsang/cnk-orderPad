package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.TableInfo;
import com.htb.cnk.lib.ScrollLayout;
import com.htb.cnk.ui.base.TableBaseActivity;
import com.htb.constant.Table;

public class TableActivity extends TableBaseActivity {

	static final String TAG = "TablesActivity";

	private final String IMAGE_ITEM = "imageItem";
	private final String ITEM_TEXT = "ItemText";

	private ArrayList<HashMap<String, Object>> lstImageItem;
	protected Button mBackBtn;
	protected Button mUpdateBtn;
	protected Button mStatisticsBtn;
	protected Button mManageBtn;
	protected GridView mGridView;
	private static int floorNum;

	private int PageCount;
	private ScrollLayout curPage;
	private LinearLayout layoutBottom;
	private List<TableInfo> lstDate = new ArrayList<TableInfo>();
	private TableInfo info = new TableInfo();
	private ImageView imgCur;

	@Override
	protected void onResume() {
		super.onResume();
		if (NETWORK_ARERTDIALOG == 1) {
			mNetWrorkcancel.cancel();
			NETWORK_ARERTDIALOG = 0;
		}
		showProgressDlg(getResources().getString(R.string.getStatus));

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Info.setMode(Info.WORK_MODE_WAITER);
		setContentView(R.layout.table_activity);
		// setViewPager();
		findViews();
		setClickListeners();
		handler();
	}

	private void setGrid() {
		curPage = (ScrollLayout) findViewById(R.id.scr);
		layoutBottom = (LinearLayout) findViewById(R.id.layout_scr_bottom);
		curPage.getLayoutParams().height = this.getWindowManager()
				.getDefaultDisplay().getHeight() * 2 / 3;
		if (mGridView != null) {
			curPage.removeAllViews();
		}
		setTableInfos();
		curPage.setPageListener(new ScrollLayout.PageListener() {
			@Override
			public void page(int page) {
				setCurPage(page);
			}
		});
	}

	/**
	 * 更新当前页码
	 */
	public void setCurPage(int page) {
		layoutBottom.removeAllViews();
		for (int i = 0; i < PageCount; i++) {
			imgCur = new ImageView(TableActivity.this);
			imgCur.setBackgroundResource(R.drawable.bg_img_item);
			imgCur.setId(i);
			// 判断当前页码来更新
			if (imgCur.getId() == page) {
				imgCur.setBackgroundResource(R.drawable.bg_img_item_true);
			}
			layoutBottom.addView(imgCur);
		}
	}

	private void handler() {
		setTableHandler(tableHandler);
		mNotificationHandler = notificationHandler;
		setRingtoneHandler(ringtoneHandler);
		mTotalPriceTableHandler = totalPriceTableHandler;
		mChangeTIdHandler = changeTIdHandler;
		mCopyTIdHandler = copyTIdHandler;
		mCombineTIdHandler = combineTIdHandler;
		mNotificationTypeHandler = notificationTypeHandler;
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back);
		mUpdateBtn = (Button) findViewById(R.id.checkOutTable);
		mStatisticsBtn = (Button) findViewById(R.id.logout);
		mManageBtn = (Button) findViewById(R.id.management);
		// mGridView = (GridView) findViewById(R.id.gridview);

	}

	protected void setClickListeners() {
		mBackBtn.setOnClickListener(backClicked);
		mUpdateBtn.setOnClickListener(checkOutClicked);
		mStatisticsBtn.setOnClickListener(logoutClicked);
		mManageBtn.setOnClickListener(manageClicked);
		mNetWrorkAlertDialog = networkDialog();
	}

	Handler changeTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == -2) {
				toastText(R.string.changeTIdWarning);
			} else if (msg.what == -1) {
				netWorkDialogShow("转台失败，"
						+ getResources()
								.getString(R.string.networkErrorWarning));
			} else if (isPrinterError(msg)) {
				toastText(R.string.changeSucc);
			} else {
				binderStart();
				toastText(R.string.changeSucc);
			}
		}
	};

	Handler copyTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == -2) {
				toastText(R.string.copyTIdwarning);
			} else if (msg.what == -1) {
				netWorkDialogShow("复制失败，"
						+ getResources()
								.getString(R.string.networkErrorWarning));
			} else {
				intent.setClass(TableActivity.this, MyOrderActivity.class);
				Info.setMode(Info.WORK_MODE_WAITER);
				TableActivity.this.startActivity(intent);
			}
		}
	};

	Handler combineTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == -2) {
				toastText(R.string.checkOutWarning);
			} else if (msg.what == -1) {
				netWorkDialogShow("合并出错，"
						+ getResources()
								.getString(R.string.networkErrorWarning));
			} else if (isPrinterError(msg)) {
				toastText(R.string.combineError);
			} else {
				binderStart();
				toastText(R.string.combineSucc);
			}
		}
	};

	Handler ringtoneHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what > 0) {
				mRingtone.play();
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
			if (msg.what < 0) {
				toastText(R.string.notificationTypeWarning);
			}
		}
	};

	Handler totalPriceTableHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				netWorkDialogShow("统计失败，"
						+ getResources()
								.getString(R.string.networkErrorWarning));
			} else {
				mTotalPrice = (double) msg.what;
				if (mTotalPrice <= 0) {
					toastText(R.string.dishNull);
				} else {
					sendPriceToCheckout();
				}
			}
			mpDialog.cancel();
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

	Handler tableHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				if (NETWORK_ARERTDIALOG == 1) {
					mNetWrorkcancel.cancel();
				}
				netWorkDialogShow(getResources().getString(
						R.string.networkErrorWarning));
			} else {
				switch (msg.what) {
				case UPDATE_TABLE_INFOS:
					setGrid();
					if (getSettings().hasPendedPhoneOrder()) {
						ringtoneHandler.sendEmptyMessage(1);
					}
					break;
				case DISABLE_GRIDVIEW:
					if(mGridView != null)
					mGridView.setOnItemClickListener(null);
					break;
				default:
					break;
				}
			}
		}
	};

	protected void setTableInfos() {
		PageCount = getSettings().getFloorNum();
		if (mGridView != null) {
			curPage.removeAllViews();
		}
		for (floorNum = 0; floorNum < PageCount; floorNum++) {
			mGridView = new GridView(TableActivity.this);
			lstImageItem = new ArrayList<HashMap<String, Object>>();
			if (lstImageItem.size() > 0) {
				setStatusAndIcon(floorNum);
			} else {
				setStatusAndIcon(floorNum);
				mImageItems = new SimpleAdapter(TableActivity.this,
						lstImageItem, R.layout.table_item, new String[] {
								IMAGE_ITEM, ITEM_TEXT }, new int[] {
								R.id.ItemImage, R.id.ItemText }) {
				};
				mGridView.setAdapter(mImageItems);
			}
			mGridView.setNumColumns(6);
			mGridView.setHorizontalSpacing(10);
			mGridView.setVisibility(View.VISIBLE);
			mGridView.setOnItemClickListener(tableItemClickListener);
			curPage.addView(mGridView);
		}
	}

	private void setStatusAndIcon(int floorNum) {
		getSettings().setFloorCount(floorNum);
		int tableSize = getSettings().getFloorSize();
		for (int i = 0, n = 0; i < tableSize; i++) {
			int status = getSettings().getStatusIndex(i);
			if (status < Table.NOTIFICATION_STATUS
					&& mNotificaion.getId(n) == getSettings().getIdIndex(i)) {
				status = status + Table.NOTIFICATION_STATUS;
				n++;
			}
			setTableIcon(i, status);
		}
	}

	private void setTableIcon(int position, int status) {
		HashMap<String, Object> map;
		if (lstImageItem.size() <= position) {
			map = new HashMap<String, Object>();
			map.put(ITEM_TEXT, getSettings().getNameIndex(position));
		} else {
			map = lstImageItem.get(position);
		}

		imageItemSwitch(position, status, map);
		if (lstImageItem.size() <= position) {
			lstImageItem.add(map);
		}
	}

	private void imageItemSwitch(int position, int status,
			HashMap<String, Object> map) {
		switch (status) {
		case 0:
			map.put(IMAGE_ITEM, R.drawable.table_red);
			break;
		case 1:
			map.put(IMAGE_ITEM, R.drawable.table_blue);
			break;
		case 50:
		case 51:
			map.put(IMAGE_ITEM, R.drawable.table_yellow);
			break;
		case 100:
			map.put(IMAGE_ITEM, R.drawable.table_rednotification);
			getSettings().setStatus(position, status);
			break;
		case 101:
			map.put(IMAGE_ITEM, R.drawable.table_bluenotification);
			getSettings().setStatus(position, status);
			break;
		case 150:
		case 151:
			map.put(IMAGE_ITEM, R.drawable.table_yellownotification);
			getSettings().setStatus(position, status);
			break;
		default:
			map.put(IMAGE_ITEM, R.drawable.table_red);
			break;
		}
	}
}