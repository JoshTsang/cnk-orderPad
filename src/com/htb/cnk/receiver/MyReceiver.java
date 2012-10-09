package com.htb.cnk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.htb.cnk.NotificationTableService;
import com.htb.cnk.TableClickActivity;
import com.htb.cnk.data.TableSetting;

public class MyReceiver extends BroadcastReceiver {

	/**
	 * 
	 */
	private final TableClickActivity tableClickReceiver;

	/**
	 * @param tableClickActivity
	 */
	public MyReceiver(TableClickActivity tableClickActivity) {
		tableClickReceiver = tableClickActivity;
	}
 
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		tableClickReceiver.setmRingtoneMsg(bundle.getInt("ringtoneMessage"));
		tableClickReceiver.setmTableMsg(bundle.getInt("tableMessage"));
		tableClickReceiver.setmSettings((TableSetting) bundle
				.getSerializable(NotificationTableService.SER_KEY));
		tableClickReceiver.getmTableHandler().sendEmptyMessage(tableClickReceiver.getmTableMsg());
		tableClickReceiver.getmRingtoneHandler().sendEmptyMessage(tableClickReceiver.getmRingtoneMsg());
	}
}