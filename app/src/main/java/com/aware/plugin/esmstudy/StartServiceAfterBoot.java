package com.aware.plugin.esmstudy;

/**
 * Created by pratyush on 05/12/14.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;


public class StartServiceAfterBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            final Intent serviceIntent = new Intent(context, Plugin.class);
            context.startService(serviceIntent);
            Toast.makeText(context, "Booted", Toast.LENGTH_LONG).show();
        }
    }
}