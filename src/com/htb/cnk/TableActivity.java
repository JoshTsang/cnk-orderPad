package com.htb.cnk;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.htb.cnk.data.Info;

public class TableActivity extends TableClickActivity {

	static final String TAG= "TablesActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler();
	}

	private void handler() {
		mNotificationHandler = notificationHandler;
		mTableHandler = tableHandler;
		mRingtoneHandler = ringtoneHandler;
		mTotalPriceTableHandler = totalPriceTableHandler;
		mChangeTIdHandler = changeTIdHandler;
		mCopyTIdHandler = copyTIdHandler;
		mCombineTIdHandler = combineTIdHandler;
	}

	Handler tableHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				if (NETWORK_ARERTDIALOG == 1) {
					mNetWrorkcancel.cancel();
				}
				netWorkDialogShow("网络连接失败，请检查连接网络重试！");
			} else {
				switch (msg.what) {
				case UPDATE_TABLE_INFOS:
					setTableInfos();
					if (mSettings.hasPendedPhoneOrder()) {
						ringtoneHandler.sendEmptyMessage(1);
					}
					break;
				case DISABLE_GRIDVIEW:
					gridview.setOnItemClickListener(null);
					break;
				default:
					break;
				}
			}
		}
	};

	Handler totalPriceTableHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				netWorkDialogShow("统计失败，请检查连接网络重试");
			} else {
				mTotalPrice = (double) msg.what;
				if (mTotalPrice <= 0) {
					toastText("菜品为空，请点菜！");
				} else {
					Intent checkOutIntent = new Intent();
					Bundle bundle =new Bundle();
					Log.d(TAG, "price:" + mTotalPrice);
	                bundle.putDouble("price", mTotalPrice);
	                bundle.putIntegerArrayList("tableId", (ArrayList<Integer>) selectedTable);
	                bundle.putStringArrayList("tableName", (ArrayList<String>) tableName);
	                checkOutIntent.putExtras(bundle); 
					checkOutIntent.setClass(TableActivity.this, CheckOutActivity.class);
					TableActivity.this.startActivity(checkOutIntent);  
				}
			}
			mpDialog.cancel();
		}
	};

	Handler changeTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == -2) {
				toastText(R.string.changeTIdWarning);
			} else if (msg.what == -1) {
				netWorkDialogShow("转台失败，请检查连接网络重试");
			} else if (isPrinterError(msg)) {
				toastText("退菜订单失败");
			} else {
				binderStart();
				toastText(R.string.changeTId);
			}
		}
	};

	Handler copyTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == -2) {
				toastText(R.string.copyTIdwarning);
			} else if (msg.what == -1) {
				netWorkDialogShow("复制失败，请检查连接网络重试");
			} else {
				intent.setClass(TableActivity.this, MyOrderActivity.class);
				Info.setMode(Info.WORK_MODE_WAITER);
				TableActivity.this.startActivity(intent);
			}
		}
	};

	
	Handler combineTIdHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == -2) {
				toastText(R.string.checkOutWarning);
			} else if (msg.what == -1) {
				netWorkDialogShow("合并出错，请检查连接网络重试");
			} else if (isPrinterError(msg)) {
				toastText("合并失败");
			} else {
				binderStart();
				toastText(R.string.combineTId);
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

	Handler notificationHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				// Todo network failure warning
			} else {
				binderStart();
			}
		}
	};

}
