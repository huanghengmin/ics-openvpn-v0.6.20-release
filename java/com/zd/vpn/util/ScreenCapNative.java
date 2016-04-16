package com.zd.vpn.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class ScreenCapNative {
	
	static {
    	System.loadLibrary("screenshot");
	};
   
	public native int nativeCaptureScreen();
	
	// String apkRoot="chmod 777 "+getPackageCodePath(); 
	public static boolean RootCommand(String command){         
		Process process = null;
		DataOutputStream os = null;
		try{
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(command + "\n");
			os.writeBytes("exit\n");
			os.flush();
			int result = process.waitFor();
		}
		catch (Exception e){             
			Log.d("*** DEBUG ***", "ROOT REE" + e.getMessage());
			return false;
		} 
		return true;
	} 
	public boolean bmp2jpg(Context paramContext,String bmpPath){
        File file = new File(paramContext.getFilesDir().getAbsolutePath()+"/screenshot.jpg");
        try {
        	if(file.exists() == false){
        		File file2 = new File(file.getParent());
        		if(!file2.exists()){
        			file2.mkdirs();
        		}
        		file.createNewFile();
        	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}   
        OutputStream out;
		try {
			out = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}   
        InputStream stream;
		try {
			stream = new FileInputStream(new File(bmpPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}   
        BitmapFactory.Options option = new BitmapFactory.Options();   
        option.inSampleSize = 1;   
        Bitmap bm = BitmapFactory.decodeStream(stream,null,option);   
        bm.compress(Bitmap.CompressFormat.JPEG,80,out);    
        try {
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}   
        return true;
    }  	
}
