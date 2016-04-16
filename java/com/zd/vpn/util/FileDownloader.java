
package com.zd.vpn.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

public class FileDownloader {
    public static final String CONFIG_PATH = "/client.ovpn";
    public static final String CONFIG_CRT = "/client.crt";
    public static final String CONFIG_KEY = "/client.key";
    public static final String KEY_PATH = "/ta.key";
    public static final String CA_PATH = "/ca.crt";
    public static final String STRATEGY_PATH = "/strategy.json";
    //服务器私钥下载地址
    public static final String DOWN_STATIC_KEY = "/ClientAction_downStaticKey.action";
    //CA下载地址
    public static final String DOWN_CA = "/ClientAction_downCa.action";
    //策略下载地址
    public static final String DOWN_STRATEGY = "/ClientStrategyAction_findStrategy.action";
    //配置文件下载地址
    public static final String DOWN_CONFIG = "/ClientAction_downAndroidConfig.action";

    private Handler handler;

    protected Executor pool;

    public FileDownloader() {
        handler = new Handler();
        pool = Executors.newFixedThreadPool(2);
    }

    public interface OnFileDownloadListener {

        public void onFileDownloadOk(String msg);

        public void onFileDownloadErr(String msg);

    }

    public void downLoadFile(
            final Context paramContext,
            final String ip,
            final String poli_port,
            final String mServerPort,
            final String mKeyPassword,
            final boolean mTcpUdp,
            final boolean mUseLzo,
            final OnFileDownloadListener listener) {

        pool.execute(new Runnable() {

            @Override
            public void run() {
                SharedPreferences sPreferences = paramContext.getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
//                boolean read = sPreferences.getBoolean("vpn.read", false);
//                if (!read) {
                    SecuTFHelper helper = new SecuTFHelper();
                    helper.loadCrt(paramContext);
//                }
                String path = ConfigPathUtil.getSSLPath();
                if (path == null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFileDownloadErr("签发路径未找到，请确定已经签发证书！");
                        }
                    });
                    return;
                } else {
                  /*  boolean flag = ConfigPathUtil.existFile();
                    if(!flag){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onFileDownloadErr("未识别加密卡，请确定已插入加密卡！");
                            }
                        });
                        return;
                    }*/
                    File sdDir = new File(ConfigPathUtil.getSSLPath());
                    if (sdDir.exists()) {
                        String url = paramContext.getFilesDir().getAbsolutePath();
                        File config_path = new File(url);
                        if (!config_path.exists()) {
                            config_path.mkdirs();
//                                File config_cer_file = new File(url+FileDownloader.CONFIG_CRT);
//                                if(!config_cer_file.exists()) {
                            File cer = new File(ConfigPathUtil.getSSLPath() + FileDownloader.CONFIG_CRT);
                            if (cer.exists())
                                try {
                                    FileUtil.copy(cer, url + FileDownloader.CONFIG_CRT);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
//                                }

//                                File config_key_file = new File(url+FileDownloader.CONFIG_KEY);
//                                if(!config_key_file.exists()) {
                            File key = new File(ConfigPathUtil.getSSLPath() + FileDownloader.CONFIG_KEY);
                            if (key.exists())
                                try {
                                    FileUtil.copy(key, url + FileDownloader.CONFIG_KEY);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
//                                }
                        } else {
//                                File config_cer_file = new File(url+FileDownloader.CONFIG_CRT);
//                                if(!config_cer_file.exists()) {
                            File cer = new File(ConfigPathUtil.getSSLPath() + FileDownloader.CONFIG_CRT);
                            if (cer.exists())
                                try {
                                    FileUtil.copy(cer, url + FileDownloader.CONFIG_CRT);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
//                                }

//                                File config_key_file = new File(url+FileDownloader.CONFIG_KEY);
//                                if(!config_key_file.exists()) {
                            File key = new File(ConfigPathUtil.getSSLPath() + FileDownloader.CONFIG_KEY);
                            if (key.exists())
                                try {
                                    FileUtil.copy(key, url + FileDownloader.CONFIG_KEY);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
//                                }
                        }
                    }
                }
                String save_path = paramContext.getFilesDir().getAbsolutePath();
                String down_static_key_url = "http://" + ip + ":" + poli_port + DOWN_STATIC_KEY;
                String down_ca_url = "http://" + ip + ":" + poli_port + DOWN_CA;
                String down_config_url = "http://" + ip + ":" + poli_port + DOWN_CONFIG;
                String down_strategy_url = "http://" + ip + ":" + poli_port + DOWN_STRATEGY;

                try {
                    byte[] data = GetByte.getData(down_static_key_url);
                    if (data != null)
                        FileUtil.copy(data, save_path + KEY_PATH);

                    data = GetByte.getData(down_ca_url);
                    if (data != null)
                        FileUtil.copy(data, save_path + CA_PATH);

                    data = GetByte.getData(down_config_url);
                    if (data != null)
                        FileUtil.copy(data, save_path + CONFIG_PATH);

                    File file = new File(save_path + CONFIG_PATH);
                    if (file.exists() && file.length() > 0) {
                        try {
                            byte[] data1 = GetByte.readInstream(new FileInputStream(file));
                            ConfigFileUtil.update(ip, mServerPort, mKeyPassword, mTcpUdp, mUseLzo, data1, save_path + CONFIG_PATH);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        data = GetByte.getData(down_strategy_url);
                        if (data != null)
                            FileUtil.copy(data, save_path + STRATEGY_PATH);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFileDownloadOk("下载策略完成！");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFileDownloadErr("下载策略异常！");
                        }
                    });
                }
            }
        });
    }
}
