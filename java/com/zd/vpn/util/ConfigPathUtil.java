package com.zd.vpn.util;


import java.io.File;

public class ConfigPathUtil {

    private static final String data_path = "/CardTools/SSL";
    private static final String sd_path0 = "/storage/extSdCard";
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
    private static final String sd_path16 = "/Removable/MicroSD";
    private static final String sd_file = "/SONICOM2.RO";

    public static String getSSLPath() {
        File file0 = new File(sd_path0+data_path);
        File file1 = new File(sd_path1+data_path);
        File file2 = new File(sd_path2+data_path);
        File file3 = new File(sd_path3+data_path);
        File file4 = new File(sd_path4+data_path);
        File file5 = new File(sd_path5+data_path);
        File file6 = new File(sd_path6+data_path);
        File file7 = new File(sd_path7+data_path);
        File file8 = new File(sd_path8+data_path);
        File file9 = new File(sd_path9+data_path);
        File file10 = new File(sd_path10+data_path);
        File file11 = new File(sd_path11+data_path);
        File file12 = new File(sd_path12+data_path);
        File file13 = new File(sd_path13+data_path);
        File file14 = new File(sd_path14+data_path);
        File file15 = new File(sd_path15+data_path);
        File file16 = new File(sd_path16+data_path);
        if (file0.exists()) {
            return sd_path0+data_path;
        } else if (file1.exists()) {
            return sd_path1+data_path;
        } else if (file2.exists()) {
            return sd_path2+data_path;
        } else if (file3.exists()) {
            return sd_path3+data_path;
        } else if (file4.exists()) {
            return sd_path4+data_path;
        } else if (file5.exists()) {
            return sd_path5+data_path;
        } else if (file6.exists()) {
            return sd_path6+data_path;
        } else if (file7.exists()) {
            return sd_path7+data_path;
        } else if (file8.exists()) {
            return sd_path8+data_path;
        } else if (file9.exists()) {
            return sd_path9+data_path;
        } else if (file10.exists()) {
            return sd_path10+data_path;
        } else if (file11.exists()) {
            return sd_path11+data_path;
        } else if (file12.exists()) {
            return sd_path12+data_path;
        } else if (file13.exists()) {
            return sd_path13+data_path;
        } else if (file14.exists()) {
            return sd_path14+data_path;
        } else if (file15.exists()) {
            return sd_path15+data_path;
        }  else if (file16.exists()) {
            return sd_path16+data_path;
        }else {
            return null;
        }
    }

    public static boolean existFile() {
        File file0 = new File(sd_path0+sd_file);
        File file1 = new File(sd_path1+sd_file);
        File file2 = new File(sd_path2+sd_file);
        File file3 = new File(sd_path3+sd_file);
        File file4 = new File(sd_path4+sd_file);
        File file5 = new File(sd_path5+sd_file);
        File file6 = new File(sd_path6+sd_file);
        File file7 = new File(sd_path7+sd_file);
        File file8 = new File(sd_path8+sd_file);
        File file9 = new File(sd_path9+sd_file);
        File file10 = new File(sd_path10+sd_file);
        File file11 = new File(sd_path11+sd_file);
        File file12 = new File(sd_path12+sd_file);
        File file13 = new File(sd_path13+sd_file);
        File file14 = new File(sd_path14+sd_file);
        File file15 = new File(sd_path15+sd_file);
        File file16 = new File(sd_path16+sd_file);
        if (file0.exists()) {
            return true;
        } else if (file1.exists()) {
            return true;
        } else if (file2.exists()) {
            return true;
        } else if (file3.exists()) {
            return true;
        } else if (file4.exists()) {
            return true;
        } else if (file5.exists()) {
            return true;
        } else if (file6.exists()) {
            return true;
        } else if (file7.exists()) {
            return true;
        } else if (file8.exists()) {
            return true;
        } else if (file9.exists()) {
            return true;
        } else if (file10.exists()) {
            return true;
        } else if (file11.exists()) {
            return true;
        } else if (file12.exists()) {
            return true;
        } else if (file13.exists()) {
            return true;
        } else if (file14.exists()) {
            return true;
        } else if (file15.exists()) {
            return true;
        }  else if (file16.exists()) {
            return true;
        }else {
            return false;
        }
    }

    public static boolean exist() {
        File file0 = new File(sd_path0+data_path);
        File file1 = new File(sd_path1+data_path);
        File file2 = new File(sd_path2+data_path);
        File file3 = new File(sd_path3+data_path);
        File file4 = new File(sd_path4+data_path);
        File file5 = new File(sd_path5+data_path);
        File file6 = new File(sd_path6+data_path);
        File file7 = new File(sd_path7+data_path);
        File file8 = new File(sd_path8+data_path);
        File file9 = new File(sd_path9+data_path);
        File file10 = new File(sd_path10+data_path);
        File file11 = new File(sd_path11+data_path);
        File file12 = new File(sd_path12+data_path);
        File file13 = new File(sd_path13+data_path);
        File file14 = new File(sd_path14+data_path);
        File file15 = new File(sd_path15+data_path);
        File file16 = new File(sd_path16+data_path);
        if (file0.exists()) {
            return true;
        } else if (file1.exists()) {
            return true;
        } else if (file2.exists()) {
            return true;
        } else if (file3.exists()) {
            return true;
        } else if (file4.exists()) {
            return true;
        } else if (file5.exists()) {
            return true;
        } else if (file6.exists()) {
            return true;
        } else if (file7.exists()) {
            return true;
        } else if (file8.exists()) {
            return true;
        } else if (file9.exists()) {
            return true;
        } else if (file10.exists()) {
            return true;
        } else if (file11.exists()) {
            return true;
        } else if (file12.exists()) {
            return true;
        } else if (file13.exists()) {
            return true;
        } else if (file14.exists()) {
            return true;
        } else if (file15.exists()) {
            return true;
        }  else if (file16.exists()) {
            return true;
        }else {
            return false;
        }
    }
}
