package com.htb.cnk.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.htb.cnk.R;
import com.htb.cnk.data.TableSetting;
import com.umeng.common.Log;

public class GridViewImageAdapter extends BaseAdapter {
	private Context mContext;
	private Vector<Integer> mImageIds = new Vector<Integer>();
	private static Vector<Boolean> mImage_bs = new Vector<Boolean>();

	private int lastPosition = -1;
	private boolean multiChoose;
	private static TableSetting mTableSetting;

	public GridViewImageAdapter(Context context, boolean isMulti) {
		mContext = context;
		multiChoose = isMulti;
		mTableSetting = new TableSetting(context);
		for (int i = 0; i < mTableSetting.getTables().size(); i++) {
			mImageIds.add(R.drawable.table_red);
			mImage_bs.add(false);
		}
		initState(context);
	}

	/**
	 * @param context
	 * @throws NumberFormatException
	 */
	private void initState(Context context) throws NumberFormatException {
		SharedPreferences sharedPre = context.getSharedPreferences(
				"waiterSetting", Context.MODE_PRIVATE);
		String waiterScopeString = sharedPre.getString("waiterScope", "");
		String stringTemp[] = null;
		stringTemp = waiterScopeString.split(",");
		Log.d("", waiterScopeString);
		if (!waiterScopeString.equals("")) {
			for (int i = 0; i < stringTemp.length; i++) {
				if (mTableSetting.getIndexByAllId(Integer
						.parseInt(stringTemp[i])) != -1) {
					mImage_bs.setElementAt(true, mTableSetting
							.getIndexByAllId(Integer.parseInt(stringTemp[i])));
				}
			}
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mImageIds.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = null;
		TextView tableName = null;
		convertView = LayoutInflater.from(mContext).inflate(
				R.layout.table_item, null);
		imageView = (ImageView) convertView.findViewById(R.id.ItemImage);
		tableName = (TextView) convertView.findViewById(R.id.ItemText);
		convertView.setBackgroundDrawable(makeBmp(
				mImageIds.elementAt(position), mImage_bs.elementAt(position)));
		tableName.setText(mTableSetting.getNameByAllIndex(position));
		return convertView;
	}

	private LayerDrawable makeBmp(int id, boolean isChosen) {
		Bitmap mainBmp = ((BitmapDrawable) mContext.getResources().getDrawable(
				id)).getBitmap();

		Bitmap seletedBmp;
		if (isChosen == true)
			seletedBmp = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.btncheck_yes);
		else
			seletedBmp = BitmapFactory.decodeResource(mContext.getResources(),
					R.drawable.btncheck_no);

		Drawable[] array = new Drawable[2];
		array[0] = new BitmapDrawable(mainBmp);
		array[1] = new BitmapDrawable(seletedBmp);
		LayerDrawable la = new LayerDrawable(array);
		la.setLayerInset(0, 0, 0, 0, 0);
		la.setLayerInset(1, 0, -5, 60, 45);

		return la;
	}

	public void changeState(int position) {
		if (multiChoose == true) {
			mImage_bs.setElementAt(!mImage_bs.elementAt(position), position);
		} else {
			if (lastPosition != -1)
				mImage_bs.setElementAt(false, lastPosition);
			mImage_bs.setElementAt(!mImage_bs.elementAt(position), position);
			lastPosition = position;
		}
		notifyDataSetChanged();
	}

	public static List<Integer> getChooseTable() {
		List<Integer> choose = new ArrayList<Integer>();
		for (int i = 0; i < mImage_bs.size(); i++) {
			if (mImage_bs.get(i).booleanValue()) {
				choose.add(Integer.valueOf(mTableSetting.getIdByAllIndex(i)));
			}
		}
		return choose;
	}
}
