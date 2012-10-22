package com.htb.cnk.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.htb.cnk.R;

public class CashDialog extends ViewDialog {

	private final String TAG = "CashDialog";
	private Context mActivity;
	private Spinner spinner;
	private ArrayAdapter adapter;
	private EditText rebateText;
	private int currentCashType;

	public CashDialog(Context context) {
		super(context);
		mActivity = context; 
	}

	public void show() {
		final AlertDialog.Builder cashAlertDialog;
		View layout = getDialogLayout(R.layout.cash_dialog);
		SharedPreferences sharedPre = mActivity.getSharedPreferences(
				"cashInfo", Context.MODE_PRIVATE);
		String cashType = sharedPre.getString("cashType", "");
		String cashRebate = sharedPre.getString("cashRebate", "");
		spinner = (Spinner) layout.findViewById(R.id.cashType);
		adapter = ArrayAdapter.createFromResource(mActivity, R.array.cashType,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = 0;i < adapter.getCount();i++) {
			if (cashType.equals(adapter.getItem(i).toString())) {
				currentCashType = i;
				break;
			}
		}
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
		spinner.setSelection(currentCashType);
		spinner.setVisibility(View.VISIBLE);

		rebateText = (EditText) layout.findViewById(R.id.rebateEdit);
		rebateText.setText(cashRebate);
		
		cashAlertDialog = viewDialog(false, layout);
		cashAlertDialog.setPositiveButton(
				mActivity.getResources().getString(R.string.ok),
				cashPositiveListener);
		cashAlertDialog.setNegativeButton(
				mActivity.getResources().getString(R.string.cancel), null);
		cashAlertDialog.show();
	}

	DialogInterface.OnClickListener cashPositiveListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			SharedPreferences sharedPre = mActivity.getSharedPreferences(
					"cashInfo", Context.MODE_PRIVATE);
			Editor editor = sharedPre.edit();
			editor.putString("cashRebate", rebateText.getText().toString());
			editor.commit();
		}
	};
	

	class SpinnerSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			SharedPreferences sharedPre = mActivity.getSharedPreferences(
					"cashInfo", Context.MODE_PRIVATE);
			Editor editor = sharedPre.edit();
			editor.putString("cashType", adapter.getItem(arg2).toString());
			editor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	}

	private View getDialogLayout(int layout_dialog) {
		LayoutInflater inflater = LayoutInflater.from(mActivity);
		View layout = inflater.inflate(layout_dialog, null);
		return layout;
	}

}
