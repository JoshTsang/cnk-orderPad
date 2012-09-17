package com.htb.cnk;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.htb.cnk.data.Setting;
import com.htb.cnk.lib.BaseActivity;

public class SettingActivity extends BaseActivity {
	private Switch persons;
	private Switch ringtone;
	private Button back;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
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
	}

	private void fillData() {
		persons.setChecked(Setting.enabledPersons());
		ringtone.setChecked(Setting.enabledRingtong());
		back.setOnClickListener(backClicked);
	}
	
	private void setClickListener() {
		persons.setOnCheckedChangeListener(personsCheckedChange);
		ringtone.setOnCheckedChangeListener(ringtoneCheckedChange);
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
	
	private OnClickListener backClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};
}
