package com.htb.cnk.ui.base;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.htb.cnk.R;
import com.htb.cnk.adapter.GuidePageAdapter;
import com.htb.cnk.adapter.TableAdapter;
import com.htb.cnk.data.NotificationTypes;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.lib.GuidePageChangeListener;
import com.htb.cnk.lib.Ringtone;
import com.htb.cnk.service.MyReceiver;
import com.htb.cnk.service.NotificationTableService;
import com.htb.cnk.service.NotificationTableService.MyBinder;

public abstract class TableGridActivity extends BaseActivity {
	public static boolean networkStatus = true;
	public final static int EXTERN_PAGE_NUM = 1; // 除了楼层以外还有几个页面
	public static TableGridActivity instance;

	private final static String TAG = "TableGridDeskActivity";
	private final int UPDATE_TABLE_INFOS = 500;
	private boolean binderFlag;
	private Intent intent;
	private int mTableMsg;
	private int mRingtoneMsg;
	private MyReceiver mReceiver;
	private NotificationTableService.MyBinder binder;
	private TableSetting mSettings;
	private Ringtone mRingtone;
	private Button mOrderNotification;
	private TextView mStatusBar;
	private int currentPage;
	private boolean isPrinterErrShown = false;
	private boolean isActivityActive;
	private TableAdapter mTableInfo;
	private GuidePageAdapter guidePageAdapter;
	private boolean flag = false;
	private ViewPager mPageView;
	private View layout;
	private TextView imgCur;
	private AlertDialog.Builder mNetWrorkAlertDialog ;
	private NotificationTypes mNotificationType = new NotificationTypes();
	
	@Override
	protected void onResume() {
		super.onResume();
		isActivityActive = true;
		if (guidePageAdapter.getImageItem() != null) {
			mTableInfo.clearLstImageItem();
			updateGridViewAdapter(currentPage);
		}

	}
	
	@Override
	protected void onDestroy() {
		unbindService(conn);
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	
	@Override
	protected void onPause() {
		isActivityActive = false;
		super.onPause();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setNewClass();
		findViews();
		intent = new Intent(this,NotificationTableService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		NotificationType();
		instance = this;
	}
	
	public TableSetting getSettings() {
		return mSettings;
	}

	public void setSettings(TableSetting mSettings) {
		this.mSettings = mSettings;
	}

	public int getRingtoneMsg() {
		return mRingtoneMsg;
	}

	public void setRingtoneMsg(int mRingtoneMsg) {
		this.mRingtoneMsg = mRingtoneMsg;
	}

	public int getTableMsg() {
		return mTableMsg;
	}

	public void setTableMsg(String mTableMsg) {
		if (mTableMsg != null) {
			getParseTableSetting(mTableMsg);
		}
	}

	public void setNetworkStatus(boolean status) {
		if (!status) {
			showNetworkErrStatus(getResources().getString(
					R.string.networkErrorWarning));
		} else {
			if (mStatusBar != null) {
				mStatusBar.setVisibility(View.INVISIBLE);
			}
		}
		networkStatus = status;
	}

	public Handler getTableHandler() {
		return tableHandler;
	}

	public void sendTableHandler(int what) {
		tableHandler.sendEmptyMessage(what);
	}

	public Handler getRingtoneHandler() {
		return ringtoneHandler;
	}

	public void sendTableMsg() {
		getTableHandler().sendEmptyMessage(getTableMsg());
	}

	public void sendRingtoneMsg() {
		getRingtoneHandler().sendEmptyMessage(getRingtoneMsg());
	}

	public void checkPendedOrder() {
		if (mPendedOrderNotificationHandler != null) {
			mPendedOrderNotificationHandler.sendEmptyMessage(0);
		}
	}

	public void sendbinderStart() {
		binderStart();
	}

	public void binderStart() {
		if (binderFlag) {
			binder.start();
			return;
		}
	}

	public static boolean isNetworkStatus() {
		return networkStatus;
	}

	/**
	 * 更新当前页码
	 */
	public void setCurPage(int page) {
		switch (page) {
		case 0:
			if (!Setting.enableChargedAreaCheckout()) {
				imgCur.setText("全部");
			} else {
				imgCur.setText("负责区域");
			}
			break;
		default:
			imgCur.setText(page - EXTERN_PAGE_NUM + 1 + "楼");
			break;
		}
		currentPage = page;
	}

	/**
	 * @param page
	 */
	public void updateGridViewAdapter(int page) {
		mTableInfo.clearLstImageItem();
		switch (page) {
		case 0:
			if (!Setting.enableChargedAreaCheckout()) {
				mTableInfo.filterTables(page, TableAdapter.FILTER_NONE);
				imgCur.setText("全部");
			} else {
				mTableInfo.filterTables(page, TableAdapter.FILTER_SCOPE);
				imgCur.setText("负责区域");
			}
			break;
		default:
			mTableInfo.filterTables(page - EXTERN_PAGE_NUM,
					TableAdapter.FILTER_FLOOR);
			break;
		}
		guidePageAdapter.NotifyimageItemDataSetChanged();
	}

	protected View getPageView() {
		return layout;
	}

	protected void showNetworkErrDlg(String msg) {
		mNetWrorkAlertDialog.setMessage(msg).show();
	}

	protected void setClassToActivity(Class<?> setClass) {
		intent.setClass(TableGridActivity.this, setClass);
		TableGridActivity.this.startActivity(intent);
	}

	private void setNewClass() {
		setSettings(new TableSetting(TableGridActivity.this));
		mRingtone = new Ringtone(TableGridActivity.this);
		mTableInfo = new TableAdapter(mSettings, TableGridActivity.this);
		guidePageAdapter = new GuidePageAdapter(TableGridActivity.this,
				mTableInfo);
		mReceiver = new MyReceiver(TableGridActivity.this);
		mNetWrorkAlertDialog = new  AlertDialog.Builder(TableGridActivity.this);
		registerReceiver(mReceiver);
	}

	private void NotificationType() {
		new Thread() {
			public void run() {
				try {
					int ret = mNotificationType.getNotifiycationsType();
					notificationTypeHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private Handler notificationTypeHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				toastText(R.string.notificationTypeWarning);
			}
		}
	};
	
	/**
	 * 
	 */
	private void findViews() {
		LayoutInflater inflater = (LayoutInflater) TableGridActivity.this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.viewpage, null);
		mPageView = (ViewPager) layout.findViewById(R.id.viewPager);
		mOrderNotification = (Button) layout
				.findViewById(R.id.orderNotification);
		mStatusBar = (TextView) layout.findViewById(R.id.statusBar);
		imgCur = (TextView) layout.findViewById(R.id.text);
	}

	private void initPagerView() {
		setCurPage(0);
		
		mPageView.setAdapter(guidePageAdapter);
		mPageView.setOnPageChangeListener(new GuidePageChangeListener(
				TableGridActivity.this, mTableInfo));
	}

	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			binder = (MyBinder) arg1;
			binder.setUPDATETABLESTATUSCOUNT();
			binder.start();
			binderFlag = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};

	private void showNetworkErrStatus(String messages) {
		if (mStatusBar != null) {
			mStatusBar.setVisibility(View.VISIBLE);
			mStatusBar.setText(messages);
		}
	}

	private void registerReceiver(BroadcastReceiver receiver) {
		IntentFilter filter = new IntentFilter(
				NotificationTableService.SERVICE_IDENTIFIER);
		registerReceiver(receiver, filter);
	}

	private Handler mPendedOrderNotificationHandler = new Handler() {
		public void handleMessage(Message msg) {
			int ret = binder.count();
			if (ret > 0) {
				Log.d(TAG, "has order Pending");
				mOrderNotification.setVisibility(View.VISIBLE);
				mOrderNotification.setText("有" + ret + "个订单挂起，系统会自动提交");
				int err = binder.getErr();
				if (err < 0) {
					if (!isPrinterErrShown) {
						if (isActivityActive) {
							new AlertDialog.Builder(TableGridActivity.this)
									.setTitle("错误")
									.setMessage("无法连接打印机或打印机缺纸，请检查打印机")
									.setPositiveButton("确定",
											new DialogInterface.OnClickListener() {
	
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													isPrinterErrShown = false;
													binder.cleanErr();
												}
											}).show();
							isPrinterErrShown = true;
						}
					}
					Log.e(TAG, "submit order failed more than 10 times");
				}
			} else {
				mOrderNotification.setVisibility(View.INVISIBLE);
			}
		}
	};

	private void getParseTableSetting(final String msg) {
		new Thread() {
			public void run() {
				try {
					int ret = getSettings().parseTableSetting(msg);
					if (ret < 0) {
						tableHandler.sendEmptyMessage(ret);
					} else {
						tableHandler.sendEmptyMessage(UPDATE_TABLE_INFOS);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	private Handler tableHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				// if (NETWORK_ARERTDIALOG == 1) {
				// mNetWrorkcancel.cancel();
				// }
				// showNetworkErrDlg(getResources().getString(
				// R.string.networkErrorWarning));
			} else {
				switch (msg.what) {
				case UPDATE_TABLE_INFOS:
					if (!flag) {
						initPagerView();
					}
					updateGridViewAdapter(currentPage);
					flag = true;
					if (getSettings().hasPendedPhoneOrder()) {
						ringtoneHandler.sendEmptyMessage(1);
					}
					break;
				default:
					Log.e(TAG,
							"unhandled case:"
									+ msg.what
									+ (new Exception()).getStackTrace()[2]
											.getLineNumber());
					break;
				}
			}
		}
	};

	private Handler ringtoneHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what > 0) {
				mRingtone.play();
			}
		}
	};

}
