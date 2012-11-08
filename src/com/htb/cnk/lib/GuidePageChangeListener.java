package com.htb.cnk.lib;

import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.htb.cnk.adapter.TableAdapter;
import com.htb.cnk.ui.base.TableGridActivity;

/** 指引页面改监听器 */
public class GuidePageChangeListener implements OnPageChangeListener {
	private final TableGridActivity mActivity;
	private TableAdapter mTableInfo;
	public GuidePageChangeListener(TableGridActivity activity,TableAdapter tableAdaoter){
		mActivity = activity;
		mTableInfo = tableAdaoter;
	}
	
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
	}

	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	public void onPageSelected(int arg0) {
		mActivity.setCurPage(arg0);
		mTableInfo.clearLstImageItem();
		mActivity.updateGridViewAdapter(arg0);
	}

}
