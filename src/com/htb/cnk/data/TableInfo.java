package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import com.htb.cnk.R;
import com.htb.constant.Table;

public class TableInfo {
	private static final String TAG = "TableInfo";
	public static final String IMAGE_ITEM = "imageItem";
	public static final String ITEM_TEXT = "ItemText";
	private int mImageItem;
	private int mTextItem;
	public TableSetting mStting = new TableSetting();
	private static ArrayList<HashMap<String, Object>> lstImageItem = new ArrayList<HashMap<String, Object>>();
	
	public void addGridItem(int page,Notifications notificaion) {
		setStatusAndIcon(page, notificaion);
	}
	
	public ArrayList<HashMap<String, Object>> getGridItem(){
		return lstImageItem;
	}
	
	private void setStatusAndIcon(int floorNum,Notifications notification) {
		lstImageItem.clear();
		mStting.setFloorCurrent(floorNum);
		Log.d(TAG, "floorNum:"+floorNum);
		int tableSize = mStting.getFloorSize();
		for (int i = 0, n = 0; i < tableSize; i++) {
			int status = mStting.getStatusIndex(i);
			if (status < Table.NOTIFICATION_STATUS
					&& notification.getId(n) == mStting.getIdIndex(i)) {
				status = status + Table.NOTIFICATION_STATUS;
				n++;
			}

			setTableIcon(i, status);
			if (i == 0) {
				Log.d(TAG, "index:" + i + " status:" + status + " lst:"
						+ lstImageItem.get(i).get(IMAGE_ITEM));
			}
		}
	}

	private void setTableIcon(int position, int status) {
		HashMap<String, Object> map;
		if (lstImageItem.size() <= position) {
			map = new HashMap<String, Object>();
			map.put(ITEM_TEXT, mStting.getNameIndex(position));
		} else {
			map = lstImageItem.get(position);
		}

		imageItemSwitch(position, status, map);
		if (lstImageItem.size() <= position) {
			lstImageItem.add(map);
		}
	}

	private void imageItemSwitch(int position, int status,
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
			mStting.setStatus(position, status);
			break;
		case 101:
			map.put(IMAGE_ITEM, R.drawable.table_bluenotification);
			mStting.setStatus(position, status);
			break;
		case 150:
		case 151:
			map.put(IMAGE_ITEM, R.drawable.table_yellownotification);
			mStting.setStatus(position, status);
			break;
		default:
			map.put(IMAGE_ITEM, R.drawable.table_red);
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
