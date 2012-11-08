package com.htb.cnk.lib;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;

import com.htb.cnk.PhoneActivity;
import com.htb.cnk.R;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.TableSetting;

public class GridClickAddPhone extends GridClick {
	private int mPosition;

	public GridClickAddPhone(Context context) {
		super(context);
	}

	public GridClickAddPhone(Context context,int position) {
		super(context);
		mPosition = position;
		resultDialog().show();
	}

	@Override
	protected Builder resultDialog() {
		return addPhoneDialog(mPosition);
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
}
