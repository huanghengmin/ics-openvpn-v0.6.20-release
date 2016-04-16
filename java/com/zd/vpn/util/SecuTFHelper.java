package com.zd.vpn.util;

//import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileInputStream;
//import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

//import com.third.SecuTFjni.SecuTFjni;


public class SecuTFHelper {
//    SecuTFjni tfJni = new SecuTFjni();

   /*public boolean checkCrt(X509Certificate x509certificate){
       File file = new File(ConfigPathUtil.getSSLPath() + FileDownloader.CONFIG_CRT);
       if (file.exists()) {
           try {
               CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
               FileInputStream f = new FileInputStream(file);
               X509Certificate cert = (X509Certificate) certificatefactory.generateCertificate(f);
               return cert.equals(x509certificate);
           }catch (Exception e){
             return true;
           }
       }
       return false;
   }*/

    public static String find_serial(Context context) {
        SharedPreferences sPreferences = context.getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
        File file = new File(ConfigPathUtil.getSSLPath() + FileDownloader.CONFIG_CRT);
        if (file.exists()) {
            try {
                CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
                FileInputStream f = new FileInputStream(file);
                X509Certificate x509certificate = (X509Certificate) certificatefactory.generateCertificate(f);
                String serialNum = x509certificate.getSerialNumber().toString(16).toUpperCase();
                Editor editor = sPreferences.edit();
                editor.putString("vpn.serialNumber", serialNum);
                editor.commit();
                return serialNum;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    public void loadCrt(final Context context) {
        SharedPreferences sPreferences = context.getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
        boolean read = sPreferences.getBoolean("vpn.read", false);
//        if(!read) {
        String pin = sPreferences.getString("vpn.pin", "");
        String strContainer = sPreferences.getString("vpn.certContainerName", "");
        final Sender sender = new Sender(context);
        if (pin.equals("")) {
            sender.sendTF("PIN码未设置,请先配置PIN码!");
            return;
        }
        if (strContainer.equals("")) {
            sender.sendTF("证书容器未设置,请先配置证书容器!");
            return;
        }
        File file = new File(ConfigPathUtil.getSSLPath() + FileDownloader.CONFIG_CRT);
        if (file.exists()) {
            try {
                CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
                FileInputStream f = new FileInputStream(file);
                X509Certificate x509certificate = (X509Certificate) certificatefactory.generateCertificate(f);
                String serialNum = x509certificate.getSerialNumber().toString(16).toUpperCase();
                Editor editor = sPreferences.edit();
                editor.putString("vpn.serialNumber", serialNum);
                editor.putBoolean("vpn.read", true);
                editor.putString("vpn.subject", x509certificate.getSubjectDN().toString());
                editor.putString("vpn.notBefore", x509certificate.getNotBefore().toString());
                editor.putString("vpn.notAfter", x509certificate.getNotAfter().toString());
                editor.putString("vpn.issue", x509certificate.getIssuerDN().toString());
                editor.commit();
            } catch (Exception e) {
                sender.sendTF("读取证书信息失败!");
            }
        }
//        }
    }

   /* public void loadCrt(final Context context) {
        SharedPreferences sPreferences = context.getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
        boolean read = sPreferences.getBoolean("vpn.read", false);
        if(!read) {
            String pin = sPreferences.getString("vpn.pin", "");
            String strContainer = sPreferences.getString("vpn.certContainerName", "");
            final Sender sender = new Sender(context);
            if (pin.equals("")) {
                sender.sendTF("PIN码未设置,请先配置PIN码!");
                return;
            }
            if (strContainer.equals("")) {
                sender.sendTF("证书容器未设置,请先配置证书容器!");
                return;
            }

            int iRet;

            iRet = tfJni.JvOpenDevice();


            byte[] bHwVer = new byte[32];
            byte[] bSwVer = new byte[32];
            iRet = tfJni.JvGetVersion(bHwVer, bSwVer);
            String bHwVer_V  = new String(bHwVer).trim();
            String bSwVer_V  = new String(bSwVer).trim();

            iRet = tfJni.JvSetActiveContainer(strContainer);


            byte[] password = pin.getBytes();
            iRet = tfJni.JvLogin(password, 6);


            byte[] sn = new byte[12];
            long[] snLen = new long[1];
            iRet = tfJni.JvReadSerialNum(sn, snLen);
            String device_sn  = new String(sn).trim();

          *//*  long[] CertLen = new long[1];
            byte[] Cert = null;
            int type = SecuTFjni.AT_KEYEXCHANGE;
            iRet = tfJni.JvReadCertificate(null, CertLen, type);
            if (iRet == -536817653) {
                type = SecuTFjni.AT_SIGNATURE;
                iRet = tfJni.JvReadCertificate(null, CertLen, type);
                Cert = new byte[(int) CertLen[0]];
                iRet = tfJni.JvReadCertificate(Cert, CertLen, type);
            } else {
                Cert = new byte[(int) CertLen[0]];
                iRet = tfJni.JvReadCertificate(Cert, CertLen, type);
            }

            CertificateFactory certificate_factory = null;
            try {
                certificate_factory = CertificateFactory.getInstance("X.509");
                X509Certificate x509certificate = (X509Certificate) certificate_factory.generateCertificate(new ByteArrayInputStream(Cert));
                String serialNum = x509certificate.getSerialNumber().toString(16).toUpperCase();
                boolean flag = checkCrt(x509certificate);
                if (!flag) {
                    sender.sendTF("读取证书信息不匹配,禁止拨号!");
                    return;
                }*//*
            File file = new File(ConfigPathUtil.getSSLPath() + FileDownloader.CONFIG_CRT);
            if (file.exists()) {
                try {
                    CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
                    FileInputStream f = new FileInputStream(file);
                    X509Certificate x509certificate = (X509Certificate) certificatefactory.generateCertificate(f);
                    String serialNum = x509certificate.getSerialNumber().toString(16).toUpperCase();
                    Editor editor = sPreferences.edit();
                    editor.putString("vpn.serialNumber", serialNum);
                    editor.putBoolean("vpn.read", true);
                    editor.putString("vpn.device_sn",device_sn);
                    editor.putString("vpn.subject", x509certificate.getSubjectDN().toString());
                    editor.putString("vpn.notBefore", x509certificate.getNotBefore().toString());
                    editor.putString("vpn.notAfter", x509certificate.getNotAfter().toString());
                    editor.putString("vpn.issue", x509certificate.getIssuerDN().toString());
                    editor.commit();
                } catch (Exception e) {
                    sender.sendTF("读取证书信息失败!");
                }
            }

            iRet = tfJni.JvLogout();
            iRet = tfJni.JvCloseDevice();
        }
    }*/

   /* public void proCrt(String crtDNInfo) {
        String[] strings = crtDNInfo.split(",");
        for (int i = 0; i < strings.length; i++) {
            String key = strings[i].substring(0, strings[i].indexOf("="));
            String value = strings[i].substring(strings[i].indexOf("=") + 1);
            hashMap.put(key, value);
        }
    }*/

    /*public void setData(X509Certificate x509Certificate) {
        sPreferences = context.getSharedPreferences("com.zd.vpn", Context.MODE_PRIVATE);
        Editor editor = sPreferences.edit();
        editor.putString("vpn.serialNumber", SerialNum);
        editor.putBoolean("vpn.read", true);
        if (hashMap.get("CN").contains(" ")) {
            editor.putString("vpn.userName", hashMap.get("CN").substring(0, hashMap.get("CN").indexOf(" ")));
        } else {
            editor.putString("vpn.userName", hashMap.get("CN"));
        }
        editor.putString("vpn.cn", hashMap.get("CN"));
        editor.putString("vpn.province", hashMap.get("ST"));
        editor.putString("vpn.city", hashMap.get("L"));
        editor.putString("vpn.company", hashMap.get("O"));
        editor.putString("vpn.department", hashMap.get("OU"));
        if (hashMap.get("CN").contains(" ")) {
            editor.putString("vpn.userid", hashMap.get("CN").substring(hashMap.get("CN").indexOf(" ") + 1));
        }
        editor.commit();
    }*/
}
