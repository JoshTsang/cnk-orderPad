package com.htb.cnk.lib;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;

import com.htb.cnk.R;
import com.htb.cnk.data.Info;

public class GridClickNotification extends GridClick {
	public GridClickNotification(Context context) {
		super(context);
		resultDialog().show();
	}

	@Override
	protected Builder resultDialog() {
		return notificationDialog();
	}

	private AlertDialog.Builder notificationDialog() {
		List<String> add = mNotification.getNotifiycationsType(Info
				.getTableId());
		String[] additems = (String[]) add.toArray(new String[add.size()]);
		return mItemDialog.itemButtonDialog(false, mContext.getResources()
				.getString(R.string.customerCall), additems, null, null,
				notificationListener);
	}

	private DialogInterface.OnClickListener notificationListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			cleanNotification();
		}
	};

	private void cleanNotification() {
		new Thread() {
			public void run() {
				try {
					int ret = mNotification.cleanNotifications(Info
							.getTableId());
					notificationHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private Handler notificationHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				toastText(R.string.notificationWarning);
			} else {
				binderStart();
			}
		}
	};
}
