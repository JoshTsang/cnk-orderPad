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
import com.htb.cnk.data.NotificationTypes;
import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.data.UserData;
import com.htb.cnk.lib.BaseActivity;

public class TableActivity extends BaseActivity {

	private TableSetting mSettings = new TableSetting();
	protected List<Map<String, String>> mTableSettings = new ArrayList<Map<String, String>>();
	private MyOrder mMyOrder = new MyOrder();
	private Button mBackBtn;
	private Button mUpdateBtn;
	private Button mStatisticsBtn;
	private Button mManageBtn;
	private GridView gridview;
	private ProgressDialog mpDialog;
	private SimpleAdapter saImageItems;
	private Handler handler = new Handler();
	private ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
	private Notifications mNotificaion = new Notifications();
	private NotificationTypes mNotificationType = new NotificationTypes();

	@Override
	protected void onDestroy() {
		handler.removeCallbacks(runnable); // 停止刷新
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		handler.removeCallbacks(runnable); // 停止刷新
		super.onStop();
	}

	@Override
	protected void onResume() {
		mpDialog.show();
		handler.postDelayed(runnable, 1000 * 1);
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.table_activity);
		findViews();
		setClickListeners();
		mpDialog = new ProgressDialog(TableActivity.this);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setTitle("请稍等");
		mpDialog.setMessage("正在获取状态...");
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(false);

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
				mMyOrder.clear();
				mSettings.clear();
				mNotificaion.clear();
				mNotificaion.getNotifiycations();
				mNotificationType.getNotifiycationsType();
				int ret = mSettings.getTableStatus();
				mpDialog.cancel();
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

	class cleanNotification implements Runnable {
		public void run() {
			try {
				Message msg = new Message();
				handler.removeCallbacks(runnable); // 停止刷新
				int ret = mNotificaion.cleanNotifications(Info.getTableId());
				if (ret < 0) {
					notificationHandle.sendEmptyMessage(ret);
					return;
				}
				msg.what = ret;
				notificationHandle.sendMessage(msg);
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

			Info.setTableName(mSettings.getName(arg2));
			Info.setTableId(mSettings.getId(arg2));
			int id = mSettings.getStatus(arg2);
			switch (id) {
			case 1:
				Dialog cleanDialog = cleanDialog();
				cleanDialog.show();
				break;
			case 2:
				AlertDialog.Builder addPhoneDilog = addPhoneDialog();
				addPhoneDilog.show();
				break;
			case 100:
			case 101:
			case 102:
				AlertDialog.Builder notificationDialog = notificationDialog();
				notificationDialog.show();
				break;
			default:
				AlertDialog.Builder addDialog = addDialog();
				addDialog.show();
				break;
			}
			saImageItems.notifyDataSetChanged();

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

	private AlertDialog.Builder networkDialog() {
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

	private AlertDialog.Builder cleanTableDialog() {
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
				// .setCancelable(true) // 不响应back按钮
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
		final CharSequence[] additems = { "手机已点的菜", "清台" };

		AlertDialog.Builder addPhoneDialog = new AlertDialog.Builder(
				TableActivity.this);
		addPhoneDialog.setTitle("选择功能") // 标题
				.setIcon(R.drawable.ic_launcher) // icon
				// .setCancelable(true) // 不响应back按钮
				.setItems(additems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						switch (which) {
						case 0:
							intent.setClass(TableActivity.this,
									PhoneActivity.class);
							TableActivity.this.startActivity(intent);
							break;
						case 1:
							final AlertDialog.Builder mAlertDialog = cleanTableDialog();
							mAlertDialog.show();
							break;
						default:
							break;
						}
					}
				}).create();
		return addPhoneDialog;
	}

	private AlertDialog.Builder notificationDialog() {
		List<String> add = mNotificaion
				.getNotifiycationsType(Info.getTableId());
		String[] additems = (String[]) add.toArray(new String[add.size()]);
		AlertDialog.Builder addPhoneDialog = new AlertDialog.Builder(
				TableActivity.this);
		addPhoneDialog.setTitle("客户呼叫需求").setIcon(R.drawable.ic_launcher)
				.setItems(additems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setNegativeButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(new cleanNotification()).start();
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
							mMyOrder.clear();
							final AlertDialog.Builder mAlertDialog = cleanTableDialog();
							mAlertDialog.show();
							break;
						case 1:
							intent.setClass(TableActivity.this,
									DelOrderActivity.class);
							TableActivity.this.startActivity(intent);
							break;
						case 2:
							mMyOrder.clear();
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
				}).create();
		return cleanDialog;
	}

	private Handler tableHandle = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				final AlertDialog.Builder mAlertDialog = networkDialog();
				mAlertDialog.show();
			} else {
				gridview = (GridView) findViewById(R.id.gridview);
				mTableSettings.clear();
				Log.d("Notification", "NotificationNum:" + mNotificaion.size());
				if (lstImageItem.size() > 0) {
					for (int i = 0, n = 0; i < mSettings.size(); i++) {
						int status = mSettings.getStatus(i);
						if (status < 100
								&& mNotificaion.getId(n) == mSettings.getId(i)) {

							status = status + 100;
							n++;
							Log.d("mNotificaionID", "mNotificaionID"
									+ mNotificaion.getId(n)
									+ " mSettings.getId:" + mSettings.getId(i));
							Log.d("statusImage", "statusImage" + status
									+ " num:" + i);
						}

						switch (status) {
						case 1:
							lstImageItem.get(i).put("imageItem",
									R.drawable.table_blue);
							break;
						case 2:
							lstImageItem.get(i).put("imageItem",
									R.drawable.table_yellow);
							break;
						case 100:
							lstImageItem.get(i).put("imageItem",
									R.drawable.table_rednotification);
							mSettings.setStatus(i, 100);
							break;
						case 101:
							lstImageItem.get(i).put("imageItem",
									R.drawable.table_bluenotification);
							mSettings.setStatus(i, 101);
							break;
						case 102:
							lstImageItem.get(i).put("imageItem",
									R.drawable.table_yellownotification);
							mSettings.setStatus(i, 102);
							break;
						default:
							lstImageItem.get(i).put("imageItem",
									R.drawable.table_red);
							break;
						}
					}
				} else {
					for (int i = 0, n = 0; i < mSettings.size(); i++) {
						int status = mSettings.getStatus(i);
						if (status < 100
								&& mNotificaion.getId(n) == mSettings.getId(i)) {
							status = status + 100;
							n++;
						}

						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("ItemText", "第" + mSettings.getName(i) + "桌");
						switch (status) {
						case 1:
							map.put("imageItem", R.drawable.table_blue);
							break;
						case 2:
							map.put("imageItem", R.drawable.table_yellow);
							break;
						case 100:
							map.put("imageItem",
									R.drawable.table_rednotification);
							mSettings.setStatus(i, 100);
							break;
						case 101:
							map.put("imageItem",
									R.drawable.table_bluenotification);
							mSettings.setStatus(i, 101);
							break;
						case 102:
							map.put("imageItem",
									R.drawable.table_yellownotification);
							mSettings.setStatus(i, 102);
							break;
						default:
							map.put("imageItem", R.drawable.table_red);
							break;

						}
						lstImageItem.add(map);
					}
					saImageItems = new SimpleAdapter(TableActivity.this,
							lstImageItem, R.layout.grid_item, new String[] {
									"imageItem", "ItemText" }, new int[] {
									R.id.ItemImage, R.id.ItemText }) {
					};
					gridview.setAdapter(saImageItems);
				}
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

	private Handler notificationHandle = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(), "b", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.claenNotificaion),
						Toast.LENGTH_SHORT).show();
				handler.postDelayed(runnable, 100 * 1);
			}
		}
	};

	private void cleanTableThread() {
		new Thread() {
			public void run() {
				try {
					Message msg = new Message();
					
				//	Log.d("aa", tableId + " aaa");
					mSettings.updatusStatus(Info.getTableId(), 0);
					Log.d("aa", mSettings.getStatus(Info.getTableId())+" aaa");
					mMyOrder.delPhoneTable(Info.getTableId(), 0);
					mSettings.cleanTalble(Info.getTableId());
					mSettings.clear();
					mNotificaion.getNotifiycations();
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

	private Runnable runnable = new Runnable() {
		public void run() {
			this.update();
		}

		void update() {
			// 刷新msg的内容
			new Thread(new tableThread()).start();
			handler.postDelayed(this, 1000 * 10);// 间隔20秒
		}
	};
}
