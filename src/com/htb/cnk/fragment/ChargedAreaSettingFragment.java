package com.htb.cnk.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.htb.cnk.R;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.dialog.MultiChoiceItemsDlg;
import com.htb.cnk.dialog.TitleAndMessageDlg;

public class ChargedAreaSettingFragment extends Fragment {
	private final static String TAG = "ChargedAreaSettingFragment";
	
	private TableSetting mTableSetting;
	private Button areaButton;
	private Switch areaSwitch;
	protected MultiChoiceItemsDlg mMultiChoiceItemsDialog;
	protected TitleAndMessageDlg mTitleAndMessageDlg;
	private String[] areaString;
	protected ProgressDialog mpDialog;
	protected boolean[] selected;
	protected List<String> selectString = new ArrayList<String>();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.areafragment, null);
		mTableSetting = new TableSetting(getActivity());
		mMultiChoiceItemsDialog = new MultiChoiceItemsDlg(getActivity());
		mTitleAndMessageDlg = new TitleAndMessageDlg(getActivity());
		areaButton = (Button) v.findViewById(R.id.areaButton);
		areaSwitch = (Switch) v.findViewById(R.id.areaSwitch);
		areaSwitch.setChecked(Setting
				.enableChargedAreaCheckout());
		areaSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				Setting.enableChargedAreaAfterCheckout(isChecked);
				if (isChecked) {
					getAreaName();
				}
			}
		});

		areaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences sharedPre = getActivity()
						.getSharedPreferences("areaSetting",
								Context.MODE_PRIVATE);
				String areaString = sharedPre.getString("areaName", "");
				mTitleAndMessageDlg.messageDialog(false, areaString, "确定", null, "换区域范围", areaListener).show();
			}
		});
		return v;
	}

	private void getAreaName() {
		mpDialog = new ProgressDialog(getActivity());
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(false);
		mpDialog.setTitle(getResources().getString(
				R.string.pleaseWait));
		mpDialog.setMessage("获取数据中。。。");
		mpDialog.show();
		new Thread() {
			public void run() {
				try {
					areaString = mTableSetting.getchargedAreaName();
					if (areaString != null) {
						areaNameHandler.sendEmptyMessage(0);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	/**
	 * 
	 */
	private void areaDialogShow() {
		mMultiChoiceItemsDialog.titleDialog(true, "选择范围区域", areaString,
				null, listListener, "确定", listPositiveListener, "取消", null).show();
	}

	Handler areaNameHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			selected = new boolean[areaString.length];
			areaDialogShow();
		}
	};
	
	DialogInterface.OnClickListener areaListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			dialogInterface.cancel();
			getAreaName();
		}
	};
	
	DialogInterface.OnClickListener listPositiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			String waiterScope = null;
			for (int i = 0; i < selectString.size(); i++) {
				if (i == 0) {
					waiterScope = selectString.get(i).toString();
				} else {
					waiterScope = waiterScope + "," + selectString.get(i).toString();
				}
			}
			SharedPreferences sharedPre = getActivity()
					.getSharedPreferences("areaSetting",
							Context.MODE_PRIVATE);
			Editor editor = sharedPre.edit();
			editor.putString("areaName", waiterScope);
			editor.commit();
		}
	};

	DialogInterface.OnMultiChoiceClickListener listListener = new DialogInterface.OnMultiChoiceClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int which,
				boolean isChecked) {
			selected[which] = isChecked;
			selectString.add(areaString[which]);
		}
	};
}
