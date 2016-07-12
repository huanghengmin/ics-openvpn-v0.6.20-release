package com.zd.vpn.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.zd.vpn.R;
import com.zd.vpn.base.BaseActivity;
import com.zd.vpn.base.TitleBar;
import com.zd.vpn.util.FileDownloader;
import com.zd.vpn.util.TitlebarFactory;
import com.zd.vpn.util.FileDownloader.OnFileDownloadListener;
import com.zd.vpn.util.NetUtil;
import com.zd.vpn.util.ToastUtils;

public class BasicSettingActivity extends BaseActivity implements OnClickListener{
    private String poli_port;
    private String ip;

    private TextView mServerAddress = null;
    private TextView mServerPort = null;
    private CheckBox mUseLzo = null;
    private ToggleButton mTcpUdp = null;
    private EditText mKeyPassword = null;
    private EditText mPoliPort = null;
    private EditText mCertContainerName = null;

    private Button basicSettingsSubmitBut = null;

    private Button basicSettingsBackBut = null;
    private SharedPreferences shPreferences = null;

    @Override
    public void doOnCreate(Bundle savedInstanceState) {
        super.doOnCreate(savedInstanceState);
        mServerAddress = (TextView) this.findViewById(R.id.address);
        mServerPort = (TextView) this.findViewById(R.id.port);
        mUseLzo = (CheckBox) this.findViewById(R.id.lzo);
        mTcpUdp = (ToggleButton) this.findViewById(R.id.tcpudp);
        mKeyPassword = (EditText) this.findViewById(R.id.ping);
        mPoliPort = (EditText) this.findViewById(R.id.poli_port);
        mCertContainerName =  (EditText) this.findViewById(R.id.cert_container_name);

        this.basicSettingsBackBut = (Button) this
                .findViewById(R.id.basic_settings__back_but);
        this.basicSettingsSubmitBut = (Button) this
                .findViewById(R.id.basic_settings__submit_but);

        this.basicSettingsBackBut.setOnClickListener(this);
        this.basicSettingsSubmitBut.setOnClickListener(this);

        shPreferences = this.getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
//        mServerAddress.setText(shPreferences.getString("vpn.ip", "192.168.110.2"));
        mServerAddress.setText(shPreferences.getString("vpn.ip", ""));
//        mServerPort.setText(shPreferences.getString("vpn.port", "1194"));
        mServerPort.setText(shPreferences.getString("vpn.port", ""));
        mUseLzo.setChecked(shPreferences.getBoolean("vpn.lzo", true));
        mTcpUdp.setChecked(shPreferences.getBoolean("vpn.tcpUdp", false));
        mKeyPassword.setText(shPreferences.getString("vpn.pin", "111111"));
//        mPoliPort.setText(shPreferences.getString("vpn.poliPort", "80"));
        mPoliPort.setText(shPreferences.getString("vpn.poliPort", ""));
        mCertContainerName.setText(shPreferences.getString("vpn.certContainerName", "KingTrustVPN"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.basic_settings__back_but:
                this.finish();
                break;
            case R.id.basic_settings__submit_but:
                // 判断网络
                if (!NetUtil.isNetworkConnected(this)) {
                    Toast.makeText(this, "请先设置网络连接！", Toast.LENGTH_LONG).show();
//                    Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");
                    Intent intent =  new Intent(Settings.ACTION_SETTINGS);
                    startActivity(intent);
                    return;
                }
                String serverAddress = mServerAddress.getText().toString();
                String serverPort = mServerPort.getText().toString();
                String pinCode = mKeyPassword.getText().toString();
                boolean isLZOChecked = mUseLzo.isChecked();
                String poliPort = mPoliPort.getText().toString();
                String certContainerName =mCertContainerName.getText().toString();
                if (serverAddress == null || serverAddress.isEmpty()|| serverPort == null || serverPort.isEmpty()|| pinCode == null || pinCode.isEmpty()|| certContainerName == null || certContainerName.isEmpty()) {
                    Toast.makeText(this, R.string.info_incomplete_text,Toast.LENGTH_SHORT).show();
                } else if (!isLZOChecked) {
                    Toast.makeText(this, R.string.check_text, Toast.LENGTH_SHORT).show();
                } else {
                    // 保存参数
                    ip = serverAddress;
                    poli_port = poliPort;

                    savePreferences();

                    // 下载证书
                    downloadCert();

                    // Toast.makeText(this, "完成更改", Toast.LENGTH_SHORT).show();
                    // this.finish();
                }
                break;
            default:
                break;
        }
    }


    private void savePreferences() {
        /*File file = new File(paramContext.getFilesDir().getAbsolutePath() + FileDownloader.CONFIG_PATH);
        if(file.exists()&&file.length()>0){
            try {
                byte[] data = GetByte.readInstream(new FileInputStream(file));
                ConfigFileUtil.update(ip,
                        mServerPort.getText().toString(),
                        mKeyPassword.getText().toString(),
                        mTcpUdp.isChecked(),
                        mUseLzo.isChecked(),
                        data,
                        paramContext.getFilesDir().getAbsolutePath() + FileDownloader.CONFIG_PATH);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

        Editor editor = shPreferences.edit();
        editor.putString("vpn.ip", mServerAddress.getText().toString());
        editor.putString("vpn.port", mServerPort.getText().toString());
        editor.putString("vpn.pin", mKeyPassword.getText().toString());
        editor.putBoolean("vpn.tcpUdp", mTcpUdp.isChecked());
        editor.putBoolean("vpn.lzo", mUseLzo.isChecked());
        editor.putString("vpn.poliPort", mPoliPort.getText().toString());
        editor.putString("vpn.certContainerName", mCertContainerName.getText().toString());
        editor.commit();

    }

    private void downloadCert() {
//        File ovpn = new File(ConfigPathUtil.getConfigPath() + CONFIG_PATH);
//        File key = new File(ConfigPathUtil.getConfigPath() + KEY_PATH);
//        File ca = new File(ConfigPathUtil.getConfigPath() + CA_PATH);
//        if (key.exists()) {
//            key.deleteOnExit();
//        }
//        if (ca.exists()) {
//            ca.deleteOnExit();
//        }
        // 下载证书文件
        showProgressDialog("正在下载文件，请稍候...");
        FileDownloader mFileDownloader = new FileDownloader();
        mFileDownloader.downLoadFile(getApplicationContext(),
                ip,
                poli_port,
                mServerPort.getText().toString(),
                mKeyPassword.getText().toString(),
                mTcpUdp.isChecked(),
                mUseLzo.isChecked(),
                new OnFileDownloadListener() {

                    @Override
                    public void onFileDownloadOk(String msg) {
                        hideProgressDialog();
                        ToastUtils.show(BasicSettingActivity.this, msg);
                        BasicSettingActivity.this.finish();
                    }

                    @Override
                    public void onFileDownloadErr(String msg) {
                        hideProgressDialog();
                        ToastUtils.show(BasicSettingActivity.this, msg);
                    }
                });
        // } else {
        // ToastUtils.show(BasicSettingActivity.this, "配置参数保存成功！");
        // BasicSettingActivity.this.finish();
        // }
    }

    private ProgressDialog mDialog;// 网络加载

    public void showProgressDialog(String msg) {
        // 如果已经存在进度条并且该进度条处于显示状态，将取消该进度条的显示。
        if (mDialog != null && mDialog.isShowing()) {
            hideProgressDialog();
        }

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(msg);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }


    public void showProgressMsg(String msg) {
        // 如果已经存在进度条并且该进度条处于显示状态，将取消该进度条的显示。
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.setMessage(msg);
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.show();
        }
    }


    public void hideProgressDialog() {
        try {
            mDialog.dismiss();
            mDialog = null;
        } catch (Exception e) {
        }
    }

    @Override
    public int rootViewRes() {
        return R.layout.basic_settings;
    }

    @Override
    public TitleBar initTitlebar() {
        TitleBar titleBar = TitlebarFactory.createCustomBackTitlebar(this, "基本设置");
        return titleBar;
    }

}
