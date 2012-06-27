package com.htb.constant;

public class ErrorNum {
	public final static int SERVER_NO_RESPOND = -1;
	public final static int INVALID_DATA = -2;
	public final static int DUPLICATE_RECORDE = -3;
	public final static int PEROID_OVERLAP = -4;
	public final static int INVALID_ARGUMENT = -5;

	/* update errors */
	public final static int DOWNLOAD_DB_FAILED = -6;
	public final static int WRITE_FILE_FAILED = -7;
	public final static int DOWNLOAD_PIC_FAILED = -8;
	public final static int COPY_DB_FAILED = -9;
	
	public final static int GET_LATEST_STATISTICS_FAILED = -10;
	public final static int GET_SOLDOUT_ITEM_FAILED = -11;
	
	public final static int HTTP_TIMEOUT = -400;
	public final static int HTTP_PAGE_NOT_FOUND = -404;
	public final static int HTTP_INTERNAL_ERR = -500;
	public final static int HTTP_NO_CONECTION = -401;
	public final static int UTF8_NOT_SUPPORTED = -402;
	public final static int FETCH_DATA_FAILED = -403;
	

}
