package com.zd.vpn.util;
/*
* 主要功能
* 清除内/外缓存，
* 清除数据库，
* 清除sharedPreference，
* 清除files
* 清除自定义目录
*/

import java.io.File;
import android.content.Context;
import android.os.Environment;

/** * 本应用数据清除管理器 */
public class DataCleanManager {
    /** * 清除本应用内部缓存(/data/data/包名/cache)  */
    public static void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
    }

    /**
     *  * 清除本应用所有数据库(/data/data/包名/databases) * * @param context */
    public static void cleanDatabases(Context context) {
        deleteFilesByDirectory(new File("/data/data/"
                + context.getPackageName() + "/databases"));
    }

    /**
     * * 清除本应用SharedPreference(/data/data/包名/shared_prefs) * * @param
     * context
     */
    public static void cleanSharedPreference(Context context) {
        deleteFilesByDirectory(new File("/data/data/"
                + context.getPackageName() + "/shared_prefs"));
    }

    /** * 清除/data/data/包名/files下的内容 * * @param context */
    public static void cleanFiles(Context context) {
        deleteFilesByDirectory(context.getFilesDir());
    }

    /**
     * * 清除外部cache下的内容(/mnt/sdcard/android/data/包名/cache)
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }

    /* 清除本应用所有的数据  */
    public static void cleanApplicationData(Context context) {
        cleanInternalCache(context);
        cleanExternalCache(context);
        cleanDatabases(context);
        cleanSharedPreference(context);
        cleanFiles(context);
    }

    /**
     * 删除目录下的文件，如果是文件直接删除
     * @param directory
     */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }else if(directory.isFile()){
            directory.delete();
        }
    }
}