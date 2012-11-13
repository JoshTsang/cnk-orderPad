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
	public static final String IMAGE_ITEM = "imageItem";
	public static final String ITEM_TEXT = "ItemText";
	public static final int FILTER_NONE = 0;
	public static final int FILTER_FLOOR = 1;
	public static final int FILTER_SCOPE = 2;
	public static final int FILTER_AREA = 3;
	
	private static final int TABLE_NOTUSE = R.drawable.table_notuse;
	private static final int TABLE_INUSE = R.drawable.table_inuse;
	private static final int TABLE_PHONE = R.drawable.table_phone;
	private static final int TABLE_NOTUSE_NOTIFICATION = R.drawable.table_notuse_notification;
	private static final int TABLE_INUSE_NOTIFICATION = R.drawable.table_inuse_notification;
	private static final int TABLE_PHONE_NOTIFICATION = R.drawable.table_phone_notification;
	
	private static final String TAG = "TableAdapter";
	private int mImageItem;
	private int mTextItem;
	private TableSetting mSetting;
	private Notifications mNotification = new Notifications();
	private ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
	private List<TableSetting.TableSettingItem> resultSet;
	private Context mContext;

	public TableAdapter(TableSetting ts, Context context) {
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

	public ArrayList<HashMap<String, Object>> getLstImageItem() {
		return lstImageItem;
	}

	private void setStatusAndIcon(int floorNum, int filterType) {
		switch (filterType) {
		case FILTER_FLOOR:
			if (mSetting.getTablesByFloor(floorNum).size() <= 0)
				return;
			resultSet = mSetting.getTablesByFloor(floorNum);
			break;
		case FILTER_SCOPE:
			if (mSetting.getTablesByScope().size() <= 0)
				return;
			resultSet = mSetting.getTablesByScope();
			break;
		case FILTER_AREA:
			if (mSetting.getTablesByArea().size() <= 0)
				return;
			resultSet = mSetting.getTablesByArea();
			break;
		default:
			if (mSetting.getTables().size() <= 0)
				return;
			resultSet = mSetting.getTables();
			break;
		}
		int tableSize = resultSet.size();
		if (tableSize > 0) {
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

	private void imageItemSwitch(int floor, int position, int status,
			HashMap<String, Object> map) {
		switch (status) {
		case 0:
			map.put(IMAGE_ITEM, TABLE_NOTUSE);
			break;
		case 1:
			map.put(IMAGE_ITEM, TABLE_INUSE);
			break;
		case 50:
		case 51:
			map.put(IMAGE_ITEM, TABLE_PHONE);
			break;
		case 100:
			map.put(IMAGE_ITEM, TABLE_NOTUSE_NOTIFICATION);
			setStatus(position, status);
			break;
		case 101:
			map.put(IMAGE_ITEM, TABLE_INUSE_NOTIFICATION);
			setStatus(position, status);
			break;
		case 150:
		case 151:
			map.put(IMAGE_ITEM, TABLE_PHONE_NOTIFICATION);
			setStatus(position, status);
			break;
		default:
			map.put(IMAGE_ITEM, TABLE_NOTUSE);
			Log.e(TAG,
					"unknown status:"
							+ status
							+ ";"
							+ (new Exception()).getStackTrace()[2]
									.getLineNumber());
			break;
		}
	}

}
