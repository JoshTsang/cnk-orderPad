package com.htb.cnk;

import java.lang.reflect.Method;

import com.htb.cnk.data.UserData;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginDlg {
	public final static int ACTION_SUBMIT = 10;
	private Context mActivity;
	private Class<?> mDestActivity;
	private int mAction;
	
	public LoginDlg(Context context, Class<?> dest) {
		mActivity = context;
		mDestActivity = dest;
	}
	
	public LoginDlg(Context context, int action) {
		mActivity = context;
		mAction = action;
	}
	
	public void show() {
		LayoutInflater factory = LayoutInflater.from(mActivity);
		final View DialogView = factory.inflate(R.layout.setting_dialog, null);
		SharedPreferences sharedPre = mActivity.getSharedPreferences("userInfo",
				Context.MODE_PRIVATE);
		String userName = sharedPre.getString("name", "");
		EditText userNameET = (EditText) DialogView.findViewById(R.id.edit_username);
		
		userNameET.setText(userName);
		
		AlertDialog dlg = new AlertDialog.Builder(mActivity)
				.setTitle("登录框")
				.setView(DialogView)
				.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								EditText mUserName = (EditText) DialogView
										.findViewById(R.id.edit_username);
								final String userName = mUserName.getText()
										.toString();
								EditText mUserPwd = (EditText) DialogView
										.findViewById(R.id.edit_password);
								final String userPwd = mUserPwd.getText()
										.toString();
								UserData.clean();
								if ("".equals(userName)
										|| "".equals(userPwd)) {
									dialog.cancel();
								} else {
									SharedPreferences sharedPre = mActivity.getSharedPreferences(
											"userInfo",
											Context.MODE_PRIVATE);
									Editor editor = sharedPre.edit();
									editor.putString("name", userName);
									editor.commit();

									UserData.setUserName(userName);
									UserData.setUserPwd(userPwd);
								}
								new Thread() {
								public void run() {
									try {
										int ret = UserData.compare();
										userHandle.sendEmptyMessage(ret);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}}.start();
							}
						}).setNegativeButton("取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create();
		dlg.show();
	}
	
	private void login() {
		if (mDestActivity != null) {
			Intent intent = new Intent();
			intent.setClass(mActivity, mDestActivity);
			mActivity.startActivity(intent);
		} else {
			switch(mAction) {
			case ACTION_SUBMIT:
				try {
					Method method = mActivity.getClass().getMethod("submitOrder", new Class[0]);
					method.invoke(mActivity, new Object[0]);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}

	private Handler userHandle = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				switch(msg.what) {
				case UserData.PWD_INCORRECT:
					Toast.makeText(mActivity,
							R.string.userWarning,
							Toast.LENGTH_SHORT).show();
					break;
				case UserData.PWD_NETWORK_ERR:
					Toast.makeText(mActivity,
							"网络错误",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					Log.e("LoginDlg", "unknown err msg");
				}
				
			} else {
				login();
			}
		}
	};
}
