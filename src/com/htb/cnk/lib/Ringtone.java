package com.htb.cnk.lib;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;

import com.htb.cnk.R;
import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;

public class Ringtone implements OnPreparedListener, OnCompletionListener{
	public final static String TAG = "Ringtone";
	
	protected MediaPlayer mediaPlayer;
	protected Context mContext;
	protected int init;
	protected static boolean needsUpdate = false; 
	protected static boolean isDefault = true;
	
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
			return ;
		}
        try {
        	mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			update();
		} catch (IOException e) {
			e.printStackTrace();
			update();
		}
	}

	public void setUpdate(boolean v) {
		needsUpdate = v;
	}
	
	public void onPrepared(MediaPlayer player) {
		if (init > 0) {
			player.start();
		}
		init++;
	}
	
	private void update() {
		if (Setting.enabledCustomedRingtone()) {
			if (mediaPlayer == null) {
				mediaPlayer = MediaPlayer.create(mContext, R.raw.ringtone);
				init = 0;
				isDefault = true;
				setLiseners();
			}
			boolean err = false;
			try {
				Log.d(TAG, mContext.getFilesDir() + "/ringtone.mp3");
				mediaPlayer.reset();
				isDefault = false;
				setLiseners();
				mediaPlayer.setDataSource(mContext.getFilesDir() + "/ringtone.mp3");
				init = 1;
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
					init = 0;
					isDefault = true;
					setLiseners();
				}
			}
		} else {
			if (mediaPlayer != null) {
				mediaPlayer.release();
			}
			mediaPlayer = MediaPlayer.create(mContext, R.raw.ringtone);
			isDefault = true;
			init = 0;
			setLiseners();
		}
	}
	
	private void setLiseners() {
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnCompletionListener(this);
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

	@Override
	public void onCompletion(MediaPlayer player) {
		player.stop();
	}
	
}
