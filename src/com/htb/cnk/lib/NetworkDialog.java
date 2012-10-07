package com.htb.cnk.lib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.htb.cnk.R;

public class NetworkDialog extends BaseDialog{
	
	private Context mActivity;
	
	public NetworkDialog(Context context) {
		super(context);
		mActivity = context;
	}

	public AlertDialog.Builder networkDialog(DialogInterface.OnClickListener positive,
			DialogInterface.OnClickListener negative) {
		return	setTitleAndMessageDialog(false,
						mActivity.getResources().getString(R.string.error),
						mActivity.getResources().getString(R.string.networkErrorWarning))
				.setPositiveButton(mActivity.getResources().getString(R.string.tryAgain),
						positive)
				.setNegativeButton(mActivity.getResources().getString(R.string.exit),
						negative);
	}

}
