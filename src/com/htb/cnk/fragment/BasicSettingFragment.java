package com.htb.cnk.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.htb.cnk.R;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.UserData;

public class BasicSettingFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
			// We have different layouts, and in one of them this
			// fragment's containing frame doesn't exist. The fragment
			// may still be created from its saved state, but there is
			// no reason to try to create its view hierarchy because it
			// won't be displayed. Note this is not needed -- we could
			// just run the code below, where we would create and return
			// the view hierarchy; it would just never be used.
			return null;
		}

		View v = inflater
				.inflate(R.layout.basic_settings, container, false);
		Switch persons;
		Switch ringtone;
		Switch pwdCheck;
		Switch cleanTableAfterCheckout;
		OnCheckedChangeListener personsCheckedChange = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Setting.enablePersons(isChecked);
			}

		};

		OnCheckedChangeListener ringtoneCheckedChange = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Setting.enableRingtone(isChecked);
			}

		};

		OnCheckedChangeListener cleanTableAfterCheckoutChange = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Log.d("clean", "cleanchange");
				Setting.enableCleanTableAfterCheckout(isChecked);
			}

		};

		OnCheckedChangeListener pwdCheckedChange = new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Setting.enablePWDCheck(isChecked);
				if (isChecked) {
					UserData.debugMode();
				}
			}
		};
		persons = (Switch) v.findViewById(R.id.persons);
		ringtone = (Switch) v.findViewById(R.id.ringtone);
		pwdCheck = (Switch) v.findViewById(R.id.pwdCheck);
		if (!Setting.enabledDebug()) {
			TextView checkPwdText = (TextView) v
					.findViewById(R.id.pwdCheckTxt);

			checkPwdText.setVisibility(View.GONE);
			pwdCheck.setVisibility(View.GONE);
		}
		cleanTableAfterCheckout = (Switch) v
				.findViewById(R.id.cleanTableAfterCheckout);
		persons.setChecked(Setting.enabledPersons());
		ringtone.setChecked(Setting.enabledRingtong());
		pwdCheck.setChecked(Setting.enabledPWDCheck());
		cleanTableAfterCheckout.setChecked(Setting
				.enabledCleanTableAfterCheckout());
		persons.setOnCheckedChangeListener(personsCheckedChange);
		ringtone.setOnCheckedChangeListener(ringtoneCheckedChange);
		pwdCheck.setOnCheckedChangeListener(pwdCheckedChange);
		cleanTableAfterCheckout
				.setOnCheckedChangeListener(cleanTableAfterCheckoutChange);

		return v;
	}
}
