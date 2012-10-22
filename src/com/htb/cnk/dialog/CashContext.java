package com.htb.cnk.dialog;

public class CashContext {
	CashSuper cs = null;

	public enum Type {
		正常收费, 打折
	}

	public CashContext(String type, String crMoney) {
		Type currentType;
		if (type.equals("")) {
			currentType = Type.正常收费;
		} else {
			currentType = Type.valueOf(type.toUpperCase());
		}
		switch (currentType) {
		case 正常收费:
			CashNormal cn = new CashNormal();
			cs = cn;
			break;
		case 打折:
			CashRebate cr = new CashRebate(crMoney);
			cs = cr;
			break;
		default:
			break;
		}
	}

	public double getResult(double money) {
		return this.cs.acceptCash(money);
	}
}
