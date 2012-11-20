package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.htb.constant.Table;

public class PendedOrder {
	public final static String TAG = "PendedOrder";
	
	class PendedOrderDetail {
		int tid;
		int status;
		String tableName;
		String MD5;
		String order;
		
		public PendedOrderDetail(int id, String name, int status, String order) {
			tid = id;
			this.tableName = name;
			this.order = order;
			this.status = status;
			MD5 = getMD5(order);
			Log.d(TAG, MD5);
		}
		
		public int getTableId() {
			return tid;
		}
		
		public String getTableName() {
			return tableName;
		}
		
		public String getOrder() {
			return order;
		}
		
		public int getStatus() {
			return status;
		}
		
		public String getMD5() {
			return MD5;
		}
		
		private String getMD5(String src) {
			String s = null;
			byte[] source = src.getBytes();
		    char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D',  'E', 'F'}; 
			try
			{
			    java.security.MessageDigest md = java.security.MessageDigest.getInstance( "MD5" );
				md.update(source);
				byte tmp[] = md.digest();         
				char str[] = new char[16 * 2];   
				
				int k = 0;                                
				for (int i = 0; i < 16; i++) {
					 byte byte0 = tmp[i];
					 str[k++] = hexDigits[byte0 >>> 4 & 0xf];
					                                        
					 str[k++] = hexDigits[byte0 & 0xf];    
				} 
				s = new String(str);
			
		   	}catch( Exception e ) {
		   		e.printStackTrace();
		    }
		   	return s;
		}
	}
	
	private List<PendedOrderDetail> pendedOrders = new ArrayList<PendedOrder.PendedOrderDetail>();
	public static final Object lock = new Object();

	public void add(int id, String name, int status, String order) {
		PendedOrderDetail porder = new PendedOrderDetail(id, name, status, order);
		pendedOrders.add(porder);
	}
	
	public void remove(int id)  {
		for (int i=count()-1; i>=0; i--) {
			if (pendedOrders.get(i).getTableId() == id) {
				pendedOrders.remove(i);
				break;
			}
		}
	}
	
	public int submit() {
		synchronized (lock) {
			int count = count();
			if (count > 0) {
				TableSetting.setLocalTableStatusById(pendedOrders.get(0).tid, Table.OPEN_TABLE_STATUS);;
				int ret = MyOrder.submitPendedOrder(pendedOrders.get(0)
						.getOrder(), pendedOrders.get(0).getStatus(), pendedOrders.get(0).getMD5());
				if (ret >= 0) {
					pendedOrders.remove(0);
				} else {
					return -1;
				}
			}
			return 0;
		}
	}
	
	public int count() {
		return pendedOrders.size();
	}
}
