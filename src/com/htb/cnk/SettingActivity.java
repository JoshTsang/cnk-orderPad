package com.htb.cnk;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.htb.cnk.data.Setting;
import com.htb.cnk.data.UserData;
import com.htb.cnk.lib.BaseActivity;

public class SettingActivity extends BaseActivity {
	private Switch persons;
	private Switch ringtone;
	private Switch pwdCheck;
	private Button back;
	private Switch cleanTableAfterCheckout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		findViews();
		fillData();
		setClickListener();
	} 
	
	private void findViews() {
		persons = (Switch) findViewById(R.id.persons);
		ringtone = (Switch) findViewById(R.id.ringtone);
		back = (Button) findViewById(R.id.back);
		pwdCheck = (Switch) findViewById(R.id.pwdCheck);
		if (!Setting.enabledDebug()) {
			TextView checkPwdText = (TextView) findViewById(R.id.pwdCheckTxt);
			
			checkPwdText.setVisibility(View.GONE);
			pwdCheck.setVisibility(View.GONE);
		}
		cleanTableAfterCheckout = (Switch)findViewById(R.id.cleanTableAfterCheckout);
	}

	private void fillData() {
		persons.setChecked(Setting.enabledPersons());
		ringtone.setChecked(Setting.enabledRingtong());
		back.setOnClickListener(backClicked);
		pwdCheck.setChecked(Setting.enabledPWDCheck());
		cleanTableAfterCheckout.setChecked(Setting.enabledCleanTableAfterCheckout());
	}
	
	private void setClickListener() {
		persons.setOnCheckedChangeListener(personsCheckedChange);
		ringtone.setOnCheckedChangeListener(ringtoneCheckedChange);
		pwdCheck.setOnCheckedChangeListener(pwdCheckedChange);
		cleanTableAfterCheckout.setOnCheckedChangeListener(cleanTableAfterCheckoutChange);
	}
	
	private OnCheckedChangeListener personsCheckedChange = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Setting.enablePersons(isChecked);
		}
		
	};
	
	private OnCheckedChangeListener ringtoneCheckedChange = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Setting.enableRingtone(isChecked);
		}
		
	};
	
	private OnCheckedChangeListener cleanTableAfterCheckoutChange = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Log.d("clean", "cleanchange");
			Setting.enableCleanTableAfterCheckout(isChecked);
		}
		
	};
	
	private OnClickListener backClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};
	
	private OnCheckedChangeListener pwdCheckedChange = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Setting.enablePWDCheck(isChecked);
			if (isChecked) {
				UserData.debugMode();
			}
		}
	};

	@Override
	public void finish() {
		Setting.enableDebug(false);
		super.finish();
	}
}
