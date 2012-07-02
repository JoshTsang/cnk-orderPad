package com.htb.cnk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.MyOrder.OrderedDish;

public class QueryOrderActivity extends Activity {
	private Button mBackBtn;
	private Button mSettingBtn;
	private TextView mTableNumTxt;
	private TextView mDishCountTxt;
	private TextView mTotalPriceTxt;
	private ListView mMyOrderLst;
	private MyOrder mMyOrder;
	private MyOrderAdapter mMyOrderAdapter;
//	private static int talbeId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.query_activity);
		findViews();
	//	updateTabelInfos();
		setClickListener();
	}

	private void findViews() {
		mBackBtn = (Button) findViewById(R.id.back_btn);
		mSettingBtn = (Button)findViewById(R.id.queryId);
		mTableNumTxt = (TextView) findViewById(R.id.tableNum);
		mDishCountTxt = (TextView) findViewById(R.id.dishCount);
		mTotalPriceTxt = (TextView) findViewById(R.id.totalPrice);
		mMyOrderLst = (ListView) findViewById(R.id.myOrderList);
		

	}

	private void fillData() {
		mTableNumTxt.setText(Info.getTableName());
		mDishCountTxt.setText(Integer.toString(mMyOrder.count())
				+ " 道菜");
		mTotalPriceTxt
				.setText(Double.toString(mMyOrder.getTotalPrice())
						+ " 元");
		mMyOrderAdapter = new MyOrderAdapter(this, mMyOrder) {
			@Override
			public View getView(int position, View convertView, ViewGroup arg2) {
				TextView dishName;
				TextView dishPrice;
				TextView dishQuantity;
				
				

				if (convertView == null) {
					convertView = LayoutInflater.from(QueryOrderActivity.this)
							.inflate(R.layout.item_queryorder, null);
				}
				OrderedDish dishDetail = mMyOrder.getOrderedDish(position);

				dishName = (TextView) convertView.findViewById(R.id.dishName);
				dishPrice = (TextView) convertView.findViewById(R.id.dishPrice);
				dishQuantity = (TextView) convertView
						.findViewById(R.id.dishQuantity);
			
				dishName.setText(dishDetail.getName());
				dishPrice.setText(Double.toString(dishDetail.getPrice())
						+ " 元/份");
				dishQuantity
						.setText(Integer.toString(dishDetail.getQuantity()));

				return convertView;
			}
		};

		mMyOrderLst.setAdapter(mMyOrderAdapter);

	}

	private void setClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
		
		mSettingBtn.setOnClickListener(settingsClicked);

	}

	private void updateTabelInfos() {
		mMyOrder = new MyOrder(QueryOrderActivity.this);
		mMyOrder.clear();
		new Thread(new delThread()).start();
	}

	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.delWarning),
						Toast.LENGTH_SHORT).show();
			} else {	
				fillData();
				mMyOrderAdapter.notifyDataSetChanged();
			}
		}
	};

	class delThread implements Runnable {
		public void run() {
			Message msg = new Message();
			try {
				int ret = mMyOrder.getTableFromDB(Info.getTableId());
				msg.what = ret;
				myHandler.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private OnClickListener backBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			QueryOrderActivity.this.finish();
		}
	};
	 private OnClickListener settingsClicked = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 点击确定转向登录对话框
				LayoutInflater factory = LayoutInflater.from(QueryOrderActivity.this);
				// 得到自定义对话框
				final View DialogView = factory.inflate(R.layout.query_dialog, null);
		
				// 创建对话框
				AlertDialog dlg = new AlertDialog.Builder(QueryOrderActivity.this)
						.setTitle("选择桌号").setView(DialogView)// 设置自定义对话框样式
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {// 设置监听事件

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										 if(which == Dialog.BUTTON_POSITIVE){  
										EditText mTableId = (EditText)DialogView.findViewById(R.id.queryTableId);
										final String talbeId = mTableId.getText().toString();
										Info.setTableId((Integer.parseInt(talbeId))-1);
										Info.setTableName(talbeId);
//										Toast.makeText(QueryOrderActivity.this, Integer
//												.toString(Info.getTableId()), Toast.LENGTH_LONG).show(); 
										updateTabelInfos();
										 }
										
									}
								}
									).setNegativeButton("取消",// 设置取消按钮
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// 点击取消后退出程序
										

									}
								}).create();// 创建对话框
				dlg.show();// 显示对话框
			}
	    	
	    };

}
