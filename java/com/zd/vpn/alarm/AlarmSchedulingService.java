package com.zd.vpn.alarm;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.zd.vpn.R;
import com.zd.vpn.ui.MoreActivity;
import com.zd.vpn.util.ConfigFileUtil;
import com.zd.vpn.util.FileDownloader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AlarmSchedulingService extends IntentService {

    public AlarmSchedulingService() {
        super("SchedulingService");
    }

    public static final String TAG = "Scheduling Service";

    public static final int NOTIFICATION_ID = 1;

    private NotificationManager mNotificationManager;

    private String readIp(Context context) {
        String save_path = context.getFilesDir().getAbsolutePath();
        if(save_path!=null) {
            File config_file = new File(save_path+ FileDownloader.CONFIG_PATH);
            if (config_file.exists()) {
                try {
                    byte[] bytes = toByteArray(config_file);
                    String ip = ConfigFileUtil.getIp(bytes);
                    return ip;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private byte[] toByteArray(File f) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bos.close();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String ip = readIp(getApplicationContext());
        if (ip != null) {
        String result = PingUtils.ping(ip);
            if (result != null) {
                sendNotification(result);
            } else {
                sendNotification("ping gateway result msg is null");
            }

            PingUtils.ping(ip);
        }
        AlarmReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MoreActivity.class), 0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.ping_result))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
