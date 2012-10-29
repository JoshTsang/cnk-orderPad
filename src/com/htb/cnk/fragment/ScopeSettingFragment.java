package com.htb.cnk.fragment;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;

import com.htb.cnk.R;
import com.htb.cnk.adapter.GridViewImageAdapter;
import com.htb.cnk.data.Setting;

public class ScopeSettingFragment extends Fragment {
	GridView gridView_check;
	GridViewImageAdapter multiCheck;
	CheckBox scopeCharged;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.area_settings, container, false);

		scopeCharged = (CheckBox) v.findViewById(R.id.area);
		scopeCharged.setOnCheckedChangeListener(scopeCheckedChange);
		scopeCharged.setChecked(Setting.enableChargedAreaCheckout());

		gridView_check = (GridView) v.findViewById(R.id.areaAridview);
		multiCheck = new GridViewImageAdapter(getActivity(), true);
		gridView_check.setAdapter(multiCheck);
		gridView_check.setOnItemClickListener(gridCheckClickListener());
		
		showLoading(Setting.enableChargedAreaCheckout());
		return v;
	}

	private void showLoading(boolean bShow) {
		if (gridView_check == null)
			return;
		gridView_check.setVisibility(bShow ? View.VISIBLE : View.GONE);
	}

	OnCheckedChangeListener scopeCheckedChange = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Setting.enableChargedAreaAfterCheckout(isChecked);
			showLoading(isChecked);
		}

	};

	private OnItemClickListener gridCheckClickListener() {
		return new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				multiCheck.changeState(position);
				String waiterScope = null;
				List<Integer> choose = GridViewImageAdapter.getChooseTable();
				for (int i = 0; i < choose.size(); i++) {
					if (i == 0) {
						waiterScope = choose.get(i).toString();
					} else {
						waiterScope = waiterScope + "," + choose.get(i);
					}
				}
				SharedPreferences sharedPre = getActivity()
						.getSharedPreferences("waiterSetting",
								Context.MODE_PRIVATE);
				Editor editor = sharedPre.edit();
				editor.putString("waiterScope", waiterScope);
				editor.commit();

			}
		};
	}
}
