package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.data.UserData;
import com.htb.cnk.lib.BaseActivity;

public class TableActivity extends BaseActivity {

	private TableSetting mSettings = new TableSetting();
	protected List<Map<String, String>> mTableSettings = new ArrayList<Map<String, String>>();
	private Button mBackBtn;
	private Button mUpdateBtn;
	private Button mStatisticsBtn;
	private Button mManageBtn;
	private GridView gridview;
	private ProgressDialog mpDialog;
	private int tableId;
	private SimpleAdapter saImageItems;

	@Override
	protected void onResume() {
		mpDialog = new ProgressDialog(TableActivity.this);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setTitle("请稍等");
		mpDialog.setMessage("正在获取状态...");
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(false);
		mpDialog.show();
		new Thread(new tableThread()).start();
		super.onResume();
	}

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
			tableId = arg2;

		}
	}

	class userThread implements Runnable {
		public void run() {
			try {
				Message msg = new Message();
				int ret = UserData.Compare();
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

	private AlertDialog.Builder networkArlertDialog() {
		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
				TableActivity.this);
		mAlertDialog.setTitle("错误");// 设置对话框标题
		mAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
		mAlertDialog.setCancelable(false);
		mAlertDialog.setPositiveButton("重试",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						dialog.cancel();
						mpDialog.show();
						new Thread(new tableThread()).start();
					}
				});
		mAlertDialog.setNegativeButton("退出",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						dialog.cancel();
						finish();
					}
				});
		return mAlertDialog;
	}

	private AlertDialog.Builder cleanTableAlertDialog() {
		final AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(
				TableActivity.this);
		mAlertDialog.setMessage("请确认是否清台");// 设置对话框内容
		mAlertDialog.setCancelable(false);
		mAlertDialog.setPositiveButton("清台",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						dialog.cancel();
						cleanTableThread();
					}
				});
		mAlertDialog.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						dialog.cancel();
					}
				});
		return mAlertDialog;
	}

	private AlertDialog.Builder addDialog() {
		final CharSequence[] additems = { "开台（客户模式）", "开台（服务员模式）" };

		AlertDialog.Builder addDialog = new AlertDialog.Builder(
				TableActivity.this);
		addDialog.setTitle("选择功能") // 标题
				.setIcon(R.drawable.ic_launcher) // icon
			//	.setCancelable(true) // 不响应back按钮
				.setItems(additems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						switch (which) {
						case 0:
							intent.setClass(TableActivity.this,
									MenuActivity.class);
							Info.setMode(Info.WORK_MODE_CUSTOMER);
							Info.setNewCustomer(true);
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
				}).create();
		return addDialog;
	}
	
	private AlertDialog.Builder addPhoneDialog() {
		final CharSequence[] additems = {"手机已点的菜"};

		AlertDialog.Builder addPhoneDialog = new AlertDialog.Builder(
				TableActivity.this);
		addPhoneDialog.setTitle("选择功能") // 标题
				.setIcon(R.drawable.ic_launcher) // icon
			//	.setCancelable(true) // 不响应back按钮
				.setItems(additems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						switch (which) {
//						case 0:
//							intent.setClass(TableActivity.this,
//									MenuActivity.class);
//							Info.setMode(Info.WORK_MODE_CUSTOMER);
//							Info.setNewCustomer(true);
//							TableActivity.this.startActivity(intent);
//							TableActivity.this.finish();
//							break;
//						case 1:
//							intent.setClass(TableActivity.this,
//									MenuActivity.class);
//							Info.setMode(Info.WORK_MODE_WAITER);
//							TableActivity.this.startActivity(intent);
//							break;
						case 0:
							intent.setClass(TableActivity.this,
									PhoneActivity.class);
							TableActivity.this.startActivity(intent);
							break;
						}
					}
				}).create();
		return addPhoneDialog;
	}
	

	private Dialog cleanDialog() {
		final CharSequence[] cleanitems = { "清台", "删除菜", "添加菜", "查看菜" };
		Dialog cleanDialog = new AlertDialog.Builder(TableActivity.this)
				.setTitle("选择功能")
				// 设置标题
				.setItems(cleanitems, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						switch (which) {
						case 0:
							final AlertDialog.Builder mAlertDialog = cleanTableAlertDialog();
							mAlertDialog.show();
							break;
						case 1:
							intent.setClass(TableActivity.this, DelOrderActivity.class);
							TableActivity.this.startActivity(intent);
							break;
						case 2:
							intent.setClass(TableActivity.this,
									MenuActivity.class);
							Info.setMode(Info.WORK_MODE_WAITER);
							TableActivity.this.startActivity(intent);
							break;
						case 3:
							intent.setClass(TableActivity.this, QueryOrderActivity.class);
							TableActivity.this.startActivity(intent);
							break;
						default:break;
						}

					}
				}).create();
		return cleanDialog;
	}

	private Handler tableHandle = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				final AlertDialog.Builder mAlertDialog = networkArlertDialog();
				mAlertDialog.show();
			} else {

				gridview = (GridView) findViewById(R.id.gridview);
				ArrayList<HashMap<String, String>> lstImageItem = new ArrayList<HashMap<String, String>>();
				lstImageItem.clear();
				mTableSettings.clear();
				for (int i = 0; i < mSettings.size(); i++) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("ItemText", "第" + mSettings.getName(i) + "桌");
					lstImageItem.add(map);
				}
				saImageItems = new SimpleAdapter(TableActivity.this,
						lstImageItem, R.layout.table_item,
						new String[] { "ItemText" },
						new int[] { R.id.ItemText }) {

					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						convertView = TableActivity.this.getLayoutInflater()
								.inflate(R.layout.grid_item, null);
						ImageView Image = (ImageView) convertView
								.findViewById(R.id.ItemImage);
						TextView text = (TextView) convertView
								.findViewById(R.id.ItemText);
						text.setText("第" + mSettings.getName(position) + "桌");
						if (mSettings.getStatus(position) == 1) {
							Image.setImageResource(R.drawable.table_blue);

						} else if (mSettings.getStatus(position) == 0) {
							Image.setImageResource(R.drawable.table_red);
						} else if(mSettings.getStatus(position) == 2){
							Image.setImageResource(R.drawable.table_yellow);
						}
						return convertView;
					}

				};
				gridview.setAdapter(saImageItems);
				saImageItems.notifyDataSetChanged();
				gridview.setOnItemClickListener(new ItemClickListener());
			}
		}
	};
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
	private Handler refreshHandle = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.tableWarning),
						Toast.LENGTH_SHORT).show();
			} else {
				Info.setTableName(mSettings.getName(tableId));
				Info.setTableId(mSettings.getId(tableId));

				AlertDialog.Builder addDialog = addDialog();
				AlertDialog.Builder addPhoneDilog = addPhoneDialog();
				Dialog cleanDialog = cleanDialog();
				

				if (mSettings.getStatus(tableId) == 0) {
					addDialog.show();
				//	addPhoneDilog.show();
				} else if(mSettings.getStatus(tableId) == 1){
					cleanDialog.show();
				} else {
					addPhoneDilog.show();
				}
				saImageItems.notifyDataSetChanged();
			}
		}
	};

	private void cleanTableThread() {
		new Thread() {
			public void run() {
				try {
					Message msg = new Message();
					mSettings.updatusStatus(mSettings.getId(tableId), 0);
					mSettings.cleanTalble(mSettings.getId(tableId));
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
		}.start();
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

			// 点击确定转向登录对话框
			LayoutInflater factory = LayoutInflater.from(TableActivity.this);
			// 得到自定义对话框
			final View DialogView = factory.inflate(R.layout.setting_dialog,
					null);
			SharedPreferences sharedPre = getSharedPreferences("userInfo",
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
			String userName = sharedPre.getString("name", "");
			EditText userNameET = (EditText) DialogView
					.findViewById(R.id.edit_username);
			userNameET.setText(userName);
			// 创建对话框
			AlertDialog dlg = new AlertDialog.Builder(TableActivity.this)
					.setTitle("登录框")
					.setView(DialogView)
					// 设置自定义对话框样式
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {// 设置监听事件

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
										SharedPreferences sharedPre = getSharedPreferences(
												"userInfo",
												Context.MODE_WORLD_WRITEABLE
														| Context.MODE_WORLD_READABLE);
										Editor editor = sharedPre.edit();
										editor.putString("name", userName);
										editor.commit();

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
								}
							}).create();// 创建对话框
			dlg.show();// 显示对话框

		}

	};


}
