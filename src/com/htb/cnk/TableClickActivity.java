package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.dialog.ItemDialog;
import com.htb.cnk.dialog.MultiChoiceItemsDialog;
import com.htb.cnk.dialog.TitleAndMessageDialog;
import com.htb.cnk.dialog.ViewDialog;
import com.htb.constant.Table;

public class TableClickActivity extends TableBaseActivity {
	static final String TAG = "TableClickActivity";
	protected final int UPDATE_TABLE_INFOS = 5;
	protected final int DISABLE_GRIDVIEW = 10;
	protected final int CHECKOUT_LIST = 1;
	protected final int COMBINE_DIALOG = 1;
	protected final int CHANGE_DIALOG = 2;
	protected List<Integer> selectedTable = new ArrayList<Integer>();
	protected double mIncome;
	protected double mChange;
	protected EditText tableIdEdit;
	protected EditText personsEdit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTitleAndMessageDialog = new TitleAndMessageDialog(
				TableClickActivity.this);
		mItemDialog = new ItemDialog(TableClickActivity.this);
		mViewDialog = new ViewDialog(TableClickActivity.this);
		mMultiChoiceItemsDialog = new MultiChoiceItemsDialog(
				TableClickActivity.this);
		mNetWrorkAlertDialog = networkDialog();
		setClickListeners();
	}

	protected void setClickListeners() {
		mTableClicked = new tableItemClickListener();
		mBackBtn.setOnClickListener(backClicked);
		mUpdateBtn.setOnClickListener(checkOutClicked);
		mStatisticsBtn.setOnClickListener(logoutClicked);
		mManageBtn.setOnClickListener(manageClicked);
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
			changeOrCombineDialog(CHANGE_DIALOG).show();
			break;
		case 2:
			changeOrCombineDialog(COMBINE_DIALOG).show();
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
				dialog.cancel();
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

	private Builder changeOrCombineDialog(final int type) {
		final AlertDialog.Builder changeTableAlertDialog;
		View layout = getDialogLayout(R.layout.change_dialog, R.id.change_dialog);
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
				String tableName = tableIdEdit.getText().toString().toUpperCase();
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
				String tableName = copyTableText.getEditableText().toString().toUpperCase();
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
				getResources().getString(R.string.pleaseInput), null,
				copyTableListener);
	}

	private EditText editTextListener() {
		final EditText editText = new EditText(this);
//		editText.setKeyListener(new DigitsKeyListener(false, true));
//		editText
//				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
		return editText;
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
		ArrayList<HashMap<String, Object>> checkOut = mSettings.getCombine();
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
		final int size = mSettings.size();
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
					mTableHandler.sendEmptyMessage(DISABLE_GRIDVIEW);
					int ret = mSettings.cleanTalble(tableId);
					if (ret < 0) {
						mTableHandler.sendEmptyMessage(ret);
						return;
					}
					if (getNotifiycations() < 0)
						return;
					if (getTableStatusFromServer() < 0)
						return;
					msg.what = UPDATE_TABLE_INFOS;
					mTableHandler.sendMessage(msg);
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
					int ret = mSettings.updateStatus(tableId,
							TableSetting.PHONE_ORDER);
					if (ret < 0) {
						mTableHandler.sendEmptyMessage(ret);
						return;
					}
					ret = mPhoneOrder.cleanServerPhoneOrder(tableId);
					if (ret < 0) {
						mTableHandler.sendEmptyMessage(ret);
						return;
					}
					if (getNotifiycations() < 0)
						return;
					if (getTableStatusFromServer() < 0)
						return;
					msg.what = UPDATE_TABLE_INFOS;
					mTableHandler.sendMessage(msg);
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
					double ret = mSettings.getTotalPriceTable(
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
					int ret = mSettings.combineTable(TableClickActivity.this,
							Info.getTableId(), destTId,
							mSettings.getName(Info.getTableId()),
							mSettings.getName(destTId), persons);
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
					int ret = mSettings.changeTable(TableClickActivity.this,
							Info.getTableId(), destTId,
							mSettings.getName(Info.getTableId()),
							mSettings.getName(destTId), persons);
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
					int ret = mSettings.getOrderFromServer(
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
					int ret = mSettings.checkOut(TableClickActivity.this,
							destIId, tableName, receivable, income, change);
					mCheckOutHandler.sendEmptyMessage(ret);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private int getTableStatusFromServer() {
		int ret = mSettings.getTableStatusFromServer();
		if (ret < 0) {
			mTableHandler.sendEmptyMessage(ret);
		}
		return ret;
	}

	private boolean isStatusLegal(String tableName, int status) {
		return mSettings.getStatusTableId(mSettings.getId(tableName)) == status;
	}

	private boolean isTId(String tableTId) {
		return (mSettings.getId(tableTId) != -1);
	}

	private boolean isBoundaryLegal(String tableName, int status) {
		if(tableName.equals(Info.getTableName())){
			return false;
		}
		return isTId(tableName) && isStatusLegal(tableName, status);
	}

	private int getNotifiycations() {
		int ret = mNotificaion.getNotifiycations();
		mRingtoneHandler.sendEmptyMessage(ret);
		return ret;
	}

	private void judgeChangeTable(String changePersons, String tableName)
			throws NumberFormatException {
		if (tableName.equals("") || changePersons.equals("")) {
			toastText(R.string.idAndPersonsIsNull);
		} else if (isBoundaryLegal(tableName, Table.NORMAL_TABLE_STAUTS)) {
			changeTable(mSettings.getId(tableName),
					Integer.parseInt(changePersons));
		} else {
			toastText(R.string.changeTIdWarning);
		}
	}

	private void judgeCombineTable(String changePersons, String tableName)
			throws NumberFormatException {
		if (tableName.equals("") || changePersons.equals("")) {
			toastText(R.string.idAndPersonsIsNull);
		} else if (isBoundaryLegal(tableName, Table.OPEN_TABLE_STATUS)) {
			combineTable(mSettings.getId(tableName),
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

	class tableItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
													// click happened
				View arg1,// The view within the AdapterView that was clicked
				int arg2,// The position of the view in the adapter
				long arg3// The row id of the item that was clicked
		) {
			if (isNameIdStatusLegal(arg2)) {
				Info.setTableName(mSettings.getNameIndex(arg2));
				Info.setTableId(mSettings.getIdIndex(arg2));
				tableItemChioceDialog(arg2, mSettings.getStatusIndex(arg2));
			} else {
				toastText("不能获取信息，请检查设备！");
			}
			mImageItems.notifyDataSetChanged();
		}

		private boolean isNameIdStatusLegal(int arg2) {
			return (mSettings.getNameIndex(arg2)) != null
					&& ((mSettings.getIdIndex(arg2)) != -1)
					&& ((mSettings.getStatusIndex(arg2)) != -1);
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
	}

}