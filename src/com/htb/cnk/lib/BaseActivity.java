package com.htb.cnk.lib;

import android.app.Activity;
import android.view.KeyEvent;

public class BaseActivity extends Activity {
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if(keyCode == KeyEvent.KEYCODE_BACK){
			finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}
