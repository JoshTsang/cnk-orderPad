package com.htb.cnk.dialog;

public class CashRebate extends CashSuper {
	private double moneyRebate ;

	public CashRebate(String rebate) {
		if(rebate == null){
			rebate = "0";
		}
		moneyRebate = Double.valueOf(rebate).doubleValue();
	}

	@Override
	public double acceptCash(double money) {
		return this.moneyRebate * money;
	}

}
