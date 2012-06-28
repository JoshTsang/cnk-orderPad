package com.htb.cnk.data;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.htb.cnk.lib.Http;
import com.htb.constant.Server;

import android.util.Log;

public class TableSetting {
	public class TableSettingItem {
		protected String mState;
		protected String mName;
		protected int mId;

		public TableSettingItem(String state, String name, int id) {
			mState = state;
			mName = name;
			mId = id;
		}

		public void setState(String state) {
			mState = state;
		}

		public String getState() {
			return mState;
		}
		
		public int getId() {
			return mId;
		}
		

	}

	static  List<TableSettingItem> mTableSettings = new ArrayList<TableSettingItem>();

	public TableSetting() {

	}

	public void add(TableSettingItem item) {
		mTableSettings.add(item);
	}

	public int size() {
		return mTableSettings.size();
	}

	public String getState(int index) {
		return mTableSettings.get(index).getState();
	}

	public int getId(int index) {
		return mTableSettings.get(index).getId();
	}
	
	public void setState(int index, String n) {
		mTableSettings.get(index).setState(n);
	}

	public void parse() {
		mTableSettings.clear();
		TableSettingItem asItem;
		TableSetting setting = new TableSetting();
		for (int i = 0; i < 4; i++) {
			asItem = new TableSettingItem(null, null, i);
			asItem.setState(String.valueOf(i));
			setting.add(asItem);
			Log.d("as", "i:" + i);
		}
		for (int i = 0; i < 4; i++) {
			Log.d("as", "i:" + mTableSettings.get(i));
		}

	}

	
	public int getJson(){
		String tableStatusPkg = Http.get(Server.GET_TABLE_STATUS,"");
		try{		
			JSONArray tableList = new JSONArray(tableStatusPkg);
			int length = tableList.length();
			TableSettingItem asItem;
			TableSetting setting = new TableSetting();
			 for(int i = 0; i < length; i++){//遍历JSONArray
	                Log.d("debugTest",Integer.toString(i));
	                JSONObject item = tableList.getJSONObject(i);
	                int id = item.getInt("id");
	                Log.d("debugTest","id"+id);
	                String name = item.getString("name");
	                String status = item.getString("status");
	                asItem = new TableSettingItem(status,name,id);
	                setting.add(asItem);
	            }
			
		}catch (Exception e) {
		
			// TODO: handle exception
		}
		
		
		return 0;
	}
	
	public byte[] readStream(InputStream inputStream) throws Exception
    {
        byte[] buffer=new byte[1024];
        int len=-1;
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        
        while((len=inputStream.read(buffer))!=-1)
        {
            byteArrayOutputStream.write(buffer,0,len);
        }
        
        inputStream.close();
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
	
	public int remove(int index) {
		mTableSettings.remove(index);
		return 0;
	}

	public int clear() {
		mTableSettings.clear();
		return 0;
	}
}
