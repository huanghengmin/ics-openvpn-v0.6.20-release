package com.zd.vpn.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class ScreenShot {
	private final static String PATH = "/sdcard/Android/data/com.hzcx.tc/pic";
    public boolean acquireScreenshot(Context mContext) {  
        DisplayMetrics metrics = new DisplayMetrics();  
        WindowManager WM = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);  
        Display display = WM.getDefaultDisplay();  
        display.getMetrics(metrics);  
        int height = metrics.heightPixels;
        int width = metrics.widthPixels; 
        int pixelformat = display.getPixelFormat();  
        PixelFormat localPixelFormat1 = new PixelFormat();  
        PixelFormat.getPixelFormatInfo(pixelformat, localPixelFormat1);  
        int deepth = localPixelFormat1.bytesPerPixel;   
      
        byte[] arrayOfByte = new byte[height* width* deepth];  
        long tmp = System.currentTimeMillis();  
        try {  
            InputStream localInputStream = readAsRoot(new File(  
                    "/dev/graphics/fb0"));  
            DataInputStream localDataInputStream = new DataInputStream(  
                    localInputStream);  
            android.util.Log.e("mytest", "-----read start-------");  
            localDataInputStream.readFully(arrayOfByte);  
            android.util.Log.e("mytest", "-----read end-------time = "  + (System.currentTimeMillis() -tmp ));  
            localInputStream.close();  
            File file = new File(PATH+"/screenshot.jpg");
            File file2 = new File(file.getParent());
            if(!file2.exists()){
            	file2.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(file);  
            int[] tmpColor = new int[width * height];  
            int r, g, b;  
            tmp = System.currentTimeMillis();  
            android.util.Log.e("mytest", "-----bitmap start-------");  
            for (int j = 0; j < width * height * deepth; j+=deepth) {  
                    r = arrayOfByte[j]&0xff;  
                    g = arrayOfByte[j+1]&0xff;  
                    b = arrayOfByte[j+2]&0xff;  
                    tmpColor[j/deepth] = (r << 16) | (g << 8) | b |(0xff000000);  
            }  
            Bitmap tmpMap = Bitmap.createBitmap(tmpColor, width, height,  
                    Bitmap.Config.ARGB_8888);  
            android.util.Log.e("mytest", "-----bitmap end-------time = "  + (System.currentTimeMillis() -tmp ));  
      
            tmp = System.currentTimeMillis();  
            android.util.Log.e("mytest", "-----compress start-------");  
            tmpMap.compress(Bitmap.CompressFormat.JPEG, 80, out);  
            android.util.Log.e("mytest", "-----compress end-------time = "  + (System.currentTimeMillis() -tmp ));  
            out.close();  
      
        } catch (Exception e) {  
            android.util.Log.e("mytest", "Exception");  
            e.printStackTrace();  
            return false;
        }  
        return true;
    }  
      
    public static InputStream readAsRoot(File paramFile) throws Exception {  
        Process localProcess = Runtime.getRuntime().exec("su");  
        String str = "cat " + paramFile.getAbsolutePath() + "\n";  
        localProcess.getOutputStream().write(str.getBytes());  
        return localProcess.getInputStream();  
    }  

}