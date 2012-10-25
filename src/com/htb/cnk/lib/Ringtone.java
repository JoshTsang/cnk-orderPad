package com.htb.cnk.lib;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;

import com.htb.cnk.R;
import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;

public class Ringtone {
	protected MediaPlayer mediaPlayer;
	
	public Ringtone(Context context) {
		mediaPlayer=MediaPlayer.create(context, R.raw.ringtone);
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
	
	public void stop() {
		mediaPlayer.stop();
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
