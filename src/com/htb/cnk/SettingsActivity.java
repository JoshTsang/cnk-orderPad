package com.htb.cnk;

import com.htb.cnk.data.TableSetting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;

public class SettingsActivity extends Activity {
	ProgressDialog settingDialog;
	private TableSetting mSettings = new TableSetting();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		// 点击确定转向登录对话框
		LayoutInflater factory = LayoutInflater.from(SettingsActivity.this);
		// 得到自定义对话框
		final View DialogView = factory.inflate(R.layout.setting_dialog, null);
		// 创建对话框
		AlertDialog dlg = new AlertDialog.Builder(SettingsActivity.this)
				.setTitle("登录框").setView(DialogView)// 设置自定义对话框样式
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {// 设置监听事件

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 输入完成后点击“确定”开始登录
								settingDialog = ProgressDialog.show(
										SettingsActivity.this, "请稍等...",
										"正在登录...", true);
								new Thread() {
									public void run() {
										try {
											Message msg = new Message();						
											int ret = 1;
											ret = mSettings.getJson();
											if (ret < 0) {
												handler.sendEmptyMessage(ret);
												return ;
											}
											msg.what = ret;
											handler.sendMessage(msg);
											SettingsActivity.this.finish();
										} catch (Exception e) {
											e.printStackTrace();
										} finally {
											// 登录结束，取消settingDialog对话框
											settingDialog.dismiss();
										}
									}
								}.start();
							}
						}).setNegativeButton("取消",// 设置取消按钮
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// 点击取消后退出程序
								SettingsActivity.this.finish();

							}
						}).create();// 创建对话框
		dlg.show();// 显示对话框
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				
			} else {
				Intent intent = new Intent();
	    		intent.setClass(SettingsActivity.this, TableActivity.class);
	    		SettingsActivity.this.startActivity(intent);
			}
		}

	};

}
