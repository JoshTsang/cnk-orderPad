package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
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
import android.widget.EditText;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;
import com.htb.constant.Table;

public class TableClickActivity extends TableBaseActivity {

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
		final AlertDialog.Builder networkAlertDialog = alertDialogBuilder(false);
		networkAlertDialog.setTitle(getResources().getString(R.string.error));// 设置对话框标题
		networkAlertDialog.setMessage(getResources().getString(
				R.string.networkErrorWarning));// 设置对话框内容
		networkAlertDialog.setPositiveButton(
				getResources().getString(R.string.tryAgain),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						dialog.cancel();
						NETWORK_ARERTDIALOG = 0;
						showProgressDlg(getResources().getString(
								R.string.getStatus));
						binderStart();
					}
				});
		networkAlertDialog.setNegativeButton(
				getResources().getString(R.string.exit),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						NETWORK_ARERTDIALOG = 0;
						dialog.cancel();
						finish();
					}
				});

		return networkAlertDialog;
	}

	private Dialog cleanDialog() {
		final CharSequence[] cleanitems = getResources().getStringArray(
				R.array.openStatus);
		Dialog cleanDialog = alertDialogBuilder(true).setTitle(getResources().getString(R.string.chooseFunction))
				.setItems(cleanitems, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						cleanChioceMode(which);
					}

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
				}).create();
		return cleanDialog;
	}

	private AlertDialog.Builder cleanTableDialog() {
		final AlertDialog.Builder cleanTableAlertDialog = alertDialogBuilder(false);
		cleanTableAlertDialog.setMessage(getResources().getString(R.string.isCleanTable));
		cleanTableAlertDialog.setPositiveButton(getResources().getString(R.string.cleanTable),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						showProgressDlg(getResources().getString(R.string.cleanTableNow));
						selectedTable.clear();
						selectedTable.add(Info.getTableId());
						cleanTableThread(selectedTable);
					}
				});
		cleanTableAlertDialog.setNegativeButton(
				getResources().getString(R.string.cancel), null);
		return cleanTableAlertDialog;
	}

	private AlertDialog.Builder cleanPhoneDialog(final int position) {
		final AlertDialog.Builder cleanPhoneAlertDialog = alertDialogBuilder(false);
		cleanPhoneAlertDialog.setMessage(getResources().getString(R.string.isCleanOrder));
		cleanPhoneAlertDialog.setPositiveButton(getResources().getString(R.string.yes),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int i) {
						showProgressDlg(getResources().getString(R.string.cleanPhoneOrderNow));
						cleanPhoneThread(position, Info.getTableId());
						dialog.cancel();
					}
				});
		cleanPhoneAlertDialog.setNegativeButton(getResources().getString(R.string.no), null);
		return cleanPhoneAlertDialog;
	}

	private AlertDialog.Builder addDialog() {
		final CharSequence[] additems = getResources().getStringArray(
				R.array.normalStatus);
		AlertDialog.Builder addDialog = alertDialogBuilder(true);
		addDialog.setTitle(getResources().getString(R.string.chooseFunction))
				.setItems(additems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mPhoneOrder.clear();
						addDialogChoiceMode(which);
					}

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
				}).create();
		return addDialog;
	}

	private AlertDialog.Builder addPhoneDialog(final int position) {
		final CharSequence[] additems = getResources().getStringArray(
				R.array.phoneStatus);
		AlertDialog.Builder addPhoneDialog = alertDialogBuilder(true);
		addPhoneDialog.setTitle(getResources().getString(R.string.chooseFunction))
				.setItems(additems, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						addPhoneChoiceMode(position, which);
					}

					private void addPhoneChoiceMode(final int position,
							int which) {
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
				}).create();
		return addPhoneDialog;
	}

	private AlertDialog.Builder notificationDialog() {
		List<String> add = mNotificaion
				.getNotifiycationsType(Info.getTableId());
		String[] additems = (String[]) add.toArray(new String[add.size()]);
		AlertDialog.Builder notificationDialog = alertDialogBuilder(false);
		notificationDialog
				.setTitle(getResources().getString(R.string.customerCall))
				.setIcon(R.drawable.ic_launcher)
				.setItems(additems, null)
				.setNegativeButton(getResources().getString(R.string.cancel),
						null)
				.setPositiveButton(getResources().getString(R.string.ok),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								cleanNotification();
							}
						}).create();
		return notificationDialog;
	}

	private Builder changeTableDialog(final int type) {
		final AlertDialog.Builder changeTableAlertDialog = alertDialogBuilder(false);
		View layout = getDialogLayout(R.layout.change_dialog, R.id.change);
		personsEdit = (EditText) layout.findViewById(R.id.personsEdit);
		if (Setting.enabledPersons()) {
			tableIdEdit = (EditText) layout.findViewById(R.id.tableIdEdit);
			changeTableAlertDialog.setView(layout);
		} else {
			tableIdEdit = editTextListener();
			changeTableAlertDialog.setView(tableIdEdit);
		}
		tableIdEdit.addTextChangedListener(watcher(tableIdEdit));
		personsEdit.addTextChangedListener(watcher(personsEdit));
		changeTableAlertDialog.setTitle(getResources().getString(R.string.pleaseInput));
		changeTableAlertDialog.setPositiveButton(
				getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						String changePersons;
						String changeTId = tableIdEdit.getText().toString();
						if (Setting.enabledPersons()) {
							changePersons = personsEdit.getText().toString();
						} else {
							changePersons = "0";
						}
						if (type == CHANGE_DIALOG) {
							judgeChangeTable(changePersons, changeTId);
						} else if (type == COMBINE_DIALOG) {
							judgeCombineTable(changePersons, changeTId);
						}
					}
				});
		changeTableAlertDialog.setNegativeButton(
				getResources().getString(R.string.cancel), null);
		return changeTableAlertDialog;
	}

	private Builder copyTableDialog() {
		final EditText copyTableText = editTextListener();
		final AlertDialog.Builder copyTableAlertDialog = alertDialogBuilder(false);
		copyTableText.addTextChangedListener(watcher(copyTableText));
		copyTableAlertDialog.setTitle(getResources().getString(R.string.pleaseInput));
		copyTableAlertDialog.setView(copyTableText);
		copyTableAlertDialog.setPositiveButton(
				getResources().getString(R.string.ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						String changeTId = copyTableText.getEditableText()
								.toString();
						if (changeTId.equals("")) {
							toastText(R.string.idAndPersonsIsNull);
						} else if (isBoundaryLegal(changeTId,
								Table.OPEN_TABLE_STATUS)) {
							copyTable(mSettings.getId(changeTId));
						} else {
							toastText(R.string.copyTIdwarning);
						}
					}
				});
		copyTableAlertDialog.setNegativeButton(
				getResources().getString(R.string.cancel), null);
		return copyTableAlertDialog;
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
		ArrayList<HashMap<String, Object>> checkOut = mSettings.getCombine();
		final List<Integer> tableId = new ArrayList<Integer>();
		final List<String> tableNameStr = new ArrayList<String>();
		final AlertDialog.Builder checkOutAlertDialog = alertDialogBuilder(false);
		if (checkOut.size() <= 0) {
			checkOutAlertDialog.setMessage(getResources().getString(R.string.tableNotOpen));
			checkOutAlertDialog.setPositiveButton(
					getResources().getString(R.string.ok), null);
			return checkOutAlertDialog;
		}
		for (HashMap<String, Object> item : checkOut) {
			tableNameStr.add(item.get("name").toString());
			tableId.add(item.get("id").hashCode());
		}
		final int size = mSettings.size();
		final boolean selected[] = new boolean[size];
		checkOutAlertDialog.setTitle(getResources().getString(R.string.chooseTableId));
		checkOutAlertDialog.setMultiChoiceItems(
				(String[]) tableNameStr.toArray(new String[0]), null,
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface,
							int which, boolean isChecked) {
						selected[which] = isChecked;
					}
				});
		DialogInterface.OnClickListener btnListener = new DialogInterface.OnClickListener() {
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
					showProgressDlg(getResources().getString(R.string.statisticsAmount));
					getTotalPriceTable();
				}

			}
		};

		checkOutAlertDialog.setPositiveButton(
				getResources().getString(R.string.ok), btnListener);
		checkOutAlertDialog.setNegativeButton(
				getResources().getString(R.string.cancel), null);
		return checkOutAlertDialog;
	}

	// protected Builder checkOutSubmitDialog() {
	// final AlertDialog.Builder changeTableAlertDialog =
	// alertDialogBuilder(false);
	// View layout = getDialogLayout(R.layout.checkout_dialog, R.id.check_out);
	// TextView receivableText = (TextView) layout
	// .findViewById(R.id.receivableQuan);
	// final EditText incomeEdit = (EditText) layout
	// .findViewById(R.id.incomeEdit);
	// final TextView changeText = (TextView) layout
	// .findViewById(R.id.changeQuan);
	// TextWatcher watcher = new TextWatcher() {
	// String tempStr;
	//
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before,
	// int count) {
	// tempStr = s.toString();
	// if (tempStr.indexOf(0) == 0) {
	// tempStr = tempStr.substring(0);
	// }
	// }
	//
	// @Override
	// public void afterTextChanged(Editable arg0) {
	// if (tempStr.length() > 0) {
	// mIncome = Double.valueOf(tempStr).doubleValue();
	// mChange = mIncome - mTotalPrice;
	// changeText.setText(String.valueOf(mChange));
	// }
	// }
	//
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count,
	// int after) {
	// }
	//
	// };
	// incomeEdit.addTextChangedListener(watcher);
	// receivableText.setText(String.valueOf(mTotalPrice));
	// changeTableAlertDialog.setView(layout);
	// changeTableAlertDialog.setTitle(getResources().getString(R.string.pleaseInput));
	// changeTableAlertDialog.setPositiveButton(getResources().getString(R.string.ok),
	// new DialogInterface.OnClickListener() {
	//
	// @Override    
	// public void onClick(DialogInterface dialog, int i) {
	// checkOut(selectedTable, tableName, mTotalPrice,
	// mIncome, mChange);
	// }
	// });
	// changeTableAlertDialog.setNegativeButton(getResources().getString(R.string.cancel),
	// null);
	// return changeTableAlertDialog;
	// }

	private AlertDialog.Builder alertDialogBuilder(boolean cancelable) {
		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
				TableClickActivity.this);
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.setCancelable(cancelable);
		return alertDialog;
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

	private boolean isName(String tableName) {
		return (mSettings.getId(tableName) != -1);
	}

	private boolean isBoundaryLegal(String changeName, int status) {
		return isName(changeName) && isStatusLegal(changeName, status);
	}

	private int getNotifiycations() {
		int ret = mNotificaion.getNotifiycations();
		mRingtoneHandler.sendEmptyMessage(ret);
		return ret;
	}

	private void judgeChangeTable(String changePersons, String changeName)
			throws NumberFormatException {
		if (changeName.equals("") || changePersons.equals("")) {
			toastText(R.string.idAndPersonsIsNull);
		} else if (isBoundaryLegal(changeName, Table.NORMAL_TABLE_STAUTS)) {
			changeTable(mSettings.getId(changeName),
					Integer.parseInt(changePersons));
		} else {
			toastText(R.string.changeTIdWarning);
		}
	}

	private void judgeCombineTable(String changePersons, String changeName)
			throws NumberFormatException {
		if (changeName.equals("") || changePersons.equals("")) {
			toastText(R.string.idAndPersonsIsNull);
		} else if (isBoundaryLegal(changeName, Table.OPEN_TABLE_STATUS)) {
			combineTable(mSettings.getId(changeName),
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
			new AlertDialog.Builder(TableClickActivity.this)
					.setTitle(getResources().getString(R.string.notice))
					.setCancelable(false)
					.setMessage(getResources().getString(R.string.islogOut))
					.setPositiveButton(getResources().getString(R.string.ok),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Info.setMode(Info.WORK_MODE_CUSTOMER);
									Info.setTableId(-1);
									finish();
								}
							})
					.setNegativeButton(
							getResources().getString(R.string.cancel), null)
					.show();
		}
	};

	class tableItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
													// click happened
				View arg1,// The view within the AdapterView that was clicked
				int arg2,// The position of the view in the adapter
				long arg3// The row id of the item that was clicked
		) {

			Info.setTableName(mSettings.getNameIndex(arg2));
			Info.setTableId(mSettings.getIdIndex(arg2));
			tableItemChioceDialog(arg2, mSettings.getStatus(arg2));
			mImageItems.notifyDataSetChanged();
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