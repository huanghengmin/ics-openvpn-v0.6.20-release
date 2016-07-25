package com.zd.vpn.ui;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.zd.vpn.LaunchVPN;
import com.zd.vpn.R;
import com.zd.vpn.core.OpenVPNService;
import com.zd.vpn.core.OpenVpnManagementThread;
import com.zd.vpn.core.ProfileManager;
import com.zd.vpn.base.BaseActivity;
import com.zd.vpn.base.TitleBar;
import com.zd.vpn.core.VpnStatus;
import com.zd.vpn.util.ConfigPathUtil;
import com.zd.vpn.util.ConfigUtil;
import com.zd.vpn.util.FileDownloader;
import com.zd.vpn.util.FileUtil;
import com.zd.vpn.util.NetUtil;
import com.zd.vpn.util.ReturnCode;
import com.zd.vpn.util.RoundCornerItem;
import com.zd.vpn.util.Sender;
import com.zd.vpn.util.StrategyUtil;
import com.zd.vpn.util.TelInfoUtil;
import com.zd.vpn.util.TerminalStatusPost;
import com.zd.vpn.util.ThreeYardsPost;
import com.zd.vpn.util.TitlebarFactory;
import com.zd.vpn.util.ToastUtils;

/**
 * 入口
 */
public class MoreActivity extends BaseActivity implements OnClickListener, VpnStatus.StateListener {

    protected OpenVPNService mService;

//    AlarmReceiver alarm = new AlarmReceiver();

    RoundCornerItem start;
    RoundCornerItem setting;
    RoundCornerItem setting_advanced;
    RoundCornerItem cert;
//    RoundCornerItem config;
    RoundCornerItem about;
    RoundCornerItem exit;

//    RoundCornerItem statusTv;

//    private Handler uiHandler;

    //    private String CONFIG_PATH = "/client.ovpn";
    SharedPreferences shPreferences;
    private boolean logout = false;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
    private Matrix mDisplayMatrix;
    private WindowManager mWindowManager;
    public MyReceiver receiver;

    private boolean register = false;

//    public static boolean init = false;

    private boolean isConnected = false;//是否已经连接VPN

    // hsc's code
    // the state values set below is for the problem that when exit the show
    // certification fragment,it will show "再按一次退出客户端"
    public static final int INT_SHOW_CERTIFICATION_FRAGMENT_CREATE = 0;
    public static final int INT_SHOW_CERTIFICATION_FRAGMENT_PAUSE = 1;
    public static final int INT_SHOW_CERTIFICATION_FRAGMENT_DESTROY = 2;
    public static final int INT_SHOW_CERTIFICATION_FRAGMENT_ATTACH = 3;

    // the first time to show the certification
//    public static boolean whether_first_time_show_certification_fragment = true;

    public static int int_show_certification_fragment_state = INT_SHOW_CERTIFICATION_FRAGMENT_DESTROY;
    private Handler handler = new Handler();
//    private BroadcastReceiver mSDReceiver;

    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            OpenVPNService.LocalBinder binder = (OpenVPNService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        start = findView(R.id.start);
        setting = findView(R.id.setting);
        setting_advanced = findView(R.id.setting_advanced);
        cert = findView(R.id.cert);
//        config = findView(R.id.config);
        about = findView(R.id.about);
        exit = findView(R.id.exit);

//        statusTv = findView(R.id.statusTv);

        start.setOnClickListener(this);
        setting.setOnClickListener(this);
        setting_advanced.setOnClickListener(this);
        cert.setOnClickListener(this);
//        config.setOnClickListener(this);
        about.setOnClickListener(this);
        exit.setOnClickListener(this);
//        statusTv.setOnClickListener(this);


        Button button = findView(R.id.titlebarLeftButton);
        button.setVisibility(View.INVISIBLE);

        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.TFINFO_BROADCAST");
        registerReceiver(receiver, filter);
        register = true;
        shPreferences = getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
//        setErrorMessage();
        getXy(shPreferences);

        /*try {
            File file = new File(ConfigPathUtil.getConfigPath() + CONFIG_PATH);
            Log.i("filePath", file.getAbsolutePath());
            //ToastUtils.showLong(this,file.getAbsolutePath());
            // 生成配置文件
            if (!file.exists()) {
//                System.out.println("client.ovpn is not exit");
                file.getParentFile().mkdir();
                file.createNewFile();
                InputStream iStream = MoreActivity.class.getResourceAsStream("client.ovpn");
                FileOutputStream fStream2 = new FileOutputStream(file);
                byte[] data = new byte[1024];
                int len = 0;
                while ((len = iStream.read(data)) != -1) {
                    fStream2.write(data, 0, len);
                }
                fStream2.flush();
                fStream2.close();
                iStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        // 初始化工作
        if (shPreferences.getString("vpn.ip", "").length() == 0) {
            ToastUtils.show(this.getApplicationContext(), "请先初始化基本配置！");
        }
        VpnStatus.addStateListener(this);
//        alarm.acquireWakeLock(this);
//        alarm.set_screen_off_timeout(this);
//        alarm.setAlarm(this);

        /*WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            int wifiState = wifiManager.getWifiState();
            switch (wifiState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    wifiManager.setWifiEnabled(false);
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    wifiManager.setWifiEnabled(false);
                    break;
            }
        }*/

        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
/*
        mSDReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)) {
                    ProfileManager.setConntectedVpnProfileDisconnected(getApplicationContext());
                    if (mService != null && mService.getManagement() != null)
                        mService.getManagement().stopVPN();
//                    finish();
//                    System.exit(0);
                } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                    ProfileManager.setConntectedVpnProfileDisconnected(getApplicationContext());
                    if (mService != null && mService.getManagement() != null)
                        mService.getManagement().stopVPN();
//                    finish();
//                    System.exit(0);
                } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                    ProfileManager.setConntectedVpnProfileDisconnected(getApplicationContext());
                    if (mService != null && mService.getManagement() != null)
                        mService.getManagement().stopVPN();
//                    finish();
//                    System.exit(0);
                } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                    ProfileManager.setConntectedVpnProfileDisconnected(getApplicationContext());
                    if (mService != null && mService.getManagement() != null)
                        mService.getManagement().stopVPN();
//                    finish();
//                    System.exit(0);
                } else if (action.equals(Intent.ACTION_MEDIA_REMOVED)) {
                    ProfileManager.setConntectedVpnProfileDisconnected(getApplicationContext());
                    if (mService != null && mService.getManagement() != null)
                        mService.getManagement().stopVPN();
//                    finish();
//                    System.exit(0);
                }
            }
        };

        IntentFilter sd_filter = new IntentFilter();
        sd_filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);//扩展介质（扩展卡）已经从 SD 卡插槽拔出，但是挂载点 (mount point) 还没解除 (unmount)
        sd_filter.addAction(Intent.ACTION_MEDIA_EJECT);//用户想要移除扩展介质（拔掉扩展卡）
        sd_filter.addAction(Intent.ACTION_MEDIA_MOUNTED);//扩展介质被插入，而且已经被挂载。
        sd_filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);//扩展介质存在，但是还没有被挂载 (mount)
        sd_filter.addAction(Intent.ACTION_MEDIA_REMOVED);  //扩展介质被移除。
//        sd_filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);  //已经扫描完介质的一个目录。
//        sd_filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);  //开始扫描介质的一个目录。。
//        sd_filter.addAction(Intent.ACTION_MEDIA_SHARED);  //扩展介质的挂载被解除 (unmount)。 因为它已经作为 USB 大容量存储被共享。。。
        sd_filter.addDataScheme("file"); // 必须要有此行，否则无法收到广播
        registerReceiver(mSDReceiver, sd_filter);*/


    }

    @Override
    public void onClick(View v) {
        SharedPreferences sPreferences = getApplicationContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
        boolean read = sPreferences.getBoolean("vpn.read", false);
        //判断各类条件
        if (v == start || v == cert /*|| v == config*/) {
            if (shPreferences.getString("vpn.ip", "").length() == 0) {
                ToastUtils.show(this.getApplicationContext(), "请先初始化基本配置！");
                return;
            } else if (v == cert && !read) {
                ToastUtils.show(this.getApplicationContext(), "正在初始化TF卡，请稍后查看！");
                return;
            } else if (v == start && !read) {
                /*boolean flag = ConfigPathUtil.existFile();
                 if(!flag){
                     ToastUtils.show(this.getApplicationContext(), "未识别加密卡，请确定插入的卡类型为加密卡！");
                    return ;
                }*/
                // 判断网络
                if (!NetUtil.isNetworkConnected(this)) {
                    Toast.makeText(this, "请先设置网络连接！", Toast.LENGTH_LONG).show();
//                    Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
                    Intent intent = new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                    return;
                }

                String save_path = this.getFilesDir().getAbsolutePath();
                File file = new File(save_path + FileDownloader.CONFIG_PATH);
                if (!file.exists()) {
                    ToastUtils.show(this.getApplicationContext(), "配置文件不存在，请先初始化基本配置！");
                    return;
                }
                file = new File(save_path + "/ca.crt");
                if (!file.exists()) {
                    ToastUtils.show(this.getApplicationContext(), "证书链文件不存在，请先初始化基本配置！");
                    return;
                }
                file = new File(save_path + "/ta.key");
                if (!file.exists()) {
                    ToastUtils.show(this.getApplicationContext(), "验证服务器私钥不存在，请先初始化基本配置！");
                    return;
                }
//                file = new File(save_path + FileDownloader.CONFIG_CRT);
//                if (!file.exists()) {
                    File ssl_crt_file = new File(ConfigPathUtil.getSSLPath(getApplicationContext()) + FileDownloader.CONFIG_CRT);//获取跟目录
                    if (ssl_crt_file.exists()) {
                        try {
                            FileUtil.copy(ssl_crt_file, save_path + FileDownloader.CONFIG_CRT);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastUtils.show(this.getApplicationContext(), "客户端证书不存在，请先做签发！");
                        return;
                    }
//                }
//                file = new File(save_path + FileDownloader.CONFIG_KEY);
//                if (!file.exists()) {
                    File ssl_key_file = new File(ConfigPathUtil.getSSLPath(getApplicationContext()) + FileDownloader.CONFIG_KEY);//获取跟目录
                    if (ssl_key_file.exists()) {
                        try {
                            FileUtil.copy(ssl_key_file, save_path + FileDownloader.CONFIG_KEY);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ToastUtils.show(this.getApplicationContext(), "客户端私钥不存在，请先做签发！");
                        return;
                    }
//                }
            }
        }

        //点击事件
        if (v == start) {
            if (isConnected) {
                //停止VPN
                this.showChoiceDialog("", "确定退出程序并停止VPN吗?", "是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                     /*   Intent intent = new Intent(getContext(), DisconnectVPN.class);
                        intent.setAction(Intent.ACTION_MAIN);
                        startActivity(intent);
                        finish();*/
                        ProfileManager.setConntectedVpnProfileDisconnected(getApplicationContext());
                        if (mService != null && mService.getManagement() != null)
                            mService.getManagement().stopVPN();
                        finish();
                        System.exit(0);
                    }
                }, "否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
            } else {
                if (ConfigPathUtil.exist(this)) {
                   /* if (!read) {
                        *//*SecuTFHelper secuTFHelper = new SecuTFHelper();
                        secuTFHelper.loadCrt(this);*//*
                        ToastUtils.show(this.getApplicationContext(), "请先初始化基本配置！");
                    }*/
                    if (read) {
                        /*boolean flag = ConfigPathUtil.existFile();
                        if(!flag){
                            Sender sender = new Sender(getContext());
                            sender.sendTF("未识别加密卡，请确定插入的卡类型为加密卡！");
                            return ;
                        }*/
                        //先校验
                        StrategyUtil strategyUtil = new StrategyUtil(getApplicationContext());
                        if (strategyUtil.getThreeyards()) {
                            //访问网络接口进行校验
                            SharedPreferences shPreferences = this.getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
                            String serialnumber = shPreferences.getString("vpn.serialNumber", "");
//                            String serialNumber = SecuTFHelper.find_serial(getApplicationContext());
                            String ip = shPreferences.getString("vpn.ip", "");
                            String port = shPreferences.getString("vpn.poliPort", "");
                            TelInfoUtil telInfo = new TelInfoUtil(this);
                            ThreeYardsPost tPost = new ThreeYardsPost(this, ip, port, telInfo.getImei(), telInfo.getSim(), serialnumber);
                            tPost.postData(new ThreeYardsPost.OnThreeYardsPostListener() {
                                @Override
                                public void onThreeYardsPostOk(String msg) {
                                    if (msg != null && !"".equals(msg)) {
                                        Sender sender = new Sender(getContext());
                                        sender.sendTF(ReturnCode.getReturnStatusMsg(msg));
                                    }

                                    ConfigUtil configUtil = new ConfigUtil(getContext());
                                    String uuid = "";
//                                    if (configUtil.getUuid() == null) {
                                        uuid = configUtil.config(getContext());
                                  /*  } else {
                                        uuid = configUtil.getUuid();
                                    }*/
                                    Intent intent = new Intent(getContext(), LaunchVPN.class);
                                    intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
                                    intent.setAction(Intent.ACTION_MAIN);
                                    startActivity(intent);
                                }

                                @Override
                                public void onThreeYardsPostErr(String msg) {
                                    if (msg != null && !"".equals(msg)) {
                                        Sender sender = new Sender(getContext());
                                        sender.sendTF(ReturnCode.getReturnStatusMsg(msg));
                                    }
                                }
                            });
                        } else {
                           /* ConfigUtil configUtil = new ConfigUtil(getContext());
                            String uuid = "";
//                            if (configUtil.getUuid() == null) {
                                uuid = configUtil.config(getApplicationContext());
                            *//*} else {
                                uuid = configUtil.getUuid();
                            }*//*
                            Intent intent = new Intent(getContext(), LaunchVPN.class);
                            intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
                            intent.setAction(Intent.ACTION_MAIN);
                            startActivity(intent);*/


                            SharedPreferences shPreferences = getContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
                            String serialNumber = shPreferences.getString("vpn.serialNumber", "");
//                        String serialNumber = SecuTFHelper.find_serial(getApplicationContext());
                            String ip = shPreferences.getString("vpn.ip", "");
                            String port = shPreferences.getString("vpn.poliPort", "");
//                            TelInfoUtil telInfo = new TelInfoUtil(getContext());
                            TerminalStatusPost tPost = new TerminalStatusPost(getContext(), ip, port, serialNumber);
                            tPost.postData(new TerminalStatusPost.OnThreeYardsPostListener() {
                                @Override
                                public void onThreeYardsPostOk(String msg) {
                                    Log.v("vpn", "ok");
//                                    if (msg != null && !"".equals(msg))
//                                        Toast.makeText(getContext(), ReturnCode.getReturnStatusMsg(msg), Toast.LENGTH_LONG).show();

                                    ConfigUtil configUtil = new ConfigUtil(getContext());
                                    String uuid  = configUtil.config(getContext());
                                    Intent intent = new Intent(getContext(), LaunchVPN.class);
                                    intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
                                    intent.setAction(Intent.ACTION_MAIN);
                                    startActivity(intent);
                                }

                                @Override
                                public void onThreeYardsPostErr(String msg) {
                                    Log.v("vpn", "err");
                                    if (msg != null && !"".equals(msg))
                                        Toast.makeText(getContext(), ReturnCode.getReturnStatusMsg(msg), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Sender sender = new Sender(getContext());
                        sender.sendTF("TF卡驱动加载失败请重试插拔TF卡或PIN码有误请检查PIN码设置");
                    }
                } else {
                    Sender sender = new Sender(getContext());
                    sender.sendTF("TF卡不在在,请安装TF卡");
                }
            }
        } else if (v == setting) {
            Intent intent = new Intent(getContext(), BasicSettingActivity.class);
            startActivity(intent);
        }else if (v == setting_advanced) {
            Intent intent = new Intent(getContext(), GeneralSettingsActivity.class);
            startActivity(intent);
        }  else if (v == cert) {
            Intent intent = new Intent(getContext(), ShowCertActivity.class);
            startActivity(intent);
        }/* else if (v == config) {
            ConfigUtil configUtil = new ConfigUtil(getContext());
            String uuid = "";
//            if (configUtil.getUuid() == null) {
                uuid = configUtil.config(getContext());
          *//*  } else {
                uuid = configUtil.getUuid();
            }*//*
            Intent vprefintent = new Intent(getContext(), VPNPreferences.class);
            vprefintent.putExtra(getContext().getPackageName() + ".profileUUID", uuid);
            startActivity(vprefintent);
        }*/  else if (v == about) {
            Intent intent = new Intent(getContext(), AboutActivity.class);
            startActivity(intent);
        } else if (v == exit) {
            this.showChoiceDialog("", "确定退出吗？", "是", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    /*Intent intent = new Intent(getContext(), DisconnectVPN.class);
                    intent.setAction(Intent.ACTION_MAIN);
                    startActivity(intent);
                    finish();*/
                    ProfileManager.setConntectedVpnProfileDisconnected(getApplicationContext());
                    if (mService != null && mService.getManagement() != null)
                        mService.getManagement().stopVPN();
                    finish();
                    System.exit(0);
                }
            }, "否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        }/*else if(v==statusTv){
            AlertDialog.Builder builder = new AlertDialog.Builder(MoreActivity.this);
            builder.setTitle("安全链路状态");
            builder.setPositiveButton("确定",null);
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setMessage(statusTv.getContent());
            builder.show();
        }*/
    }

    @Override
    public int rootViewRes() {
        return R.layout.main;
    }

    @Override
    public TitleBar initTitlebar() {
        TitleBar titleBar = TitlebarFactory.createCustomBackTitlebar(this, "安全接入网关客户端");
        return titleBar;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getXy(SharedPreferences shPreferences) {
        String sdk = android.os.Build.VERSION.SDK;
        int SDK = Integer.parseInt(sdk);
        if (SDK >= 15) {
            mDisplayMatrix = new Matrix();
            mWindowManager = (WindowManager) this
                    .getSystemService(Context.WINDOW_SERVICE);
            mDisplay = mWindowManager.getDefaultDisplay();
            mDisplayMetrics = new DisplayMetrics();
            mDisplay.getRealMetrics(mDisplayMetrics);
            mDisplay.getRealMetrics(mDisplayMetrics);
            float[] dims = {mDisplayMetrics.widthPixels,
                    mDisplayMetrics.heightPixels};
            float degrees = getDegreesForRotation(mDisplay.getRotation());
            boolean requiresRotation = (degrees > 0);
            if (requiresRotation) {
                // Get the dimensions of the device in its native orientation
                mDisplayMatrix.reset();
                mDisplayMatrix.preRotate(-degrees);
                mDisplayMatrix.mapPoints(dims);
                dims[0] = Math.abs(dims[0]);
                dims[1] = Math.abs(dims[1]);
                shPreferences.edit().putFloat("vpn.width", dims[0]).commit();
                shPreferences.edit().putFloat("vpn.height", dims[1]).commit();
            }
        }
    }

    private float getDegreesForRotation(int value) {
        switch (value) {
            case Surface.ROTATION_90:
                return 360f - 90f;
            case Surface.ROTATION_180:
                return 360f - 180f;
            case Surface.ROTATION_270:
                return 360f - 270f;
        }
        return 0f;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (register) {
            unregisterReceiver(receiver);
        }
        unbindService(mConnection);
//        alarm.releaseWakeLock();
//        alarm.cancelAlarm();
    }

    // 按两次退出
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0
                && int_show_certification_fragment_state == INT_SHOW_CERTIFICATION_FRAGMENT_DESTROY) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {

                if (logout == false) {
                    logout = true;
                    Toast.makeText(this, "再按一次退出客户端", Toast.LENGTH_SHORT)
                            .show();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            logout = false;
                        }
                    };
                    new Timer().schedule(task, 2000);
                } else {
                    finish();
                }
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0
                && !(int_show_certification_fragment_state == INT_SHOW_CERTIFICATION_FRAGMENT_DESTROY)) {
            this.finish();
        }

        return true;
    }

    @Override
    protected void onResume() {
        Log.i("vpn", "onResume");
        super.onResume();
       /* SharedPreferences sPreferences = getApplicationContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
        final boolean read = sPreferences.getBoolean("vpn.read", false);
        if (!read) {
           *//* SecuTFHelper helper = new SecuTFHelper();
            helper.loadCrt(getApplication());*//*
            ToastUtils.show(this.getApplicationContext(), "请先初始化基本配置！");
        }*/
        /*if (!init) {
            new Thread() {
                public void run() {
                    Log.i("vpn", "start vpn");
                    try {
                        if(!read) {
                            SecuTFHelper helper = new SecuTFHelper();
                            helper.loadCrt(getApplication());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void updateState(final String state, final String logmessage, final int localizedResId, final VpnStatus.ConnectionStatus level) {
        //改变状态信息
        Log.v("vpn state", state + "|" + logmessage + "|" + level.toString());
        handler.post(new Runnable() {
            @Override
            public void run() {
//                statusTv.setContent(getString(localizedResId));
                if (level == VpnStatus.ConnectionStatus.LEVEL_CONNECTED) {
                    start.setContent("停止VPN");
                    isConnected = true;
                } else if (level == VpnStatus.ConnectionStatus.UNKNOWN_LEVEL || level == VpnStatus.ConnectionStatus.LEVEL_NOTCONNECTED) {
                    start.setContent("启动VPN");
                    isConnected = false;
                } else if (level == VpnStatus.ConnectionStatus.LEVEL_WAITING_FOR_USER_INPUT) {
                    start.setContent("准备链接VPN");
                    isConnected = false;
                } else {
                    start.setContent(getString(localizedResId));
                    isConnected = false;
                }
            }
        });

    }

    public class MyReceiver extends BroadcastReceiver {

        private static final String TAG = "MyReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("msg");
            if (msg.equals("PIN码被锁住")) {
                ProfileManager
                        .setConntectedVpnProfileDisconnected(getApplicationContext());
                OpenVpnManagementThread.stopOpenVPN();
//                TFInfo.TunnelState = false;
//                TFInfo.TFSTATE = false;
                finish();
            }
            Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show();
        }
    }

  /*  public void setErrorMessage() {
        Editor editor = shPreferences.edit();
        editor.putString("vpn.serialNumber", "信息不全");
        editor.putString("vpn.userName", "信息不全");
        editor.putString("vpn.province", "信息不全");
        editor.putString("vpn.city", "信息不全");
        editor.putString("vpn.company", "信息不全");
        editor.putString("vpn.department", "信息不全");
        editor.putString("vpn.cn", "信息不全");
        editor.commit();
    }*/
}
