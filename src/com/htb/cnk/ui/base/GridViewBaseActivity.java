package com.htb.cnk.ui.base;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.htb.cnk.lib.Ringtone;
import com.htb.constant.Table;
import com.umeng.common.Log;

public class GridViewBaseActivity extends BaseActivity{
	private final static String TAG = "GridViewBaseActivity";
	protected final int UPDATE_TABLE_INFOS = 500;
	protected final int CHECKOUT_LIST = 1;
	protected final int COMBINE_DIALOG = 1;
	protected final int CHANGE_DIALOG = 2;
	
	protected boolean binderFlag;
	protected Intent intent;

	private int mTableMsg;
	private int mRingtoneMsg;


	protected List<String> tableName = new ArrayList<String>();
	protected List<Integer> selectedTable = new ArrayList<Integer>();
	
	protected NotificationTypes mNotificationType = new NotificationTypes();
	
	protected PhoneOrder mPhoneOrder;
	protected TableSetting mSettings;
	protected Ringtone mRingtone;

	protected EditText tableIdEdit;
	protected EditText personsEdit;

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
	protected boolean networkStatus;
	protected TextView mStatusBar;
	protected Notifications mNotification = new Notifications();
	
	public GridViewBaseActivity(TableSetting settings){
		mSettings = new TableSetting(GridViewBaseActivity.this);
		mSettings = settings;
		Log.d(TAG, "setting:"+mSettings.getFloorNum());
	}
	
	public GridViewBaseActivity(){
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mRingtone = new Ringtone(GridViewBaseActivity.this);
		NotificationType();
	}

	

	DialogInterface.OnClickListener cleanListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			cleanChioceMode(which);
		}
	};

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
			Info.setMode(Info.WORK_MODE_PHONE);
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
					Log.e(TAG, TAG+msg);
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
//					if (!flag) {
//						initViewPager();
//					}
//					updateGridViewAdapter(currentPage);
//					flag = true;
//					if (getSettings().hasPendedPhoneOrder()) {
//						ringtoneHandler.sendEmptyMessage(1);
//					}
					break;
				default:
//					Log.e(TAG,
//							"unhandled case:"
//									+ msg.what
//									+ (new Exception()).getStackTrace()[2]
//											.getLineNumber());
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
		intent.setClass(GridViewBaseActivity.this, setClass);
		GridViewBaseActivity.this.startActivity(intent);
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
		Log.e(TAG, TAG+mTableMsg);
		getParseTableSetting(mTableMsg);
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
	
	public boolean isNetworkStatus() {
		return networkStatus;
	}
	protected void showNetworkErrStatus(String messages) {
		if (mStatusBar != null) {
			mStatusBar.setVisibility(View.VISIBLE);
			mStatusBar.setText(messages);
		}
	}
}
