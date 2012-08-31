package com.htb.cnk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import com.htb.cnk.lib.DBFile;
import com.htb.constant.ErrorNum;
import com.htb.constant.Server;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BackupAndRestoreDlg {
	public final static int ACTION_BACKUP = 10;
	public final static int ACTION_RESTORE = 11;

	private String TAG = "BackupAndRestore";
	private Context mActivity;
	ProgressDialog pdialog;
	private DBFile db;
	private int mAction;
	
	public BackupAndRestoreDlg(Context context, int action) {
		mActivity = context;
		mAction = action;
		pdialog = new ProgressDialog(mActivity);
		db = new DBFile(context, null);
		pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pdialog.setIndeterminate(false);
		pdialog.setCancelable(false);
		if (action == ACTION_BACKUP) {
			pdialog.setMessage("正在从服务器备份数据...");
		} else if (action == ACTION_RESTORE){
			pdialog.setMessage("正在将数据恢复到服务器...");
		} else {
			Log.e(TAG, "unknown action:" + mAction);
		}
		pdialog.show();
		restoreAndBackup();
	}
	
	public void show(int action) {
		mAction = action;
		pdialog.show();
		restoreAndBackup();
	}
	private void restoreAndBackup() {
		new Thread() {
			public void run() {
				int ret = 0;
				if (mAction == ACTION_BACKUP) {
					ret = backup();
				} else if (mAction == ACTION_RESTORE) {
					ret = restore();
				} else {
					Log.e(TAG, "unknown action:" + mAction);
				}
				handler.sendEmptyMessage(ret);
			}
		}.start();
		
	}
	
	private int restore() {
		int ret = 0;
		
		/* restore menu */
		ret = uploadDB(Server.SERVER_DB_MENU);
		if (ret < 0) {
			Log.e(TAG, "upload menu db failed");
			return ret;
		}
		
		/* restore sales data */
		ret = uploadDB(Server.SERVER_DB_SALES);
		if (ret < 0) {
			Log.e(TAG, "upload sales db failed");
			return ret;
		}
		
		/* upload pic */
		ret = uploadPic();
		if (ret < 0) {
			Log.e(TAG, "upload pic failed");
			return ret;
		}
		
		return 0;
	}

	private int backup() {
		int ret = 0;
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = df.format(date);
		SharedPreferences sharedPre = mActivity.getSharedPreferences(
				"userInfo",
				Context.MODE_PRIVATE);
		Editor editor = sharedPre.edit();
		
		/* backup menu */
		ret = db.backup(Server.SERVER_DB_MENU);
		if (ret < 0) {
			return ret;
		} else {
			editor.putString("menuBackupTime", time);
			editor.commit();
		}
		
		/* backup sales data */
		ret = downloadDB(Server.SERVER_DB_SALES);
		if (ret < 0) {
			return ret;
		} else {
			editor.putString("salesBackupTime", time);
			editor.commit();
		}
		
		return ret;
	}
	
	private int uploadPic() {
        try {
        	FTPClient ftpClient = new FTPClient();
            
        	try {
        	    ftpClient.connect(InetAddress.getByName(Server.SERVER_IP));
        	    ftpClient.login(Server.FTP_USERNAME, Server.FTP_PWD);
        	    ftpClient.changeWorkingDirectory(Server.SERVER_PIC_DIR);

        	    if (ftpClient.getReplyString().contains("250")) {
        	        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        	        BufferedInputStream buffIn = null;
        	        boolean ret = true;
        	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        	        ftpClient.enterLocalPassiveMode();
        	        File dir = mActivity.getFilesDir();
        	        String filelist[] = dir.list();
        	        FileInputStream inStream;
        	        Log.d(TAG, "filelist size:" + filelist.length);
        	        for (String fileName:filelist) {
        	        	if (fileName.matches(".+\\.(jpg|png)")) {
        	        		Log.d(TAG, "pic file:" + fileName);   
        	        		inStream = mActivity.openFileInput(fileName);
		        	        buffIn = new BufferedInputStream(inStream);
		        	        ret = ftpClient.storeFile(fileName, buffIn);
		        	        buffIn.close();
        	        	} else {
        	        		Log.d(TAG, "not pic file:" + fileName);
        	        	}
        	        }
        	        ftpClient.logout();
        	        ftpClient.disconnect();
        	        if (!ret) {
        	        	return ErrorNum.DOWNLOAD_DB_FAILED;
        	        }
        	        
        	    } else {
        	    	Log.w("ftp reply", ftpClient.getReplyString());
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
        	
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorNum.DOWNLOAD_DB_FAILED;
        }
	}
	
	private int downloadDB(String serverDBName) {
		String filePath = Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/cainaoke/backup/";
		String fileName = filePath + serverDBName;
        try {
        	FTPClient ftpClient = new FTPClient();
        	File dir=new File(filePath);
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file=new File(fileName);
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
        	        buffIn = new BufferedOutputStream(new FileOutputStream(fileName));
        	        ftpClient.enterLocalPassiveMode();
        	        boolean ret = ftpClient.retrieveFile(serverDBName, buffIn);
        	        buffIn.close();
        	        ftpClient.logout();
        	        ftpClient.disconnect();
        	        if (!ret) {
        	        	return ErrorNum.DOWNLOAD_DB_FAILED;
        	        }
        	        
        	    } else {
        	    	Log.w("ftp reply", ftpClient.getReplyString());
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
        	
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorNum.DOWNLOAD_DB_FAILED;
        }
	}
	
	private int uploadDB(String serverDBName) {
		String filePath = Environment
                .getExternalStorageDirectory().getAbsolutePath()
                + "/cainaoke/backup/";
		String fileName = filePath + serverDBName;
        try {
        	FTPClient ftpClient = new FTPClient();
        	File dir=new File(filePath);
            if (!dir.exists()) {
                return -1;
            }
            File file=new File(fileName);
            if (!file.exists()) {
            	return -1;
            }
            
        	try {
        	    ftpClient.connect(InetAddress.getByName(Server.SERVER_IP));
        	    ftpClient.login(Server.FTP_USERNAME, Server.FTP_PWD);
        	    ftpClient.changeWorkingDirectory(Server.FTP_DB_DIR);

        	    if (ftpClient.getReplyString().contains("250")) {
        	        ftpClient.setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        	        BufferedInputStream buffIn = null;
        	        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        	        buffIn = new BufferedInputStream(new FileInputStream(fileName));
        	        ftpClient.enterLocalPassiveMode();
        	        boolean ret = ftpClient.storeFile(serverDBName, buffIn);
        	        buffIn.close();
        	        ftpClient.logout();
        	        ftpClient.disconnect();
        	        if (!ret) {
        	        	return ErrorNum.DOWNLOAD_DB_FAILED;
        	        }
        	        
        	    } else {
        	    	Log.w("ftp reply", ftpClient.getReplyString());
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
        	
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return ErrorNum.DOWNLOAD_DB_FAILED;
        }
	}
	
	private void errDlg(String msg) {
		new AlertDialog.Builder(mActivity)
		.setTitle("错误")
		.setCancelable(false)
		.setMessage(msg)
		.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						
					}
				})
		.show();
	}
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			pdialog.cancel();
			if (msg.what < 0) {
				if (mAction == ACTION_BACKUP) {
					errDlg("备份数据失败！");
				} else if(mAction == ACTION_RESTORE) {
					errDlg("恢复数据失败！");
				} else {
					Log.e(TAG, "unknown action:" + mAction);
				}
			} else {
				Method refreshBackupTime;
				try {
					refreshBackupTime = mActivity.getClass().getMethod("fillData", new Class[0]);
					refreshBackupTime.invoke(mActivity, new Object[0]);
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
			}
		}
	};
}
