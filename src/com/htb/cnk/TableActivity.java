package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.data.UserData;
import com.htb.cnk.lib.BaseActivity;

public class TableActivity extends BaseActivity {

	private TableSetting mSettings = new TableSetting();
	protected List<Map<String, String>> mTableSettings = new ArrayList<Map<String, String>>();
	private MyOrder myOrder = new MyOrder();
	private Button mBackBtn;
	private Button mUpdateBtn;
	private Button mStatisticsBtn;
	private Button mManageBtn;
	private ProgressDialog mpDialog;
	private int tableId;
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
				int ret = mSettings.getTableStatus();
				if (ret < 0) {
					tableHandle.sendEmptyMessage(ret);
					return;
				}
				msg.what = ret;
				tableHandle.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Handler tableHandle = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.tableWarning),
						Toast.LENGTH_SHORT).show();
			} else {
				GridView gridview = (GridView) findViewById(R.id.gridview);
				ArrayList<HashMap<String, String>> lstImageItem = new ArrayList<HashMap<String, String>>();
				mTableSettings.clear();
				for (int i = 0; i < mSettings.size(); i++) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("ItemText", "第" + mSettings.getName(i) + "桌");
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
	
	class refreshThread implements Runnable {
		public void run() {
			try {
				Message msg = new Message();
				mSettings.clear();
				int ret = mSettings.getTableStatus();
				if (ret < 0) {
					refreshHandle.sendEmptyMessage(ret);
					return;
				}
				msg.what = ret;
				refreshHandle.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Handler refreshHandle = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.tableWarning),
						Toast.LENGTH_SHORT).show();
			} else {
			//	Log.d("status", "status:"+ mSettings.getstatus(tableId)+"Id:"+tableId);
				Info.setTableName(mSettings.getName(tableId));
				Info.setTableId(mSettings.getId(tableId));
				final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
				Dialog addDialog = new AlertDialog.Builder(TableActivity.this)
						.setTitle("选择功能")
						// 设置标题
						.setSingleChoiceItems(new String[] { "开台（客户模式）", "开台（服务员模式）" }, 0,
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
											TableActivity.this.startActivity(intent);
											TableActivity.this.finish();
											break;
										case 1:
											intent.setClass(TableActivity.this,
													MenuActivity.class);
											Info.setMode(Info.WORK_MODE_WAITER);
											TableActivity.this.startActivity(intent);
											break;
										}
									}
								}).setNegativeButton("取消", null).create();

				Dialog cleanDialog = new AlertDialog.Builder(TableActivity.this)
						.setTitle("选择功能")
						// 设置标题
						.setSingleChoiceItems(new String[] { "清台", "删除菜", "添加菜","查看菜" },
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
																		.getId(tableId),
																0);
														mSettings
																.CleanTalble(mSettings
																		.getId(tableId));

													} catch (Exception e) {
														e.printStackTrace();
													}
												}
											}.start();
											break;
										case 1:
											intent.setClass(TableActivity.this,
													DelOrderActivity.class);
											TableActivity.this.startActivity(intent);
											break;
										case 2:
											myOrder.clear();
											intent.setClass(TableActivity.this,
													MenuActivity.class);
											Info.setMode(Info.WORK_MODE_WAITER);
											TableActivity.this.startActivity(intent);
											break;
										case 3:
											intent.setClass(TableActivity.this,
													QueryOrderActivity.class);
											TableActivity.this.startActivity(intent);
										}
										
									}
								}).setNegativeButton("取消", null).create();

				if (mSettings.getstatus(tableId) == 0) {
					addDialog.show();
				} else {
					cleanDialog.show();
				}


			}
		}
	};
	
	class ItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
													// click happened
				View arg1,// The view within the AdapterView that was clicked
				int arg2,// The position of the view in the adapter
				long arg3// The row id of the item that was clicked
		) {
			mpDialog = new ProgressDialog(TableActivity.this);  
	        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mpDialog.setTitle("请稍等");
	        mpDialog.setMessage("正在获取状态...");  
	        mpDialog.setIndeterminate(false);
	        mpDialog.setCancelable(false);
	        mpDialog.show();
			new Thread(new refreshThread()).start();
//			HashMap<String, Object> item = (HashMap<String, Object>) arg0
//					.getItemAtPosition(arg2);
			tableId = arg2;
			

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
			TableActivity.this.finish();

		}
	};
	
	private OnClickListener manageClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent= new Intent();        
            intent.setAction("android.intent.action.VIEW");    
            Uri content_url = Uri.parse(getResources().getString(R.string.manageUri));   
            intent.setData(content_url);
            //intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");   
            startActivity(intent);
			
		}
	};
	
	private OnClickListener statisticsClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			// 点击确定转向登录对话框
			LayoutInflater factory = LayoutInflater.from(TableActivity.this);
			// 得到自定义对话框
			final View DialogView = factory.inflate(R.layout.setting_dialog, null);
			// 创建对话框
			AlertDialog dlg = new AlertDialog.Builder(TableActivity.this)
					.setTitle("登录框").setView(DialogView)// 设置自定义对话框样式
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {// 设置监听事件

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									EditText mUserName = (EditText)DialogView.findViewById(R.id.edit_username);
									final String userName = mUserName.getText().toString();
									EditText mUserPwd = (EditText)DialogView.findViewById(R.id.edit_password);
									final String userPwd = mUserPwd.getText().toString();
									UserData.clean();
									if("".equals(userName) || "".equals(userPwd)){
										dialog.cancel();
									}else{
										UserData.setUserName(userName);
										UserData.setUserPwd(userPwd);
									}
									new Thread(new userThread()).start();
								}
							}).setNegativeButton("取消",// 设置取消按钮
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 点击取消后退出程序
								}
							}).create();// 创建对话框
				dlg.show();// 显示对话框
			
		}
    	
	};
	
	 class userThread implements Runnable {
			public void run() {
				try {
					Message msg = new Message();						
					int ret = UserData.Compare();
					if(ret < 0){
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
							getResources().getString(R.string.userWarning),
							Toast.LENGTH_SHORT).show();
				} else {
					Intent intent = new Intent();
		    		intent.setClass(TableActivity.this, StatisticsActivity.class);
		    		TableActivity.this.startActivity(intent);
				}
			}
	    };
	    
}
