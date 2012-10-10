package com.htb.cnk.service;


import com.htb.cnk.Cnk_orderPadActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartupIntentReceiver extends BroadcastReceiver{
	
	
	@Override 
	  public void onReceive(Context context, Intent intent) { 
	    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
	    { 
	      Intent newIntent = new Intent(context, Cnk_orderPadActivity.class); 
	      newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败 
	      context.startActivity(newIntent);       
	    }       
	  } 
}
