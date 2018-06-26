package com.agprince.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private NotificationReceiver mReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
       LogUtil.i("get result code "+getResultCode());
        if(getResultCode()!= Activity.RESULT_OK){
            return;
        }

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE,0);
        Notification notification = intent.getParcelableExtra(PollService.NOTIFICATION);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(requestCode,notification);

        mReceiver.goAsync();


    }

}
