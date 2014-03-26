package com.htb.cnk.utils;

import android.util.Log;

public class MyLog
{
    private static final String TAG = "MyLog";
    private static boolean _lock = false;
    // xueqiang wx13263,20130215,������־����
    private static RotatingLog log = null;
    /** Log level. Only logs with a level less or equal to this are written. */
//    public static int debug_level = Log.INFO;
    public static int debug_level = Log.VERBOSE;
    /**
     * Path for the log folder where log files are written. By default, it is used the "./log" folder. Use ".", to store
     * logs in the current root folder.
     */
    public static String log_path = "/mnt/sdcard/cainaoke/";
    /** The size limit of the log file [kB] */
    public static int max_logsize = 3072; // 3MB modified for DTS2013122705490
    /**
     * The number of rotations of log files. Use '0' for NO rotation, '1' for rotating a single file
     */
    public static int log_rotations = 2;
    /**
     * The rotation period, in MONTHs or DAYs or HOURs or MINUTEs examples: log_rotation_time=3 MONTHS, log_rotations=90
     * DAYS Default value: log_rotation_time=2 MONTHS
     */
    private static String log_rotation_time = "2 MONTHS";
    /** The rotation time scale */
    public static int rotation_scale = RotatingLog.MONTH;
    /** The rotation time value */
    public static int rotation_time = 2;

    public static boolean isService = false;

    public MyLog()
    {
        this._lock = false;
    }

    private static synchronized RotatingLog getInstance()
    {
        String filename = log_path;
        if(isService)
        {
            filename = filename + "cnk_service.log";
        }
        else
        {
            filename = filename + "cnk.log";
        }
        if(log == null)
        {
            log = new RotatingLog(filename, null, debug_level, max_logsize * 1024, log_rotations, rotation_scale,
                    rotation_time);
        }

        return log;
    }

    //
    public static void e(String tag, String content)
    {
        if(_lock)
            return;

        if(log == null)
        {
            log = getInstance();
        }
        log.println(tag, String.valueOf(content), Log.ERROR);
        android.util.Log.e(tag, String.valueOf(content));
    }

    //
    public static void v(String tag, String content)
    {
        if(_lock)
            return;
        if(log == null)
        {
            log = getInstance();
        }
        log.println(tag, String.valueOf(content), Log.VERBOSE);
        // houyuchun modify 20120428 begin
        android.util.Log.v(tag, String.valueOf(content));
        // houyuchun modify 20120428 end
    }

    //
    public static void i(String tag, String content)
    {
        if(_lock)
            return;
        if(log == null)
        {
            log = getInstance();
        }
        log.println(tag, String.valueOf(content), Log.INFO);
        // houyuchun modify 20120428 begin
        android.util.Log.i(tag, String.valueOf(content));
        // houyuchun modify 20120428 end
    }

    public static void d(String tag, String content)
    {
        if(_lock)
            return;
        if(log == null)
        {
            log = getInstance();
        }
        log.println(tag, String.valueOf(content), Log.DEBUG);
        // houyuchun modify 20120428 begin
        android.util.Log.d(tag, String.valueOf(content));
        // houyuchun modify 20120428 end
    }

    public static void w(String tag, String content)
    {
        if(_lock)
            return;
        if(log == null)
        {
            log = getInstance();
        }
        log.println(tag, String.valueOf(content), Log.WARN);
        // houyuchun modify 20120428 begin
        android.util.Log.i(tag, String.valueOf(content));
        // houyuchun modify 20120428 end
    }

    public static void a(String tag, String content)
    {
        if(_lock)
            return;
        if(log == null)
        {
            log = getInstance();
        }
        log.println(tag, String.valueOf(content), Log.ASSERT);
        // houyuchun modify 20120428 begin
        android.util.Log.i(tag, String.valueOf(content));
        // houyuchun modify 20120428 end
    }
}
