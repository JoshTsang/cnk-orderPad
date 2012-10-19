package com.htb.cnk.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.TableSetting;

public class NotificationTableService extends Service {

	private final int DISABLE_GRIDVIEW = 1000;
	private final int MILLISECONDS = 1000 * 15;
	private Notifications mNotificaion = new Notifications();
	private final int UPDATE_TABLE_INFOS = 500;
	private Intent intent = new Intent(SERVICE_IDENTIFIER);
	private Bundle bundle = new Bundle();
	private static Handler objHandler = new Handler();
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
				int notification = mNotificaion.getNotifiycations();
				intent.putExtra("ringtoneMessage", notification);
				String ret = TableSetting.getTableStatusFromServer();
				intent.putExtra("tableMessage", ret);
				intent.putExtras(bundle);
				sendBroadcast(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		objHandler.postDelayed(mTasks, 1);
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
