package com.htb.cnk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Setting {
	private static final String SettingPerferenceName = "setting";
	private static SharedPreferences Perference;
	
	public Setting(Context context) {
		Perference = context.getSharedPreferences(SettingPerferenceName,
			Context.MODE_PRIVATE);
	}
	
	public static boolean enabledPersons() {
		return Perference.getBoolean("persons", false);
	}
	
	public static void enablePersons(boolean enable) {
		commitPerference("persons", enable);
	}
	
	public static boolean enabledRingtong() {
		return Perference.getBoolean("Ringtone", false);
	}
	
	public static void enableRingtone(boolean enable) {
		commitPerference("Ringtone", enable);
	}
	
	private static void commitPerference(String key, boolean value) {
		Editor editor = Perference.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
}
