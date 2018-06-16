package com.agprince.android.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.List;

import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {
    private static final String TAG = "PollService";

    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    public static Intent newIntent(Context context){
        return new Intent(context,PollService.class);
    }

    public PollService(){
        super(TAG);
    }

    public static void setServiceAlarm(Context context,boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,0);

        AlarmManager  alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if(isOn){
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),POLL_INTERVAL_MS,pi);

        }else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }
    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return  pi !=null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if(!isNetworkAvailableAndConnected()){
            return;
        }
        LogUtil.d("receiver a intent : "+intent);

        String query = QueryPreferences.getStoredQuery(this);
        String lastResuleId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if(query == null){
            items = new FlickrFetchr().fetchRecentPhotos();
        }else {
            items  = new FlickrFetchr().searchPhotos(query);
        }

        String resultid = items.get(0).getId();
        if(resultid.equals(lastResuleId)){
            LogUtil.d("Got a old result : "+resultid);
        }else{
            LogUtil.d("Got a new result : "+ resultid);

            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this,0,i,0);

           Notification notification = new NotificationCompat.Builder(this)
                   .setTicker(resources.getString(R.string.new_pictures_title))
                   .setSmallIcon(android.R.drawable.ic_menu_report_image)
                   .setContentTitle(resources.getString(R.string.new_pictures_title))
                   .setContentText(resources.getString(R.string.new_pictures_text))
                   .setContentIntent(pi)
                   .setAutoCancel(true)
                   .build();

           NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
           notificationManagerCompat.notify(0,notification);


        }
        QueryPreferences.setLastResultId(this,resultid);

    }

    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() !=null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
