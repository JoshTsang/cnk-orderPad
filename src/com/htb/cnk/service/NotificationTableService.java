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

	private final int MILLISECONDS = 1000 * 15;
	private Notifications mNotificaion = new Notifications();
	private Intent intent = new Intent(SERVICE_IDENTIFIER);
	private Bundle bundle = new Bundle();
	private static Handler objHandler = new Handler();
	public static final String SERVICE_IDENTIFIER = "cainaoke.service";
	public final static String SER_KEY = "cainaoke.ser";
	private MyBinder myBinder = new MyBinder();
	private PendedOrder pendedOrder = new PendedOrder();
	private int count;

	public class MyBinder extends Binder {
		public void start() {
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
//
//	public class PendedOrderBinder extends Binder {
//		
//	}
	
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
				if (pendedOrder.submit() < 0) {
					count++;
				} else {
					count = 0;
				}
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
