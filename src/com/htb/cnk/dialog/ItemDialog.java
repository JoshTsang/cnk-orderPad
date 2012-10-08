package com.htb.cnk.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.htb.cnk.R;
import com.htb.cnk.lib.BaseDialog;

public class ItemDialog extends BaseDialog {
	private Context mActivity;

	public ItemDialog(Context context) {
		super(context);
		mActivity = context;
	}

	public AlertDialog.Builder itemChooseFunctionDialog(CharSequence[] items,
			DialogInterface.OnClickListener itemListener) {
		return setTitleDialog(true,
				mActivity.getResources().getString(R.string.chooseFunction))
				.setItems(items, itemListener);
	}

	public AlertDialog.Builder itemButtonDialog(boolean cancelable,
			String title,CharSequence[] items,
			DialogInterface.OnClickListener itemListener,
			DialogInterface.OnClickListener negativeListener,
			DialogInterface.OnClickListener positiveListener) {
		return setTitleDialog(cancelable,title)
				.setItems(items, itemListener)
				.setPositiveButton(mActivity.getResources().getString(R.string.ok), positiveListener)
				.setNegativeButton(mActivity.getResources().getString(R.string.cancel), negativeListener);
	}
}
