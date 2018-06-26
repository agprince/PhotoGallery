package com.agprince.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

public abstract class  VisibleFragment extends Fragment {
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mShowNotification,intentFilter,PollService.PERM_PRIVATE,null);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mShowNotification);
    }

    private BroadcastReceiver mShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,"Got a broadcast : "+intent.getAction(),Toast.LENGTH_SHORT).show();
            LogUtil.i("canceling notification");
            setResultCode(Activity.RESULT_CANCELED);

        }
    };
}
