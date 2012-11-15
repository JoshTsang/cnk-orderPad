package com.htb.cnk.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.htb.cnk.ui.base.TableGridActivity;

public class MyReceiver extends BroadcastReceiver {
	static final String TAG = "MyReceiver";
	static final String MYREC = "MyReceiver";
	/**
	 * 
	 */
	private final TableGridActivity tableDeskReceiver;

	/**
	 * @param tableDeskActivity
	 */
	public MyReceiver(TableGridActivity tableDeskActivity) {
		tableDeskReceiver = tableDeskActivity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		if (bundle.getInt("tableHandler") > 0) {
			tableDeskReceiver.sendTableHandler(bundle.getInt("tableHandler"));
			tableDeskReceiver.setNetworkStatus(true);
		}
		if (bundle.getBoolean("binder")) {
			tableDeskReceiver.sendbinderStart();
		} else {
			tableDeskReceiver.setRingtoneMsg(bundle.getInt("ringtoneMessage"));
			tableDeskReceiver.setTableMsg(bundle.getString("tableMessage"));
			tableDeskReceiver.setNetworkStatus(bundle
					.getBoolean("networkStatus"));
			tableDeskReceiver.sendRingtoneMsg();
			tableDeskReceiver.checkPendedOrder();
		}

	}
}