package com.htb.cnk;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.TableSetting;

public class NotificationTableService extends Service {

	private final int DISABLE_GRIDVIEW = 10;
	private final int MILLISECONDS = 1000 * 10;
	private Notifications mNotificaion = new Notifications();
	private TableSetting mSettings = new TableSetting();
	private final int UPDATE_TABLE_INFOS = 5;
	private Intent intent = new Intent(SERVICE_IDENTIFIER);
	private Bundle bundle = new Bundle();
	private Handler objHandler = new Handler();
	public static final String SERVICE_IDENTIFIER = "cainaoke.service";
	public final static String SER_KEY = "cainaoke.ser";
	private MyBinder myBinder = new MyBinder();

	public class MyBinder extends Binder {
		public void start() {
			new Thread(new tableThread()).start();
		}
	}

	public Runnable mTasks = new Runnable() {
		public void run() {
			this.update();
		}

		void update() {
			new Thread(new tableThread()).start();
			objHandler.postDelayed(mTasks, MILLISECONDS);
		}
	};

	class tableThread implements Runnable {
		public void run() {
			try {
				tableHandle.sendEmptyMessage(DISABLE_GRIDVIEW);
				int ret = mNotificaion.getNotifiycations();
				ringtoneHandler.sendEmptyMessage(ret);
				ret = mSettings.getTableStatusFromServer();
				if (ret < 0) {
					tableHandle.sendEmptyMessage(ret);
				} else {
					tableHandle.sendEmptyMessage(UPDATE_TABLE_INFOS);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private Handler ringtoneHandler = new Handler() {
		public void handleMessage(Message msg) {
			intent.putExtra("ringtoneMessage", msg.what);
			sendBroadcast(intent);
		}
	};

	private Handler tableHandle = new Handler() {
		public void handleMessage(Message msg) {
			// 发送特定action的广播
			intent.putExtra("tableMessage", msg.what);
			bundle.putSerializable(SER_KEY, mSettings);
			intent.putExtras(bundle);
			sendBroadcast(intent);
		}
	};

	@Override
	public void onCreate() {
		Log.d("service", "onCreate");
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("service", "onStart");
		objHandler.postDelayed(mTasks, 1);
		super.onStart(intent, startId);
	}

	@Override
	public void onDestroy() {
		objHandler.removeCallbacks(mTasks);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {

		return myBinder;
	}

}