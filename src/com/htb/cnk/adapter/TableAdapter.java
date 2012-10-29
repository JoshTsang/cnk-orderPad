package com.htb.cnk.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.htb.cnk.R;
import com.htb.cnk.data.Notifications;
import com.htb.cnk.data.TableSetting;
import com.htb.constant.Table;

public class TableAdapter {
	private static final String TAG = "TableAdapter";
	public static final String IMAGE_ITEM = "imageItem";
	public static final String ITEM_TEXT = "ItemText";

	public static final int FILTER_NONE = 0;
	public static final int FILTER_FLOOR = 1;
	public static final int FILTER_SCOPE = 2;
	public static final int FILTER_AREA = 3;
	private int mImageItem;
	private int mTextItem;
	private TableSetting mSetting;
	private Notifications mNotification;
	private ArrayList<HashMap<String, Object>> lstImageItem;
	private List<TableSetting.TableSettingItem> resultSet;
	private Context mContext;
	public TableAdapter(ArrayList<HashMap<String, Object>> lst,
			Notifications notification, TableSetting ts,Context context) {
		lstImageItem = lst;
		mNotification = notification;
		mSetting = ts;
		mContext = context;
	}

	public void filterTables(int arg, int filterType) {
		setStatusAndIcon(arg, filterType);
	}

	public String getName(int index) {
		return resultSet.get(index).getName();
	}

	public int getId(int index) {
		return resultSet.get(index).getId();
	}

	public int getStatus(int index) {
		return resultSet.get(index).getStatus();
	}

	public void setStatus(int index, int status) {
		resultSet.get(index).setStatus(status);
	}

	public void clearLstImageItem() {
		lstImageItem.clear();
	}

	private void setStatusAndIcon(int floorNum, int filterType) {
		switch (filterType) {
		case FILTER_FLOOR:
			resultSet = mSetting.getTablesByFloor(floorNum);
			break;
		case FILTER_SCOPE:
			resultSet = mSetting.getTablesByScope();
			break;
		case FILTER_AREA:
			resultSet = mSetting.getTablesByArea();
			break;
		default:
			resultSet = mSetting.getTables();
			break;
		}
		
		int tableSize = resultSet.size();
		for (int i = 0, n = 0; i < tableSize; i++) {
			int status = resultSet.get(i).getStatus();
			if (status < Table.NOTIFICATION_STATUS
					&& mNotification.getId(n) == resultSet.get(i).getId()) {
				status = status + Table.NOTIFICATION_STATUS;
				n++;
			}

			setTableIcon(floorNum, i, status);
		}
	}

	private void setTableIcon(int floor, int position, int status) {
		HashMap<String, Object> map;
		if (lstImageItem.size() <= position) {
			map = new HashMap<String, Object>();
			map.put(ITEM_TEXT, resultSet.get(position).getName());
		} else {
			map = lstImageItem.get(position);
		}

		imageItemSwitch(floor, position, status, map);
		if (lstImageItem.size() <= position) {
			lstImageItem.add(map);
		}
	}

	// TODO define belows
	private void imageItemSwitch(int floor, int position, int status,
			HashMap<String, Object> map) {
		switch (status) {
		case 0:
			map.put(IMAGE_ITEM, R.drawable.table_red);
			break;
		case 1:
			map.put(IMAGE_ITEM, R.drawable.table_blue);
			break;
		case 50:
		case 51:
			map.put(IMAGE_ITEM, R.drawable.table_yellow);
			break;
		case 100:
			map.put(IMAGE_ITEM, R.drawable.table_rednotification);
			setStatus(position, status);
			break;
		case 101:
			map.put(IMAGE_ITEM, R.drawable.table_bluenotification);
			setStatus(position, status);
			break;
		case 150:
		case 151:
			map.put(IMAGE_ITEM, R.drawable.table_yellownotification);
			setStatus(position, status);
			break;
		default:
			map.put(IMAGE_ITEM, R.drawable.table_red);
			Log.e(TAG,
					"unknown status:"
							+ status
							+ ";"
							+ (new Exception()).getStackTrace()[2]
									.getLineNumber());
			break;
		}
	}

	public void setImageItem(int imageItem) {
		this.mImageItem = imageItem;
	}

	public int getImageItem() {
		return this.mImageItem;
	}

	public void setTextItem(int textItem) {
		this.mTextItem = textItem;
	}

	public int getTextItem() {
		return this.mTextItem;
	}

}
