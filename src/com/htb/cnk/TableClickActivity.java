package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
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
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;

import com.htb.cnk.NotificationTableService.MyBinder;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.NotificationTypes;
import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.PhoneOrder;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.lib.Ringtone;
import com.htb.cnk.receiver.MyReceiver;
import com.htb.constant.Table;

public class TableClickActivity extends TableBaseActivity {
	
	
	protected final int UPDATE_TABLE_INFOS = 5;
	protected final int DISABLE_GRIDVIEW = 10;
	protected final int CHECKOUT_LIST = 1;
	protected final int COMBINE_DIALOG = 1;
	protected final int CHANGE_DIALOG = 2;
	
	protected int NETWORK_ARERTDIALOG = 0;
	
	protected double mIncome;
	protected double mChange;
	protected double mTotalPrice;
	
	protected EditText tableIdEdit;
	protected EditText personsEdit;
	
	protected PhoneOrder mPhoneOrder;
	private TableSetting mSettings;
	protected Ringtone mRingtone;
	
	protected List<String> tableName = new ArrayList<String>();
	protected List<Integer> selectedTable = new ArrayList<Integer>();
	
	protected AlertDialog.Builder mChangeDialog;
	protected ProgressDialog mpDialog;
	protected SimpleAdapter mImageItems;
	
	protected Intent intent;
	protected boolean binderFlag;
	protected MyReceiver mReceiver;
	protected NotificationTableService.MyBinder binder;
	private int mTableMsg;
	private int mRingtoneMsg;
	protected AlertDialog mNetWrorkcancel;
	protected AlertDialog.Builder mNetWrorkAlertDialog;
	
	protected Notifications mNotificaion = new Notifications();
	protected NotificationTypes mNotificationType = new NotificationTypes();
	
	protected Handler mNotificationHandler;
	private Handler mTableHandler;
	private Handler mRingtoneHandler;
	protected Handler mTotalPriceTableHandler;
	protected Handler mChangeTIdHandler;
	protected Handler mCombineTIdHandler;
	protected Handler mCopyTIdHandler;
	protected Handler mCheckOutHandler;
	protected Handler mNotificationTypeHandler;
	
	@Override
	protected void onResume() {
		super.onResume();
		if (NETWORK_ARERTDIALOG == 1) {
			mNetWrorkcancel.cancel();
			NETWORK_ARERTDIALOG = 0;
		}
		showProgressDlg(getResources().getString(R.string.getStatus));
		binderStart();
	}

	@Override
	protected void onDestroy() {
		unbindService(conn);
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setNewClass();
		
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(false);
		mpDialog.setTitle(getResources().getString(R.string.pleaseWait));
		
		startService(NotificationTableService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		registerReceiver(mReceiver);
		
		mNetWrorkAlertDialog = networkDialog();
		setClickListeners();
	}

	private void setNewClass() {
		mPhoneOrder = new PhoneOrder(TableClickActivity.this);
		setmSettings(new TableSetting());
		mRingtone = new Ringtone(TableClickActivity.this);
		mpDialog = new ProgressDialog(TableClickActivity.this);
		mReceiver = new MyReceiver(this);
	}

	private void registerReceiver(BroadcastReceiver receiver) {
		IntentFilter filter = new IntentFilter(
				NotificationTableService.SERVICE_IDENTIFIER);
		registerReceiver(receiver, filter);
	}


	private void startService(Class<?> cla) {
		intent = new Intent(TableClickActivity.this,
				cla);
		startService(intent);
	}

	protected void setClickListeners() {
		mBackBtn.setOnClickListener(backClicked);
		mUpdateBtn.setOnClickListener(checkOutClicked);
		mStatisticsBtn.setOnClickListener(logoutClicked);
		mManageBtn.setOnClickListener(manageClicked);
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

	protected void binderStart() {
		if (binderFlag) {
			binder.start();
			return;
		}
	}

	protected AlertDialog.Builder networkDialog() {
		return mTitleAndMessageDialog.networkDialog(networkPositiveListener,
				networkNegativeListener);
	}

	DialogInterface.OnClickListener networkPositiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			dialog.cancel();
			NETWORK_ARERTDIALOG = 0;
			showProgressDlg(getResources().getString(R.string.getStatus));
			binderStart();
		}
	};
	DialogInterface.OnClickListener networkNegativeListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			NETWORK_ARERTDIALOG = 0;
			dialog.cancel();
			finish();
		}
	};

	private AlertDialog.Builder cleanDialog() {
		final CharSequence[] cleanitems = getResources().getStringArray(
				R.array.openStatus);
		return mItemDialog.itemChooseFunctionDialog(cleanitems, cleanListener);
	}

	DialogInterface.OnClickListener cleanListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			cleanChioceMode(which);
		}
	};

	private void cleanChioceMode(int which) {
		switch (which) {
		case 0:
			cleanTableDialog().show();
			break;
		case 1:
			changeTableDialog(CHANGE_DIALOG).show();
			break;
		case 2:
			changeTableDialog(COMBINE_DIALOG).show();
			break;
		case 3:
			setClassToActivity(DelOrderActivity.class);
			break;
		case 4:
			Info.setMode(Info.WORK_MODE_WAITER);
			setClassToActivity(MenuActivity.class);
			break;
		case 5:
			setClassToActivity(QueryOrderActivity.class);
			break;
		default:
			break;
		}
	}

	public void showProgressDlg(String msg) {
		mpDialog.setMessage(msg);
		mpDialog.show();
	}

	protected void netWorkDialogShow(String messages) {
		NETWORK_ARERTDIALOG = 1;
		mNetWrorkcancel = mNetWrorkAlertDialog.setMessage(messages).show();
	}
	
	private AlertDialog.Builder cleanTableDialog() {
		return mTitleAndMessageDialog.messageDialog(false, getResources()
				.getString(R.string.isCleanTable),
				getResources().getString(R.string.yes), cleanTableListener,
				getResources().getString(R.string.no), null);
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

	private AlertDialog.Builder addDialog() {
		final CharSequence[] additems = getResources().getStringArray(
				R.array.normalStatus);
		return mItemDialog.itemChooseFunctionDialog(additems, addListener);
	}

	DialogInterface.OnClickListener addListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mPhoneOrder.clear();
			addDialogChoiceMode(which);
		}
	};

	private void addDialogChoiceMode(int which) {
		switch (which) {
		case 0:
			Info.setMode(Info.WORK_MODE_CUSTOMER);
			Info.setNewCustomer(true);
			setClassToActivity(MenuActivity.class);
			TableClickActivity.this.finish();
			break;
		case 1:
			if (Info.getMenu() == Info.ORDER_QUCIK_MENU) {
				setClassToActivity(QuickMenuActivity.class);
			} else {
				Info.setMode(Info.WORK_MODE_WAITER);
				setClassToActivity(MenuActivity.class);
			}
			break;
		case 2:
			copyTableDialog().show();
			break;
		default:
			break;
		}
	}

	private AlertDialog.Builder addPhoneDialog(final int position) {
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
			setClassToActivity(PhoneActivity.class);
			break;
		case 1:
			cleanPhoneDialog(position).show();
			break;
		default:
			break;
		}
	}

	private AlertDialog.Builder notificationDialog() {
		List<String> add = mNotificaion
				.getNotifiycationsType(Info.getTableId());
		String[] additems = (String[]) add.toArray(new String[add.size()]);
		return mItemDialog.itemButtonDialog(false,
				getResources().getString(R.string.customerCall), additems,
				null, null, notificationListener);
	}

	DialogInterface.OnClickListener notificationListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			cleanNotification();
		}
	};

	private Builder changeTableDialog(final int type) {
		final AlertDialog.Builder changeTableAlertDialog;
		View layout = getDialogLayout(R.layout.change_dialog, R.id.change);
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
		changeTableAlertDialog.setTitle(getResources().getString(
				R.string.pleaseInput));

		DialogInterface.OnClickListener changeTablePositiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String changePersons;
				String tableName = tableIdEdit.getText().toString();
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
				String changeTId = copyTableText.getEditableText().toString();
				if (changeTId.equals("")) {
					toastText(R.string.idAndPersonsIsNull);
				} else if (isBoundaryLegal(changeTId, Table.OPEN_TABLE_STATUS)) {
					copyTable(getmSettings().getId(changeTId));
				} else {
					toastText(R.string.copyTIdwarning);
				}
			}
		};

		return mViewDialog.viewAndTitleAndButtonDialog(false, copyTableText,
				getResources().getString(R.string.pleaseInput), null,
				copyTableListener);
	}

	private EditText editTextListener() {
		final EditText copyTableText = new EditText(this);
		copyTableText.setKeyListener(new DigitsKeyListener(false, true));
		copyTableText
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
		return copyTableText;
	}

	private View getDialogLayout(int layout_dialog, int id) {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(layout_dialog,
				(ViewGroup) findViewById(id));
		return layout;
	}

	private Builder listTableNameDialog(final int type) {
		final List<Integer> tableId = new ArrayList<Integer>();
		final List<String> tableNameStr = new ArrayList<String>();
		final AlertDialog.Builder checkOutAlertDialog;
		ArrayList<HashMap<String, Object>> checkOut = getmSettings().getCombine();
		if (checkOut.size() <= 0) {
			checkOutAlertDialog = mTitleAndMessageDialog.messageDialog(false,
					getResources().getString(R.string.tableNotOpen),
					getResources().getString(R.string.ok), null, null, null);
			return checkOutAlertDialog;
		}
		for (HashMap<String, Object> item : checkOut) {
			tableNameStr.add(item.get("name").toString());
			tableId.add(item.get("id").hashCode());
		}
		final int size = getmSettings().size();
		final boolean selected[] = new boolean[size];

		DialogInterface.OnClickListener listPositiveListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which) {
				selectedTable.clear();
				tableName.clear();
				for (int i = 0; i < selected.length; i++) {
					if (selected[i] == true) {
						selectedTable.add(tableId.get(i));
						tableName.add(tableNameStr.get(i));
					}
				}
				if (type == CHECKOUT_LIST) {
					showProgressDlg(getResources().getString(
							R.string.statisticsAmount));
					getTotalPriceTable();
				}

			}
		};

		DialogInterface.OnMultiChoiceClickListener listListener = new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which,
					boolean isChecked) {
				selected[which] = isChecked;
			}
		};

		return mMultiChoiceItemsDialog.titleDialog(false, getResources()
				.getString(R.string.chooseTableId), (String[]) tableNameStr
				.toArray(new String[0]), null, listListener, getResources()
				.getString(R.string.ok), listPositiveListener, getResources()
				.getString(R.string.cancel), null);
	}

	private void cleanNotification() {
		new Thread() {
			public void run() {
				try {
					int ret = mNotificaion
							.cleanNotifications(Info.getTableId());
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
					getmTableHandler().sendEmptyMessage(DISABLE_GRIDVIEW);
					int ret = getmSettings().cleanTalble(tableId);
					if (ret < 0) {
						getmTableHandler().sendEmptyMessage(ret);
						return;
					}
					if (getNotifiycations() < 0)
						return;
					if (getTableStatusFromServer() < 0)
						return;
					msg.what = UPDATE_TABLE_INFOS;
					getmTableHandler().sendMessage(msg);
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
					int ret = getmSettings().updateStatus(tableId,
							TableSetting.PHONE_ORDER);
					if (ret < 0) {
						getmTableHandler().sendEmptyMessage(ret);
						return;
					}
					ret = mPhoneOrder.cleanServerPhoneOrder(tableId);
					if (ret < 0) {
						getmTableHandler().sendEmptyMessage(ret);
						return;
					}
					if (getNotifiycations() < 0)
						return;
					if (getTableStatusFromServer() < 0)
						return;
					msg.what = UPDATE_TABLE_INFOS;
					getmTableHandler().sendMessage(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void getTotalPriceTable() {
		new Thread() {
			public void run() {
				try {
					double ret = getmSettings().getTotalPriceTable(
							TableClickActivity.this, selectedTable, tableName);
					mTotalPriceTableHandler.sendEmptyMessage((int) ret);
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
					int ret = getmSettings().combineTable(TableClickActivity.this,
							Info.getTableId(), destTId,
							getmSettings().getName(Info.getTableId()),
							getmSettings().getName(destTId), persons);
					mCombineTIdHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void changeTable(final int destTId, final int persons) {
		new Thread() {
			public void run() {
				try {
					int ret = getmSettings().changeTable(TableClickActivity.this,
							Info.getTableId(), destTId,
							getmSettings().getName(Info.getTableId()),
							getmSettings().getName(destTId), persons);
					mChangeTIdHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void copyTable(final int srcTId) {
		new Thread() {
			public void run() {
				try {
					int ret = getmSettings().getOrderFromServer(
							TableClickActivity.this, srcTId);
					mCopyTIdHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected void checkOut(final List<Integer> destIId,
			final List<String> tableName, final Double receivable,
			final Double income, final Double change) {
		new Thread() {
			public void run() {
				try {
					int ret = getmSettings().checkOut(TableClickActivity.this,
							destIId, tableName, receivable, income, change);
					mCheckOutHandler.sendEmptyMessage(ret);
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
					mNotificationTypeHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private int getTableStatusFromServer() {
		int ret = getmSettings().getTableStatusFromServer();
		if (ret < 0) {
			getmTableHandler().sendEmptyMessage(ret);
		}
		return ret;
	}

	private boolean isStatusLegal(String tableName, int status) {
		return getmSettings().getStatusTableId(getmSettings().getId(tableName)) == status;
	}

	private boolean isTId(String tableTId) {
		return (getmSettings().getId(tableTId) != -1);
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
		int ret = mNotificaion.getNotifiycations();
		getmRingtoneHandler().sendEmptyMessage(ret);
		return ret;
	}

	private void judgeChangeTable(String changePersons, String tableName)
			throws NumberFormatException {
		if (equalNameAndPersons(changePersons, tableName)) {
			toastText(R.string.idAndPersonsIsNull);
			return;
		}

		if (isBoundaryLegal(tableName, Table.NORMAL_TABLE_STAUTS)) {
			changeTable(getmSettings().getId(tableName),
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
			combineTable(getmSettings().getId(tableName),
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
		intent.setClass(TableClickActivity.this, setClass);
		TableClickActivity.this.startActivity(intent);
	}

	public int getmRingtoneMsg() {
		return mRingtoneMsg;
	}

	public void setmRingtoneMsg(int mRingtoneMsg) {
		this.mRingtoneMsg = mRingtoneMsg;
	}

	public int getmTableMsg() {
		return mTableMsg;
	}

	public void setmTableMsg(int mTableMsg) {
		this.mTableMsg = mTableMsg;
	}

	public TableSetting getmSettings() {
		return mSettings;
	}

	public void setmSettings(TableSetting mSettings) {
		this.mSettings = mSettings;
	}

	public Handler getmTableHandler() {
		return mTableHandler;
	}

	public void setmTableHandler(Handler mTableHandler) {
		this.mTableHandler = mTableHandler;
	}

	public Handler getmRingtoneHandler() {
		return mRingtoneHandler;
	}

	public void setmRingtoneHandler(Handler mRingtoneHandler) {
		this.mRingtoneHandler = mRingtoneHandler;
	}

	private OnClickListener checkOutClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			listTableNameDialog(CHECKOUT_LIST).show();
		}
	};

	private OnClickListener manageClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setClassToActivity(ManageActivity.class);
		}
	};

	private OnClickListener backClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			TableClickActivity.this.finish();
		}
	};

	private OnClickListener logoutClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mTitleAndMessageDialog.titleAndMessageDialog(false,
					getResources().getString(R.string.notice),
					getResources().getString(R.string.islogOut),
					getResources().getString(R.string.ok),
					logoutPositiveListener,
					getResources().getString(R.string.cancel), null).show();
		}
	};

	DialogInterface.OnClickListener logoutPositiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			Info.setMode(Info.WORK_MODE_CUSTOMER);
			Info.setTableId(-1);
			finish();
		}
	};

	OnItemClickListener  tableItemClickListener = new OnItemClickListener() {

				public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
															// click happened
						View arg1,// The view within the AdapterView that was clicked
						int arg2,// The position of the view in the adapter
						long arg3// The row id of the item that was clicked
				) {
					if (isNameIdStatusLegal(arg2)) {
						Info.setTableName(getmSettings().getNameIndex(arg2));
						Info.setTableId(getmSettings().getIdIndex(arg2));
						tableItemChioceDialog(arg2, getmSettings().getStatusIndex(arg2));
					} else {
						toastText("不能获取信息，请检查设备！");
					}
					mImageItems.notifyDataSetChanged();
				}

				private boolean isNameIdStatusLegal(int arg2) {
					return (getmSettings().getNameIndex(arg2)) != null
							&& ((getmSettings().getIdIndex(arg2)) != -1)
							&& ((getmSettings().getStatusIndex(arg2)) != -1);
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
						addPhoneDialog(arg2).show();
						break;
					case 100:
					case 101:
					case 150:
					case 151:
						notificationDialog().show();
						break;
					default:
						break;
					}
				}
			
	};
}
