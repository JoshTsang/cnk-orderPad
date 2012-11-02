package com.htb.cnk.ui.base;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.htb.cnk.DelOrderActivity;
import com.htb.cnk.MenuActivity;
import com.htb.cnk.PhoneActivity;
import com.htb.cnk.QueryOrderActivity;
import com.htb.cnk.QuickMenuActivity;
import com.htb.cnk.R;
import com.htb.cnk.adapter.GuidePageAdapter;
import com.htb.cnk.adapter.TableAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.NotificationTypes;
import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.PhoneOrder;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.lib.GuidePageChangeListener;
import com.htb.cnk.lib.Ringtone;
import com.htb.cnk.service.MyReceiver;
import com.htb.cnk.service.NotificationTableService;
import com.htb.cnk.service.NotificationTableService.MyBinder;
import com.htb.constant.Table;

public abstract class TableGridDeskActivity extends BaseActivity {

	private final static String TAG = "TableGridDeskActivity";
	protected final int UPDATE_TABLE_INFOS = 500;
	protected final int CHECKOUT_LIST = 1;
	protected final int COMBINE_DIALOG = 1;
	protected final int CHANGE_DIALOG = 2;

	protected boolean binderFlag;
	protected Intent intent;

	private int mTableMsg;
	private int mRingtoneMsg;
	protected boolean networkStatus;

	private MyReceiver mReceiver;
	protected NotificationTableService.MyBinder binder;
	protected Notifications mNotification = new Notifications();
	protected NotificationTypes mNotificationType = new NotificationTypes();

	protected List<String> tableName = new ArrayList<String>();
	protected List<Integer> selectedTable = new ArrayList<Integer>();

	protected PhoneOrder mPhoneOrder;
	protected TableSetting mSettings;
	protected Ringtone mRingtone;

	protected EditText tableIdEdit;
	protected EditText personsEdit;
	protected Button mOrderNotification;
	protected TextView mStatusBar;

	protected Handler mNotificationHandler;
	protected Handler mChangeTIdHandler;
	protected Handler mCombineTIdHandler;
	protected Handler mCopyTIdHandler;

	protected int currentPage;
	protected boolean isPrinterErrShown = false;
	protected TableAdapter mTableInfo;
	protected GuidePageAdapter guidePageAdapter;

	public final static int EXTERN_PAGE_NUM = 1; // 除了楼层以外还有几个页面
	public boolean flag = false;
	private ViewPager mPageView;
	private ArrayList<View> pageViewsList;
	private View layout;
	private TextView imgCur;
	private LinearLayout layoutBottom;

	@Override
	protected void onResume() {
		super.onResume();
		if (guidePageAdapter.getImageItem() != null) {
			mTableInfo.clearLstImageItem();
			updateGridViewAdapter(currentPage);
		}

	}

	@Override
	protected void onDestroy() {
		unbindService(conn);
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setNewClass();
		findViews();
		startService(NotificationTableService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		mReceiver = new MyReceiver(TableGridDeskActivity.this);
		registerReceiver(mReceiver);
		NotificationType();
	}

	private void setNewClass() {
		mPhoneOrder = new PhoneOrder(TableGridDeskActivity.this);
		setSettings(new TableSetting(TableGridDeskActivity.this));
		mRingtone = new Ringtone(TableGridDeskActivity.this);
		mTableInfo = new TableAdapter(mNotification, mSettings,
				TableGridDeskActivity.this);
		guidePageAdapter = new GuidePageAdapter(TableGridDeskActivity.this,
				mTableInfo);
	}

	/**
	 * 
	 */
	private void findViews() {
		imgCur = new TextView(TableGridDeskActivity.this);
		LayoutInflater inflater = (LayoutInflater) TableGridDeskActivity.this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(R.layout.viewpage, null);
		mPageView = (ViewPager) layout.findViewById(R.id.viewPager);
		layoutBottom = (LinearLayout) layout
				.findViewById(R.id.layout_scr_bottom);
	}

	protected View getPageView() {
		return layout;
	}

	public void initViewPager() {
		addLayoutBottom();
		setCurPage(0);
		mPageView.getLayoutParams().height = this.getWindowManager()
				.getDefaultDisplay().getHeight() * 4 / 5;
		pageViewsList = new ArrayList<View>();
		pageViewsList.add(getLayoutInflater().inflate(R.layout.gridview, null));
		mPageView.setAdapter(guidePageAdapter);
		mPageView.setOnPageChangeListener(new GuidePageChangeListener(
				TableGridDeskActivity.this, mTableInfo));
	}

	/**
	 * 
	 */
	private void addLayoutBottom() {
		imgCur.setTextColor(0xFF4D2412);
		imgCur.setTextSize(22);
		layoutBottom.addView(imgCur);
	}

	/**
	 * 更新当前页码
	 */
	public void setCurPage(int page) {
		switch (page) {
		case 0:
			if (!Setting.enableChargedAreaCheckout()) {
				imgCur.setText("全部");
			} else {
				imgCur.setText("负责区域");
			}
			break;
		default:
			imgCur.setText(page - EXTERN_PAGE_NUM + 1 + "楼");
			break;
		}
		currentPage = page;
	}

	/**
	 * @param page
	 */
	public void updateGridViewAdapter(int page) {
		mTableInfo.clearLstImageItem();
		switch (page) {
		case 0:
			if (!Setting.enableChargedAreaCheckout()) {
				mTableInfo.filterTables(page, TableAdapter.FILTER_NONE);
				imgCur.setText("全部");
			} else {
				mTableInfo.filterTables(page, TableAdapter.FILTER_SCOPE);
				imgCur.setText("负责区域");
			}
			break;
		default:
			mTableInfo.filterTables(page - EXTERN_PAGE_NUM,
					TableAdapter.FILTER_FLOOR);
			break;
		}
		guidePageAdapter.NotifyimageItemDataSetChanged();
	}

	protected ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			binder = (MyBinder) arg1;
			binderFlag = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	};

	protected void showNetworkErrStatus(String messages) {
		if (mStatusBar != null) {
			mStatusBar.setVisibility(View.VISIBLE);
			mStatusBar.setText(messages);
		}
	}

	protected void registerReceiver(BroadcastReceiver receiver) {
		IntentFilter filter = new IntentFilter(
				NotificationTableService.SERVICE_IDENTIFIER);
		registerReceiver(receiver, filter);
	}

	protected void startService(Class<?> cla) {
		intent = new Intent(TableGridDeskActivity.this, cla);
		startService(intent);
	}

	DialogInterface.OnClickListener cleanListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			cleanChioceMode(which);
		}
	};

	protected void binderStart() {
		if (binderFlag) {
			binder.start();
			return;
		}
	}

	public AlertDialog.Builder cleanDialog() {
		final CharSequence[] cleanitems = getResources().getStringArray(
				R.array.openStatus);
		return mItemDialog.itemChooseFunctionDialog(cleanitems, cleanListener);
	}

	DialogInterface.OnClickListener cleanTableListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			showProgressDlg(getResources().getString(R.string.cleanTableNow));
			selectedTable.clear();
			selectedTable.add(Info.getTableId());
			cleanTableThread(selectedTable);
		}
	};

	private void cleanChioceMode(int which) {
		switch (which) {
		case 0:
			if (networkStatus) {
				cleanTableDialog().show();
			} else {
				networkErrDlg();
			}
			break;
		case 1:
			if (networkStatus) {
				changeOrCombineDialog(CHANGE_DIALOG).show();
			} else {
				networkErrDlg();
			}
			break;
		case 2:
			if (networkStatus) {
				changeOrCombineDialog(COMBINE_DIALOG).show();
			} else {
				networkErrDlg();
			}
			break;
		case 3:
			setClassToActivity(DelOrderActivity.class);
			break;
		case 4:
			chooseTypeToMenu();
			break;
		case 5:
			setClassToActivity(QueryOrderActivity.class);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 */
	private void chooseTypeToMenu() {
		if (Info.getMenu() == Info.ORDER_QUCIK_MENU) {
			setClassToActivity(QuickMenuActivity.class);
		} else {
			Info.setMode(Info.WORK_MODE_WAITER);
			setClassToActivity(MenuActivity.class);
		}
	}

	private AlertDialog.Builder cleanTableDialog() {
		return mTitleAndMessageDialog.messageDialog(false, getResources()
				.getString(R.string.isCleanTable),
				getResources().getString(R.string.yes), cleanTableListener,
				getResources().getString(R.string.no), null);
	}

	DialogInterface.OnClickListener addListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mPhoneOrder.clear();
			addDialogChoiceMode(which);
		}
	};

	private AlertDialog.Builder cleanPhoneDialog(final int position) {
		DialogInterface.OnClickListener cleanPhoneListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				showProgressDlg(getResources().getString(
						R.string.cleanPhoneOrderNow));
				cleanPhoneThread(position, Info.getTableId());
			}
		};

		return mTitleAndMessageDialog.messageDialog(false, getResources()
				.getString(R.string.isCleanOrder),
				getResources().getString(R.string.yes), cleanPhoneListener,
				getResources().getString(R.string.no), null);
	}

	public AlertDialog.Builder addDialog() {
		final CharSequence[] additems = getResources().getStringArray(
				R.array.normalStatus);
		return mItemDialog.itemChooseFunctionDialog(additems, addListener);
	}

	DialogInterface.OnClickListener notificationListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			cleanNotification();
		}
	};

	Handler mPendedOrderNotificationHandler = new Handler() {
		public void handleMessage(Message msg) {
			int ret = binder.count();
			if (ret > 0) {
				Log.d(TAG, "has order Pending");
				mOrderNotification.setVisibility(View.VISIBLE);
				mOrderNotification.setText("有" + ret + "个订单挂起，系统会自动提交");
				int err = binder.getErr();
				if (err < 0) {
					if (!isPrinterErrShown) {
						new AlertDialog.Builder(TableGridDeskActivity.this)
								.setTitle("错误")
								.setMessage("无法连接打印机或打印机缺纸，请检查打印机")
								.setPositiveButton("确定",
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												isPrinterErrShown = false;
												binder.cleanErr();
											}
										}).show();
						isPrinterErrShown = true;
					}
					binder.cleanErr();
					Log.e(TAG, "submit order failed more than 10 times");
				}
			} else {
				mOrderNotification.setVisibility(View.INVISIBLE);
			}
		}
	};

	private void addDialogChoiceMode(int which) {
		switch (which) {
		case 0:
			if (networkStatus) {
				Info.setMode(Info.WORK_MODE_CUSTOMER);
				Info.setNewCustomer(true);
				setClassToActivity(MenuActivity.class);
				finish();
			} else {
				networkErrDlg();
			}
			break;
		case 1:
			chooseTypeToMenu();
			break;
		case 2:
			if (networkStatus) {
				copyTableDialog().show();
			} else {
				networkErrDlg();
			}
			break;
		default:
			break;
		}
	}

	public AlertDialog.Builder addPhoneDialog(final int position) {
		final CharSequence[] additems = getResources().getStringArray(
				R.array.phoneStatus);
		DialogInterface.OnClickListener addPhoneListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addPhoneChoiceMode(position, which);
			}
		};

		return mItemDialog.itemChooseFunctionDialog(additems, addPhoneListener);
	}

	private void addPhoneChoiceMode(final int position, int which) {
		switch (which) {
		case 0:
			Info.setMode(Info.WORK_MODE_WAITER);
			setClassToActivity(PhoneActivity.class);
			break;
		case 1:
			cleanPhoneDialog(position).show();
			break;
		default:
			break;
		}
	}

	public AlertDialog.Builder notificationDialog() {
		List<String> add = mNotification.getNotifiycationsType(Info
				.getTableId());
		String[] additems = (String[]) add.toArray(new String[add.size()]);
		return mItemDialog.itemButtonDialog(false,
				getResources().getString(R.string.customerCall), additems,
				null, null, notificationListener);
	}

	public OnItemClickListener tableItemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
													// click happened
				View arg1,// The view within the AdapterView that was clicked
				int arg2,// The position of the view in the adapter
				long arg3// The row id of the item that was clicked
		) {
			Log.d(TAG,
					"arg2:" + arg2 + " name: " + mTableInfo.getName(arg2)
							+ "id: " + mTableInfo.getId(arg2) + " status:"
							+ mTableInfo.getStatus(arg2));
			if (isNameIdStatusLegal(arg2)) {
				Info.setTableName(mTableInfo.getName(arg2));
				Info.setTableId(mTableInfo.getId(arg2));
				tableItemChioceDialog(arg2, mTableInfo.getStatus(arg2));
			} else {
				toastText("不能获取信息，请检查设备！");
			}
		}

		private boolean isNameIdStatusLegal(int arg2) {
			return (mTableInfo.getName(arg2)) != null
					&& (mTableInfo.getId(arg2) != -1)
					&& (mTableInfo.getStatus(arg2) != -1);
		}

		private void tableItemChioceDialog(int arg2, int status) {
			switch (status) {
			case 0:
				addDialog().show();
				break;
			case 1:
				cleanDialog().show();
				break;
			case 50:
			case 51:
				if (networkStatus) {
					addPhoneDialog(arg2).show();
				} else {
					networkErrDlg();
				}
				break;
			case 100:
			case 101:
			case 150:
			case 151:
				if (networkStatus) {
					notificationDialog().show();
				} else {
					networkErrDlg();
				}
				break;
			default:
				addDialog().show();
				break;
			}
		}

	};

	private Builder changeOrCombineDialog(final int type) {
		final AlertDialog.Builder changeTableAlertDialog;
		View layout = getDialogLayout(R.layout.change_dialog,
				R.id.change_dialog);
		personsEdit = (EditText) layout.findViewById(R.id.personsEdit);
		if (Setting.enabledPersons()) {
			tableIdEdit = (EditText) layout.findViewById(R.id.tableIdEdit);
			changeTableAlertDialog = mViewDialog.viewDialog(false, layout);
		} else {
			tableIdEdit = editTextListener();
			changeTableAlertDialog = mViewDialog.viewDialog(false, tableIdEdit);
		}
		tableIdEdit.addTextChangedListener(watcher(tableIdEdit));
		personsEdit.addTextChangedListener(watcher(personsEdit));
		if (type == CHANGE_DIALOG) {
			changeTableAlertDialog.setTitle(getResources().getString(
					R.string.pleaseInput)
					+ "转入桌号");
		} else if (type == COMBINE_DIALOG) {
			changeTableAlertDialog.setTitle(getResources().getString(
					R.string.pleaseInput)
					+ "并入桌号");
		}

		DialogInterface.OnClickListener changeTablePositiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String changePersons;
				String tableName = tableIdEdit.getText().toString()
						.toUpperCase();
				if (Setting.enabledPersons()) {
					changePersons = personsEdit.getText().toString();
				} else {
					changePersons = "0";
				}
				if (type == CHANGE_DIALOG) {
					judgeChangeTable(changePersons, tableName);
				} else if (type == COMBINE_DIALOG) {
					judgeCombineTable(changePersons, tableName);
				}
			}
		};

		changeTableAlertDialog.setPositiveButton(
				getResources().getString(R.string.ok),
				changeTablePositiveListener);
		changeTableAlertDialog.setNegativeButton(
				getResources().getString(R.string.cancel), null);
		return changeTableAlertDialog;
	}

	private Builder copyTableDialog() {
		final EditText copyTableText = editTextListener();
		copyTableText.addTextChangedListener(watcher(copyTableText));

		DialogInterface.OnClickListener copyTableListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String tableName = copyTableText.getEditableText().toString()
						.toUpperCase();
				if (tableName.equals("")) {
					toastText(R.string.idAndPersonsIsNull);
				} else if (isBoundaryLegal(tableName, Table.OPEN_TABLE_STATUS)) {
					copyTable(mSettings.getId(tableName));
				} else {
					toastText(R.string.copyTIdwarning);
				}
			}
		};

		return mViewDialog.viewAndTitleAndButtonDialog(false, copyTableText,
				getResources().getString(R.string.pleaseInput) + "桌号", null,
				copyTableListener);
	}

	private EditText editTextListener() {
		final EditText editText = new EditText(this);
		// editText.setKeyListener(new DigitsKeyListener(false, true));
		// editText
		// .setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
		return editText;
	}

	private View getDialogLayout(int layout_dialog, int id) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(layout_dialog,
				(ViewGroup) findViewById(id));
		return layout;
	}

	private void cleanNotification() {
		new Thread() {
			public void run() {
				try {
					int ret = mNotification.cleanNotifications(Info
							.getTableId());
					mNotificationHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected void cleanTableThread(final List<Integer> tableId) {
		new Thread() {
			public void run() {
				try {
					Message msg = new Message();
					int ret = getSettings().cleanTalble(tableId);
					if (ret < 0) {
						tableHandler.sendEmptyMessage(ret);
						return;
					}
					if (getNotifiycations() < 0)
						return;
					if (getTableStatusFromServer() < 0)
						return;
					msg.what = UPDATE_TABLE_INFOS;
					tableHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void cleanPhoneThread(final int position, final int tableId) {
		new Thread() {
			public void run() {
				try {
					Message msg = new Message();
					int ret = getSettings().updateStatus(tableId,
							TableSetting.PHONE_ORDER);
					if (ret < 0) {
						tableHandler.sendEmptyMessage(ret);
						return;
					}
					ret = mPhoneOrder.cleanServerPhoneOrder(tableId);
					if (ret < 0) {
						tableHandler.sendEmptyMessage(ret);
						return;
					}
					if (getNotifiycations() < 0)
						return;
					if (getTableStatusFromServer() < 0)
						return;
					msg.what = UPDATE_TABLE_INFOS;
					tableHandler.sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void combineTable(final int destTId, final int persons) {
		new Thread() {
			public void run() {
				try {
					int ret = getSettings().combineTable(Info.getTableId(),
							destTId, mSettings.getName(Info.getTableId()),
							mSettings.getName(destTId), persons);
					mCombineTIdHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void changeTable(final int destTId, final int persons) {
		mpDialog.show();
		new Thread() {
			public void run() {
				try {
					int ret = getSettings().changeTable(Info.getTableId(),
							destTId, getSettings().getName(Info.getTableId()),
							getSettings().getName(destTId), persons);
					mChangeTIdHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void copyTable(final int srcTId) {
		mpDialog.show();
		new Thread() {
			public void run() {
				try {
					int ret = getSettings().getOrderFromServer(srcTId);
					mCopyTIdHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected void NotificationType() {
		new Thread() {
			public void run() {
				try {
					int ret = mNotificationType.getNotifiycationsType();
					notificationTypeHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	Handler notificationTypeHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				toastText(R.string.notificationTypeWarning);
			}
		}
	};

	private int getTableStatusFromServer() {
		int ret = getSettings().getTableStatusFromServerActivity();
		if (ret < 0) {
			tableHandler.sendEmptyMessage(ret);
		}
		return ret;
	}

	private void getParseTableSetting(final String msg) {
		new Thread() {
			public void run() {
				try {
					// mTableHandler.sendEmptyMessage(DISABLE_GRIDVIEW);
					int ret = getSettings().parseTableSetting(msg);
					if (ret < 0) {
						tableHandler.sendEmptyMessage(ret);
					} else {
						tableHandler.sendEmptyMessage(UPDATE_TABLE_INFOS);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	Handler tableHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				// if (NETWORK_ARERTDIALOG == 1) {
				// mNetWrorkcancel.cancel();
				// }
				// showNetworkErrDlg(getResources().getString(
				// R.string.networkErrorWarning));
			} else {
				switch (msg.what) {
				case UPDATE_TABLE_INFOS:
					if (!flag) {
						initViewPager();
					}
					updateGridViewAdapter(currentPage);
					flag = true;
					if (getSettings().hasPendedPhoneOrder()) {
						ringtoneHandler.sendEmptyMessage(1);
					}
					break;
				default:
					Log.e(TAG,
							"unhandled case:"
									+ msg.what
									+ (new Exception()).getStackTrace()[2]
											.getLineNumber());
					break;
				}
			}
		}
	};

	Handler ringtoneHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what > 0) {
				mRingtone.play();
			}
		}
	};

	private boolean isStatusLegal(String tableName, int status) {
		return getSettings().getStatusTableId(getSettings().getId(tableName)) == status;
	}

	private boolean isTId(String tableTId) {
		return (getSettings().getId(tableTId) != -1);
	}

	private boolean isBoundaryLegal(String tableName, int status) {
		if (tableName.equals(Info.getTableName())) {
			return false;
		}
		return isTId(tableName) && isStatusLegal(tableName, status);
	}

	private boolean equalNameAndPersons(String changePersons, String tableName) {
		return tableName.equals("") || changePersons.equals("");
	}

	private int getNotifiycations() {
		int ret = mNotification.getNotifiycations();
		ringtoneHandler.sendEmptyMessage(ret);
		return ret;
	}

	private void judgeChangeTable(String changePersons, String tableName)
			throws NumberFormatException {
		if (equalNameAndPersons(changePersons, tableName)) {
			toastText(R.string.idAndPersonsIsNull);
			return;
		}

		if (isBoundaryLegal(tableName, Table.NORMAL_TABLE_STAUTS)) {
			changeTable(getSettings().getId(tableName),
					Integer.parseInt(changePersons));
		} else {
			toastText(R.string.changeTIdWarning);
		}
	}

	private void judgeCombineTable(String changePersons, String tableName)
			throws NumberFormatException {
		if (equalNameAndPersons(changePersons, tableName)) {
			toastText(R.string.idAndPersonsIsNull);
			return;
		}

		if (isBoundaryLegal(tableName, Table.OPEN_TABLE_STATUS)) {
			combineTable(getSettings().getId(tableName),
					Integer.parseInt(changePersons));
		} else {
			toastText(R.string.combineTIdWarning);
		}
	}

	public TextWatcher watcher(final EditText id) {
		TextWatcher watcher = new TextWatcher() {
			String tempStr;
			EditText edit;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				tempStr = s.toString();
				if (tempStr.indexOf("0") == 0) {
					tempStr = tempStr.substring(1, tempStr.length());
					edit = id;
					edit.setText(tempStr);
				}
			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

		};
		return watcher;
	}

	protected void setClassToActivity(Class<?> setClass) {
		intent.setClass(TableGridDeskActivity.this, setClass);
		TableGridDeskActivity.this.startActivity(intent);
	}

	public TableSetting getSettings() {
		return mSettings;
	}

	public void setSettings(TableSetting mSettings) {
		this.mSettings = mSettings;
	}

	public int getRingtoneMsg() {
		return mRingtoneMsg;
	}

	public void setRingtoneMsg(int mRingtoneMsg) {
		this.mRingtoneMsg = mRingtoneMsg;
	}

	public int getTableMsg() {
		return mTableMsg;
	}

	public void setTableMsg(String mTableMsg) {
		if (mTableMsg != null) {
			getParseTableSetting(mTableMsg);
		}
	}

	public void setNetworkStatus(boolean status) {
		if (!status) {
			showNetworkErrStatus(getResources().getString(
					R.string.networkErrorWarning));
		} else {
			if (mStatusBar != null) {
				mStatusBar.setVisibility(View.INVISIBLE);
			}
		}
		networkStatus = status;
	}

	public Handler getTableHandler() {
		return tableHandler;
	}

	public void setTableHandler(Handler mTableHandler) {
		this.tableHandler = mTableHandler;
	}

	public Handler getRingtoneHandler() {
		return ringtoneHandler;
	}

	public void setRingtoneHandler(Handler mRingtoneHandler) {
		this.ringtoneHandler = mRingtoneHandler;
		sendRingtoneMsg();
	}

	public void sendTableMsg() {
		getTableHandler().sendEmptyMessage(getTableMsg());
	}

	public void sendRingtoneMsg() {
		getRingtoneHandler().sendEmptyMessage(getRingtoneMsg());
	}

	public void checkPendedOrder() {
		if (mPendedOrderNotificationHandler != null) {
			mPendedOrderNotificationHandler.sendEmptyMessage(0);
		}
	}

	public boolean isNetworkStatus() {
		return networkStatus;
	}
}
