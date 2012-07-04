package com.htb.cnk.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.htb.cnk.R;
import com.htb.cnk.data.Categories;

/**
 * @author josh
 *
 */
public class CategoryListAdapter extends BaseAdapter {

	private Context mContext;
	private Categories mCategories;
	
	public CategoryListAdapter(Context context, Categories categories) {
		mContext = context;
		mCategories = categories;
	}
	
	@Override
	public int getCount() {
		return mCategories.count();
	}

	@Override
	public Object getItem(int arg0) {
		return mCategories.getName(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup view) {
		if(convertView==null)
		{
			convertView=LayoutInflater.from(mContext).inflate(R.layout.item_category, null);
		}
		TextView category = (TextView) convertView.findViewById(R.id.categoryItem);
		category.setText(mCategories.getName(position));
		return convertView;
	}

}
