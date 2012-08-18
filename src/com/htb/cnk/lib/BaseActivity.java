package com.htb.cnk.lib;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

import com.umeng.analytics.MobclickAgent;

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
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MobclickAgent.onError(this);
    }
	
	@Override
	protected void onResume() {
	    super.onResume();
	    MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}
}
