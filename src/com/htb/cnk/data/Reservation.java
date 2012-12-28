package com.htb.cnk.data;


public class Reservation {
	private String name;
	private String tel;
	private String datetime;
	private String addr;
	private String comment;
	private String tableNames;
	private String tableIds;
	private int deposit;
	private int persons;
	
	public void setName(String v) {
		name = v;
	}
	
	public String getName() {
		return name;
	}
	
	public void setTel(String v) {
		tel = v;
	}
	
	public String getTel() {
		return tel;
	}
	
	public void setDatetime(String v) {
		datetime = v;
	}
	
	public String getDatetime() {
		return datetime;
	}
	
	public void setAddr(String v) {
		addr = v;
	}
	
	public String getAddr() {
		return addr;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String v) {
		comment = v;
	}
	
	public void setTableNames(String v) {
		tableNames = v;
	}
	
	public String getTableNames() {
		return tableNames;
	}
	
	public String getTableNames(String splitBy) {
		if (tableNames != null) {
			String[] tableName = tableNames.split(",");
			StringBuffer tables = new StringBuffer();
			for (int i=0; i<tableName.length; i++) {
				tables.append(tableName[i] + splitBy);
			}
			return tables.toString();
		} else {
			return "";
		}
	}
	
	public String getTableIds() {
		return tableIds;
	}

	public void setTableIds(String v) {
		tableIds = v;
	}
	
	public void setDeposit(int v) {
		deposit = v;
	}

	public int getDeposit() {
		return deposit;
	}
	
	public void setPersons(int v) {
		persons = v;
	}

	public int getPersons() {
		return persons;
	}
}
