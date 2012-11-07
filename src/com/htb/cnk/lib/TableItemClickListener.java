package com.htb.cnk.lib;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.htb.cnk.adapter.TableAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.ui.base.GridBaseActivity;
import com.htb.cnk.ui.base.TableGridDeskActivity;

public class TableItemClickListener implements OnItemClickListener{ 
	private final static String TAG = "TableItemClickListener";
	private  GridBaseActivity tableGridDesk;
	protected TableAdapter mTableInfo;
	public TableItemClickListener(Context context,TableAdapter tableInfo){
		tableGridDesk = new GridBaseActivity(context){};
		mTableInfo = tableInfo;
	}
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
			tableGridDesk.toastText("不能获取信息，请检查设备！");
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
			tableGridDesk.addDialog().show();
			break;
		case 1:
			tableGridDesk.cleanDialog().show();
			break;
		case 50:
		case 51:
			if (TableGridDeskActivity.networkStatus) {
				tableGridDesk.addPhoneDialog(arg2).show();
			} else {
				tableGridDesk.networkErrDlg();
			}
			break;
		case 100:
		case 101:
		case 150:
		case 151:
			if (TableGridDeskActivity.networkStatus) {
				tableGridDesk.notificationDialog().show();
			} else {
				tableGridDesk.networkErrDlg();
			}
			break;
		default:
			tableGridDesk.addDialog().show();
			break;
		}
	}

}
