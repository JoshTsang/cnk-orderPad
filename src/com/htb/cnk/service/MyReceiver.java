package com.htb.cnk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.htb.cnk.data.TableSetting;
import com.htb.cnk.ui.base.TableGridDeskActivity;

public class MyReceiver extends BroadcastReceiver {
	static final String TAG = "MyReceiver";
	static final String MYREC = "MyReceiver";
	/**
	 * 
	 */
	private final TableGridDeskActivity tableDeskReceiver;

	/**
	 * @param tableDeskActivity
	 */
	public MyReceiver(TableGridDeskActivity tableDeskActivity) {
		tableDeskReceiver = tableDeskActivity;
	}
 
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		tableDeskReceiver.setmSettings((TableSetting) bundle
				.getSerializable(NotificationTableService.SER_KEY));
		tableDeskReceiver.setmRingtoneMsg(bundle.getInt("ringtoneMessage"));
		tableDeskReceiver.setmTableMsg(bundle.getInt("tableMessage"));
		tableDeskReceiver.sendTableMsg();
	}
}