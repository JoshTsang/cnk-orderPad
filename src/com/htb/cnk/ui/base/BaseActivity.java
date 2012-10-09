package com.htb.cnk.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;

import com.htb.cnk.dialog.ItemDialog;
import com.htb.cnk.dialog.MultiChoiceItemsDialog;
import com.htb.cnk.dialog.TitleAndMessageDialog;
import com.htb.cnk.dialog.ViewDialog;
import com.htb.constant.ErrorNum;
import com.umeng.analytics.MobclickAgent;

public class BaseActivity extends Activity {

	private final static boolean enableUmeng = false;
	protected TitleAndMessageDialog mTitleAndMessageDialog;
	protected ItemDialog mItemDialog;
	protected ViewDialog mViewDialog;
	protected MultiChoiceItemsDialog mMultiChoiceItemsDialog;
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTitleAndMessageDialog = new TitleAndMessageDialog(
				BaseActivity.this);
		mItemDialog = new ItemDialog(BaseActivity.this);
		mViewDialog = new ViewDialog(BaseActivity.this);
		mMultiChoiceItemsDialog = new MultiChoiceItemsDialog(
				BaseActivity.this);
		if (enableUmeng) {
			MobclickAgent.onError(this);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (enableUmeng) {
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onPause() {
		if (enableUmeng) {
			MobclickAgent.onPause(this);
		}
		super.onPause();
	}

	protected boolean isPrinterError(Message msg) {
		return msg.what == ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT
				|| msg.what == ErrorNum.PRINTER_ERR_NO_PAPER;
	}

	protected void toastText(int r) {
		Toast.makeText(getApplicationContext(), getResources().getString(r),
				Toast.LENGTH_LONG).show();
	}

	protected void toastText(String r) {
		Toast.makeText(getApplicationContext(), r, Toast.LENGTH_LONG).show();
	}
}
