package com.zd.vpn.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpClientUtil {
	private String code;
	private String url;
	private String data;
	public HttpClientUtil(String url,String code,String data){
		this.code = code ;
		this.url = url ;
		this.data = data ;
	}
	public HttpResponse execute() throws Exception{
		HttpGet httpGet = new HttpGet(url);
		HttpParams params = new BasicHttpParams(); 
        HttpConnectionParams.setConnectionTimeout(params, 5000); //�������
        HttpConnectionParams.setSoTimeout(params, 10000); //�������
        httpGet.setParams(params);
		httpGet.addHeader("code",code);
		httpGet.addHeader("user", urlEncoder(MobileInfo.toJosn()));
		httpGet.addHeader("data", urlEncoder(data));
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(httpGet);
		return response;
	}
	
	private String urlEncoder(String data){
		if(data == null || data.equals("")){
			return "";
		}
		String encrypt = "";
		try {
			encrypt = URLEncoder.encode(data, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encrypt;
	}
}
