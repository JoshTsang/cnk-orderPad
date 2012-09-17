package com.htb.cnk.lib;

import java.io.IOException;

import com.htb.cnk.R;
import com.htb.cnk.data.Setting;

import android.content.Context;
import android.media.MediaPlayer;

public class Ringtone {
	protected MediaPlayer mediaPlayer;
	
	public Ringtone(Context context) {
		mediaPlayer=MediaPlayer.create(context, R.raw.ringtone);
	}
	
	public void play() {
		if (!Setting.enabledRingtong()) {
			return ;
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

	@Override
	protected void finalize() throws Throwable {
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		super.finalize();
	}
	
	
}
