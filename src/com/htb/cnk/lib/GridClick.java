package com.htb.cnk.lib;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.htb.cnk.MenuActivity;
import com.htb.cnk.QuickMenuActivity;
import com.htb.cnk.R;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.PhoneOrder;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.dialog.ItemDlg;
import com.htb.cnk.dialog.TitleAndMessageDlg;
import com.htb.cnk.dialog.ViewDlg;
import com.htb.cnk.service.NotificationTableService;
import com.htb.constant.ErrorNum;

public abstract class GridClick extends Activity {
	private static final String TAG = "GridBaseActivity";
	protected final int UPDATE_TABLE_INFOS = 500;

	protected List<Integer> selectedTable = new ArrayList<Integer>();
	protected Ringtone mRingtone;
	protected AlertDialog.Builder mNetWrorkAlertDialog;

	protected final int COMBINE_DIALOG = 1;
	protected final int CHANGE_DIALOG = 2;
	protected Context mContext;
	protected ItemDlg mItemDialog;
	protected TableSetting mSettings;
	protected ProgressDialog mpDialog;
	protected ViewDlg mViewDialog;
	protected Intent intent = new Intent();
	protected PhoneOrder mPhoneOrder;
	protected TitleAndMessageDlg mTitleAndMessageDialog;
	protected Notifications mNotification = new Notifications();

	public GridClick(Context context) {
		mTitleAndMessageDialog = new TitleAndMessageDlg(context);
		mItemDialog = new ItemDlg(context);
		mViewDialog = new ViewDlg(context);
		mPhoneOrder = new PhoneOrder(context);

		setSettings(new TableSetting(context));
		mContext = context;
		initProgressDlg();
	}

	public void networkErrDlg() {
		toastText(R.string.functionDisableCauseNetworkUnavalialbe);
	}

	public void toastText(String r) {
		Toast.makeText(mContext.getApplicationContext(), r, Toast.LENGTH_LONG)
				.show();
	}

	protected abstract AlertDialog.Builder resultDialog();

	protected void chooseTypeToMenu() {
		if (Info.getMenu() == Info.ORDER_QUCIK_MENU) {
			setClassToActivity(QuickMenuActivity.class);
		} else {
			Info.setMode(Info.WORK_MODE_WAITER);
			setClassToActivity(MenuActivity.class);
		}
	}

	protected void toastText(int r) {
		Toast.makeText(mContext.getApplicationContext(),
				mContext.getResources().getString(r), Toast.LENGTH_LONG).show();
	}

	protected void showProgressDlg(String msg) {
		mpDialog.setMessage(msg);
		mpDialog.show();
	}

	protected void setClassToActivity(Class<?> setClass) {
		intent.setClass(mContext, setClass);
		mContext.startActivity(intent);
	}

	protected TextWatcher watcher(final EditText id) {
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

	protected EditText editTextListener() {
		final EditText editText = new EditText(mContext);
		// editText.setKeyListener(new DigitsKeyListener(false, true));
		// editText
		// .setFilters(new InputFilter[] { new InputFilter.LengthFilter(6) });
		return editText;
	}

	protected View getDialogLayout(int layout_dialog, int id) {
		LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
		View layout = inflater.inflate(layout_dialog, null);
		return layout;
	}

	protected boolean isTId(String tableTId) {
		return (getSettings().getId(tableTId) != -1);
	}

	protected boolean isBoundaryLegal(String tableName, int status) {
		if (tableName.equals(Info.getTableName())) {
			return false;
		}
		return isTId(tableName) && isStatusLegal(tableName, status);
	}

	protected boolean equalNameAndPersons(String changePersons, String tableName) {
		return tableName.equals("") || changePersons.equals("");
	}

	protected int getNotifiycations() {
		int ret = mNotification.getNotifiycations();
		ringtoneHandler.sendEmptyMessage(ret);
		return ret;
	}

	protected int getTableStatusFromServer() {
		int ret = getSettings().getTableStatusFromServerActivity();
		if (ret < 0) {
			tableHandler.sendEmptyMessage(ret);
		}
		return ret;
	}

	protected Handler tableHandler = new Handler() {
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

	protected Handler ringtoneHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what > 0) {
				mRingtone.play();
			}
		}
	};

	protected boolean isPrinterError(Message msg) {
		return msg.what == ErrorNum.PRINTER_ERR_CONNECT_TIMEOUT
				|| msg.what == ErrorNum.PRINTER_ERR_NO_PAPER;
	}

	protected void showNetworkErrDlg(String msg) {
		mNetWrorkAlertDialog.setMessage(msg).show();
	}

	protected void binderStart() {
		Intent intent = new Intent(NotificationTableService.SERVICE_IDENTIFIER);
		intent.putExtra("binder", true);
		mContext.sendBroadcast(intent);
	}

	protected TableSetting getSettings() {
		return mSettings;
	}

	protected void setSettings(TableSetting mSettings) {
		this.mSettings = mSettings;
	}

	private boolean isStatusLegal(String tableName, int status) {
		return getSettings().getStatusTableId(getSettings().getId(tableName)) == status;
	}

	private void initProgressDlg() {
		mpDialog = new ProgressDialog(mContext);
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(false);
		mpDialog.setTitle(mContext.getResources()
				.getString(R.string.pleaseWait));
	}

}