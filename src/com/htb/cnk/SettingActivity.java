package com.htb.cnk;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.htb.cnk.data.Setting;
import com.htb.cnk.fragment.BasicSettingFragment;
import com.htb.cnk.fragment.RingtoneSettingFragment;
import com.htb.cnk.fragment.ScopeSettingFragment;

public class SettingActivity extends Activity {

	private Button back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings_activity);

		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(backClicked);
	}

	public static class DetailsActivity extends Activity {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				finish();
				return;
			}
			if (savedInstanceState == null) {
				BasicSettingFragment details = new BasicSettingFragment();
				details.setArguments(getIntent().getExtras());
				getFragmentManager().beginTransaction()
						.add(android.R.id.content, details).commit();
			}
		}
	}

	public static class TitlesFragment extends ListFragment {
		boolean mDualPane;
		int mCurCheckPosition = 0;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			setListAdapter(new ArrayAdapter<String>(getActivity(),
					R.layout.perference_item,
					Setting.TITLES));

			View detailsFrame = getActivity().findViewById(R.id.details);
			mDualPane = detailsFrame != null
					&& detailsFrame.getVisibility() == View.VISIBLE;

			if (savedInstanceState != null) {
				mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
			}

			if (mDualPane) {
				getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				showDetails(mCurCheckPosition);
			}
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt("curChoice", mCurCheckPosition);
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			showDetails(position);
		}

		void showDetails(int index) {

			if (mDualPane) {
				getListView().setItemChecked(index, true);

				Fragment details = (Fragment) getFragmentManager()
						.findFragmentById(R.id.details);
				if (details == null || mCurCheckPosition != index) {
					switch (index) {
					case 0:
						details = new BasicSetting();
						break;
					case 1:
						details = new RingtoneSetting();
						break;
					case 2:
						details = new ScopeSettingFragment();
						break;
					default:
						break;
					}
					
					FragmentTransaction ft = getFragmentManager()
							.beginTransaction();
					ft.replace(R.id.details, details);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					ft.commit();
				}

			} else {
				// Do nothing single panel is not supportted
			}

			mCurCheckPosition = index;
		}
	}

	public static class BasicSetting extends BasicSettingFragment {
		
	}

	public static class RingtoneSetting extends RingtoneSettingFragment {
		
	}

	public static class ScopeSetting extends ScopeSettingFragment {
		
	}

	private OnClickListener backClicked = new OnClickListener() {
	
		@Override
		public void onClick(View v) {
			finish();
		}
	};
}
