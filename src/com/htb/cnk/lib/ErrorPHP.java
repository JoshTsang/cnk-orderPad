package com.htb.cnk.lib;

import java.io.Serializable;

import org.json.JSONObject;

import android.util.Log;

public class ErrorPHP implements Serializable{
	private static final long serialVersionUID = 2L;
	private String mSucc;
	private String mError;
	
	public boolean isSucc(String errorStr,String errorName) {
		
		try {
			JSONObject errorString = new JSONObject(errorStr);
			String succ = errorString.getString("succ");
			if(succ.equals("true")){
				this.mSucc = succ;
				return true;
			}else {
				this.mSucc = "flase";
				this.mError = errorStr;
			}
		} catch (Exception e) {
			
		}
		Log.e(errorName, errorStr);
		return false;
	}

	public String getSucc(){
		return this.mSucc;
	}
	
	public String getErroe(){
		return this.mError;
	}
}
