package com.htb.cnk.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.htb.cnk.ui.base.BaseDialog;

public class MultiChoiceItemsDlg extends BaseDialog {
	private Context mActivity;

	public MultiChoiceItemsDlg(Context context) {
		super(context);
		mActivity = context;
	}
	
	public AlertDialog.Builder titleDialog(boolean cancelable,
			String title, 
			CharSequence[] items, boolean[] checkedItems, 
			DialogInterface.OnMultiChoiceClickListener listener,
			String positiveText,DialogInterface.OnClickListener positive,
			String negativeText,DialogInterface.OnClickListener negative){
		return setTitleDialog(cancelable, title).setMultiChoiceItems(items, checkedItems, listener)
				.setPositiveButton(positiveText, positive)
				.setNegativeButton(negativeText, negative);
	}

	
	
}
