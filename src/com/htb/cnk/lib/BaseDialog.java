package com.htb.cnk.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.htb.cnk.R;

public class BaseDialog extends Activity {
	Context mContext;
	AlertDialog.Builder alertDialog;

	public BaseDialog(Context context) {
		mContext = context;
	}

	private AlertDialog.Builder alertDialogBuilder(boolean cancelable) {
		alertDialog = new AlertDialog.Builder(mContext);
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.setCancelable(cancelable);
		return alertDialog;
	}

	public AlertDialog.Builder setViewDialog(boolean cancelable, View view) {
		alertDialog = alertDialogBuilder(cancelable);
		alertDialog.setView(view);
		return alertDialog;
	}

	public AlertDialog.Builder setViewAndTitleDialog(boolean cancelable,
			View view, String title) {
		alertDialog = alertDialogBuilder(cancelable);
		alertDialog.setView(view);
		alertDialog.setTitle(title);
		return alertDialog;
	}

	public AlertDialog.Builder setViewAndTitleAndMesDialog(boolean cancelable,
			View view, String title, String message) {
		alertDialog = alertDialogBuilder(cancelable);
		alertDialog.setView(view);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);// 设置对话框内容
		return alertDialog;
	}

	public AlertDialog.Builder setTitleDialog(boolean cancelable, String title) {
		alertDialog = alertDialogBuilder(cancelable);
		alertDialog.setTitle(title);
		return alertDialog;
	}

	public AlertDialog.Builder setMessageDialog(boolean cancelable,
			String message) {
		alertDialog = alertDialogBuilder(cancelable);
		alertDialog.setMessage(message);
		return alertDialog;
	}

	public AlertDialog.Builder setTitleAndMessageDialog(boolean cancelable,
			String title, String message) {
		alertDialog = alertDialogBuilder(cancelable);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		return alertDialog;
	}

	public AlertDialog.Builder setPositiveAndNegativeButton(AlertDialog.Builder dialog,
			String positiveText,String negativeText, DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener negativeListener) {
		dialog.setPositiveButton(positiveText, positiveListener);
		dialog.setNegativeButton(negativeText, negativeListener);
		return dialog;
	}
	

}
