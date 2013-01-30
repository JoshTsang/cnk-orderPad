package com.htb.cnk.ui.base;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.htb.cnk.R;
import com.htb.cnk.ReservationInfoActivity;
import com.htb.cnk.TableActivity;
import com.htb.cnk.adapter.MyOrderAdapter;
import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.PhoneOrder;
import com.htb.cnk.data.Setting;
import com.htb.cnk.data.TableSetting;
import com.htb.cnk.dialog.LoginDlg;
import com.htb.cnk.dialog.TitleAndMessageDlg;
import com.htb.cnk.service.NotificationTableService;
import com.htb.constant.Permission;
import com.htb.constant.Table;

/**
 * @author josh
 * 
 */
public class OrderBaseActivity extends BaseActivity {
	private final static String TAG = "OrderBaseActivity";

    protected final int SELECT_TABLES_DIALOG = 1;
    
	protected Button mBackBtn;
	protected Button mSubmitBtn;
	protected Button mLeftBtn;
	protected Button mRefreshBtn;
	protected Button mComment;
	protected TextView mTableNumTxt;
	protected TextView mDishCountTxt;
	protected TextView mTotalPriceTxt;
	protected ListView mMyOrderLst;
	protected PhoneOrder mMyOrder;
	protected MyOrderAdapter mMyOrderAdapter;
	protected int mPersons;
	protected TableSetting mSettings;
	protected TitleAndMessageDlg mNetworkDialog;
	protected NotificationTableService.MyBinder mPendOrderBinder;
	protected boolean mBinded;
	protected boolean[] mTableSelected;
	protected List<Integer> multiOrderIds;
	protected StringBuffer multiOrderNames;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myorder_activity);
		mMyOrder = new PhoneOrder(OrderBaseActivity.this);
		mNetworkDialog = new TitleAndMessageDlg(OrderBaseActivity.this);
		mSettings = new TableSetting(OrderBaseActivity.this);
		getPersons();
		findViews();
		fillData();
		setClickListener();

		Intent intent = new Intent(this, NotificationTableService.class);  
//        startService(intent); 
        bindService(intent, conn, Context.BIND_AUTO_CREATE);  
	}
	
    protected void showTableSelectDialog() {
        List<String> tableNames = mSettings.getTableNames();
        if (mTableSelected == null) {
        	mTableSelected = new boolean[tableNames.size()];
        }
        int tableNum = mSettings.tableSeetingsSize();
        for (int i=0; i<tableNum; i++) {
        	mTableSelected[i] = false;
        }
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("请选择");
            DialogInterface.OnMultiChoiceClickListener mutiListener = 
                    new DialogInterface.OnMultiChoiceClickListener() {
                                
                                @Override
                                public void onClick(DialogInterface dialogInterface, 
                                                int which, boolean isChecked) {
                                	mTableSelected[which] = isChecked;
                                }
                        };
            builder.setMultiChoiceItems((CharSequence[])tableNames.toArray(new String[0]), 
            		mTableSelected, mutiListener);
            DialogInterface.OnClickListener btnListener = 
                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                            		boolean flag = true;
                            		if (multiOrderIds == null) {
                            			multiOrderIds = new ArrayList<Integer>();
                            		} else {
                            			multiOrderIds.clear();
                            		}
                            		if (multiOrderNames == null) {
                            			multiOrderNames = new StringBuffer();
                            		} else {
                            			multiOrderNames.setLength(0);
                            		}
                                    for(int i=0; i<mTableSelected.length; i++) {
                                        if(mTableSelected[i] == true) {
                                        	flag = false;
                                            multiOrderIds.add(mSettings.getIdByAllIndex(i));
                                            multiOrderNames.append(mSettings.getNameByAllIndex(i) + ",");
                                        }
                                    }
                                    if(multiOrderIds.size() > 0) {
                                    	multiOrderNames.deleteCharAt(multiOrderNames.length() - 1);
                                    }
                                    showMultiOrderConfimDlg(flag);
                                }
                        };
            builder.setPositiveButton("确定", btnListener);
            builder.setNegativeButton("取消", null);
            builder.create().show();
	}

	public void submitOrder() {
		submitToService();
		mMyOrder.clear();
		if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
			Info.setMode(Info.WORK_MODE_WAITER);
			Intent intent = new Intent();
			intent.setClass(OrderBaseActivity.this,
					TableActivity.class);
			startActivity(intent);
		}

		finish();
	}

	protected void getPersons() {
		new Thread() {
			public void run() {
				int ret = MyOrder.loodPersons(Info.getTableId());
				if (ret < 0) {
					mPersons = 0;
				} else {
					mPersons = ret;
				}
			}
		}.start();
	}

	protected void updateTabelInfos() {
		DecimalFormat format = new DecimalFormat("#.00");
		mDishCountTxt.setText(Integer.toString(mMyOrder.getTotalQuantity())
				+ " 道菜");
		mTotalPriceTxt.setText(format.format(mMyOrder.getTotalPrice()) + " 元");
	}

	/**
	 * @return
	 */
	protected int updateStatus(int tableId, int orderType) {
		return mSettings.updateStatus(Info.getTableId(),
				orderType);
	}

	protected void showUpdateQuantityDlg(final int index) {
		final EditText changeTableText = new EditText(OrderBaseActivity.this);
		changeTableText.setKeyListener(new DigitsKeyListener(false, true));
		changeTableText
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(4) });
		if (mPersons > 0) {
			changeTableText.setText(Integer.toString(mPersons));
		}
		final AlertDialog.Builder quantitySettingDlg = new AlertDialog.Builder(
				OrderBaseActivity.this);
		quantitySettingDlg.setTitle("请输入数量");
		quantitySettingDlg.setIcon(R.drawable.ic_launcher);
		quantitySettingDlg.setCancelable(false);
		quantitySettingDlg.setView(changeTableText);
		quantitySettingDlg.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						String quantity;
						quantity = changeTableText.getEditableText().toString();
						if (quantity.equals("")) {
							new AlertDialog.Builder(OrderBaseActivity.this)
									.setCancelable(false).setTitle("注意")
									.setMessage("数量不能为空")
									.setPositiveButton("确定", null).show();
							return;
						}

						float quantityValue = Float.parseFloat(quantity);
						if (quantityValue > 0) {
							mMyOrder.updateQuantity(index, quantityValue);
							mMyOrderAdapter.notifyDataSetChanged();
							updateTabelInfos();
						} else {
							new AlertDialog.Builder(OrderBaseActivity.this)
									.setCancelable(false).setTitle("注意")
									.setMessage("数量不合法")
									.setPositiveButton("确定", null).show();
						}
					}
				});
		quantitySettingDlg.setNegativeButton("取消", null);
		quantitySettingDlg.show();
	}

	protected void showSetPersonsDlg() {
		final EditText changeTableText = new EditText(OrderBaseActivity.this);
		changeTableText.setKeyListener(new DigitsKeyListener(false, true));
		changeTableText
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
		if (mPersons > 0) {
			changeTableText.setText(Integer.toString(mPersons));
		}
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
						persons = changeTableText.getEditableText().toString();
						if (persons.equals("")) {
							new AlertDialog.Builder(OrderBaseActivity.this)
									.setCancelable(false).setTitle("注意")
									.setMessage("人数不能为空")
									.setPositiveButton("确定", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											showSetPersonsDlg();
										}
									}).show();
							return;
						}

						try {
							int personCount = Integer.parseInt(persons);
							if (personCount > 0) {
								mMyOrder.setPersons(personCount);
								prepareSubmitOrder();
							} else {
								new AlertDialog.Builder(OrderBaseActivity.this)
										.setCancelable(false).setTitle("注意")
										.setMessage("人数不合法")
										.setPositiveButton("确定", null).show();
							}
						} catch(NumberFormatException e) {
							new AlertDialog.Builder(OrderBaseActivity.this)
							.setCancelable(false).setTitle("注意")
							.setMessage("人数不合法")
							.setPositiveButton("确定", null).show();
						}

					}
				});
		personSettingDlg.setNegativeButton("取消", null);
		personSettingDlg.show();
	}
	
	protected void showSetAdvPaymentDlg() {
		final EditText changeTableText = new EditText(OrderBaseActivity.this);
		changeTableText.setKeyListener(new DigitsKeyListener(false, true));
		changeTableText
				.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
		if (mMyOrder.getAdvPayment() > 0) {
			changeTableText.setText(Float.toString(mMyOrder.getAdvPayment()));
		}
		final AlertDialog.Builder personSettingDlg = new AlertDialog.Builder(
				OrderBaseActivity.this);
		personSettingDlg.setTitle("请输入预付款");
		personSettingDlg.setIcon(R.drawable.ic_launcher);
		personSettingDlg.setCancelable(false);
		personSettingDlg.setView(changeTableText);
		personSettingDlg.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int i) {
						String payment;
						payment = changeTableText.getEditableText().toString();
						if (payment.equals("")) {
							new AlertDialog.Builder(OrderBaseActivity.this)
									.setCancelable(false).setTitle("注意")
									.setMessage("预付款不能为空")
									.setPositiveButton("确定", new DialogInterface.OnClickListener() {
										
										@Override
										public void onClick(DialogInterface dialog, int which) {
											showSetAdvPaymentDlg();
										}
									}).show();
							return;
						}

						float advPayment = Float.parseFloat(payment);
						if (advPayment > 0) {
							mMyOrder.setAdvPayment(advPayment);
							mRefreshBtn.setText("预付:" + payment);
						} else {
							new AlertDialog.Builder(OrderBaseActivity.this)
									.setCancelable(false).setTitle("注意")
									.setMessage("预付款不合法")
									.setPositiveButton("确定", null).show();
							mRefreshBtn.setText("预付款");
						}
					}
				});
		personSettingDlg.setNegativeButton("取消", null);
		personSettingDlg.show();
	}

	protected void showComment() {
		LayoutInflater factory = LayoutInflater.from(OrderBaseActivity.this);
		final View DialogView = factory.inflate(R.layout.comment_dialog, null);
	
		final EditText commentET = (EditText) DialogView
				.findViewById(R.id.comment);
	
		commentET.setText(mMyOrder.getComment());
	
		AlertDialog dlg = new AlertDialog.Builder(OrderBaseActivity.this)
				.setTitle("备注").setView(DialogView)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mMyOrder.setComment(commentET.getText().toString());
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).setCancelable(false).create();
		dlg.show();
	}

	protected OnClickListener flavorClicked = new OnClickListener() {
	
		@Override
		public void onClick(View v) {
				Button text = (Button) v.findViewById(R.id.flavor);
				text.setTextColor(android.graphics.Color.WHITE);
				final int position = Integer.parseInt(v.getTag().toString());
				flavorDialog(position);
		}
	};
	
	protected OnClickListener orderTimeTypeClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int type = mMyOrder.getOrderTimeType();
			mMyOrder.setOrderTimeType(type==MyOrder.ORDER_INSTANT?MyOrder.ORDER_PEND:MyOrder.ORDER_INSTANT);
			mLeftBtn.setText(mMyOrder.getOrderTimeType()==MyOrder.ORDER_INSTANT?"即单":"叫单");
		}
	};
	
	protected OnClickListener advPaymentClicked= new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showSetAdvPaymentDlg();
		}
	};
	
	protected OnClickListener printClicked = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (mMyOrder.count() == 0) {
				Toast.makeText(getApplicationContext(), "没有菜品信息！", Toast.LENGTH_SHORT).show();
				return ;
			}
			mpDialog.setMessage("正在打印...");
			mpDialog.show();
			new Thread() {
				public void run() {
					int ret = mMyOrder.print();
					printHandler.sendEmptyMessage(ret);
				}
			}.start();
		}
	};

	@Override
	protected void onDestroy() {
		if (mBinded) {  
	        unbindService(conn);              
	    } 
		super.onDestroy();
	}

	protected int submitToService() {
		String order = mMyOrder.getOrderJson();
		if (order == null) {
			Log.e(TAG, "order==null");
			return -1;
		}
		mPendOrderBinder.add(Info.getTableId(), Info.getTableName(), mSettings.getStatusById(Info.getTableId()), order);
		int tableStatus = TableSetting.getLocalTableStatusById(Info.getTableId());
		if (tableStatus%10 == 0) {
			TableSetting.setLocalTableStatusById(Info.getTableId(), tableStatus + Table.OPEN_TABLE_STATUS);
		}
		return 0;
	}
	
	protected int submitMultiOrderToService() {
		String order = mMyOrder.getMultiOrderJson(multiOrderIds, multiOrderNames.toString());
		if (order == null) {
			Log.e(TAG, "order==null");
			return -1;
		}
		
		mPendOrderBinder.add(multiOrderIds.get(0), multiOrderNames.toString(), mSettings.getStatusById(multiOrderIds.get(0)), order);
		int tableStatus = TableSetting.getLocalTableStatusById(multiOrderIds.get(0));
		if (tableStatus%10 == 0) {
			TableSetting.setLocalTableStatusById(multiOrderIds.get(0), tableStatus + Table.OPEN_TABLE_STATUS);
		}
		return 0;
	}
	
	protected void flavorDialog(final int position) {
		final boolean[] selected = mMyOrder.slectedFlavor(position);
		new AlertDialog.Builder(OrderBaseActivity.this)
				.setTitle("口味选择")
				.setMultiChoiceItems(MyOrder.getFlavor(), selected,
						new DialogInterface.OnMultiChoiceClickListener() {

							@Override
							public void onClick(
									DialogInterface dialogInterface, int which,
									boolean isChecked) {
								selected[which] = isChecked;
							}
						})
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface,
							int which) {

						mMyOrder.setFlavor(selected, position);
					}
				}).setNegativeButton("取消", null).show();
	}
	
	protected OnLongClickListener quantityLongClicked = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			final int position = Integer.parseInt(v.getTag().toString());
			showUpdateQuantityDlg(position);
			return false;
		}

	};
	
	protected OnClickListener quantityClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final int position = Integer.parseInt(v.getTag().toString());
			showUpdateQuantityDlg(position);
		}

	};
	
	private void showMultiOrderConfimDlg(boolean noTableSelected) {
		if (noTableSelected) {
			new AlertDialog.Builder(OrderBaseActivity.this)
			.setMessage("没有选中任何桌号")
			.setPositiveButton("确定", null).show();
		} else {
			new AlertDialog.Builder(OrderBaseActivity.this)
			.setMessage("请确认桌号：" + multiOrderNames)
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface,
						int which) {
					if (submitMultiOrderToService() >= 0) {
						finish();
					} else {
						Toast.makeText(getBaseContext(), "提交订单失败", Toast.LENGTH_LONG).show();
					}
				}
			}).setNegativeButton("取消", null).show();
		}
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
		mComment = (Button) findViewById(R.id.comment);
	}

	private void fillData() {
		mTableNumTxt.setText(Info.getTableName());
		if (Info.getTableId() == -1) {
			mSubmitBtn.setText("清除菜单");
		}
		updateTabelInfos();
	}

	private void setClickListener() {
		mBackBtn.setOnClickListener(backBtnClicked);
		mSubmitBtn.setOnClickListener(submitBtnClicked);
		mComment.setOnClickListener(commentClicked);
	}

	private void prepareSubmitOrder() {
		if (Info.getTableId() == -1) {
			mMyOrder.clear();
			mMyOrderAdapter.notifyDataSetChanged();
			updateTabelInfos();
			return;
		} else if (Info.getMode() == Info.WORK_MODE_CUSTOMER) {
			customerSubmitOrderDlg();
		} else if(Info.getTableId() == MyOrder.MULTI_ORDER) {
			showTableSelectDialog();
		} else if(Info.getTableId() == MyOrder.PERSERVE_ORDER) {
			Intent intent = new Intent();
			intent.setClass(getApplicationContext(), ReservationInfoActivity.class);
			startActivity(intent);
			finish();
		} else {
			submitOrder();
		}
	}

	private void customerSubmitOrderDlg() {
		new AlertDialog.Builder(OrderBaseActivity.this).setTitle("提交订单")
				.setMessage("呼叫服务员确认订单")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
	
					@Override
					public void onClick(DialogInterface dialog, int which) {
						LoginDlg loginDlg = new LoginDlg(
								OrderBaseActivity.this, LoginDlg.ACTION_SUBMIT);
						loginDlg.show(Permission.STUFF);
					}
				}).setNegativeButton("继续点菜", null).show();
	}

	private ServiceConnection conn = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (service == null) {
				Log.d("TAG", "service==null");
			}
			mPendOrderBinder = (NotificationTableService.MyBinder)service;
			mBinded = true;
		}
	};

	private OnClickListener submitBtnClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			if (mMyOrder.count() <= 0) {
				new AlertDialog.Builder(OrderBaseActivity.this).setTitle("请注意")
						.setMessage("您还没有点任何东西").setPositiveButton("确定", null)
						.show();
				return;
			}
			if (setPersonsNeed()) {
				showSetPersonsDlg();
			} else {
				mMyOrder.setPersons(0);
				prepareSubmitOrder();
			}
		}
	};

	private boolean setPersonsNeed() {
		return Setting.enabledPersons() && Info.getTableId() != MyOrder.PERSERVE_ORDER;
	}
	
	private OnClickListener commentClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			showComment();
		}
	};

	private OnClickListener backBtnClicked = new OnClickListener() {
	
		@Override
		public void onClick(View v) {
			OrderBaseActivity.this.finish();
		}
	};
	
	protected Handler printHandler = new Handler() {
		public void handleMessage(Message msg) {
			mpDialog.cancel();
			if (msg.what < 0) {
				Toast.makeText(getApplicationContext(), "打印时发生错误！", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), "打印任务已提交！", Toast.LENGTH_LONG).show();
			}
			
		}
	};
	
}
