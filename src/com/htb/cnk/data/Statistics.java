package com.htb.cnk.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.htb.cnk.lib.DBFile;
import com.htb.constant.ErrorNum;
import com.htb.constant.Server;

/**
 * @author josh
 *
 */
public class Statistics {
	class SalesRow {
		int did;
		String dName;
		int quantity;
		double salesAmount;
		
		public SalesRow(int did, int amount, int quantity) {
			this.did = did;
			salesAmount = amount;
			this.quantity = quantity;
		}
	}
	
	private Context mContext;

	private static List<SalesRow> mSalesData = new ArrayList<SalesRow>(); 
	private CnkDbHelper mCnkDbMenu;
	private CnkDbHelper mCnkDbSales;
	private SQLiteDatabase mDbSales;
	private SQLiteDatabase mDbMenu;
	private double mTotalAmount = 0;
	
	
	public Statistics(Context context) {
		mContext = context;
		mCnkDbMenu = new CnkDbHelper(context, CnkDbHelper.DB_MENU,
				null, 1);
		mDbMenu = mCnkDbMenu.getReadableDatabase();
	}
	
	public int downloadDB(String serverDBPath) {
		URL url;
		String filePath = Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/cainaoke/";
		DBFile mDBFile = new DBFile(mContext, CnkDbHelper.DB_SALES);
		
        try {
            url=new URL(Server.SERVER_DOMIN + "/" + serverDBPath);
            
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            InputStream istream=connection.getInputStream();
            String filename= CnkDbHelper.DB_SALES;
            
            File dir=new File(filePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file=new File(filePath+filename);
            file.createNewFile();
            
            OutputStream output=new FileOutputStream(file);
            byte[] buffer=new byte[1024*4];
            while (istream.read(buffer)!=-1) {
                output.write(buffer);
            }
            output.flush();
            output.close();
            istream.close();
            if (mDBFile.copyDatabase(CnkDbHelper.DB_SALES) < 0) {
            	return ErrorNum.COPY_DB_FAILED;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorNum.DOWNLOAD_DB_FAILED;
        }
	}
	
	public int perpareResult(Calendar start, Calendar end) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String startDT = df.format(start.getTime());
		String endDT = df.format(end.getTime());
		final int DID = 0;
		final int TOTAL_AMOUNT = 1;
		final int COUNT = 2;

		conectDB();
		mSalesData.clear();
		mTotalAmount = 0;
		Cursor resultSet = mDbSales.query(CnkDbHelper.SALES_DATA, new String[] {"dish_id",
				  "sum(price*quantity)",
				  "sum(quantity)"},
				  "timestamp>'"+startDT +"' and timestamp<'" + endDT+"'",
				  null, "dish_id", null, null, null);
		while (resultSet.moveToNext()) {
			SalesRow salesRow = new SalesRow(resultSet.getInt(DID),
					resultSet.getInt(TOTAL_AMOUNT), resultSet.getInt(COUNT));
			String dishName = getDishName(salesRow.did);
			salesRow.dName = dishName;
			mSalesData.add(salesRow);
			
			mTotalAmount += resultSet.getInt(TOTAL_AMOUNT);
		}
		return 0;
	}
	
	public int print() {
		return 0;
	}
	
	public String getName(int index) {
		return mSalesData.get(index).dName;
	}
	
	public double getAmount(int index) {
		return mSalesData.get(index).salesAmount;
	}
	
	public int getQuantity(int index) {
		return mSalesData.get(index).quantity;
	}
	
	public double getTotalAmount() {
		return mTotalAmount;
	}
	
	public int count() {
		return mSalesData.size();
	}
	
	private void conectDB() {
		if (mCnkDbSales == null) {
			mCnkDbSales = new CnkDbHelper(mContext, CnkDbHelper.DB_SALES,
					null, 1);
			mDbSales = mCnkDbSales.getReadableDatabase();
		}
	}
	
	public String getDishName(int did) {
		String name = getDishNameFromDB(did);
		if (name == null) {
			return "菜名错误";
		}
		return name;
	}
	
	private String getDishNameFromDB(int id) {
		Cursor cur = mDbMenu.query(CnkDbHelper.DISH_TABLE_NAME, new String[] {CnkDbHelper.DISH_NAME},
				  	CnkDbHelper.DISH_ID + "=" + id, null, null, null, null);
		
		if (cur.moveToNext()) {
			return cur.getString(0);
		}
		return null;
	}
}
