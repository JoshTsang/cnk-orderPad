package com.htb.cnk.lib;

import java.io.IOException;

import com.htb.cnk.R;

import android.content.Context;
import android.media.MediaPlayer;

public class Ringtone {
	protected MediaPlayer mediaPlayer;
	
	public Ringtone(Context context) {
		mediaPlayer=MediaPlayer.create(context, R.raw.ringtone);
	}
	
	public void play() {
		mediaPlayer.stop();
        try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		if (mediaPlayer != null) {
			mediaPlayer.release();
			mediaPlayer = null;
		}
		super.finalize();
	}
	
	
}
