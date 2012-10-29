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
	protected boolean prepared = false;
	protected static boolean needsUpdate = false; 
	
	public Ringtone(Context context) {
		mContext = context;
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
		
		playForSetting();
		needsUpdate = false;
	}
	
	public void playForSetting() {
		if (needsUpdate) {
			update();
		}
		
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
        try {
        	if (!prepared) {
        		mediaPlayer.prepare();
        		prepared = true;
        	}
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

	public void setUpdate(boolean v) {
		needsUpdate = v;
	}
	
	private void update() {
		if (Setting.enabledCustomedRingtone()) {
			if (mediaPlayer == null) {
				mediaPlayer = MediaPlayer.create(mContext, R.raw.ringtone);
				prepared = true;
			}
			boolean err = false;
			try {
				Log.d(TAG, mContext.getFilesDir() + "/ringtone.mp3");
				mediaPlayer.reset();
				prepared = false;
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
					prepared = false;
				}
			}
		} else {
			if (mediaPlayer != null) {
				mediaPlayer.release();
			}
			mediaPlayer = MediaPlayer.create(mContext, R.raw.ringtone);
			prepared = true;
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
