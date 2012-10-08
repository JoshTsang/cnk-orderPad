package com.htb.cnk.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.htb.cnk.R;
import com.htb.cnk.lib.BaseDialog;

public class ViewDialog extends BaseDialog {
	private Context mActivity;

	public ViewDialog(Context context) {
		super(context);
		mActivity = context;
	}

	public AlertDialog.Builder viewDialog(boolean cancelable, View view) {
		return setViewDialog(cancelable, view);
	}

	public AlertDialog.Builder viewAndTitleDialog(boolean cancelable,
			View view, String title) {
		return setViewAndTitleDialog(cancelable, view, title);
	}

	public AlertDialog.Builder viewAndTitleAndMesDialog(boolean cancelable,
			View view, String title, String message) {
		return setViewAndTitleAndMesDialog(cancelable, view, title, message);
	}

	public AlertDialog.Builder viewAndTitleAndButtonDialog(boolean cancelable,
			View view, String title,
			DialogInterface.OnClickListener negativeListener,
			DialogInterface.OnClickListener positiveListener) {
		return viewAndTitleDialog(cancelable, view, title)
				.setPositiveButton(mActivity.getResources().getString(R.string.ok, positiveListener),positiveListener)
						.setNegativeButton(mActivity.getResources().getString(R.string.cancel),negativeListener);
	}
}
