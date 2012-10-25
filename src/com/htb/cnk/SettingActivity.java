package com.htb.cnk;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.htb.cnk.adapter.GridViewImageAdapter;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.UserData;

public class SettingActivity extends Activity {

	private Button back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings_activity);

		back = (Button) findViewById(R.id.back);
		back.setOnClickListener(backClicked);
	}

	/**
	 * This is a secondary activity, to show what the user has selected when the
	 * screen is not large enough to show it all in one activity.
	 */

	public static class DetailsActivity extends Activity {

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
				// If the screen is now in landscape mode, we can show the
				// dialog in-line with the list so we don't need this activity.
				finish();
				return;
			}
			if (savedInstanceState == null) {
				// During initial setup, plug in the details fragment.
				BasicSettingFragment details = new BasicSettingFragment();
				details.setArguments(getIntent().getExtras());
				getFragmentManager().beginTransaction()
						.add(android.R.id.content, details).commit();
			}
		}
	}

	private OnClickListener backClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	/**
	 * This is the "top-level" fragment, showing a list of items that the user
	 * can pick. Upon picking an item, it takes care of displaying the data to
	 * the user as appropriate based on the currrent UI layout.
	 */

	public static class TitlesFragment extends ListFragment {
		boolean mDualPane;
		int mCurCheckPosition = 0;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			// Populate list with our static array of titles.
			setListAdapter(new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_activated_1,
					Setting.TITLES));

			// Check to see if we have a frame in which to embed the details
			// fragment directly in the containing UI.
			View detailsFrame = getActivity().findViewById(R.id.details);
			mDualPane = detailsFrame != null
					&& detailsFrame.getVisibility() == View.VISIBLE;

			if (savedInstanceState != null) {
				// Restore last state for checked position.
				mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
			}

			if (mDualPane) {
				// In dual-pane mode, the list view highlights the selected
				// item.
				getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
				// Make sure our UI is in the correct state.
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

		/**
		 * Helper function to show the details of a selected item, either by
		 * displaying a fragment in-place in the current UI, or starting a whole
		 * new activity in which it is displayed.
		 */
		void showDetails(int index) {

			if (mDualPane) {
				// We can display everything in-place with fragments, so update
				// the list to highlight the selected item and show the data.
				getListView().setItemChecked(index, true);

				// Check what fragment is currently shown, replace if needed.
				Fragment details = (Fragment) getFragmentManager()
						.findFragmentById(R.id.details);
				if (details == null || mCurCheckPosition != index) {
					// Make new fragment to show this selection.
					switch (index) {
					case 0:
						details = new BasicSettingFragment();
						break;
					case 1:
						details = new RingtoneSettingFragment();
						break;
					case 2:
						details = new ScopeSettingFragment();
						break;
					default:
						break;
					}
					// Execute a transaction, replacing any existing fragment
					// with this one inside the frame.
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

	public static class BasicSettingFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			if (container == null) {
				// We have different layouts, and in one of them this
				// fragment's containing frame doesn't exist. The fragment
				// may still be created from its saved state, but there is
				// no reason to try to create its view hierarchy because it
				// won't be displayed. Note this is not needed -- we could
				// just run the code below, where we would create and return
				// the view hierarchy; it would just never be used.
				return null;
			}

			View v = inflater
					.inflate(R.layout.basic_settings, container, false);
			Switch persons;
			Switch ringtone;
			Switch pwdCheck;
			Switch cleanTableAfterCheckout;
			OnCheckedChangeListener personsCheckedChange = new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Setting.enablePersons(isChecked);
				}

			};

			OnCheckedChangeListener ringtoneCheckedChange = new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Setting.enableRingtone(isChecked);
				}

			};

			OnCheckedChangeListener cleanTableAfterCheckoutChange = new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Log.d("clean", "cleanchange");
					Setting.enableCleanTableAfterCheckout(isChecked);
				}

			};

			OnCheckedChangeListener pwdCheckedChange = new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Setting.enablePWDCheck(isChecked);
					if (isChecked) {
						UserData.debugMode();
					}
				}
			};
			persons = (Switch) v.findViewById(R.id.persons);
			ringtone = (Switch) v.findViewById(R.id.ringtone);
			pwdCheck = (Switch) v.findViewById(R.id.pwdCheck);
			if (!Setting.enabledDebug()) {
				TextView checkPwdText = (TextView) v
						.findViewById(R.id.pwdCheckTxt);

				checkPwdText.setVisibility(View.GONE);
				pwdCheck.setVisibility(View.GONE);
			}
			cleanTableAfterCheckout = (Switch) v
					.findViewById(R.id.cleanTableAfterCheckout);
			persons.setChecked(Setting.enabledPersons());
			ringtone.setChecked(Setting.enabledRingtong());
			pwdCheck.setChecked(Setting.enabledPWDCheck());
			cleanTableAfterCheckout.setChecked(Setting
					.enabledCleanTableAfterCheckout());
			persons.setOnCheckedChangeListener(personsCheckedChange);
			ringtone.setOnCheckedChangeListener(ringtoneCheckedChange);
			pwdCheck.setOnCheckedChangeListener(pwdCheckedChange);
			cleanTableAfterCheckout
					.setOnCheckedChangeListener(cleanTableAfterCheckoutChange);

			return v;
		}
	}

	public static class RingtoneSettingFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.ringtone_setting, container,
					false);
			return v;
		}
	}

	public static class ScopeSettingFragment extends Fragment {
		GridView gridView_check; 
		GridViewImageAdapter multiCheck;  

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View v = inflater.inflate(R.layout.gridview, container, false);
			gridView_check = (GridView) v.findViewById(R.id.gridview);
			multiCheck = new GridViewImageAdapter(getActivity(), true);
			gridView_check.setAdapter(multiCheck);
			gridView_check.setOnItemClickListener(gridCheckClickListener());
			return v;
		}

		private OnItemClickListener gridCheckClickListener() {
			return new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					multiCheck.changeState(position);
					String waiterScope = null;
					List<Integer> choose = GridViewImageAdapter
							.getChooseTable();
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
}
