package com.htb.cnk.adapter;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.htb.cnk.R;
import com.htb.cnk.lib.TableItemClickListener;
import com.htb.cnk.ui.base.TableGridDeskActivity;

public class GuidePageAdapter extends PagerAdapter {
	private ViewGroup layout;
	private GridView mGridView;
	private final TableGridDeskActivity tableGridDesk;
	protected SimpleAdapter mImageItems;
	private TableAdapter mTableInfo;
	public GuidePageAdapter(TableGridDeskActivity tableDeskActivity,SimpleAdapter imageItems,TableAdapter tableAdapter){
		tableGridDesk = tableDeskActivity;
		mImageItems = imageItems;
		mTableInfo = tableAdapter;
	}
	
	public void init() {
		mGridView = (GridView) layout.findViewById(R.id.gridview);
		mGridView.setAdapter(mImageItems);
		mGridView.setOnItemClickListener(new TableItemClickListener(tableGridDesk, mTableInfo));
	}

	@Override
	public int getCount() {
		return tableGridDesk.getSettings().getFloorNum() + TableGridDeskActivity.EXTERN_PAGE_NUM;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	// 这里是销毁上次滑动的页面，很重要
	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		View view = (View) arg2;
		((ViewPager) arg0).removeView(view);
		view = null;
	}

	// 这里是初始化gridView的过程
	@Override
	public Object instantiateItem(View arg0, int arg1) {
		LayoutInflater inflate = tableGridDesk.getLayoutInflater();
		layout = (ViewGroup) inflate.inflate(R.layout.gridview, null);
		init();
		((ViewPager) arg0).addView(layout);
		layout.setTag(arg1);
		return layout;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finishUpdate(View arg0) {
		// TODO Auto-generated method stub

	}
}
