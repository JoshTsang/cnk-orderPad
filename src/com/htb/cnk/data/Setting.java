package com.htb.cnk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Setting {
	private static final String SettingPerferenceName = "setting";
	private static boolean debug = false;
	private static SharedPreferences Perference;

	public static final String[] TITLES = { "基本设置", "铃声设置", "区域设置", };

	public Setting(Context context) {
		Perference = context.getSharedPreferences(SettingPerferenceName,
				Context.MODE_PRIVATE);
	}

	public static void enableDebug(boolean enable) {
		debug = enable;
	}

	public static boolean enabledDebug() {
		return debug;
	}

	public static boolean enabledPersons() {
		return Perference.getBoolean("persons", false);
	}

	public static void enablePersons(boolean enable) {
		commitPerferenceBoolean("persons", enable);
	}

	public static boolean enabledRingtong() {
		return Perference.getBoolean("Ringtone", false);
	}

	public static void enableRingtong(boolean enable) {
		commitPerferenceBoolean("Ringtone", enable);
	}
	
	public static void enableAreaRingtone(boolean enable) {
		commitPerferenceBoolean("RingtoneArea", enable);
	}
	
	public static boolean enabledAreaRingtone() {
		return Perference.getBoolean("RingtoneArea", false);
	}

	public static boolean enabledPWDCheck() {
		return Perference.getBoolean("pwdCheck", true);
	}

	public static void enablePWDCheck(boolean enable) {
		commitPerferenceBoolean("pwdCheck", enable);
	}

	public static boolean enabledCleanTableAfterCheckout() {
		return Perference.getBoolean("trigerCleanTable", false);
	}

	public static void enableCleanTableAfterCheckout(boolean enable) {
		commitPerferenceBoolean("trigerCleanTable", enable);
	}

	public static boolean enableChargedAreaCheckout() {
		return Perference.getBoolean("chargedArea", false);
	}

	public static void enableChargedAreaAfterCheckout(boolean enable) {
		commitPerferenceBoolean("chargedArea", enable);
	}

	
	public static void enableCustomedRingtone(boolean enable) {
		commitPerferenceBoolean("customRingtone", enable);
	}
	
	public static boolean enabledCustomedRingtone() {
		return Perference.getBoolean("customRingtone", false);
	}
	
	public static void enableSaveFlavor(String flavor) {
		commitPerferenceString("flavor", flavor);
	}
	
	public static String enabledGetFlavor() {
		return Perference.getString("flavor", "");
	}
	
	private static void commitPerferenceBoolean(String key, boolean value) {
		Editor editor = Perference.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	private static void commitPerferenceString(String key, String value){
		Editor editor = Perference.edit();
		editor.putString(key, value);
		editor.commit();
	}

}
