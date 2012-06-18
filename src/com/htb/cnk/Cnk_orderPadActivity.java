package com.htb.cnk;

import com.htb.cnk.data.Info;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class Cnk_orderPadActivity extends Activity {
    /** Called when the activity is first created. */
	private ImageButton mMenuBtn;
	private ImageButton mOrderBtn;
	private ImageButton mSettingsBtn;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViews();
        setClickListeners();
    }
    
    private void findViews(){
    	mMenuBtn = (ImageButton) findViewById(R.id.menu);
    	mOrderBtn = (ImageButton) findViewById(R.id.order);
    	mSettingsBtn = (ImageButton) findViewById(R.id.settings);
    }
    
    private void setClickListeners() {
    	mMenuBtn.setOnClickListener(menuClicked);
    	mOrderBtn.setOnClickListener(orderClicked);
    	mSettingsBtn.setOnClickListener(settingsClicked);
    }
    
    private OnClickListener menuClicked = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent();
			intent.setClass(Cnk_orderPadActivity.this, MenuActivity.class);
			Info.setMode(Info.WORK_MODE_CUSTOMER);
			startActivity(intent);
		}
    	
    };
    
    private OnClickListener orderClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(Cnk_orderPadActivity.this, MenuActivity.class);
			Info.setMode(Info.WORK_MODE_WAITER);
			startActivity(intent);
		}
   
    };
    
    private OnClickListener settingsClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(Cnk_orderPadActivity.this, UpdateMenuActivity.class);
			startActivity(intent);
		}
    	
    };
}