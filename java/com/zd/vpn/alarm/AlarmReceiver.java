package com.zd.vpn.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
//import android.net.ConnectivityManager;
//import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;

import java.lang.reflect.Method;

public class AlarmReceiver extends WakefulBroadcastReceiver {

    public static final String TAG = "Alarm Receiver";

    private AlarmManager alarmMgr;

//    private PowerManager.WakeLock mWakeLock;

    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent service = new Intent(context, AlarmSchedulingService.class);

        startWakefulService(context, service);
    }

//    public void set_screen_off_timeout(Context context) {
//        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, -1);
//    }

   /* public void releaseWakeLock() {
        if (null != mWakeLock && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    public void acquireWakeLock(Context context) {
        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
| PowerManager.ON_AFTER_RELEASE
, TAG);
            if (null != mWakeLock) {
                mWakeLock.acquire();
            }
        }
    }*/

    public void setAlarm(Context context) {
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        long triggerAtTime = SystemClock.elapsedRealtime();
        //首次运行在5分钟以后
        triggerAtTime += 5 * 60 * 1000;
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, 5 * 60 * 1000, alarmIntent);
//        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
//        ComponentName receiver = new ComponentName(context, OnBootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//        pm.setComponentEnabledSetting(receiver,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm() {

        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
//        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
//        ComponentName receiver = new ComponentName(context, OnBootReceiver.class);
//        PackageManager pm = context.getPackageManager();
//        pm.setComponentEnabledSetting(receiver,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
    }
}
