package com.htb.cnk.lib;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.htb.cnk.R;
import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;

public class Ringtone {
	public final static String TAG = "Ringtone";
	
	protected MediaPlayer mediaPlayer;
	protected Context mContext;
	
	public Ringtone(Context context) {
		mContext = context;
		mediaPlayer=MediaPlayer.create(mContext, R.raw.ringtone);
		update();
	}
	
	public void play() {
		if (!Setting.enabledRingtong()) {
			return ;
		}
		if (Setting.enabledAreaRingtone()) {
			if (!willRingForChargedArea()) {
				return ;
			}
		}
		mediaPlayer.stop();
        try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaPlayer.start();
		mediaPlayer.start();
	}
	
	public void playForSetting() {
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
        try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mediaPlayer.start();
	}
	
	public void stop() {
		mediaPlayer.stop();
	}

	public void update() {
		if (Setting.enabledCustomedRingtone()) {
			boolean err = false;
			try {
				Log.d(TAG, mContext.getFilesDir() + "/ringtone.mp3");
				mediaPlayer.reset();
				mediaPlayer.setDataSource(mContext.getFilesDir() + "/ringtone.mp3");
			} catch (IllegalArgumentException e) {
				err = true;
				e.printStackTrace();
			} catch (SecurityException e) {
				err = true;
				e.printStackTrace();
			} catch (IllegalStateException e) {
				err = true;
				e.printStackTrace();
			} catch (IOException e) {
				err = true;
				e.printStackTrace();
			} finally {
				if (err) {
					mediaPlayer.release();
					mediaPlayer = MediaPlayer.create(mContext, R.raw.ringtone);
				}
			}
		} else {
			mediaPlayer.release();
			mediaPlayer = MediaPlayer.create(mContext, R.raw.ringtone);
		}
	}
	
	private boolean willRingForChargedArea() {
		if (TableSetting.hasPhoneOrderPendingForChargedTables()) {
			return true;
		} 
		
		if (Notifications.hasNotificationPendedForChargedArea()) {
			return true;
		}
		
		return false;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		super.finalize();
	}
	
	
}
