package com.htb.cnk.lib;

import java.lang.Thread.State;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.htb.cnk.data.WifiAdmin;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends Activity {
	private final static boolean enableUmeng = false;
	private boolean mWifiLock = false;
	private WifiAdmin mWifiAdmin;
	private Thread wifiLockThread;
	private Thread wifiLockNodifyThread;
	private final int MILLISECONDS = 1000 * 10;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (enableUmeng) {
			MobclickAgent.onError(this);
		}
		mWifiAdmin = new WifiAdmin(BaseActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mWifiLock == true) {
			synchronized (wifiLockNodifyThread) {
				try {
					wifiLockNodifyThread.interrupt();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			wifiLockNodifyThread = null;
			mWifiLock = false;
		}
		if (enableUmeng) {
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		startReleaseLock(true);
		if (enableUmeng) {
			MobclickAgent.onPause(this);
		}
		super.onPause();
	}

	private void startReleaseLock(boolean flg) {
		mWifiLock = flg;
//		if (wifiLockThread == null) {
//			wifiLockThread = new releaseLockThread();
//			Log.d("releaseLock", "getID: " + wifiLockThread.getId());
//			wifiLockThread.start();
//		}
		if (wifiLockNodifyThread == null) {
			wifiLockNodifyThread = new nodifyTableThead();
			wifiLockNodifyThread.start();
		}
		if(wifiLockNodifyThread.isAlive() == false){
			Log.d("wifiLockNodifyThread", "isAlive: " + wifiLockNodifyThread.getId());
			wifiLockNodifyThread.run();
			
		}
	}

//	class releaseLockThread extends Thread {
//		public void run() {
//			if (mWifiLock == true) {
//				try {
//					mWifiAdmin.creatWifiLock();
//					mWifiAdmin.acquireWifiLock();
//					synchronized (wifiLockThread) {
//						try {
//							wait();
//						} catch (Exception e) {
//							e.printStackTrace();
//						}
//					}
//					mWifiAdmin.releaseWifiLock();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			} else {
//				wifiLockThread = null;
//				return;
//			}
//		}
//	}

	private class nodifyTableThead extends Thread {
		public void run() {
			if (mWifiLock == true) {
				try {
					Log.d("nodify", "mWifiLock");
					mWifiAdmin.creatWifiLock();
					mWifiAdmin.acquireWifiLock();
					Thread.sleep(MILLISECONDS);
				} catch (Exception e) {
					Log.d("nodify", "exit");
					Thread.currentThread().interrupt();//重新设置中断标示   
				}
			} else {
				wifiLockNodifyThread = null;
				return;
			}
		}
	}

//	private void nodifyWifiLockThread() {
//		if (wifiLockThread.getState() == State.WAITING) {
//			synchronized (wifiLockThread) {
//				try {
//					wifiLockThread.notify();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

}
