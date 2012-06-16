package com.htb.cnk;


import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.htb.cnk.adapter.CategoryListAdapter;
import com.htb.cnk.adapter.DishListAdapter;
import com.htb.cnk.data.Categories;
import com.htb.cnk.data.Dish;
import com.htb.cnk.data.Dishes;
import com.htb.cnk.data.MyOrder;

public class MenuActivity extends Activity {

	private ListView mCategoriesLst;
	private ListView mDishesLst;
	private Button mBackBtn;
	private Button mSettingsBtn;
	private Button mMyOrderBtn;
	private TextView mDishLstTitle;
	private TextView mOrderedDishCount;
	private Categories mCategories = new Categories();
	private Dishes mDishes = new Dishes();
	private DishListAdapter mDishLstAdapter;
	private MyOrder mMyOrder = new MyOrder();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_activity);
		findViews();
		setTestData();
		setListData();
		setClickListener();
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back_btn);
		mSettingsBtn = (Button) findViewById(R.id.settings_btn);
		mMyOrderBtn = (Button) findViewById(R.id.myOrder);
		mCategoriesLst = (ListView) findViewById(R.id.categories);
		mDishesLst = (ListView) findViewById(R.id.dishes);
		mDishLstTitle = (TextView) findViewById(R.id.category);
		mOrderedDishCount = (TextView) findViewById(R.id.orderedCount);
	}
	
	private void setListData() {
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
				if(convertView==null)
				{
					convertView=LayoutInflater.from(MenuActivity.this).inflate(R.layout.item_dish, null);
				}
				Dish dishDetail = mDishes.getDish(position);

				ImageButton dishPic = (ImageButton) convertView.findViewById(R.id.pic);
				TextView dishName = (TextView) convertView.findViewById(R.id.dishName);
				TextView dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
				Button plusBtn = (Button) convertView.findViewById(R.id.dishPlus);
				Button minusBtn = (Button) convertView.findViewById(R.id.dishMinus);
				
				
				FileInputStream inStream = null;
				try {
					Log.d("fileName", "ldpi_" + dishDetail.getPic() + ".jpg");
					inStream = openFileInput("ldpi_" + dishDetail.getPic() + ".jpg");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (inStream != null) {
					Bitmap photo = BitmapFactory.decodeStream(inStream);  
					Drawable drawable = new BitmapDrawable(photo);
					dishPic.setTag(position);
					dishPic.setBackgroundDrawable(drawable);
					dishPic.setOnClickListener(new Button.OnClickListener() {  
			            public void onClick(View view) {  
			            	final int position = Integer.parseInt(view.getTag().toString());
					        final Dialog dialog = new Dialog(MenuActivity.this, R.style.TANCStyle);

					        ImageView dishPicView;  
					        
					        dialog.setContentView(R.layout.dish_detail);  
					        dishPicView = (ImageView) dialog.findViewById(R.id.dishBigPic);
					        dialog.setCancelable(true);  
					        
					        FileInputStream isBigPic = null;
							try {
								isBigPic = openFileInput("hdpi_" + mDishes.getDish(position).getPic() + ".jpg");
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
							if (isBigPic != null) {
								Bitmap photo = BitmapFactory.decodeStream(isBigPic);  
								Drawable drawable = new BitmapDrawable(photo);
								dishPicView.setBackgroundDrawable(drawable);
							} else {
						        dishPicView.setBackgroundResource(R.drawable.no_pic_bigl);
							}
							
					        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);  
					        btnCancel.setOnClickListener(new Button.OnClickListener() {  
					            public void onClick(View view) {  
					                dialog.cancel();  
					            }  
					        });  
					        dialog.show();  
			            }  
					});
				} else {
					Resources resources = MenuActivity.this.getResources();
					dishPic.setBackgroundDrawable(resources.getDrawable(R.drawable.no_pic_bigl));
				}
	            
				dishName.setText(dishDetail.getName());
				dishPrice.setText(Double.toString(dishDetail.getPrice()) + " 元/份");
				
				plusBtn.setTag(position);
				plusBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						final int position = Integer.parseInt(v.getTag().toString());
						mMyOrder.add(mDishes.getDish(position), 1);
						updateOrderedDishCount();
					}
				});
				
				minusBtn.setTag(position);

				minusBtn.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {
						final int position = Integer.parseInt(v.getTag().toString());
						mMyOrder.minus(mDishes.getDish(position), 1);
						updateOrderedDishCount();
					}
				});
				return convertView;
			}
		};
		
		mDishLstTitle.setText(mCategories.getName(0));
		mDishesLst.setAdapter(mDishLstAdapter);
	}
	
	private void setClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
		mSettingsBtn.setOnClickListener(settingBtnClicked);
		mMyOrderBtn.setOnClickListener(myOrderBtnClicked);
		mCategoriesLst.setOnItemClickListener(CategoryListClicked);
	}
	
	private void setTestData() {
//		mCategories.add("凉菜");
//		mCategories.add("热菜");
//		mCategories.add("汤菜");
//		mCategories.add("饮料");
//		mCategories.add("测试1");
//		mCategories.add("测试2");
//		mCategories.add("测试3");
//		mCategories.add("测试4");
//		mCategories.add("测试5");
//		mCategories.add("测试6");
	}
	
	private OnItemClickListener CategoryListClicked = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,
				long arg3) {
			mDishLstTitle.setText(mCategories.getName(position));
			mDishes.setCategory(mCategories.getId(position));
			mDishLstAdapter.notifyDataSetChanged();
		}
	};
	
	private OnClickListener backBtnClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			MenuActivity.this.finish();
		}
	};
	
	private OnClickListener settingBtnClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(MenuActivity.this, SettingsActivity.class);
			startActivity(intent);
			MenuActivity.this.finish();
		}
	};
	
	private OnClickListener myOrderBtnClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(MenuActivity.this, MyOrderActivity.class);
			startActivity(intent);
		}
	};
	
	private void updateOrderedDishCount() {
		mOrderedDishCount.setText(Integer.toString(mMyOrder.count()));
	}
}
