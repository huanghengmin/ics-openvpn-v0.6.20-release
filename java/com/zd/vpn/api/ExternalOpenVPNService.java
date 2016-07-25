/*
 * Copyright (c) 2012-2014 Arne Schwabe
 * Distributed under the GNU GPL v2. For full terms see the file doc/LICENSE.txt
 */

package com.zd.vpn.api;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.VpnService;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import com.zd.vpn.LaunchVPN;
import com.zd.vpn.VpnProfile;
import com.zd.vpn.core.ConfigParser;
import com.zd.vpn.core.ConfigParser.ConfigParseError;
import com.zd.vpn.core.OpenVPNService;
import com.zd.vpn.core.OpenVPNService.LocalBinder;
import com.zd.vpn.core.ProfileManager;
import com.zd.vpn.core.VPNLaunchHelper;
import com.zd.vpn.core.VpnStatus;
import com.zd.vpn.core.VpnStatus.ConnectionStatus;
import com.zd.vpn.core.VpnStatus.StateListener;
import com.zd.vpn.ui.MoreActivity;
import com.zd.vpn.util.ConfigFileUtil;
import com.zd.vpn.util.ConfigPathUtil;
import com.zd.vpn.util.ConfigUtil;
import com.zd.vpn.util.FileDownloader;
import com.zd.vpn.util.FileUtil;
import com.zd.vpn.util.GetByte;
import com.zd.vpn.util.NetUtil;
import com.zd.vpn.util.ReturnCode;
import com.zd.vpn.util.SecuTFHelper;
import com.zd.vpn.util.StrategyUtil;
//import com.zd.vpn.util.TFInfo;
import com.zd.vpn.util.TelInfoUtil;
import com.zd.vpn.util.TerminalStatusNoThreadPost;
import com.zd.vpn.util.TerminalStatusPost;
import com.zd.vpn.util.ThreeYardsNoThreadPost;
import com.zd.vpn.util.ThreeYardsPost;
import com.zd.vpn.util.ToastUtils;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class ExternalOpenVPNService extends Service implements StateListener {

    private static final int SEND_TOALL = 0;

    final RemoteCallbackList<IOpenVPNStatusCallback> mCallbacks =
            new RemoteCallbackList<IOpenVPNStatusCallback>();

    private OpenVPNService mService;
    private ExternalAppDatabase mExtAppDb;

    private static String RETURN_CODE = ReturnCode.RETURN_CLIENT_STATUS_ERROR;


    private ServiceConnection mConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        VpnStatus.addStateListener(this);
        mExtAppDb = new ExternalAppDatabase(this);

        Intent intent = new Intent(getBaseContext(), OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mHandler.setService(this);
    }

    private final IOpenVPNAPIService.Stub mBinder = new IOpenVPNAPIService.Stub() {

        private void checkOpenVPNPermission() throws SecurityRemoteException {
            PackageManager pm = getPackageManager();

            for (String apppackage : mExtAppDb.getExtAppList()) {
                ApplicationInfo app;
                try {
                    app = pm.getApplicationInfo(apppackage, 0);
                    if (Binder.getCallingUid() == app.uid) {
                        return;
                    }
                } catch (NameNotFoundException e) {
                    // App not found. Remove it from the list
                    mExtAppDb.removeApp(apppackage);
                }

            }
            throw new SecurityException("Unauthorized OpenVPN API Caller");
        }

        @Override
        public List<APIVpnProfile> getProfiles() throws RemoteException {
            checkOpenVPNPermission();

            ProfileManager pm = ProfileManager.getInstance(getBaseContext());

            List<APIVpnProfile> profiles = new LinkedList<APIVpnProfile>();

            for (VpnProfile vp : pm.getProfiles())
                profiles.add(new APIVpnProfile(vp.getUUIDString(), vp.mName, vp.mUserEditable));

            return profiles;
        }

        private void startProfile(VpnProfile vp) {
            Intent vpnPermissionIntent = VpnService.prepare(ExternalOpenVPNService.this);
            SharedPreferences sPreferences = getApplicationContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
            boolean read = sPreferences.getBoolean("vpn.read", false);
            if (ConfigPathUtil.exist(getApplicationContext())) {
                if (read) {
                    /*boolean flag = ConfigPathUtil.existFile();
                    if(!flag){
                        ToastUtils.show(getApplicationContext(),"未识别加密卡，请确定插入的卡类型为加密卡！");
                        return ;
                    }*/
                    if (vpnPermissionIntent != null) {
                        Intent shortVPNIntent = new Intent(Intent.ACTION_MAIN);
                        shortVPNIntent.setClass(getBaseContext(), com.zd.vpn.LaunchVPN.class);
                        shortVPNIntent.putExtra(com.zd.vpn.LaunchVPN.EXTRA_KEY, vp.getUUIDString());
                        shortVPNIntent.putExtra(com.zd.vpn.LaunchVPN.EXTRA_HIDELOG, true);
                        shortVPNIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(shortVPNIntent);
                    } else {
                        VPNLaunchHelper.startOpenVpn(vp, getBaseContext());
                    }
                }
            }
        }

        @Override
        public void startProfile(String profileUUID) throws RemoteException {
            /*checkOpenVPNPermission();

            VpnProfile vp = ProfileManager.get(getBaseContext(), profileUUID);
            startProfile(vp);*/

            checkOpenVPNPermission();
            SharedPreferences sPreferences = getApplicationContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
            boolean read = sPreferences.getBoolean("vpn.read", false);
            if (ConfigPathUtil.exist(getApplicationContext())) {
                if (read) {
                    /*boolean flag = ConfigPathUtil.existFile();
                    if(!flag){
                        ToastUtils.show(getApplicationContext(),"未识别加密卡，请确定插入的卡类型为加密卡！");
                        return ;
                    }*/
                    StrategyUtil strategyUtil = new StrategyUtil(getApplicationContext());
                    if (strategyUtil.getThreeyards()) {
                        //访问网络接口进行校验
                        SharedPreferences shPreferences = getBaseContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
                        String serialNumber = shPreferences.getString("vpn.serialNumber", "");
//                        String serialNumber = SecuTFHelper.find_serial(getApplicationContext());
                        String ip = shPreferences.getString("vpn.ip", "");
                        String port = shPreferences.getString("vpn.poliPort", "");
                        TelInfoUtil telInfo = new TelInfoUtil(getBaseContext());
                        ThreeYardsPost tPost = new ThreeYardsPost(getBaseContext(), ip, port, telInfo.getImei(), telInfo.getSim(), serialNumber);
                        tPost.postData(new ThreeYardsPost.OnThreeYardsPostListener() {
                            @Override
                            public void onThreeYardsPostOk(String msg) {
                                Log.v("vpn", "ok");
//                                if (msg != null && !"".equals(msg))
//                                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                                //start vpn
                                /*ConfigUtil configUtil = new ConfigUtil(getBaseContext());
                                String uuid = "";
//                                if (configUtil.getUuid() == null) {
                                uuid = configUtil.config(getApplicationContext());
                                *//*} else {
                                    uuid = configUtil.getUuid();
                                }*//*
                                VpnProfile vp = ProfileManager.get(getBaseContext(), uuid);
                                startProfile(vp);*/

                                ConfigUtil configUtil = new ConfigUtil(getApplicationContext());
                                String uuid = configUtil.config(getApplicationContext());
                                Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                                intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onThreeYardsPostErr(String msg) {
                                Log.v("vpn", "err");
                                if (msg != null && !"".equals(msg))
                                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        //start vpn
                      /*  ConfigUtil configUtil = new ConfigUtil(getBaseContext());
                        String uuid = "";
//                        if (configUtil.getUuid() == null) {
                        uuid = configUtil.config(getApplicationContext());
                       *//* } else {
                            uuid = configUtil.getUuid();
                        }*//*

                        VpnProfile vp = ProfileManager.get(getBaseContext(), uuid);
                        startProfile(vp);*/


                        SharedPreferences shPreferences = getBaseContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
                        String serialNumber = shPreferences.getString("vpn.serialNumber", "");
//                        String serialNumber = SecuTFHelper.find_serial(getApplicationContext());
                        String ip = shPreferences.getString("vpn.ip", "");
                        String port = shPreferences.getString("vpn.poliPort", "");
//                        TelInfoUtil telInfo = new TelInfoUtil(getBaseContext());
                        TerminalStatusPost tPost = new TerminalStatusPost(getBaseContext(), ip, port, serialNumber);
                        tPost.postData(new TerminalStatusPost.OnThreeYardsPostListener() {
                            @Override
                            public void onThreeYardsPostOk(String msg) {
                                Log.v("vpn", "ok");
//                                if (msg != null && !"".equals(msg))
//                                    Toast.makeText(getBaseContext(), ReturnCode.getReturnStatusMsg(msg), Toast.LENGTH_LONG).show();
                                /*ConfigUtil configUtil = new ConfigUtil(getBaseContext());
                                String uuid = configUtil.config(getApplicationContext());

                                VpnProfile vp = ProfileManager.get(getBaseContext(), uuid);
                                startProfile(vp);*/

                                ConfigUtil configUtil = new ConfigUtil(getApplicationContext());
                                String uuid = configUtil.config(getApplicationContext());
                                Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                                intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onThreeYardsPostErr(String msg) {
                                Log.v("vpn", "err");
                                if (msg != null && !"".equals(msg))
                                    Toast.makeText(getBaseContext(), ReturnCode.getReturnStatusMsg(msg), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }

        public void startVPN(String inlineconfig) throws RemoteException {
            /*checkOpenVPNPermission();

            ConfigParser cp = new ConfigParser();
            try {
                cp.parseConfig(new StringReader(inlineconfig));
                VpnProfile vp = cp.convertProfile();
                if (vp.checkProfile(getApplicationContext()) != R.string.no_error_found)
                    throw new RemoteException(getString(vp.checkProfile(getApplicationContext())));


                ProfileManager.setTemporaryProfile(vp);
                startProfile(vp);

            } catch (IOException e) {
                throw new RemoteException(e.getMessage());
            } catch (ConfigParseError e) {
                throw new RemoteException(e.getMessage());
            }*/

            checkOpenVPNPermission();
            SharedPreferences sPreferences = getApplicationContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
            boolean read = sPreferences.getBoolean("vpn.read", false);
            if (ConfigPathUtil.exist(getApplicationContext())) {
                if (read) {
                    /*boolean flag = ConfigPathUtil.existFile();
                    if(!flag){
                        ToastUtils.show(getApplicationContext(),"未识别加密卡，请确定插入的卡类型为加密卡！");
                        return ;
                    }*/
                    StrategyUtil strategyUtil = new StrategyUtil(getApplicationContext());
                    if (strategyUtil.getThreeyards()) {
                        //访问网络接口进行校验
                        SharedPreferences shPreferences = getBaseContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
                        String serialNumber = shPreferences.getString("vpn.serialNumber", "");
//                        String serialNumber = SecuTFHelper.find_serial(getApplicationContext());
                        String ip = shPreferences.getString("vpn.ip", "");
                        String port = shPreferences.getString("vpn.poliPort", "");
                        TelInfoUtil telInfo = new TelInfoUtil(getBaseContext());
                        ThreeYardsPost tPost = new ThreeYardsPost(getBaseContext(), ip, port, telInfo.getImei(), telInfo.getSim(), serialNumber);
                        tPost.postData(new ThreeYardsPost.OnThreeYardsPostListener() {
                            @Override
                            public void onThreeYardsPostOk(String msg) {
                                Log.v("vpn", "ok");
//                                if (msg != null && !"".equals(msg))
//                                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                                //start vpn
                                /*ConfigUtil configUtil = new ConfigUtil(getBaseContext());
                                String uuid = "";
//                                if (configUtil.getUuid() == null) {
                                uuid = configUtil.config(getApplicationContext());
                               *//* } else {
                                    uuid = configUtil.getUuid();
                                }*//*

                                VpnProfile vp = ProfileManager.get(getBaseContext(), uuid);
                                startProfile(vp);*/

                                ConfigUtil configUtil = new ConfigUtil(getApplicationContext());
                                String uuid = configUtil.config(getApplicationContext());
                                Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                                intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onThreeYardsPostErr(String msg) {
                                Log.v("vpn", "err");
                                if (msg != null && !"".equals(msg))
                                    Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        //start vpn
                       /* ConfigUtil configUtil = new ConfigUtil(getBaseContext());
                        String uuid = "";
//                        if (configUtil.getUuid() == null) {
                        uuid = configUtil.config(getApplicationContext());
                       *//* } else {
                            uuid = configUtil.getUuid();
                        }*//*

                        VpnProfile vp = ProfileManager.get(getBaseContext(), uuid);
                        startProfile(vp);*/

                        SharedPreferences shPreferences = getBaseContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
                        String serialNumber = shPreferences.getString("vpn.serialNumber", "");
//                        String serialNumber = SecuTFHelper.find_serial(getApplicationContext());
                        String ip = shPreferences.getString("vpn.ip", "");
                        String port = shPreferences.getString("vpn.poliPort", "");
//                        TelInfoUtil telInfo = new TelInfoUtil(getBaseContext());
                        TerminalStatusPost tPost = new TerminalStatusPost(getBaseContext(), ip, port, serialNumber);
                        tPost.postData(new TerminalStatusPost.OnThreeYardsPostListener() {
                            @Override
                            public void onThreeYardsPostOk(String msg) {
                                Log.v("vpn", "ok");
//                                if (msg != null && !"".equals(msg))
//                                    Toast.makeText(getBaseContext(), ReturnCode.getReturnStatusMsg(msg), Toast.LENGTH_LONG).show();
                               /* ConfigUtil configUtil = new ConfigUtil(getBaseContext());
                                String uuid = configUtil.config(getApplicationContext());
                                VpnProfile vp = ProfileManager.get(getBaseContext(), uuid);
                                startProfile(vp);*/

                                ConfigUtil configUtil = new ConfigUtil(getApplicationContext());
                                String uuid = configUtil.config(getApplicationContext());
                                Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                                intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onThreeYardsPostErr(String msg) {
                                Log.v("vpn", "err");
                                if (msg != null && !"".equals(msg))
                                    Toast.makeText(getBaseContext(), ReturnCode.getReturnStatusMsg(msg), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        }

        @Override
        public boolean addVPNProfile(String name, String config) throws RemoteException {
            checkOpenVPNPermission();

            ConfigParser cp = new ConfigParser();
            try {
                cp.parseConfig(new StringReader(config));
                VpnProfile vp = cp.convertProfile();
                vp.mName = name;
                ProfileManager pm = ProfileManager.getInstance(getBaseContext());
                VpnProfile oldProfile = pm.getProfileByName(name);
                if (oldProfile != null) {
                    pm.removeProfile(getBaseContext(), oldProfile);
                }
                pm.addProfile(vp);
                pm.saveProfile(getBaseContext(), vp);
                pm.saveProfileList(getBaseContext());
            } catch (IOException e) {
                VpnStatus.logException(e);
                return false;
            } catch (ConfigParseError e) {
                VpnStatus.logException(e);
                return false;
            }
            return true;
        }

        @Override
        public Intent prepare(String packagename) {
            ExternalAppDatabase appDatabase = new ExternalAppDatabase(ExternalOpenVPNService.this);
            if (appDatabase.isAllowed(packagename))
                return null;
            else {
                appDatabase.addApp(packagename);
                return null;
            }


            /*Intent intent = new Intent();
            intent.setClass(ExternalOpenVPNService.this, ConfirmDialog.class);
            return intent;*/
        }

        @Override
        public Intent prepareVPNService() throws RemoteException {
            checkOpenVPNPermission();

            if (VpnService.prepare(ExternalOpenVPNService.this) == null)
                return null;
            else
                return new Intent(getBaseContext(), GrantPermissionsActivity.class);
        }

        @Override
        public void registerStatusCallback(IOpenVPNStatusCallback cb)
                throws RemoteException {
            checkOpenVPNPermission();

            if (cb != null) {
                cb.newStatus(mMostRecentState.vpnUUID, mMostRecentState.state,mMostRecentState.logmessage, mMostRecentState.level.name());
                mCallbacks.register(cb);
            }
        }

        @Override
        public void unregisterStatusCallback(IOpenVPNStatusCallback cb)
                throws RemoteException {
            checkOpenVPNPermission();

            if (cb != null)
                mCallbacks.unregister(cb);
        }

        @Override
        public void disconnect() throws RemoteException {
            checkOpenVPNPermission();
            if (mService != null && mService.getManagement() != null)
                mService.getManagement().stopVPN();
        }

        @Override
        public void pause() throws RemoteException {
            checkOpenVPNPermission();
            if (mService != null)
                mService.userPause(true);
        }

        @Override
        public void resume() throws RemoteException {
            checkOpenVPNPermission();
            if (mService != null)
                mService.userPause(false);

        }

        @Override
        public String startZDVPN() throws RemoteException {
            checkOpenVPNPermission();
            SharedPreferences sPreferences = getApplicationContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
            boolean read = sPreferences.getBoolean("vpn.read", false);
            if (ConfigPathUtil.exist(getApplicationContext())) {
                if (read) {
                    /*boolean f = ConfigPathUtil.existFile();
                    if(!f){
                        ToastUtils.show(getApplicationContext(),"未识别加密卡，请确定插入的卡类型为加密卡！");
                        return ReturnCode.getReturnStatusMsg(ReturnCode.RETURN_CLIENT_READ_CERT_ERROR);
                    }*/
                    StrategyUtil strategyUtil = new StrategyUtil(getApplicationContext());
                    if (strategyUtil.getThreeyards()) {
                        SharedPreferences shPreferences = getBaseContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
                        String ip = shPreferences.getString("vpn.ip", "");
                        String port = shPreferences.getString("vpn.poliPort", "");
                        TelInfoUtil telInfo = new TelInfoUtil(getBaseContext());
                        ThreeYardsNoThreadPost tPost = new ThreeYardsNoThreadPost(getBaseContext(), ip, port, telInfo.getImei(), telInfo.getSim());
                        String code =  tPost.postData();
                        boolean flag = ReturnCode.getBooleanMsg(code);
                        if(flag){
                       /*     ConfigUtil configUtil = new ConfigUtil(getBaseContext());
                            String uuid = configUtil.config(getBaseContext());
                            VpnProfile vp = ProfileManager.get(getBaseContext(), uuid);
                            startProfile(vp);*/

                            ConfigUtil configUtil = new ConfigUtil(getApplicationContext());
                            String uuid = configUtil.config(getApplicationContext());
                            Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                            intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
                            intent.setAction(Intent.ACTION_MAIN);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            return code;
                        }else {
//                            if (code != null && !"".equals(code))
//                                Toast.makeText(getBaseContext(), ReturnCode.getReturnStatusMsg(code), Toast.LENGTH_LONG).show();
                            return code;
                        }
                    } else {
                        SharedPreferences shPreferences = getBaseContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
                        String ip = shPreferences.getString("vpn.ip", "");
                        String port = shPreferences.getString("vpn.poliPort", "");
                        TerminalStatusNoThreadPost tPost = new TerminalStatusNoThreadPost(getBaseContext(), ip, port);
                        String code =  tPost.postData();
                        boolean flag = ReturnCode.getBooleanMsg(code);
                        if(flag){
                            /*ConfigUtil configUtil = new ConfigUtil(getBaseContext());
                            String uuid = configUtil.config(getBaseContext());
                            VpnProfile vp = ProfileManager.get(getBaseContext(), uuid);
                            startProfile(vp);*/
                            ConfigUtil configUtil = new ConfigUtil(getApplicationContext());
                            String uuid = configUtil.config(getApplicationContext());
                            Intent intent = new Intent(getApplicationContext(), LaunchVPN.class);
                            intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
                            intent.setAction(Intent.ACTION_MAIN);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            return code;
                        }else {
//                            if (code != null && !"".equals(code))
//                                Toast.makeText(getBaseContext(), ReturnCode.getReturnStatusMsg(code), Toast.LENGTH_LONG).show();
                            return code;
                        }
                    }
                }else {
                    return ReturnCode.RETURN_PLEASE_INIT_ERROR;
                }
            }
            return ReturnCode.RETURN_CLIENT_READ_CERT_ERROR;
        }

        @Override
        public void stopZDVPN() throws RemoteException {
            checkOpenVPNPermission();
            if (mService != null && mService.getManagement() != null)
                mService.getManagement().stopVPN();
        }

        @Override
        public boolean init(String ip, int port, int strategyPort, String pinCode, String container, boolean tcpudp, boolean lzo) {
            try {
                checkOpenVPNPermission();
            } catch (SecurityRemoteException e) {
                e.printStackTrace();
            }
            // 判断网络
            if (!NetUtil.isNetworkConnected(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "请先设置网络连接！", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                return false;
            }
            // 下载证书
            return downloadCert(getApplicationContext(), ip, port, strategyPort, pinCode, container, tcpudp, lzo);
        }

        @Override
        public String[] loadCert() {
            try {
                checkOpenVPNPermission();
            } catch (SecurityRemoteException e) {
                e.printStackTrace();
            }
            SharedPreferences sPreferences = getApplicationContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
            boolean read = sPreferences.getBoolean("vpn.read", false);
            if (!read) {
                /*boolean f = ConfigPathUtil.existFile();
                if(!f){
                    ToastUtils.show(getApplicationContext(),"未识别加密卡，请确定插入的卡类型为加密卡！");
                    return null;
                }*/
                /*SecuTFHelper helper = new SecuTFHelper();
                helper.loadCrt(getApplication());*/
                //请先初始化基本配置
                Toast.makeText(getBaseContext(), "读取证书信息出错，请先初始化基本配置！", Toast.LENGTH_LONG).show();
                return null;
            }
            String subject = sPreferences.getString("vpn.subject", "");
            String serialNumber = sPreferences.getString("vpn.serialNumber", "");
            String notBefore = sPreferences.getString("vpn.notBefore", "");
            String notAfter = sPreferences.getString("vpn.notAfter", "");
            String issue = sPreferences.getString("vpn.issue", "");
            String[] strs = new String[5];
            strs[0] = subject;
            strs[1] = serialNumber;
            strs[2] = notBefore;
            strs[3] = notAfter;
            strs[4] = issue;
            return strs;
        }
    };

    private boolean downloadCert(Context paramContext,
                                 String ip,
                                 int port,
                                 int poli_port,
                                 String pinCode,
                                 String container,
                                 boolean tcpudp,
                                 boolean lzo) {

        SharedPreferences sPreferences = getApplicationContext().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.putString("vpn.ip", ip);
        editor.putString("vpn.port", String.valueOf(port));
        editor.putString("vpn.pin", pinCode);
        editor.putBoolean("vpn.tcpUdp", tcpudp);
        editor.putBoolean("vpn.lzo", lzo);
        editor.putString("vpn.poliPort", String.valueOf(poli_port));
        editor.putString("vpn.certContainerName", container);
        editor.commit();

//        boolean read = sPreferences.getBoolean("vpn.read", false);

//        if (!read) {
            SecuTFHelper helper = new SecuTFHelper();
            helper.loadCrt(getApplication());
//        }
        String path = ConfigPathUtil.getSSLPath(getApplicationContext());
        if (path == null) {
            return false;
        } else {
            /*boolean flag = ConfigPathUtil.existFile();
            if(!flag){
                return false;
            }*/
            File sdDir = new File(ConfigPathUtil.getSSLPath(getApplicationContext()));
            if (sdDir.exists()) {
                String url = paramContext.getFilesDir().getAbsolutePath();
                File config_path = new File(url);
                if (!config_path.exists()) {
                    config_path.mkdirs();
//                        File config_cer_file = new File(url + FileDownloader.CONFIG_CRT);
//                        if (!config_cer_file.exists()) {
                    File cer = new File(ConfigPathUtil.getSSLPath(getApplicationContext()) + FileDownloader.CONFIG_CRT);
                    if (cer.exists())
                        try {
                            FileUtil.copy(cer, url + FileDownloader.CONFIG_CRT);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
//                        }

//                        File config_key_file = new File(url + FileDownloader.CONFIG_KEY);
//                        if (!config_key_file.exists()) {
                    File key = new File(ConfigPathUtil.getSSLPath(getApplicationContext()) + FileDownloader.CONFIG_KEY);
                    if (key.exists())
                        try {
                            FileUtil.copy(key, url + FileDownloader.CONFIG_KEY);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
//                        }

                } else {
//                        File config_cer_file = new File(url + FileDownloader.CONFIG_CRT);
//                        if (!config_cer_file.exists()) {
                    File cer = new File(ConfigPathUtil.getSSLPath(getApplicationContext()) + FileDownloader.CONFIG_CRT);
                    if (cer.exists())
                        try {
                            FileUtil.copy(cer, url + FileDownloader.CONFIG_CRT);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
//                        }

//                        File config_key_file = new File(url + FileDownloader.CONFIG_KEY);
//                        if (!config_key_file.exists()) {
                    File key = new File(ConfigPathUtil.getSSLPath(getApplicationContext()) + FileDownloader.CONFIG_KEY);
                    if (key.exists())
                        try {
                            FileUtil.copy(key, url + FileDownloader.CONFIG_KEY);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
//                        }
                }
            }
        }
        String save_path = paramContext.getFilesDir().getAbsolutePath();
        String down_static_key_url = "http://" + ip + ":" + poli_port + FileDownloader.DOWN_STATIC_KEY;
        String down_ca_url = "http://" + ip + ":" + poli_port + FileDownloader.DOWN_CA;
        String down_config_url = "http://" + ip + ":" + poli_port + FileDownloader.DOWN_CONFIG;
        String down_strategy_url = "http://" + ip + ":" + poli_port + FileDownloader.DOWN_STRATEGY;
        byte[] data = new byte[0];
        try {
            data = GetByte.getData(down_static_key_url);
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
        if (data != null)
            try {
                FileUtil.copy(data, save_path + FileDownloader.KEY_PATH);
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }

        try {
            data = GetByte.getData(down_ca_url);
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
        if (data != null)
            try {
                FileUtil.copy(data, save_path + FileDownloader.CA_PATH);
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }

        try {
            data = GetByte.getData(down_config_url);
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
        if (data != null)
            try {
                FileUtil.copy(data, save_path + FileDownloader.CONFIG_PATH);
            } catch (IOException e1) {
                e1.printStackTrace();
                return false;
            }

        File file = new File(save_path + FileDownloader.CONFIG_PATH);
        if (file.exists() && file.length() > 0) {
            try {
                byte[] data1 = GetByte.readInstream(new FileInputStream(file));
                ConfigFileUtil.update(ip, String.valueOf(port), pinCode, tcpudp, lzo, data1, save_path + FileDownloader.CONFIG_PATH);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            data = GetByte.getData(down_strategy_url);
            if (data != null)
                FileUtil.copy(data, save_path + FileDownloader.STRATEGY_PATH);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private UpdateMessage mMostRecentState;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallbacks.kill();
        unbindService(mConnection);
        VpnStatus.removeStateListener(this);
    }

    class UpdateMessage {
        public String state;
        public String logmessage;
        public ConnectionStatus level;
        public String vpnUUID;

        public UpdateMessage(String state, String logmessage, ConnectionStatus level) {
            this.state = state;
            this.logmessage = logmessage;
            this.level = level;
        }
    }

    @Override
    public void updateState(String state, String logmessage, int resid, ConnectionStatus level) {
        mMostRecentState = new UpdateMessage(state, logmessage, level);
        if (ProfileManager.getLastConnectedVpn() != null)
            mMostRecentState.vpnUUID = ProfileManager.getLastConnectedVpn().getUUIDString();

        Message msg = mHandler.obtainMessage(SEND_TOALL, mMostRecentState);
        msg.sendToTarget();

    }

    private static final OpenVPNServiceHandler mHandler = new OpenVPNServiceHandler();


    static class OpenVPNServiceHandler extends Handler {
        WeakReference<ExternalOpenVPNService> service = null;

        private void setService(ExternalOpenVPNService eos) {
            service = new WeakReference<ExternalOpenVPNService>(eos);
        }

        @Override
        public void handleMessage(Message msg) {

            RemoteCallbackList<IOpenVPNStatusCallback> callbacks;
            switch (msg.what) {
                case SEND_TOALL:
                    if (service == null || service.get() == null)
                        return;

                    callbacks = service.get().mCallbacks;


                    // Broadcast to all clients the new value.
                    final int N = callbacks.beginBroadcast();
                    for (int i = 0; i < N; i++) {
                        try {
                            sendUpdate(callbacks.getBroadcastItem(i), (UpdateMessage) msg.obj);
                        } catch (RemoteException e) {
                            // The RemoteCallbackList will take care of removing
                            // the dead object for us.
                        }
                    }
                    callbacks.finishBroadcast();
                    break;
            }
        }

        private void sendUpdate(IOpenVPNStatusCallback broadcastItem,
                                UpdateMessage um) throws RemoteException {
            broadcastItem.newStatus(um.vpnUUID, um.state, um.logmessage, um.level.name());
        }
    }
}