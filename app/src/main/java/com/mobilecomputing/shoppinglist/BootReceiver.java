package com.mobilecomputing.shoppinglist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by rolithunderbird on 17.06.16.
 */
public class BootReceiver extends BroadcastReceiver {

    AlarmReceiver alarm = new AlarmReceiver();


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }
}