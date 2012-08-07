package com.htb.cnk.lib;

import com.htb.cnk.data.MyOrder;

import android.app.Activity;
import android.view.KeyEvent;

public class BaseActivity extends Activity {
	private MyOrder mMyOrder = new MyOrder();
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			mMyOrder.clear();
			finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

}
