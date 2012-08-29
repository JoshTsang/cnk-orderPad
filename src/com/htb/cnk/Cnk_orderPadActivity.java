package com.htb.cnk;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.WifiAdmin;
import com.htb.cnk.lib.BaseActivity;

public class Cnk_orderPadActivity extends BaseActivity {

	/** Called when the activity is first created. */
	private ImageButton mMenuBtn;
	private TextView mMenuTxt;
	private ImageButton mSettingsBtn;
	private TextView mSettingsTxt;
	private ProgressDialog mpDialog;
	private final static int UPDATE_MENU = 0;
	private final static int LATEST_MENU = 1;

	private WifiAdmin mWifiAdmin;
	private boolean mWifiLock = false;
	private Thread wifiLockNodifyThread;

	@Override
	protected void onResume() {
		initWifi();
		super.onResume();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		findViews();
		setClickListeners();
		Info.setNewCustomer(true);
		Info.setMode(Info.WORK_MODE_CUSTOMER);
		Info.setTableId(-1);
		mWifiAdmin = new WifiAdmin(Cnk_orderPadActivity.this);
		mpDialog = new ProgressDialog(Cnk_orderPadActivity.this);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setTitle("请稍等");
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(false);
		syncWithServer();
		startReleaseLock(true);
	}

	private void findViews() {
		mMenuBtn = (ImageButton) findViewById(R.id.menu);
		mMenuTxt = (TextView) findViewById(R.id.menuTxt);
		mSettingsBtn = (ImageButton) findViewById(R.id.settings);
		mSettingsTxt = (TextView) findViewById(R.id.settingsTxt);
	}

	private void setClickListeners() {
		mMenuBtn.setOnClickListener(menuClicked);
		mMenuTxt.setOnClickListener(menuClicked);
		mSettingsBtn.setOnClickListener(settingsClicked);
		mSettingsTxt.setOnClickListener(settingsClicked);
	}

	private int getCurrentMenuVer() {
		SharedPreferences sharedPre = getSharedPreferences("menuDB",
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		return sharedPre.getInt("ver", -1);
	}

	private void syncWithServer() {
		mpDialog = new ProgressDialog(Cnk_orderPadActivity.this);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// mpDialog.setTitle("请稍等");
		mpDialog.setMessage("正在与服务器同步...");
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(false);
		mpDialog.show();
		new Thread() {
			public void run() {
				int menuVer = getCurrentMenuVer();
				if (UpdateMenuActivity.isUpdateNeed(menuVer)) {
					handlerSync.sendEmptyMessage(UPDATE_MENU);
				} else {
					handlerSync.sendEmptyMessage(LATEST_MENU);
				}
			}
		}.start();
	}

	private OnClickListener menuClicked = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (Info.getTableId() < 0
					|| Info.getMode() != Info.WORK_MODE_CUSTOMER) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.tableNotSet),
						Toast.LENGTH_SHORT).show();
			} else {
				Intent intent = new Intent();
				intent.setClass(Cnk_orderPadActivity.this, MenuActivity.class);
				Info.setMode(Info.WORK_MODE_CUSTOMER);
				startActivity(intent);
			}
		}

	};

	private OnClickListener settingsClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
				LoginDlg loginDlg = new LoginDlg(Cnk_orderPadActivity.this,
						TableActivity.class);
				loginDlg.show();
			} else {
				Intent intent = new Intent();
				intent.setClass(Cnk_orderPadActivity.this, TableActivity.class);
				Cnk_orderPadActivity.this.startActivity(intent);
			}
		}

	};

	private Handler handlerSync = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == UPDATE_MENU) {
				Intent intent = new Intent();
				intent.setClass(Cnk_orderPadActivity.this,
						UpdateMenuActivity.class);
				startActivity(intent);
			}
			mpDialog.cancel();
		}
	};

	public void initWifi() {
		if (mWifiAdmin.checkNetCardState() == 0
				|| mWifiAdmin.checkNetCardState() == 1) {
			wifiDialog().show();
		} else {
			mpDialog.cancel();
		}
	}

	private AlertDialog.Builder wifiDialog() {
		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
				Cnk_orderPadActivity.this);
		mAlertDialog.setTitle("错误");// 设置对话框标题
		mAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
		mAlertDialog.setCancelable(false);
		mAlertDialog.setPositiveButton("连接",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						mpDialog.setMessage("正在连接wifi，请稍等");
						mpDialog.show();
						new Thread(new wifiConnect()).start();
					}
				});
		mAlertDialog.setNegativeButton("退出",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						finish();
					}
				});

		return mAlertDialog;
	}

	class wifiConnect implements Runnable {
		public void run() {
			try {
				mWifiAdmin.openNetCard();
				if (mWifiAdmin.checkNetCardState() == 0
						|| mWifiAdmin.checkNetCardState() == 1) {
					wifiConnectHandle.sendEmptyMessage(-1);
				} else {
					wifiConnectHandle.sendEmptyMessage(1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Handler wifiConnectHandle = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				wifiDialog();
			} else {
				Toast.makeText(Cnk_orderPadActivity.this, "当前wifi状态已经连接", 1)
						.show();
			}
		}
	};

	private void startReleaseLock(boolean flg) {
		mWifiLock = flg;
		if (wifiLockNodifyThread == null) {
			wifiLockNodifyThread = new nodifyWifiLockThead();
			wifiLockNodifyThread.start();
		}
		if (wifiLockNodifyThread.isAlive() == false) {
			Log.d("wifiLockNodifyThread",
					"isAlive: " + wifiLockNodifyThread.getId());
			wifiLockNodifyThread.run();

		}
	}

	private class nodifyWifiLockThead extends Thread {
		public void run() {
			try {
				mWifiAdmin.creatWifiLock();
				mWifiAdmin.acquireWifiLock();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}