package com.zd.vpn.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UploadFile {
	
	public void uploadFile(String actionUrl,String newName,File uploadFile,String serialnumber,String type){
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try{
        	URL url =new URL(actionUrl);
        	HttpURLConnection conn =(HttpURLConnection)url.openConnection();
        	
        	conn = (HttpURLConnection)url.openConnection();  
            conn.setReadTimeout(10000); 
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");  
            conn.setRequestProperty("Charsert", "UTF-8");   
            conn.setRequestProperty("content-type", "text/html");  
            conn.setRequestProperty("serialnumber", serialnumber);
            conn.setRequestProperty("type", type);
            conn.setRequestProperty("user",urlEncoder(MobileInfo.toJosn()));
            conn.setRequestProperty("code", CodeUtil.UPDATE_VIEW);
            OutputStream osw = conn.getOutputStream();  
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(uploadFile));  
            int BUFFER_SIZE = 1024;   
            byte[] buf = new byte[BUFFER_SIZE];      
            int size = 0;      
            try {  
            	while ((size = bis.read(buf)) != -1)       
            		osw.write(buf, 0, size);  
            } catch (IOException e) {  
            	e.printStackTrace();  
            }      
            finally {  
            	try {  
            		bis.close();  
            		osw.close();  
            	} catch (IOException e) {  
                   // TODO Auto-generated catch block  
                   e.printStackTrace();  
            	}  
            }   
               
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)  
                System.out.println( "connect failed!");  
        } catch (IOException e) {  
        	e.printStackTrace();  
        }  
	}
	private String urlEncoder(String data){
		return java.net.URLEncoder.encode(data);
	}
}
