package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.TableSetting;

public class TableActivity extends Activity {

	private TableSetting mSettings = new TableSetting();
	protected List<Map<String, String>> mTableSettings = new ArrayList<Map<String, String>>();
	protected int tableButton[];
	private MyOrder myOrder = new MyOrder();
	private Button mBackBtn;
	private Button mUpdateBtn;
	private Button mStatisticsBtn;
	private Button mManageBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.table_activity);
		findViews();
		setClickListeners();
		new Thread(new tableThread()).start();

	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back);
		mUpdateBtn = (Button) findViewById(R.id.updateMenu);
		mStatisticsBtn = (Button) findViewById(R.id.statistics);
		mManageBtn = (Button) findViewById(R.id.management);
	}

	private void setClickListeners() {
		mBackBtn.setOnClickListener(backClicked);
		mUpdateBtn.setOnClickListener(updateClicked);
		mStatisticsBtn.setOnClickListener(statisticsClicked);
		mManageBtn.setOnClickListener(manageClicked);
	}

	class tableThread implements Runnable {
		public void run() {
			try {
				Message msg = new Message();
				mSettings.clear();
				int ret = mSettings.getJson();
				if (ret < 0) {
					userHandle.sendEmptyMessage(ret);
					return;
				}
				msg.what = ret;
				userHandle.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Handler userHandle = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();
			} else {
				GridView gridview = (GridView) findViewById(R.id.gridview);
				ArrayList<HashMap<String, String>> lstImageItem = new ArrayList<HashMap<String, String>>();
				mTableSettings.clear();
				tableButton = new int[mSettings.size()];
				for (int i = 0; i < mSettings.size(); i++) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("ItemText", "第" + mSettings.getId(i) + "桌");
					tableButton[i] = mSettings.getstatus(i);
					lstImageItem.add(map);
				}

				SimpleAdapter saImageItems = new SimpleAdapter(
						TableActivity.this, lstImageItem, R.layout.table_item,
						new String[] { "ItemText" },
						new int[] { R.id.ItemText });
				gridview.setAdapter(saImageItems);
				gridview.setOnItemClickListener(new ItemClickListener());
			}
		}
	};

	class ItemClickListener implements OnItemClickListener {

		@SuppressWarnings("unchecked")
		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
													// click happened
				View arg1,// The view within the AdapterView that was clicked
				int arg2,// The position of the view in the adapter
				long arg3// The row id of the item that was clicked
		) {
			HashMap<String, Object> item = (HashMap<String, Object>) arg0
					.getItemAtPosition(arg2);
			final int TableId = arg2;
			Info.setTableName(Integer.toString(TableId + 1));
			Info.setTableId(TableId + 1);
			final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
			Dialog addDialog = new AlertDialog.Builder(TableActivity.this)
					.setTitle("选择功能")
					// 设置标题
					.setSingleChoiceItems(new String[] { "开台", "快速点菜" }, 0,
							choiceListener)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									int choiceWhich = choiceListener.getWhich();
									myOrder.clear();
									Intent intent = new Intent();
									switch (choiceWhich) {
									case 0:
										intent.setClass(TableActivity.this,
												MenuActivity.class);
										Info.setMode(Info.WORK_MODE_CUSTOMER);
										break;
									case 1:
										intent.setClass(TableActivity.this,
												MenuActivity.class);
										Info.setMode(Info.WORK_MODE_WAITER);
										break;
									}
									TableActivity.this.startActivity(intent);
									TableActivity.this.finish();
								}
							}).setNegativeButton("取消", null).create();

			Dialog clearDialog = new AlertDialog.Builder(TableActivity.this)
					.setTitle("选择功能")
					// 设置标题
					.setSingleChoiceItems(new String[] { "清台", "删除菜", "快速点菜" },
							0, choiceListener)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									int choiceWhich = choiceListener.getWhich();
									Intent intent = new Intent();
									switch (choiceWhich) {
									case 0:
										myOrder.clear();
										new Thread() {
											public void run() {
												try {
													mSettings.UpdatusStatus(
															mSettings
																	.getId(TableId),
															0);
													mSettings
															.CleanTalble(mSettings
																	.getId(TableId));

												} catch (Exception e) {
													e.printStackTrace();
												}
											}
										}.start();
										intent.setClass(TableActivity.this,
												TableActivity.class);
										break;
									case 1:
										intent.setClass(TableActivity.this,
												DelOrderActivity.class);
										break;
									case 2:
										myOrder.clear();
										intent.setClass(TableActivity.this,
												MenuActivity.class);
										Info.setMode(Info.WORK_MODE_WAITER);
										break;
									}
									TableActivity.this.startActivity(intent);
									TableActivity.this.finish();
								}
							}).setNegativeButton("取消", null).create();

			if (mSettings.getstatus(arg2) == 0) {
				addDialog.show();
			} else {
				clearDialog.show();
			}

		}
	}

	private class ChoiceOnClickListener implements
			DialogInterface.OnClickListener {

		private int which = 0;

		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			Log.d("a", "a" + which);
			this.which = which;
		}

		public int getWhich() {
			return which;
		}
	}
	
	private OnClickListener backClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TableActivity.this.finish();
		}
	};
	
	private OnClickListener updateClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(TableActivity.this, UpdateMenuActivity.class);
			TableActivity.this.startActivity(intent);
		}
	};
	
	private OnClickListener statisticsClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			//TODO check permission
			Intent intent = new Intent();
			intent.setClass(TableActivity.this, StatisticsActivity.class);
			TableActivity.this.startActivity(intent);
		}
	};
	
	private OnClickListener manageClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
	};
}
