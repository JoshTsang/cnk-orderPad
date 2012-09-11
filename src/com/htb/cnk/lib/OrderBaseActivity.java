package com.htb.cnk.lib;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.htb.cnk.LoginDlg;
import com.htb.cnk.MyOrderActivity;
import com.htb.cnk.R;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.lib.BaseActivity;

/**
 * @author josh
 *
 */
public class OrderBaseActivity extends BaseActivity {
	protected Button mBackBtn;
	protected Button mSubmitBtn;
	protected Button mLeftBtn;
	protected Button mRefreshBtn;
	protected TextView mTableNumTxt;
	protected TextView mDishCountTxt;
	protected TextView mTotalPriceTxt;
	protected ListView mMyOrderLst;
	protected MyOrder mMyOrder;
	protected ProgressDialog mpDialog;
	protected Handler mSubmitHandler;
	private TableSetting mSettings = new TableSetting();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myorder_activity);
		mMyOrder = new MyOrder(OrderBaseActivity.this);
		mpDialog = new ProgressDialog(OrderBaseActivity.this);  
		mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mpDialog.setIndeterminate(false);
		mpDialog.setCancelable(false);
		findViews();
		fillData();
		setClickListener();
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back_btn);
		mSubmitBtn = (Button) findViewById(R.id.submit);
		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);
		mLeftBtn = (Button) findViewById(R.id.left_btn);
		mRefreshBtn = (Button) findViewById(R.id.refresh);
	}

	private void fillData() {
		mTableNumTxt.setText(Info.getTableName());
		updateTabelInfos();
	}

	private void setClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
		mSubmitBtn.setOnClickListener(submitBtnClicked);
	}

	protected void updateTabelInfos() {
		mDishCountTxt.setText(Integer.toString(mMyOrder.totalQuantity()) + " 道菜");
		mTotalPriceTxt.setText(Double.toString(mMyOrder.getTotalPrice()) + " 元");
	}

	protected void showProgressDlg(String msg) {
		mpDialog.setMessage(msg);
		mpDialog.show();
	}
	
	private void customerSubmitOrderDlg() {
		new AlertDialog.Builder(OrderBaseActivity.this)
				.setTitle("提交订单")
				.setMessage("呼叫服务员确认订单")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						LoginDlg loginDlg = new LoginDlg(OrderBaseActivity.this,
								LoginDlg.ACTION_SUBMIT);
						loginDlg.show();
					}
				})
				.setNegativeButton("继续点菜",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).show();
	}

	public void submitOrder() {
		showProgressDlg("正在提交订单...");
		new Thread() {
			public void run() {
				int ret = mMyOrder.submit();
				if (ret < 0) {
					mSubmitHandler.sendEmptyMessage(ret);
				} else {
					int result = mSettings
							.getItemTableStatus(Info.getTableId());
					if (result >= 50) {
						mSettings.updateStatus(Info.getTableId(), result);
					} else {
						mSettings.updateStatus(Info.getTableId(), 1);
					}
					mSubmitHandler.sendEmptyMessage(0);
				}
			}
		}.start();
	}
	
	protected void showSetPersonsDlg() {
		final EditText changeTableText = new EditText(OrderBaseActivity.this);
		changeTableText.setKeyListener(new DigitsKeyListener(false, true));
		changeTableText
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
		final AlertDialog.Builder personSettingDlg = new AlertDialog.Builder(
				OrderBaseActivity.this);
		personSettingDlg.setTitle("请输入人数");
		personSettingDlg.setIcon(R.drawable.ic_launcher);
		personSettingDlg.setCancelable(false);
		personSettingDlg.setView(changeTableText);
		personSettingDlg.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						String persons;
						persons = changeTableText.getEditableText()
								.toString();
						if (persons.equals("")) {
							new AlertDialog.Builder(OrderBaseActivity.this)
							.setCancelable(false).setTitle("注意")
							.setMessage("人数不能为空").setPositiveButton("确定", null)
							.show();
							return;
						}
						
						int personCount = Integer.parseInt(persons);
						if (personCount > 0) {
							mMyOrder.setPersons(personCount);
							if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
								customerSubmitOrderDlg();
							} else {
								submitOrder();
							}
						} else {
							new AlertDialog.Builder(OrderBaseActivity.this)
							.setCancelable(false).setTitle("注意")
							.setMessage("人数不合法").setPositiveButton("确定", null)
							.show();
						}

					}
				});
		personSettingDlg.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						dialog.cancel();
					}
				});
		personSettingDlg.show();
	}
	
	private OnClickListener submitBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mMyOrder.count() <= 0) {
				new AlertDialog.Builder(OrderBaseActivity.this)
						.setTitle("请注意")
						.setMessage("您还没有点任何东西")
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).show();
				return;
			}
			
			showSetPersonsDlg();
		}
	};
	
	private OnClickListener backBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			OrderBaseActivity.this.finish();
		}
	};

}
