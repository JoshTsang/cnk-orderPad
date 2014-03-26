package com.htb.cnk;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.htb.cnk.adapter.CategoryListAdapter;
import com.htb.cnk.adapter.DishListAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.ui.base.MenuBaseActivity;
import com.htb.cnk.utils.MyLog;

/**
 * @author josh
 * 
 */
public class MenuActivity extends MenuBaseActivity {
	public final String TAG = "MenuActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		layout = R.layout.menu_activity;
		super.onCreate(savedInstanceState);
		mMyOrder.talbeClear();
		setListData();
		setMenuView();
		setMenuClickListener();
		if (Info.isNewCustomer() && Info.getMode() == Info.WORK_MODE_CUSTOMER) {
			showGuide();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateOrderedDishCount();
		if (mDishLstAdapter != null) {
			mDishLstAdapter.notifyDataSetChanged();
		}
	}
	
	private void setMenuView() {
		if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
			mSettingsBtn.setVisibility(View.GONE);
		} else {
			mSettingsBtn.setText("快捷");
		}
	}
	
	private void setMenuClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
		mSettingsBtn.setOnClickListener(settingBtnClicked);
		mCategoriesLst.setOnItemClickListener(CategoryListClicked);
	}
	
	private void updateDishes(final int position) {
		mDishes.clear();
		mDishLstAdapter.notifyDataSetChanged();
		// new Thread() {
		// public void run() {
		// int ret = mDishes.setCategory(
		// mCategories.getCategoryId(position),
		// mCategories.getTableName(position));
		// handler.sendEmptyMessage(ret);
		// }
		// }.start();
		mDishes.setCategory(mCategories.getCategoryId(position));
		mDishLstAdapter.notifyDataSetChanged();
	}
	
	private void setListData() {
		if (mCategories.count() <= 0) {
			errorAccurDlg("菜谱数据损坏,请更新菜谱!", FINISH_ACTIVITY);
			MyLog.e(TAG, "menu data base is broken, categories.count <= 0");
			return;
		}
		setCategories();
		setDishes();
		updateOrderedDishCount();
	}

	private void setCategories() {
		mCategoriesLst.setAdapter(new CategoryListAdapter(this, mCategories));
	}

	private void setDishes() {
		mDishLstAdapter = new DishListAdapter(this, mDishes) {
			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
					return getMenuView(position, convertView);
				} else {
					return getFastOrderMenu(position, convertView);
				}
			}
		};
		if (mCategories.count() <= 0) {
			return;
		}
		mDishLstTitle.setText(mCategories.getName(0));
		mDishesLst.setAdapter(mDishLstAdapter);
		View emptyView = getLayoutInflater().inflate(R.layout.empty_list, null);

		((ViewGroup) mDishesLst.getParent()).addView(emptyView,
				new LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));
		mDishesLst.setEmptyView(emptyView);

		updateDishes(0);
	}

	private void showGuide() {
		final Dialog dialog = new Dialog(MenuActivity.this,
				R.style.FULLTANCStyle);

		dialog.setContentView(R.layout.guide);
		dialog.setCancelable(true);

		ImageButton btnCancel = (ImageButton) dialog.findViewById(R.id.guide);
		btnCancel.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View view) {
				dialog.cancel();
			}
		});
		Info.setNewCustomer(false);
		dialog.show();
	}

	private OnItemClickListener CategoryListClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {
			if (!mCategories.getName(position).equals(mDishLstTitle.getText())) {
				mDishLstTitle.setText(mCategories.getName(position));
				updateDishes(position);
			}
		}
	};

	private OnClickListener backBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			MenuActivity.this.finish();
		}
	};

	private OnClickListener settingBtnClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(MenuActivity.this, QuickMenuActivity.class);
			Info.setMenu(Info.ORDER_QUCIK_MENU);
			MenuActivity.this.startActivity(intent);
			finish();
		}

	};

}
