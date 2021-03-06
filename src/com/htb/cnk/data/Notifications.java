package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.htb.cnk.lib.Http;
import com.htb.cnk.utils.MyLog;
import com.htb.constant.Server;

public class Notifications {
	private static final int TIME_OUT = -1;

	public class NotificationItem {
		protected String mType[];
		protected int mId;

		public NotificationItem(int id, String type[]) {
			mType = type;
			mId = id;
		}

		public int getId() {
			return mId;
		}

		public String[] getTypeAll() {
			return mType;
		}

		public String getType(int index) {
			return mType[index];
		}
	}

	static List<NotificationItem> notifications = new ArrayList<NotificationItem>();

	private void add(NotificationItem item) {
		notifications.add(item);
	}

	public void clear() {
		notifications.clear();
	}

	public int getId(int index) {
		if (index >= notifications.size()) {
			return -1;
		}
		return notifications.get(index).getId();
	}

	public String[] getTypeAll(int index) {
		int i;
		for (i = 0; i < notifications.size() - 1; i++) {
			if (index == notifications.get(i).getId()) {
				break;
			}
		}
		return notifications.get(i).getTypeAll();
	}

	public String getType(int notification, int type) {
		return notifications.get(notification).getType(type);
	}

	public int getNotifiycations() {
		String notificationPkg = Http.get(Server.GET_NOTIFICATION, null);
		if (notificationPkg == null || "".equals(notificationPkg)) {
			return TIME_OUT;
		} else if ("null".equals(notificationPkg)) {
			notifications.clear();
			return 0;
		}
		try {
			JSONArray tableList = new JSONArray(notificationPkg);
			int length = tableList.length();
			NotificationItem asItem;
			notifications.clear();
			Notifications setting = new Notifications();
			for (int i = 0; i < length; i++) {
				JSONObject item = tableList.getJSONObject(i);
				int id = item.getInt("tid");
				String notifications = item.getString("notifications");
				String types = notifications.substring(1,
						notifications.indexOf(']'));
				String typesTemp[] = null;
				typesTemp = types.split(",");
				asItem = new NotificationItem(id, typesTemp);
				setting.add(asItem);
			}
			return 1;
		} catch (Exception e) {
			e.printStackTrace();
			MyLog.e("getNotification.php", notificationPkg);
		}

		return -2;
	}

	public List<String> getNotifiycationsType(int index) {
		Notifications setting = new Notifications();
		String types[] = setting.getTypeAll(index);
		List<String> temp = new ArrayList<String>();
		for (int i = 0; i < types.length; i++) {
			temp.add(NotificationTypes.getName(Integer.parseInt(types[i])));
		}
		return temp;
	}

	public static boolean hasNotificationPendedForChargedArea() {
		for (NotificationItem notification:notifications) {
			if (TableSetting.isTableUnderCharge(notification.getId())) {
				return true;
			}
		}
		
		return false;
	}
	
	public int cleanNotifications(int index) {
		String notificationPkg = Http.get(Server.CLEANNOTIFICATION, "TID="
				+ index);
		if (notificationPkg == null) {
			return TIME_OUT;
		}
		return 0;
	}

}
