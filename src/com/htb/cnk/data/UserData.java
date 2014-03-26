package com.htb.cnk.data;

import com.htb.cnk.lib.Http;
import com.htb.cnk.utils.MyLog;
import com.htb.constant.Server;

public class UserData {
	final static String TAG = "UserData";
	public final static int PWD_NETWORK_ERR = -1;
	public final static int PWD_INCORRECT = -2;
	public final static int NO_PERMISSION = -3;

	static protected String mId;
	static protected String mName;
	static protected String mPwd;
	static protected int mPermission;

	public static void clean() {
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
		UserData.mName = name.trim();
	}

	public static void setUserPwd(String pwd) {
		UserData.mPwd = pwd;
	}
	
	public static String getUID() {
		return mId;
	}

	public static int compare(int permissionRequire) {
		String userPwd = Http.get(Server.GET_PWD, "UNAME=" + UserData.mName);
		if (userPwd == null) {
			return PWD_NETWORK_ERR;
		}
		
		MyLog.d("pwd", "userPwd:" + userPwd);
		String userPerminssion = Http.get(Server.GET_PERMINSSION, "UNAME="
				+ UserData.mName);
		if (userPerminssion == null) {
			return PWD_NETWORK_ERR;
		}
		int start = userPerminssion.indexOf("[");
		int end = userPerminssion.indexOf("]");

		if ((start < 0) || (end < 0)) {
			MyLog.e("userPerminssion", "userPerminssion is " + userPerminssion);
			return PWD_INCORRECT;
		}

		String userPerminssionRet = userPerminssion.subSequence(start + 1, end)
				.toString();
		if (userPerminssionRet.length() <= 0) {
			MyLog.e("userPermission", "userPermission length < 0");
			return PWD_INCORRECT;
		}

		String pwd[] = userPwd.split(",");
		
		mId = pwd[0];
		
		if (pwd[1].equals(UserData.mPwd)) {
			
		} else {
			MyLog.e("compare", "userPwd.error:" + userPwd + " userPermission:"
					+ userPerminssionRet);
			return PWD_INCORRECT;
		}

		mPermission = Integer.valueOf(userPerminssionRet);
		if (mPermission > permissionRequire) {
			MyLog.e(TAG, "no permission:" + mPermission + " require:" + permissionRequire);
			return NO_PERMISSION;
		}
		
		return 0;
	}
	
	public static int getPermission() {
		return mPermission;
	}
	
	public static void debugMode() {
		mId = "1";
		mName = "admin";
	}
}
