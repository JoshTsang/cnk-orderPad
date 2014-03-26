package com.htb.cnk.lib;

import java.io.Serializable;

import org.json.JSONObject;
import com.htb.cnk.utils.MyLog;

public class ErrorPHP implements Serializable{
	private static final long serialVersionUID = 2L;
	private static String mSucc;
	private static String mError;
	
	public static boolean isSucc(String responsePkg,String errorTAG) {
		if(responsePkg == null){
			MyLog.e(errorTAG, errorTAG+".timeOut");
			return false;
		}
		try {
			JSONObject errorString = new JSONObject(responsePkg);
			String succ = errorString.getString("succ");
			if(succ.equals("true")){
				mSucc = succ;
				return true;
			}else {
				mSucc = "flase";
				mError = responsePkg;
			}
		} catch (Exception e) {
			
		}
		MyLog.e(errorTAG, responsePkg);
		return false;
	}

	public static String getSucc(){
		return mSucc;
	}
	
	public static String getErroe(){
		return mError;
	}
}
