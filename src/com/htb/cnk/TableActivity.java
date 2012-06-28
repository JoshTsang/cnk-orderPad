package com.htb.cnk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.htb.cnk.data.Info;
import com.htb.cnk.data.MyOrder;
import com.htb.cnk.data.TableSetting;

public class TableActivity extends Activity {

	private TableSetting mSettings = new TableSetting();
	protected List<Map<String, String>> mTableSettings = new ArrayList<Map<String, String>>();
	protected int tableButton[];
	private MyOrder myOrder = new MyOrder();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.table_activity);
		GridView gridview = (GridView) findViewById(R.id.gridview);
		ArrayList<HashMap<String, String>> lstImageItem = new ArrayList<HashMap<String, String>>();
		mTableSettings.clear();
		tableButton = new int[mSettings.size()];
		for (int i = 0; i < mSettings.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("ItemText", "第" + (i+1) + "桌");
			tableButton[i] = Integer.parseInt(mSettings.getState(i));
			lstImageItem.add(map);
		}

		SimpleAdapter saImageItems = new SimpleAdapter(this, lstImageItem,
				R.layout.table_item, new String[] { "ItemText" },
				new int[] { R.id.ItemText });
//		ImageButton v = (ImageButton)findViewById(R.id.TableItemImage);
//		v.setBackgroundColor(Color.BLACK);
		gridview.setAdapter(saImageItems);
		gridview.setOnItemClickListener(new ItemClickListener());
		

	}

	class ItemClickListener implements OnItemClickListener {

		public void onItemClick(AdapterView<?> arg0,// The AdapterView where the
													// click happened
				View arg1,// The view within the AdapterView that was clicked
				int arg2,// The position of the view in the adapter
				long arg3// The row id of the item that was clicked
		) {
			final int temp = arg2;
			final ChoiceOnClickListener choiceListener = new ChoiceOnClickListener();
			new AlertDialog.Builder(TableActivity.this)
					.setTitle("选择功能")
					// 设置标题
					.setSingleChoiceItems(
							new String[] { "开台", "删除菜", "快速点菜", "更新数据" },
							0, choiceListener)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									int choiceWhich = choiceListener.getWhich();
									Intent intent = new Intent();
									switch (choiceWhich) {
									case 0:
										myOrder.clear();
										intent.setClass(TableActivity.this,
												MenuActivity.class);
										Info.setMode(Info.WORK_MODE_CUSTOMER);
										break;
									case 1:
										intent.setClass(TableActivity.this,
												DelOrderActivity.class);
										break;
									case 2:
										intent.setClass(TableActivity.this,
												MenuActivity.class);
										Info.setMode(Info.WORK_MODE_WAITER);
										break;
									case 3:
										intent.setClass(TableActivity.this,
												UpdateMenuActivity.class);
										break;
									}
									Log.d("temp","aaa::"+temp);
									Info.setTableId(mSettings.getId(temp));
									Log.d("tableId", ""+Info.getTableId());
									TableActivity.this.startActivity(intent);
								}
							}).setNegativeButton("取消", null).show();

		}
	}

	private class ChoiceOnClickListener implements
			DialogInterface.OnClickListener {

		private int which = 0;

		@Override
		public void onClick(DialogInterface dialogInterface, int which) {
			Log.d("a", "a" + which);
			this.which = which;
		}

		public int getWhich() {
			return which;
		}
	}

}
