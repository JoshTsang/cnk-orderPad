package com.htb.cnk.ui.base;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.htb.cnk.DelOrderActivity;
import com.htb.cnk.MenuActivity;
import com.htb.cnk.MyOrderActivity;
import com.htb.cnk.PhoneActivity;
import com.htb.cnk.QueryOrderActivity;
import com.htb.cnk.QuickMenuActivity;
import com.htb.cnk.R;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.NotificationTypes;
import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.PhoneOrder;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.dialog.ItemDlg;
import com.htb.cnk.dialog.TitleAndMessageDlg;
import com.htb.cnk.dialog.ViewDlg;
import com.htb.cnk.lib.Ringtone;
import com.htb.cnk.service.NotificationTableService;
import com.htb.constant.ErrorNum;
import com.htb.constant.Table;

public class GridBaseActivity extends Activity {
	private static final String TAG = "GridBaseActivity";
	private final int UPDATE_TABLE_INFOS = 500;
	private final int COMBINE_DIALOG = 1;
	private final int CHANGE_DIALOG = 2;

	private Notifications mNotification = new Notifications();
	private NotificationTypes mNotificationType = new NotificationTypes();
	private List<Integer> selectedTable = new ArrayList<Integer>();
	private PhoneOrder mPhoneOrder;
	private TableSetting mSettings;
	private Ringtone mRingtone;
	private EditText tableIdEdit;
	private EditText personsEdit;
	private Intent intent;
	private Context mContext;
	private TitleAndMessageDlg mTitleAndMessageDialog;
	private ItemDlg mItemDialog;
	private ViewDlg mViewDialog;
	private ProgressDialog mpDialog;
	private AlertDialog.Builder mNetWrorkAlertDialog;

	public GridBaseActivity(Context context) {
		super();
		mTitleAndMessageDialog = new TitleAndMessageDlg(context);
		mItemDialog = new ItemDlg(context);
		mViewDialog = new ViewDlg(context);
		mContext = context;
		mPhoneOrder = new PhoneOrder(context);
		intent = new Intent();
		initProgressDlg();
		setSettings(new TableSetting(mContext));
		NotificationType();
	}

	public void toastText(String r) {
		Toast.makeText(mContext.getApplicationContext(), r, Toast.LENGTH_LONG)
				.show();
	}

	public void networkErrDlg() {
		toastText(R.string.functionDisableCauseNetworkUnavalialbe);
	}

	public AlertDialog.Builder addDialog() {
		final CharSequence[] additems = mContext.getResources().getStringArray(
				R.array.normalStatus);
		if (mItemDialog != null)
			return mItemDialog.itemChooseFunctionDialog(additems, addListener);
		return null;
	}

	public AlertDialog.Builder notificationDialog() {
		List<String> add = mNotification.getNotifiycationsType(Info
				.getTableId());
		String[] additems = (String[]) add.toArray(new String[add.size()]);
		return mItemDialog.itemButtonDialog(false, mContext.getResources()
				.getString(R.string.customerCall), additems, null, null,
				notificationListener);
	}

	public AlertDialog.Builder cleanDialog() {
		final CharSequence[] cleanitems = mContext.getResources()
				.getStringArray(R.array.openStatus);
		return mItemDialog.itemChooseFunctionDialog(cleanitems, cleanListener);
	}

	public AlertDialog.Builder addPhoneDialog(final int position) {
		final CharSequence[] additems = mContext.getResources().getStringArray(
				R.array.phoneStatus);
		DialogInterface.OnClickListener addPhoneListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addPhoneChoiceMode(position, which);
			}
		};

		return mItemDialog.itemChooseFunctionDialog(additems, addPhoneListener);
	}

	private AlertDialog.Builder cleanTableDialog() {
		return mTitleAndMessageDialog.messageDialog(false, mContext
				.getResources().getString(R.string.isCleanTable), mContext
				.getResources().getString(R.string.yes), cleanTableListener,
				mContext.getResources().getString(R.string.no), null);
	}

	private DialogInterface.OnClickListener cleanListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			cleanChioceMode(which);
		}
	};

	private void cleanChioceMode(int which) {
		switch (which) {
		case 0:
			if (TableGridDeskActivity.networkStatus) {
				cleanTableDialog().show();
			} else {
				networkErrDlg();
			}
			break;
		case 1:
			if (TableGridDeskActivity.networkStatus) {
				changeOrCombineDialog(CHANGE_DIALOG).show();
			} else {
				networkErrDlg();
			}
			break;
		case 2:
			if (TableGridDeskActivity.networkStatus) {
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

	private void toastText(int r) {
		Toast.makeText(mContext.getApplicationContext(),
				mContext.getResources().getString(r), Toast.LENGTH_LONG).show();
	}

	private void initProgressDlg() {
		mpDialog = new ProgressDialog(mContext);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(false);
		mpDialog.setTitle(mContext.getResources()
				.getString(R.string.pleaseWait));
	}

	private void showProgressDlg(String msg) {
		mpDialog.setMessage(msg);
		mpDialog.show();
	}

	private void chooseTypeToMenu() {
		if (Info.getMenu() == Info.ORDER_QUCIK_MENU) {
			setClassToActivity(QuickMenuActivity.class);
		} else {
			Info.setMode(Info.WORK_MODE_WAITER);
			setClassToActivity(MenuActivity.class);
		}
	}

	private DialogInterface.OnClickListener cleanTableListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int i) {
			showProgressDlg(mContext.getResources().getString(
					R.string.cleanTableNow));
			selectedTable.clear();
			selectedTable.add(Info.getTableId());
			cleanTableThread(selectedTable);
		}
	};

	private DialogInterface.OnClickListener addListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mPhoneOrder.clear();
			addDialogChoiceMode(which);
		}
	};

	private void addDialogChoiceMode(int which) {
		switch (which) {
		case 0:
			if (TableGridDeskActivity.networkStatus) {
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
			if (TableGridDeskActivity.networkStatus) {
				copyTableDialog().show();
			} else {
				networkErrDlg();
			}
			break;
		default:
			break;
		}
	}

	private AlertDialog.Builder cleanPhoneDialog(final int position) {
		DialogInterface.OnClickListener cleanPhoneListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				showProgressDlg(mContext.getResources().getString(
						R.string.cleanPhoneOrderNow));
				cleanPhoneThread(position, Info.getTableId());
			}
		};

		return mTitleAndMessageDialog.messageDialog(false, mContext
				.getResources().getString(R.string.isCleanOrder), mContext
				.getResources().getString(R.string.yes), cleanPhoneListener,
				mContext.getResources().getString(R.string.no), null);
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

	private void setClassToActivity(Class<?> setClass) {
		intent.setClass(mContext, setClass);
		mContext.startActivity(intent);
	}

	private DialogInterface.OnClickListener notificationListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			cleanNotification();
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
			changeTableAlertDialog.setTitle(mContext.getResources().getString(
					R.string.pleaseInput)
					+ "转入桌号");
		} else if (type == COMBINE_DIALOG) {
			changeTableAlertDialog.setTitle(mContext.getResources().getString(
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

		changeTableAlertDialog.setPositiveButton(mContext.getResources()
				.getString(R.string.ok), changeTablePositiveListener);
		changeTableAlertDialog.setNegativeButton(mContext.getResources()
				.getString(R.string.cancel), null);
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
				mContext.getResources().getString(R.string.pleaseInput) + "桌号",
				null, copyTableListener);
	}

	private EditText editTextListener() {
		final EditText editText = new EditText(mContext);
		// editText.setKeyListener(new DigitsKeyListener(false, true));
		// editText
		// .setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
		return editText;
	}

	private View getDialogLayout(int layout_dialog, int id) {
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		View layout = inflater.inflate(layout_dialog, null);
		return layout;
	}

	private void cleanNotification() {
		new Thread() {
			public void run() {
				try {
					int ret = mNotification.cleanNotifications(Info
							.getTableId());
					notificationHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void cleanTableThread(final List<Integer> tableId) {
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
					combineTIdHandler.sendEmptyMessage(ret);
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
					changeTIdHandler.sendEmptyMessage(ret);
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
					copyTIdHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void NotificationType() {
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

	private TextWatcher watcher(final EditText id) {
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

	private int getTableStatusFromServer() {
		int ret = getSettings().getTableStatusFromServerActivity();
		if (ret < 0) {
			tableHandler.sendEmptyMessage(ret);
		}
		return ret;
	}

	private Handler tableHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {

			} else {
				Intent intent = new Intent(
						NotificationTableService.SERVICE_IDENTIFIER);
				Bundle bundle = new Bundle();
				intent.putExtra("tableHandler", msg.what);
				intent.putExtras(bundle);
				mContext.sendBroadcast(intent);
			}
		}
	};

	private Handler ringtoneHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what > 0) {
				mRingtone.play();
			}
		}
	};

	private Handler changeTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			switch (msg.what) {
			case -10:
				toastText("本地数据库出错，请从网络重新更新数据库");
				break;
			case -2:
				toastText(R.string.changeTIdWarning);
				break;
			case -1:
				showNetworkErrDlg("转台失败，"
						+ mContext.getResources().getString(
								R.string.networkErrorWarning));
				break;
			default:
				if (!isPrinterError(msg)) {
					binderStart();
				}
				toastText(R.string.changeSucc);
				break;
			}
		}
	};

	private boolean isPrinterError(Message msg) {
		return msg.what == ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT
				|| msg.what == ErrorNum.PRINTER_ERR_NO_PAPER;
	}

	// TODO define
	private Handler copyTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			switch (msg.what) {
			case -10:
				toastText("本地数据库出错，请从网络重新更新数据库");
				break;
			case -2:
				toastText(R.string.copyTIdwarning);
				break;
			case -1:
				showNetworkErrDlg("复制失败，"
						+ mContext.getResources().getString(
								R.string.networkErrorWarning));
				break;
			default:
				intent.setClass(mContext, MyOrderActivity.class);
				Info.setMode(Info.WORK_MODE_WAITER);
				mContext.startActivity(intent);
				break;
			}
		}
	};

	private Handler combineTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			switch (msg.what) {
			case -10:
				toastText("本地数据库出错，请从网络重新更新数据库");
				break;
			case -2:
				toastText(R.string.checkOutWarning);
				break;
			case -1:
				showNetworkErrDlg("合并出错，"
						+ mContext.getResources().getString(
								R.string.networkErrorWarning));
				break;
			default:
				if (isPrinterError(msg)) {
					toastText(R.string.combineError);
				} else {
					binderStart();
					toastText(R.string.combineSucc);
				}
				break;
			}
		}
	};

	private void showNetworkErrDlg(String msg) {
		mNetWrorkAlertDialog.setMessage(msg).show();
	}

	private Handler notificationHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				toastText(R.string.notificationWarning);
			} else {
				binderStart();
			}
		}
	};

	private void binderStart() {
		Intent intent = new Intent(NotificationTableService.SERVICE_IDENTIFIER);
		Bundle bundle = new Bundle();
		intent.putExtra("binder", true);
		intent.putExtras(bundle);
		mContext.sendBroadcast(intent);
	}

	private Handler notificationTypeHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				toastText(R.string.notificationTypeWarning);
			}
		}
	};

	private TableSetting getSettings() {
		return mSettings;
	}

	private void setSettings(TableSetting mSettings) {
		this.mSettings = mSettings;
	}

}