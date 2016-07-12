package com.zd.vpn.util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;


public class GetByte {

    public static byte[] getData(String path) throws Exception {
        InputStream iStream = null;
        byte[] data = new byte[0];
        URL url = null;
        url = new URL(path);
//        System.out.println(path);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        httpURLConnection.setConnectTimeout(10 * 1000);
        if (httpURLConnection.getResponseCode() == 200) {
            iStream = httpURLConnection.getInputStream();
        }
        data = readInstream(iStream);
        return data;
    }

    public static byte[] download(String path) throws Exception {
        InputStream iStream = null;
        byte[] data = new byte[0];
//        System.out.println(path);
        HttpGet httpGet = new HttpGet(path);
//        System.out.println(1);
        HttpClient httpClient = new DefaultHttpClient();
//        System.out.println(2);
        HttpResponse response = httpClient.execute(httpGet);
//        System.out.println(3);
        data = readInstream(response.getEntity().getContent());
//        System.out.println(4);
        return data;
    }

    public static byte[] readInstream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int lengeh = -1;
        while ((lengeh = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, lengeh);
        }
        byteArrayOutputStream.close();
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }
}
