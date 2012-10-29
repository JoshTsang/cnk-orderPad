package com.htb.cnk.fragment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.R;
import com.htb.cnk.data.Setting;
import com.htb.cnk.lib.Ringtone;

public class RingtoneSettingFragment extends Fragment {

	public final static String TAG = "RingtoneSettingFragment";
	
	CheckBox ringtone;
	CheckBox notifyOnlyCharged;
	Button play;
	Button chooseRingtone;
	TextView ringtoneSetting;
	Ringtone ringtonePlayer;
	AlertDialog.Builder dlg;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.ringtone_setting, container,
				false);

		ringtone = (CheckBox) v.findViewById(R.id.ringtone);
		notifyOnlyCharged = (CheckBox) v.findViewById(R.id.notifyOnlyForCharged);
		play = (Button) v.findViewById(R.id.PlayRingtone);
		chooseRingtone = (Button) v.findViewById(R.id.chooseRingtone);
		ringtoneSetting = (TextView) v.findViewById(R.id.ringtoneSetting);
		
		setRingtoneType(Setting.enabledCustomedRingtone());
		ringtone.setChecked(Setting.enabledRingtong());
		notifyOnlyCharged.setChecked(Setting.enabledAreaRingtone());
		enableNotifyOnlyCharged(Setting.enabledRingtong());
		
		play.setOnClickListener(playClicked);
		chooseRingtone.setOnClickListener(chooseRingtoneClicked);
		ringtone.setOnCheckedChangeListener(ringtoneCheckedChange);
		notifyOnlyCharged.setOnCheckedChangeListener(notifyOnlyChargedCheckedChange);
		
		ringtonePlayer = new Ringtone(getActivity());
		return v;
	}


	private void setRingtoneType(boolean isCustomed) {
		if (isCustomed) {
			ringtoneSetting.setText("用户提示音");
		} else {
			ringtoneSetting.setText("系统提示音");
		}
	}
	

	private void enableNotifyOnlyCharged(boolean enable) {
		if (enable) {
			notifyOnlyCharged.setVisibility(View.VISIBLE);
		} else {
			notifyOnlyCharged.setVisibility(View.INVISIBLE);
		}
	}


	OnCheckedChangeListener ringtoneCheckedChange = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Setting.enableRingtong(isChecked);
			enableNotifyOnlyCharged(isChecked);
		}

	};
	
	OnCheckedChangeListener notifyOnlyChargedCheckedChange = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Setting.enableAreaRingtone(isChecked);
		}

	};
	
	OnClickListener chooseRingtoneClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (dlg == null) {
				dlg = new AlertDialog.Builder(getActivity());
				dlg.setItems(R.array.ringtoneType, ringtoneTypeSelect);
				dlg.setPositiveButton("确定", null);
				dlg.setTitle("选择提示音");
			}
			dlg.show();
		}
	};

	DialogInterface.OnClickListener ringtoneTypeSelect = new DialogInterface.OnClickListener() {
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case 0:
				Setting.enableCustomedRingtone(false);
				setRingtoneType(false);
				ringtonePlayer.setUpdate(true);
				break;
			case 1:
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	            intent.setType("audio/*");
	            startActivityForResult(intent, 0);
				break;
			default:
				break;
			}
		}
	};
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		  super.onActivityResult(requestCode, resultCode, data);
		  if (resultCode == Activity.RESULT_OK) {
			  Uri uri = data.getData();
			  String path = uri.getPath();
			  String extension = path.substring(path.lastIndexOf(".")+1, path.length());
			  if (!"mp3".equals(extension.toLowerCase())) {
				  Toast.makeText(getActivity(),
						  "请选择mp3格式的音频文件", Toast.LENGTH_LONG).show();
			  } else {
				try {
					if (saveRingtone(path) < 0) {
						Toast.makeText(getActivity(),
								  "设置失败", Toast.LENGTH_LONG).show();
					} else {
					    Setting.enableCustomedRingtone(true);
					    setRingtoneType(true);
					    ringtonePlayer.setUpdate(true);
					    Toast.makeText(getActivity(),
							      "设置成功", Toast.LENGTH_LONG).show();
					}
					
				} catch (IOException e) {
					Toast.makeText(getActivity(),
							  "设置失败", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			  }
			  Log.d(TAG, uri.getPath() + " ext:" + extension);
		  }
	};
	
	private int saveRingtone(String src) throws IOException {
		FileInputStream is = new FileInputStream(src);
		FileOutputStream fileOutputStream = null;
		if (is != null) {
			fileOutputStream = getActivity().openFileOutput(
					"ringtone.mp3", Activity.MODE_WORLD_READABLE);

			byte[] buf = new byte[1024];
			int ch = -1;
			while ((ch = is.read(buf)) != -1) {
				fileOutputStream.write(buf, 0, ch);
			}
		}
		fileOutputStream.flush();
		if (fileOutputStream != null) {
			fileOutputStream.close();
		}
		
		return 0;
	}
	
	OnClickListener playClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			ringtonePlayer.playForSetting();
		}
	};
}