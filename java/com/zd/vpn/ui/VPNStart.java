package com.zd.vpn.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.zd.vpn.util.SecuTFHelper;
import com.zd.vpn.util.Sender;
import com.zd.vpn.util.StrategyUtil;
//import com.zd.vpn.util.TFInfo;

import com.zd.vpn.LaunchVPN;
import com.zd.vpn.VpnProfile;
import com.zd.vpn.activities.ConfigConverter;
import com.zd.vpn.activities.FileSelect;
import com.zd.vpn.core.ProfileManager;
import com.zd.vpn.util.ConfigUtil;
import com.zd.vpn.util.TelInfoUtil;
import com.zd.vpn.util.ThreeYardsPost;

public class VPNStart extends Fragment {

    final static int RESULT_VPN_DELETED = Activity.RESULT_FIRST_USER;

    private static final int START_VPN_CONFIG = 92;
    private static final int SELECT_PROFILE = 43;
    private static final int IMPORT_PROFILE = 231;

    private ArrayAdapter<VpnProfile> mArrayadapter;

    protected VpnProfile mEditProfile = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //先校验三码是否合一
        //加载配置文件
//        StrategyUtil strategyUtil = new StrategyUtil();
//        if(strategyUtil.getThreeyards()) {
        //访问网络接口进行校验
        SharedPreferences shPreferences = this.getActivity().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
        boolean read = shPreferences.getBoolean("vpn.read", false);
        String serialnumber = shPreferences.getString("vpn.serialNumber", "");
//        String serialnumber = SecuTFHelper.find_serial(getActivity());
        String ip = shPreferences.getString("vpn.ip", "");
        String port = shPreferences.getString("vpn.poliPort", "");
        TelInfoUtil telInfo = new TelInfoUtil(this.getActivity());
//            Log.i("vpn","imei:"+telInfo.getImei()+"|sim:"+telInfo.getSim());
//            Log.i("vpn","tfcardid:"+TFInfo.CardID);
//            Log.i("vpn","serialNumber:"+serialnumber);
        if (read) {
            ThreeYardsPost tPost = new ThreeYardsPost(this.getActivity(), ip, port, telInfo.getImei(), telInfo.getSim(), serialnumber);
            tPost.postData(new ThreeYardsPost.OnThreeYardsPostListener() {
                @Override
                public void onThreeYardsPostOk(String msg) {
                    Log.v("vpn", "ok");
                    Sender sender = new Sender(getActivity());
                    sender.sendTF(msg);
                    startVPN(getActivity());
                }

                @Override
                public void onThreeYardsPostErr(String msg) {
                    Log.v("vpn", "err");
                    Sender sender = new Sender(getActivity());
                    sender.sendTF(msg);
                }
            });

        }
//        }

    }

    private ProfileManager getPM() {
        return ProfileManager.getInstance(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_VPN_DELETED) {
            if (mArrayadapter != null && mEditProfile != null)
                mArrayadapter.remove(mEditProfile);
        }

        if (resultCode != Activity.RESULT_OK)
            return;

        if (requestCode == START_VPN_CONFIG) {
            String configuredVPN = data
                    .getStringExtra(VpnProfile.EXTRA_PROFILEUUID);

            VpnProfile profile = ProfileManager.get(this.getActivity(), configuredVPN);
            getPM().saveProfile(getActivity(), profile);
            // Name could be modified, reset List adapter
            // setListAdapter();

        } else if (requestCode == SELECT_PROFILE) {
            String filedata = data.getStringExtra(FileSelect.RESULT_DATA);
            Intent startImport = new Intent(getActivity(),
                    ConfigConverter.class);
            startImport.setAction(ConfigConverter.IMPORT_PROFILE);
            Uri uri = new Uri.Builder().path(filedata).scheme("file").build();
            startImport.setData(uri);
            startActivityForResult(startImport, IMPORT_PROFILE);
        } else if (requestCode == IMPORT_PROFILE) {
            String profileUUID = data
                    .getStringExtra(VpnProfile.EXTRA_PROFILEUUID);
            mArrayadapter.add(ProfileManager.get(this.getActivity(), profileUUID));
        }

    }

    /*
     * private void startVPN(VpnProfile profile) {
     *
     * getPM().saveProfile(getActivity(), profile);
     *
     * Intent intent = new Intent(getActivity(),LaunchVPN.class);
     * intent.putExtra(LaunchVPN.EXTRA_KEY, profile.getUUID().toString());
     * intent.setAction(Intent.ACTION_MAIN); startActivity(intent);
     *
     * getActivity().finish(); }
     */
    private void startVPN(Context paramContext) {
        ConfigUtil configUtil = new ConfigUtil(getActivity());
        String uuid = "";
//        if (configUtil.getUuid() == null) {
            uuid = configUtil.config(paramContext);
       /* } else {
            uuid = configUtil.getUuid();
        }*/
        Log.i("vpn", "uuid" + uuid);
//        System.out.println("uuid" + uuid);
        SharedPreferences shPreferences = this.getActivity().getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
        boolean read = shPreferences.getBoolean("vpn.read", false);
//        boolean tf_ok = TFInfo.TFSTATE;
//        Log.i("vpn", "tf_ok" + tf_ok);
        if (read) {
            Intent intent = new Intent(getActivity(), LaunchVPN.class);
            intent.putExtra(LaunchVPN.EXTRA_KEY, uuid);
            intent.setAction(Intent.ACTION_MAIN);
            startActivity(intent);
        } else {
            Sender sender = new Sender(getActivity());
            sender.sendTF("TF卡驱动加载失败请重试插拔TF卡或pin码有误请检查pin码设置");
        }
        getActivity().finish();
    }
}
