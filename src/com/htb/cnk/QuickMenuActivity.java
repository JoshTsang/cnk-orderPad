package com.htb.cnk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.htb.cnk.adapter.DishListAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.QuickOrder;
import com.htb.cnk.lib.MenuBaseActivity;

public class QuickMenuActivity extends MenuBaseActivity {
	private QuickOrder mQuickOrder;
	private InputMethodManager imm;
	private EditText mEditQucik;
	private TextView mTextQucik;
	private int BUTTON_TEXT_CHANGED = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		layout = R.layout.quick_activity;
		super.onCreate(savedInstanceState);
		mQuickOrder = new QuickOrder(QuickMenuActivity.this);
		mQuickOrder.setQucik();
		findQuickMenuViews();
		setQuickMenuClickListener();
		setListData();
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (QuickMenuActivity.this.getCurrentFocus() != null) {
				if (QuickMenuActivity.this.getCurrentFocus().getWindowToken() != null) {
					imm.hideSoftInputFromWindow(QuickMenuActivity.this
							.getCurrentFocus().getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}
		}
		return super.onTouchEvent(event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mEditQucik.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		if (mDishLstAdapter != null) {
			mDishLstAdapter.notifyDataSetChanged();
		}
	}

	private void findQuickMenuViews() {
		mEditQucik = (EditText) findViewById(R.id.edit_quick);
		mSettingsBtn.setText("普通");
		mTextQucik = (TextView) findViewById(R.id.text_quick);
	}
	
	private void setListData() {
		setDishes();
		updateOrderedDishCount();
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
		mDishesLst.setAdapter(mDishLstAdapter);
		mDishLstAdapter.notifyDataSetChanged();
	}

	private void setQuickMenuClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
		mSettingsBtn.setOnClickListener(settingBtnClicked);
		mEditQucik.addTextChangedListener(watcher);
	}

	private OnClickListener backBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			QuickMenuActivity.this.finish();
		}
	};

	private OnClickListener settingBtnClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(QuickMenuActivity.this, MenuActivity.class);
			Info.setMenu(Info.ORDER_LIST_MENU);
			QuickMenuActivity.this.startActivity(intent);
			finish();
		}

	};
	
	@Override
	public void updateOrderedDishCount() {
		BUTTON_TEXT_CHANGED = 1;
		mEditQucik.setText("");
		mOrderedDishCount.setText(Integer.toString(mMyOrder.totalQuantity()));
	}
	
	private TextWatcher watcher = new TextWatcher() {
		private String temp;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			temp = s.toString();
			mTextQucik.setText(s.toString());
			mQuickOrder.queryDish(temp);
			if (BUTTON_TEXT_CHANGED == 0) {
				mDishes.addAll(mQuickOrder.getListDish());
			} else {
				BUTTON_TEXT_CHANGED = 0;
			}
			mDishLstAdapter.notifyDataSetChanged();
		}

		@Override
		public void afterTextChanged(Editable arg0) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

	};

}
