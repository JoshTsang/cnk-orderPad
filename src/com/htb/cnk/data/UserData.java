package com.htb.cnk.data;

import android.util.Log;

import com.htb.cnk.lib.Http;
import com.htb.constant.Server;

public class UserData {
	public final static int PWD_NETWORK_ERR = -1;
	public final static int PWD_INCORRECT = -2;
	
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

	//TODO need parameter permission
	public static int compare(){
		String userPwd = Http.get(Server.GET_PWD, "UNAME="+ UserData.mName);
		if(userPwd == null){
			return PWD_NETWORK_ERR;
		}
		Log.d("pwd", "userPwd:"+userPwd);
		String userPerminssion = Http.get(Server.GET_PERMINSSION, "UNAME="+ UserData.mName);
		if (userPerminssion == null){
			return PWD_NETWORK_ERR;
		}
		Log.d("pwd", "userPerminssion:"+userPerminssion);
		if(userPwd.equals(UserData.mPwd) && "0".equals(userPerminssion)){
			return 1;
		}else{
			return PWD_INCORRECT;
		}
				
	}
}
