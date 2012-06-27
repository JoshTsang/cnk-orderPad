package com.htb.cnk.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.htb.cnk.data.CnkDbHelper;

public class DBFile {
	private Context mContext;
	private CnkDbHelper mDbHelper;
	private String mDbName;
	
	public DBFile(Context context, String name) {
	    this.mContext = context;
	    mDbName = name;
	}
	
	public void creatDBifNotExist() {
		mDbHelper = new CnkDbHelper(mContext, mDbName, null, 1);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		//db.query("a", new String[], null, null, null, null, null);
		db.close();
	}
	
	public int copyDatabase(String name) {
        // 获得正在使用的数据库路径，我的是 sdcard 目录下的 /dlion/db_dlion.db
		// 默认路径是 /data/data/(包名)/databases/*.db
        File dbFile = mContext.getDatabasePath(name);
        File exportDir = new File(Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/cainaoke/");
        
        creatDBifNotExist();
        
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        File backup = new File(exportDir, dbFile.getName());
        try {
            fileCopy(backup, dbFile);
            Log.d("restore", "success");
            return 0;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
           Log.d("restore", "fail");
           return -1;
        }
        
    }

    private void fileCopy(File dbFile, File backup) throws IOException {
        FileChannel inChannel = new FileInputStream(dbFile).getChannel();
        FileChannel outChannel = new FileOutputStream(backup).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        }
    }
}
