package com.zd.vpn.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class MobileInfo {
	
	public static String deviceId;
	
	public static String phoneNO;
	
	public static String serialNumber;

	public static String subscriberId;
	
	public static String userName;
	
	public static  String province;
	
	public static String city;
	
	public static String company;
	
	public static String department;
	
	public static String userid;
	
	public static boolean init = false;
	
	public MobileInfo(Context context){
		init = true;
		init(context);
		SharedPreferences sharedPreferences = context.getSharedPreferences("mt",context.MODE_WORLD_READABLE);
		phoneNO = sharedPreferences.getString("mt.phoneno", "");
		serialNumber = sharedPreferences.getString("mt.serialNumber", "");
		subscriberId = sharedPreferences.getString("mt.subscriberId", "");
		userName = sharedPreferences.getString("mt.userName", "");
		province = sharedPreferences.getString("mt.province","");
		city = sharedPreferences.getString("mt.city", "");
		company = sharedPreferences.getString("mt.company", "");
		department = sharedPreferences.getString("mt.department", "");
		userid = sharedPreferences.getString("mt.userid", "");
		deviceId = sharedPreferences.getString("mt.deviceId","");
	}
	public void init(Context context){
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		SharedPreferences sharedPreferences = context.getSharedPreferences("mt",context.MODE_WORLD_READABLE);
		if(tm.getLine1Number() != null && !tm.getLine1Number().toString().equals("")){
			sharedPreferences.edit().putString("mt.phoneno",tm.getLine1Number()).commit();
		}
		sharedPreferences.edit().putString("mt.subscriberId",tm.getSubscriberId()).commit();
		sharedPreferences.edit().putString("mt.deviceId",tm.getDeviceId()).commit();
	}
	public static String toJosn(){
		StringBuilder sb = new StringBuilder("{");
		sb.append("phone:'").append(phoneNO).append("',");
		sb.append("phonenetid:'").append(subscriberId).append("',");
		sb.append("serialnumber:'").append(serialNumber).append("',");
		sb.append("province:'").append(province).append("',");
		sb.append("city:'").append(city).append("',");
		sb.append("organization:'").append(company).append("',");
		sb.append("institutions:'").append(department).append("',");
		sb.append("userid:'").append(userid).append("',");
		sb.append("username:'").append(userName).append("',");
		sb.append("deviceid:'").append(deviceId).append("',");
		sb.append("}");
		return sb.toString();
	}
}
