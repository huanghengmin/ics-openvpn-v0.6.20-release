package com.zd.vpn.util;


import android.content.Context;
import android.os.Build;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ConfigPathUtil {
    private static final String sd_path = null;
    /* private static final String sd_path0 = "/storage/extSdCard";
     private static final String sd_path1 = "/storage/sdcard0";
     private static final String sd_path2 = "/storage/sdcard1";
     private static final String sd_path3 = "/storage/ext_sd";
     private static final String sd_path4 = "/mnt/sdcard2";
     private static final String sd_path5 = "/mnt/sdcard-ext";
     private static final String sd_path6 = "/mnt/ext_sdcard";
     private static final String sd_path7 = "/mnt/sdcard/SD_CARD";
     private static final String sd_path8 = "/mnt/sdcard/extra_sd";
     private static final String sd_path9 = "/mnt/extrasd_bind";
     private static final String sd_path10 = "/mnt/sdcard/ext_sd";
     private static final String sd_path11 = "/mnt/sdcard/external_SD";
     private static final String sd_path12 = "/sdcard";
     private static final String sd_path13 = "/mnt/sdcard/external_sdcard";
     private static final String sd_path14 = "/mnt/extsd";
     private static final String sd_path15 = "/mnt/external_sd";
     private static final String sd_path16 = "/Removable/MicroSD";*/
    private static final String sd_file = "/SONICOM2.RO";
    private static final String data_path = "/CardTools/SSL";

    public static String existFile(Context context) {
        if (Build.VERSION.SDK_INT >= 14) {
            ArrayList<String> list = SdCardUtil.getSDCardInfoAbove14(context);
            for (String l : list) {
                File file = new File(l + sd_file);
                if (file.exists()) {
                    return l;
                }
            }
        } else {
            HashMap<String, SDCardInfo> map = SdCardUtil.getSDCardInfoBelow14();
            Set<String> stringSet = map.keySet();
            for (String l : stringSet) {
                File file = new File(l + sd_file);
                if (file.exists()) {
                    return l;
                }
            }
        }
        return null;
    }

    public static String getSSLPath(Context context) {
        if (Build.VERSION.SDK_INT >= 14) {
            ArrayList<String> list = SdCardUtil.getSDCardInfoAbove14(context);
            for (String l : list) {
                File file = new File(l + data_path);
                if (file.exists()) {
                    return l+data_path;
                }
            }
        } else {
            HashMap<String, SDCardInfo> map = SdCardUtil.getSDCardInfoBelow14();
            Set<String> stringSet = map.keySet();
            for (String l : stringSet) {
                File file = new File(l + data_path);
                if (file.exists()) {
                    return l+data_path;
                }
            }
        }
        return null;
    }

    public static boolean exist(Context context) {
        if (Build.VERSION.SDK_INT >= 14) {
            ArrayList<String> list = SdCardUtil.getSDCardInfoAbove14(context);
            for (String l : list) {
                File file = new File(l + data_path);
                if (file.exists()) {
                    return true;
                }
            }
        } else {
            HashMap<String, SDCardInfo> map = SdCardUtil.getSDCardInfoBelow14();
            Set<String> stringSet = map.keySet();
            for (String l : stringSet) {
                File file = new File(l + data_path);
                if (file.exists()) {
                    return true;
                }
            }
        }
        return false;
    }
}
