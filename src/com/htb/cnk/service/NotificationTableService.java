package com.htb.cnk.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.PendedOrder;
import com.htb.cnk.data.TableSetting;

public class NotificationTableService extends Service {
	private final static String TAG = "NotificationTableService";
	private final int MILLISECONDS = 1000 * 3;
	private final static int UPDATE_TABLE_STATUS_COUNT = 5;
	private Notifications mNotificaion = new Notifications();
	private Intent intent = new Intent(SERVICE_IDENTIFIER);
	private Bundle bundle = new Bundle();
	private static Handler objHandler = new Handler();
	public static final String SERVICE_IDENTIFIER = "cainaoke.service";
	public final static String SER_KEY = "cainaoke.ser";
	private MyBinder myBinder = new MyBinder();
	private PendedOrder pendedOrder = new PendedOrder();
	private int count;
	private int updateTableStatusCount = 0;

	public class MyBinder extends Binder {
		public void start() {
			updateTableStatusCount = UPDATE_TABLE_STATUS_COUNT;
			new Thread(new tableThread()).start();
		} 
		
		public void add(int id, String name, int status, String order) {
			pendedOrder.add(id, name, status, order);
		}
		
		public void cancle(int id) {
			pendedOrder.remove(id);
		}
		
		public int count() {
			return pendedOrder.count();
		}
		
		public int getErr() {
			if (count > 10) {
				return -1;
			}
			return 0;
		}
		
		public void cleanErr() {
			count = 0;
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
				int notification = mNotificaion.getNotifiycations();;
				String ret = null;
				
				if (updateTableStatusCount >= UPDATE_TABLE_STATUS_COUNT) {
					ret = TableSetting.getTableStatusFromServer();
					updateTableStatusCount = 0;
				}
				
				if (notification < 0) {
					count--;
				}
				
				intent.putExtra("networkStatus", notification==-1?false:true);
				intent.putExtra("ringtoneMessage", notification);
				intent.putExtra("tableMessage", ret);
				intent.putExtras(bundle);
				if (pendedOrder.submit() < 0) {
					count++;
				} else {
					count = 0;
				}
				sendBroadcast(intent);
				updateTableStatusCount++;
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
		updateTableStatusCount = UPDATE_TABLE_STATUS_COUNT;
		mTasks.run();
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
