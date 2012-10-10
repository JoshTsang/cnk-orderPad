package com.htb.cnk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.htb.cnk.data.UserData;
import com.htb.cnk.ui.base.BaseActivity;
import com.htb.constant.Permission;


public class ManageActivity extends BaseActivity {
	private Button mBackBtn;
	private Button mUpdateBtn;
	private Button mStatisticsBtn;
	private Button mManageBtn;
	private Button mBackupBtn;
	private Button mRestoreBtn;
	private Button mSettingBtn;
	private Button mPerformanceBtn;
	private TextView mMenuBackupTime;
	private TextView mSalesBackupTime;
	private TextView mPicBackupTime;
	private BackupAndRestoreDlg backupAndRestoreDlg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage_activity);
		findViews();
		setClickListeners();
		fillData();
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back);
		mBackupBtn = (Button) findViewById(R.id.backup);
		mRestoreBtn = (Button) findViewById(R.id.restore);
		mUpdateBtn = (Button) findViewById(R.id.updateMenu);
		mStatisticsBtn = (Button) findViewById(R.id.statistics);
		mManageBtn = (Button) findViewById(R.id.management);
		mSettingBtn = (Button) findViewById(R.id.settings);
		mPerformanceBtn = (Button) findViewById(R.id.performance);
		
		mMenuBackupTime = (TextView) findViewById(R.id.menuBackupTime);
		mSalesBackupTime = (TextView) findViewById(R.id.salesBackupTime);
		mPicBackupTime = (TextView) findViewById(R.id.menuPicBackupTime);
	}
	
	private void setClickListeners() {
		mBackBtn.setOnClickListener(backClicked);
		mBackupBtn.setOnClickListener(backupClicked);
		mRestoreBtn.setOnClickListener(restoreClicked);
		mUpdateBtn.setOnClickListener(updateClicked);
		mStatisticsBtn.setOnClickListener(statisticsClicked);
		mManageBtn.setOnClickListener(manageClicked);
		mSettingBtn.setOnClickListener(settingClicked);
		mPerformanceBtn.setOnClickListener(performanceClicked);
	}
	
	public void fillData() {
		SharedPreferences sharedPre = ManageActivity.this.getSharedPreferences("userInfo",
				Context.MODE_PRIVATE);
		String menuBackupTime = sharedPre.getString("menuBackupTime", "尚未备份");
		String salseBackupTime = sharedPre.getString("salesBackupTime", "尚未备份");
		String picBackupTime = sharedPre.getString("menuPicBackupTime", "尚未备份");
		
		mMenuBackupTime.setText(menuBackupTime);
		mSalesBackupTime.setText(salseBackupTime);
		mPicBackupTime.setText(picBackupTime);
	}
	
	private OnClickListener backClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ManageActivity.this.finish();
		}
	};
	
	private void errDlg(String msg) {
		new AlertDialog.Builder(ManageActivity.this)
		.setTitle("错误")
		.setCancelable(false)
		.setMessage(msg)
		.setPositiveButton("确定", null)
		.show();
	}
	
	private OnClickListener backupClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (UserData.getPermission() > Permission.MANANGER) {
				errDlg("权限不足");
				return;
			}
			
			new AlertDialog.Builder(ManageActivity.this)
			.setTitle("提示")
			.setMessage("确认备份数据")
			.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
									if (backupAndRestoreDlg == null) {
										backupAndRestoreDlg = new BackupAndRestoreDlg(
												ManageActivity.this,
												BackupAndRestoreDlg.ACTION_BACKUP);
									} else {
								backupAndRestoreDlg.show(BackupAndRestoreDlg.ACTION_BACKUP);
							}
						}
					})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {

				}
			}).show();
		} 
	};
	
	private OnClickListener restoreClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (UserData.getPermission() > Permission.MANANGER) {
				errDlg("权限不足");
				return;
			}
			new AlertDialog.Builder(ManageActivity.this)
			.setTitle("提示")
			.setMessage("确认将数据恢复到服务器")
			.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
									if (backupAndRestoreDlg == null) {
										backupAndRestoreDlg = new BackupAndRestoreDlg(
												ManageActivity.this,
												BackupAndRestoreDlg.ACTION_RESTORE);
									} else {
								backupAndRestoreDlg.show(BackupAndRestoreDlg.ACTION_RESTORE);
							}
						}
					})
			.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {

				}
			}).show();
		}
	};
	
	private OnClickListener updateClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(ManageActivity.this, UpdateMenuActivity.class);
			ManageActivity.this.startActivity(intent);
		}
	};

	private OnClickListener performanceClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (UserData.getPermission() > Permission.MANANGER) {
				errDlg("权限不足");
				return;
			}
			
			Intent intent = new Intent();
			intent.setClass(ManageActivity.this, StuffPerformanceActivity.class);
			ManageActivity.this.startActivity(intent);
		
		}
	};
	private OnClickListener manageClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_url = Uri.parse(getResources().getString(
					R.string.manageUri));
			intent.setData(content_url);
			// intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
			startActivity(intent);
		}
	};

	private OnClickListener statisticsClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (UserData.getPermission() > Permission.MANANGER) {
				errDlg("权限不足");
				return;
			}
			
			Intent intent = new Intent();
			intent.setClass(ManageActivity.this, StatisticsActivity.class);
			ManageActivity.this.startActivity(intent);
		}
	};
	
	private OnClickListener settingClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(ManageActivity.this, SettingActivity.class);
			ManageActivity.this.startActivity(intent);
		}
	}; 
}
