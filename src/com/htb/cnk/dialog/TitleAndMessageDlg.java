package com.htb.cnk.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.htb.cnk.R;
import com.htb.cnk.lib.BaseDialog;

public class TitleAndMessageDlg extends BaseDialog {

	private Context mActivity;

	public TitleAndMessageDlg(Context context) {
		super(context);
		mActivity = context;
	}

	public AlertDialog.Builder networkDialog(
			DialogInterface.OnClickListener positive,
			DialogInterface.OnClickListener negative) {
		return setTitleAndMessageDialog(
				false,
				mActivity.getResources().getString(R.string.error),
				mActivity.getResources()
						.getString(R.string.networkErrorWarning))
				.setPositiveButton(
						mActivity.getResources().getString(R.string.tryAgain),
						positive).setNegativeButton(
						mActivity.getResources().getString(R.string.exit),
						negative);
	}

	public AlertDialog.Builder messageDialog(boolean cancelable,
			String message, String positiveText,DialogInterface.OnClickListener positive,
			String negativeText,DialogInterface.OnClickListener negative) {
		return setMessageDialog(cancelable, message).setPositiveButton(
				positiveText, positive)
				.setNegativeButton(negativeText,negative);
	}
	
	public AlertDialog.Builder titleDialog(boolean cancelable,
			String title, String positiveText,DialogInterface.OnClickListener positive,
			String negativeText,DialogInterface.OnClickListener negative){
		return setTitleDialog(cancelable, title).setPositiveButton(positiveText, positive)
				.setNegativeButton(negativeText, negative);
	}
	
	public AlertDialog.Builder titleAndMessageDialog(boolean cancelable,
			String title, String message,String positiveText,DialogInterface.OnClickListener positive,
			String negativeText,DialogInterface.OnClickListener negative){
		return setTitleAndMessageDialog(cancelable, title, message).setPositiveButton(positiveText, positive)
				.setNegativeButton(negativeText, negative);
	}

}
