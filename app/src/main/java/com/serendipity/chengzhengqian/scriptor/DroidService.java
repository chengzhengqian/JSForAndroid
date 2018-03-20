package com.serendipity.chengzhengqian.scriptor;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;

public class DroidService extends Service {
    private int ONGOING_NOTIFICATION_ID=1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private static final int WEBSERVER_PORT = 10000;
    ScriptorServer server;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            server=new ScriptorServer(WEBSERVER_PORT);
            server.start();
            //Toast.makeText(this, "Server Start", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =

                new Notification.Builder(this)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText("localhost:"+String.valueOf(WEBSERVER_PORT))
                        .setSmallIcon(R.drawable.icon)
                        .setContentIntent(pendingIntent)
                        .setTicker(getText(R.string.ticker_text))
                        .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
        return START_STICKY;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        server.stop();
        Toast.makeText(this, "Server Destroyed", Toast.LENGTH_LONG).show();
    }

}
