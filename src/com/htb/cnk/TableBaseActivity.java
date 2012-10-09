package com.htb.cnk;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;

import com.htb.cnk.data.Info;
import com.htb.cnk.ui.base.BaseActivity;

public class TableBaseActivity extends BaseActivity {
	protected Button mBackBtn;
	protected Button mUpdateBtn;
	protected Button mStatisticsBtn;
	protected Button mManageBtn;
	protected GridView gridview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.table_activity);
		Info.setMode(Info.WORK_MODE_WAITER);
		findViews();
		
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back);
		mUpdateBtn = (Button) findViewById(R.id.checkOutTable);
		mStatisticsBtn = (Button) findViewById(R.id.logout);
		mManageBtn = (Button) findViewById(R.id.management);
		gridview = (GridView) findViewById(R.id.gridview);
	}


}