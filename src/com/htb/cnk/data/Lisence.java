package com.htb.cnk.data;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.htb.cnk.lib.Http;
import com.htb.constant.Server;

public class Lisence {
	public final static int NETWORK_ERR = -1;
	public final static int LEGAL_COPY = 0;
	
	private static final String TAG = "Lisence";
	private static String did;
	
	public static int validateDevice(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		
		did = tm.getDeviceId();
		Log.d(TAG, "uuid:"+did);
		String response = Http.get(Server.PAD_VALIDATE, "UUID="+did);
		if (response == null || "".equals(response)) {
			Log.e(TAG, "respons.null");
			return -1;
		} else {
			int start, end;
			start = response.indexOf("[");
			end = response.indexOf("]");
			if (start < 0 || end < 0) {
				Log.e(TAG, "validateDevice.response:"+response);
				return -1;
			}
			String str = response.substring(start+1, end).trim();
			return Integer.parseInt(str);
		}
	}
	
	public static String getDeviceId() {
		return did;
	}
}
