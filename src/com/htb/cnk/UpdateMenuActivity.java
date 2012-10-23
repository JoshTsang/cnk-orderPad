package com.htb.cnk;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.htb.cnk.data.CnkDbHelper;
import com.htb.cnk.lib.DBFile;
import com.htb.cnk.lib.Http;
import com.htb.cnk.ui.base.BaseActivity;
import com.htb.constant.ErrorNum;
import com.htb.constant.Server;

/**
 * @author josh
 *
 */
public class UpdateMenuActivity extends BaseActivity {
	final static int DOWNLOAD_THUMBNAIL = 1;
	final static int DOWNLOAD_PIC = 2;
	final static String TAG = "UpdateMenuActivity";
	private TextView mStateTxt;
	private DBFile mDBFile;
	private SharedPreferences mSharedPre = null;
	private static int mMenuVer;
	private CnkDbHelper mCnkDbHelper;
	private SQLiteDatabase mDb;
	private int retry = 0;
	private Thread updateMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			mCnkDbHelper = new CnkDbHelper(UpdateMenuActivity.this,
					CnkDbHelper.DATABASE_NAME,
					null, 1);
			mDb = mCnkDbHelper.getReadableDatabase();
			mDb.close();
		} catch (Exception e) {
			File file = UpdateMenuActivity.this.getDatabasePath(CnkDbHelper.DB_MENU);
            file.delete();
            mCnkDbHelper = new CnkDbHelper(UpdateMenuActivity.this,
					CnkDbHelper.DATABASE_NAME,
					null, 1);
			mDb = mCnkDbHelper.getReadableDatabase();
		}
		setContentView(R.layout.update_menu_activity);
		mStateTxt = (TextView) findViewById(R.id.state);
		
		mStateTxt.setText("正在准备更新...");
		mDBFile = new DBFile(this, CnkDbHelper.DB_MENU);
		updateMenu();
	}

	private void updateMenu() {
		updateMenu = new Thread() {
			public void run() {
				int ret;
			
				ret = downloadDB(Server.SERVER_DB_MENU);
				if (ret < 0) {
					handler.sendEmptyMessage(ret);
					return ;
				}
				
//				handler.sendEmptyMessage(DOWNLOAD_THUMBNAIL);
//				ret = downloadSmallPic();
//				if (ret < 0) {
//					handler.sendEmptyMessage(ret);
//					return ;
//				}
				
				handler.sendEmptyMessage(DOWNLOAD_PIC);
				ret = downloadHugePic();
				if (ret == ErrorNum.DOWNLOAD_PIC_FAILED || ret >= 0) {
					mSharedPre = getSharedPreferences("menuDB",
							Context.MODE_WORLD_WRITEABLE
									| Context.MODE_WORLD_READABLE);
					Editor editor = mSharedPre.edit();
					editor.putInt("ver", mMenuVer);
					editor.commit();
				}
				if (ret < 0) {
					handler.sendEmptyMessage(ret);
					return ;
				}

				handler.sendEmptyMessage(0);
			}
		};
		
		updateMenu.start();
	}
	
	public static boolean isUpdateNeed(int currentMenuVer) {
		String serverRespond = Http.get(Server.MENU_VERSION, "");
		if (serverRespond == null || "".equals(serverRespond)) {
			Log.e(TAG, "no respond when request menu version, currentMVer:" + currentMenuVer);
			return false;
		} else {
			int start = serverRespond.indexOf("[") + 1;
			int end = serverRespond.indexOf("]");
			if (start < 0 || end < 0) {
				Log.e(TAG, "illegal package, action:getMenuVersion, pkg:" + serverRespond);
				return false;
			} else {
				String ver = serverRespond.substring(start, end);
				mMenuVer = Integer.parseInt(ver);
			}
		}
		if (mMenuVer == currentMenuVer) {
			return false;
		} else {
			Log.d(TAG, "MenuVer =" + mMenuVer + "current:" + currentMenuVer);
			return true;
		}
	}
	
	private int downloadDB(String serverDBName) {
		String filePath = Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/cainaoke/";
        try {
        	FTPClient ftpClient = new FTPClient();
        	File dir=new File(filePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file=new File(filePath+"cnk.db");
            file.delete();
            file.createNewFile();
            
        	try {
        	    ftpClient.connect(InetAddress.getByName(Server.SERVER_IP));
        	    ftpClient.login(Server.FTP_USERNAME, Server.FTP_PWD);
        	    ftpClient.changeWorkingDirectory(Server.FTP_DB_DIR);

        	    if (ftpClient.getReplyString().contains("250")) {
        	        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        	        BufferedOutputStream buffIn = null;
        	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        	        buffIn = new BufferedOutputStream(new FileOutputStream(filePath+"cnk.db"));
        	        ftpClient.enterLocalPassiveMode();
        	        boolean ret = ftpClient.retrieveFile(serverDBName, buffIn);
        	        buffIn.close();
        	        ftpClient.logout();
        	        ftpClient.disconnect();
        	        if (!ret) {
        	        	return ErrorNum.DOWNLOAD_DB_FAILED;
        	        }
        	        
        	    } else {
        	    	Log.d("ftp reply", ftpClient.getReplyString());
        	    	return ErrorNum.DOWNLOAD_DB_FAILED;
        	    }
        	} catch (SocketException e) {
        		e.printStackTrace();
        		return ErrorNum.DOWNLOAD_DB_FAILED;
        	} catch (UnknownHostException e) {
        		e.printStackTrace();
        		return ErrorNum.DOWNLOAD_DB_FAILED;
        	} catch (IOException e) {
        		e.printStackTrace();
        		return ErrorNum.DOWNLOAD_DB_FAILED;
        	}
        	
            if (mDBFile.copyDatabase(CnkDbHelper.DB_MENU) < 0) {
            	return ErrorNum.COPY_DB_FAILED;
            }
            file.delete();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorNum.DOWNLOAD_DB_FAILED;
        }
	}
	
	private int downloadHugePic() {
		int ret;
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		int count = 0;
		try {
			mCnkDbHelper = new CnkDbHelper(UpdateMenuActivity.this,
					CnkDbHelper.DATABASE_NAME,
					null, 1);
			mDb = mCnkDbHelper.getReadableDatabase();
			
			Cursor dishes = mDb.query(CnkDbHelper.TABLE_DISH_INFO, new String[] {
					  CnkDbHelper.DISH_PIC, CnkDbHelper.DISH_NAME},
					  null, null, null, null, null);
			while (dishes.moveToNext()) {
				String picName = dishes.getString(0);
				if (picName != null && !"".equals(picName) && !"null".equals(picName)) {
					ret = downloadPic(Server.IMG_PATH+ picName, picName);
					Log.i(TAG, "downloading pic " + picName + " for " + dishes.getString(1));
					if (ret < 0) {
						count++;
						if (count >= 10) {
							return ErrorNum.DOWNLOAD_PIC_FAILED;
						}
					}
				} else {
					Log.i(TAG, "no pic for " + dishes.getString(1));
				}
			}
			
			mDb.close();
		} catch (Exception e) {
			File file = UpdateMenuActivity.this.getDatabasePath(CnkDbHelper.DB_MENU);
            file.delete();
            e.printStackTrace();
			return ErrorNum.DB_BROKEN;
		}
		SharedPreferences sharedPre = UpdateMenuActivity.this.getSharedPreferences(
				"userInfo",
				Context.MODE_PRIVATE);
		Editor editor = sharedPre.edit();
		editor.putString("menuPicBackupTime", time);
		editor.commit();
		
		if(count > 0) {
			return ErrorNum.DOWNLOAD_PIC_FAILED;
		}
		return 0;
	}
	
	private int downloadPic(String src, String dest) {
		try {
            byte[] data = getImage(src);        
            if(data!=null){        
                save(dest, data);
            }else{        
                return ErrorNum.DOWNLOAD_PIC_FAILED;       
            }  
            
        } catch (Exception e) {         
            e.printStackTrace();    
            return ErrorNum.DOWNLOAD_PIC_FAILED;
        }    
		return 0;
	}
	
	private void errDlg(int errnum) {
		new AlertDialog.Builder(UpdateMenuActivity.this)
		.setTitle("错误")
		.setCancelable(false)
		.setMessage("更新菜谱失败,请重试.错误码:" + errnum)
		.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						finish();
					}
				})
		.show();
	}
	
	public byte[] getImage(String path) throws Exception{     
        URL url = new URL(path);     
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();     
        conn.setConnectTimeout(5 * 1000);     
        conn.setRequestMethod("GET");     
        InputStream inStream = conn.getInputStream();     
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){     
            return readStream(inStream);     
        }     
        return null;     
    } 
	
	public int save(String fileName, byte buffer[])
	{
	    try {
	        FileOutputStream outStream=this.openFileOutput(fileName, Context.MODE_PRIVATE);
			outStream.write(buffer);
	        outStream.close();
	    } catch (FileNotFoundException e) {
	        return ErrorNum.WRITE_FILE_FAILED;
	    }
	    catch (IOException e){
	        return ErrorNum.WRITE_FILE_FAILED;
	    }
	    return 0;
	}
	
	public static byte[] readStream(InputStream inStream) throws Exception{     
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();     
        byte[] buffer = new byte[1024];     
        int len = 0;     
        while( (len=inStream.read(buffer)) != -1){     
            outStream.write(buffer, 0, len);     
        }     
        outStream.close();     
        inStream.close();     
        return outStream.toByteArray();     
    }  
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	  
		if(keyCode == KeyEvent.KEYCODE_BACK){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("确定退出");
			builder.setCancelable(false);
			builder.setMessage("退出将菜谱无法更新,请等待菜谱更新完毕后,系统自动退出");
			
			builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					 //do nothing
				}
			});
			   
			AlertDialog dialog = builder.create();
			dialog.show();

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	   
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			mDb.close();
			if (msg.what < 0) {
				if (msg.what == ErrorNum.DB_BROKEN) {
					if (retry < 5) {
						retry++;
						Log.d("update menu failed", "retry:" + retry);
						updateMenu();
						return;
					}
				}
				
				errDlg(msg.what);
				
			} else {
				switch(msg.what) {
					case DOWNLOAD_THUMBNAIL:
						mStateTxt.setText("正在下载缩略图...");
						break;
					case DOWNLOAD_PIC:
						mStateTxt.setText("正在下载菜品图片...");
						break;
					default:
						new AlertDialog.Builder(UpdateMenuActivity.this)
						.setMessage("菜谱已更新")
						.setCancelable(false)
						.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
									}
								})
						.show();
				}
			}
		}
	};
}
