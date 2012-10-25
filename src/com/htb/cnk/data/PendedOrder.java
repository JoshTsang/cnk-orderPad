package com.htb.cnk.data;

import java.util.ArrayList;
import java.util.List;

public class PendedOrder {

	class PendedOrderDetail {
		int tid;
		String name;
		String order;
		
		public PendedOrderDetail(int id, String name, String order) {
			tid = id;
			this.name = name;
			this.order = order;
		}
		
		public int getTableId() {
			return tid;
		}
		
		public String getTableName() {
			return name;
		}
		
		public String getOrder() {
			return order;
		}
	}
	
	private List<PendedOrderDetail> pendedOrders = new ArrayList<PendedOrder.PendedOrderDetail>();
	
	public void add(int id, String name, String order) {
		PendedOrderDetail porder = new PendedOrderDetail(id, name, order);
		
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
	
	public int count() {
		return pendedOrders.size();
	}
}
