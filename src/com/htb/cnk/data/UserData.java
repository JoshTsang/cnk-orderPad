package com.htb.cnk.data;

import android.util.Log;

import com.htb.cnk.lib.Http;
import com.htb.constant.Server;

public class UserData {
	static protected String mName;
	static protected String mPwd;
	
	public static void clean(){
	  	mName = null;
	  	mPwd = null;
	}
	
	public static String getUserName() {
		return mName;
	}
	
	public static String getUserPwd() {
		return mPwd;
	}
	
	public static void setUserName(String name) {
		UserData.mName = name;
	}
	
	public static void setUserPwd(String pwd) {
		UserData.mPwd = pwd;
	}

	
	public static int Compare(){
		String userPwd = Http.get(Server.GET_PWD, "UNAME="+ UserData.mName);
		Log.d("pwd", "pwd1:"+userPwd+"pwd2:"+UserData.mPwd);
		if(userPwd == null){
			return -1;
		}
		
		String userPerminssion = Http.get(Server.GET_PERMINSSION, "UNAME="+ UserData.mName);
		if (userPerminssion == null){
			return -1;
		}
		Log.d("pwd", "userPerminssion:"+userPerminssion);
		if(userPwd.equals(UserData.mPwd) && "0".equals(userPerminssion)){
			return 1;
		}else{
			return -1;
		}
				
	}
}
