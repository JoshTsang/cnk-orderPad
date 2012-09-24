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
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.Setting;
import com.htb.constant.Table;

public class TableClickActivity extends TableBaseActivity {

	protected final int UPDATE_TABLE_INFOS = 5;
	protected final int DISABLE_GRIDVIEW = 10;
	protected final int CHECKOUT_LIST = 1;
	protected final int COMBINE_DIALOG = 1;
	protected final int CHANGE_DIALOG = 2;
	protected List<Integer> selectedTable = new ArrayList<Integer>();
	private double mIncome;
	private double mChange;

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
		networkAlertDialog.setTitle("错误");// 设置对话框标题
		networkAlertDialog.setMessage("网络连接失败，请检查网络后重试");// 设置对话框内容
		networkAlertDialog.setPositiveButton("重试",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						dialog.cancel();
						NETWORK_ARERTDIALOG = 0;
						showProgressDlg("正在获取状态...");
						binderStart();
					}
				});
		networkAlertDialog.setNegativeButton("退出",
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
		final CharSequence[] cleanitems = { "清台", "转台", "并台", "退菜", "添加菜",
				"查看菜" };
		Dialog cleanDialog = alertDialogBuilder(true).setTitle("选择功能")
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
		cleanTableAlertDialog.setMessage("请确认是否清台");
		cleanTableAlertDialog.setPositiveButton("清台",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						showProgressDlg("正在清台...");
						selectedTable.clear();
						selectedTable.add(Info.getTableId());
						cleanTableThread(selectedTable);
					}
				});
		cleanTableAlertDialog.setNegativeButton("取消", null);
		return cleanTableAlertDialog;
	}

	private AlertDialog.Builder cleanPhoneDialog(final int position) {
		final AlertDialog.Builder cleanPhoneAlertDialog = alertDialogBuilder(false);
		cleanPhoneAlertDialog.setMessage("是否清除顾客点的菜");
		cleanPhoneAlertDialog.setPositiveButton("是",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int i) {
						showProgressDlg("正在删除手机点的菜...");
						cleanPhoneThread(position, Info.getTableId());
						dialog.cancel();
					}
				});
		cleanPhoneAlertDialog.setNegativeButton("否", null);
		return cleanPhoneAlertDialog;
	}

	private AlertDialog.Builder addDialog() {
		final CharSequence[] additems = { "开台-顾客模式 ", "开台-服务模式", "开台-复制模式" };
		AlertDialog.Builder addDialog = alertDialogBuilder(true);
		addDialog.setTitle("选择功能")
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
		final CharSequence[] additems = { "查看顾客已点的菜", "取消顾客已点的菜" };
		AlertDialog.Builder addPhoneDialog = alertDialogBuilder(true);
		addPhoneDialog.setTitle("选择功能")
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
		notificationDialog.setTitle("客户呼叫需求").setIcon(R.drawable.ic_launcher)
				.setItems(additems, null).setNegativeButton("取消", null)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						cleanNotification();
					}
				}).create();
		return notificationDialog;
	}

	private Builder changeTableDialog(final int type) {
		final AlertDialog.Builder changeTableAlertDialog = alertDialogBuilder(false);
		final EditText tableIdEdit;
		final EditText personsEdit;
		View layout = getDialogLayout(R.layout.change_dialog, R.id.change);
		personsEdit = (EditText) layout.findViewById(R.id.personsEdit);
		if (Setting.enabledPersons()) {
			tableIdEdit = (EditText) layout.findViewById(R.id.tableIdEdit);
			changeTableAlertDialog.setView(layout);
		} else {
			tableIdEdit = editTextListener();
			changeTableAlertDialog.setView(tableIdEdit);
		}
		changeTableAlertDialog.setTitle("请输入");
		changeTableAlertDialog.setPositiveButton("确定",
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
						}else if(type == COMBINE_DIALOG){
							judgeCombineTable(changePersons, changeTId);
						}
					}
				});
		changeTableAlertDialog.setNegativeButton("取消", null);
		return changeTableAlertDialog;
	}

	private Builder copyTableDialog() {
		final EditText copyTableText = editTextListener();
		final AlertDialog.Builder copyTableAlertDialog = alertDialogBuilder(false);
		copyTableAlertDialog.setTitle("请输入");
		copyTableAlertDialog.setView(copyTableText);
		copyTableAlertDialog.setPositiveButton("确定",
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
		copyTableAlertDialog.setNegativeButton("取消", null);
		return copyTableAlertDialog;
	}

	private EditText editTextListener() {
		final EditText copyTableText = new EditText(this);
		copyTableText.setKeyListener(new DigitsKeyListener(false, true));
		copyTableText
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
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
		for (HashMap<String, Object> item : checkOut) {
			tableNameStr.add(item.get("name").toString());
			tableId.add(item.get("id").hashCode());
		}
		final int size = mSettings.size();
		final boolean selected[] = new boolean[size];
		final AlertDialog.Builder checkOutAlertDialog = alertDialogBuilder(false);
		checkOutAlertDialog.setTitle("请选择桌号");
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
					showProgressDlg("正在统计金额，请稍等");
					getTotalPriceTable();
				}

			}
		};

		checkOutAlertDialog.setPositiveButton("确认", btnListener);
		checkOutAlertDialog.setNegativeButton("取消", null);
		return checkOutAlertDialog;
	}

	protected Builder checkOutSubmitDialog() {
		final AlertDialog.Builder changeTableAlertDialog = alertDialogBuilder(false);
		View layout = getDialogLayout(R.layout.checkout_dialog, R.id.check_out);
		TextView receivableText = (TextView) layout
				.findViewById(R.id.receivableQuan);
		final EditText incomeEdit = (EditText) layout
				.findViewById(R.id.incomeEdit);
		final TextView changeText = (TextView) layout
				.findViewById(R.id.changeQuan);
		TextWatcher watcher = new TextWatcher() {
			String tempStr;

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				tempStr = s.toString();
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (tempStr.length() > 0) {
					mIncome = Double.valueOf(tempStr).doubleValue();
					mChange = mIncome - mTotalPrice;
					changeText.setText(String.valueOf(mChange));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

		};
		incomeEdit.addTextChangedListener(watcher);
		receivableText.setText(String.valueOf(mTotalPrice));
		changeTableAlertDialog.setView(layout);
		changeTableAlertDialog.setTitle("请输入");
		changeTableAlertDialog.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						checkOut(selectedTable, tableName, mTotalPrice,
								mIncome, mChange);
					}
				});
		changeTableAlertDialog.setNegativeButton("取消", null);
		return changeTableAlertDialog;
	}

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
							mSettings.getStatus(position) - Table.PHONE_STATUS);
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
							TableClickActivity.this, selectedTable);
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

	private void checkOut(final List<Integer> destIId,
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
	
	private boolean isNameMinimum(int tId) {
		return tId >= Integer.parseInt(mSettings.getNameIndex(0));
	}

	private boolean isNameMaximum(int tId) {
		return tId <= Integer
				.parseInt(mSettings.getNameIndex(mSettings.size() - 1));
	}

	private boolean isStatusLegal(String changeTId, int status) {
		return mSettings.getStatusTableId(mSettings.getId(changeTId)) == status;
	}

	private boolean isBoundaryLegal(String changeTId, int status) {
		int tId = Integer.parseInt(changeTId);
		return isNameMinimum(tId) && isNameMaximum(tId)
				&& isStatusLegal(changeTId, status);
	}

	private int getNotifiycations() {
		int ret = mNotificaion.getNotifiycations();
		mRingtoneHandler.sendEmptyMessage(ret);
		return ret;
	}

	private void judgeChangeTable(String changePersons, String changeTId)
			throws NumberFormatException {
		if (changeTId.equals("") || changePersons.equals("")) {
			toastText(R.string.idAndPersonsIsNull);
		} else if (isBoundaryLegal(changeTId, Table.NORMAL_TABLE_STAUTS)) {
			changeTable(mSettings.getId(changeTId),
					Integer.parseInt(changePersons));
		} else {
			toastText(R.string.changeTIdWarning);
		}
	}
	
	private void judgeCombineTable(String changePersons, String changeTId)
			throws NumberFormatException {
		if (changeTId.equals("") || changePersons.equals("")) {
			toastText(R.string.idAndPersonsIsNull);
		} else if (isBoundaryLegal(changeTId, Table.OPEN_TABLE_STATUS)) {
			combineTable(mSettings.getId(changeTId),
					Integer.parseInt(changePersons));
		} else {
			toastText(R.string.combineTIdWarning);
		}
	}
 
	/**
	 * 
	 */
	private void setClassToActivity(Class<?> setClass) {
		intent.setClass(TableClickActivity.this,
				setClass);
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
					.setTitle("注意")
					.setCancelable(false)
					.setMessage("确认交班？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Info.setMode(Info.WORK_MODE_CUSTOMER);
									Info.setTableId(-1);
									finish();
								}
							}).setNegativeButton("取消", null).show();
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