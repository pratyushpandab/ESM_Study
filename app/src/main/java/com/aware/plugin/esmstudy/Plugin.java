package com.aware.plugin.esmstudy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Random;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.ESM;
import com.aware.plugin.esmstudy.R.drawable;

import com.aware.utils.Aware_Plugin;


public class Plugin extends Aware_Plugin {

    public static AlarmManager alarmManager;
    public PendingIntent pendingIntent;

    public static Calendar sCalendar;
    public static Calendar eCalendar;
    public static Calendar calendar;
    public static SharedPreferences preferences;
    public static SharedPreferences.Editor editor;
    public static int counter = 1;
    public static int alarm_counter = 0;
    public static int condition_counter = 0;
    private static final ESMStatusListener esm_statuses = new ESMStatusListener();
    public ArrayList<Long> alarm_time = new ArrayList<Long>();
    public ArrayList<PendingIntent> intentArray;

    // conditions of the study
    public static String C1 = "fixed-interval + 5 times";
    public static String C2 = "fixed-interval + 10 times";
    public static String C3 = "fixed-interval + 15 times";
    public static String C4 = "random-interval + 5 times";
    public static String C5 = "random-interval + 10 times";
    public static String C6 = "random-interval + 15 times";
    public static String CONDITION = null;

    public static final int START_YEAR = 2015;
    public static final int START_MONTH = Calendar.JANUARY;
    public static final int START_DAY = 12; // day of the month
    public static final int START_HOUR = 9; // the ESM period is 9:00 am to 9:00 pm (12 hours)
    public static final int START_MINUTE = 0;

    public static final int END_YEAR = 2015;
    public static final int END_MONTH = Calendar.MARCH;
    public static final int END_DAY = 11;
    public static final int END_HOUR = 21;
    public static final int END_MINUTE = 0;

    public static int NO_OF_DAYS;

    public static int SAMPLING_RATE = 0; // number of ESM questionnaires per day

    public static int INTERVAL; // for scheduling
    public static int DIFF; // for scheduling
    public static int MIN_TIME = 30; // minimum time between 2 random alerts is 30 minutes

    public static FileWriter fw;
    public static BufferedWriter bw;
    public ArrayList<String> fcontent = new ArrayList<String>();
    private static String TAG = "PilotESMstudy";

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(getBaseContext(), "ESMstudy Started", Toast.LENGTH_LONG).show();
        System.out.println("ESM study started.....");
        //Set AWARE settings
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ESM, true);
        //Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_ESM, true);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.DEBUG_FLAG, true);

//        Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, true);
        //Aware.setSetting(getContentResolver(), Aware_Preferences.DEBUG_FLAG, true);

        // Register BroadcastReceiver
        IntentFilter esm_filter = new IntentFilter();
        esm_filter.addAction(ESM.ACTION_AWARE_ESM_QUEUE_COMPLETE);
        esm_filter.addAction(ESM.ACTION_AWARE_ESM_DISMISSED);
        esm_filter.addAction(ESM.ACTION_AWARE_ESM_EXPIRED);
        registerReceiver(esm_statuses, esm_filter);
        Log.d(TAG, "activate ESM");
        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));

        preferences = this.getApplicationContext().getSharedPreferences("ESMpreferences", Context.MODE_PRIVATE);
        boolean alarm_set = preferences.getBoolean("ALARM_SET", false);
        if (!alarm_set) { // need to setup alarms
            editor = preferences.edit();
            editor.putBoolean("ALARM_SET", true);
            editor.putString("TAG", TAG);

            sCalendar = Calendar.getInstance();
            sCalendar.set(Calendar.YEAR, START_YEAR);
            sCalendar.set(Calendar.MONTH, START_MONTH);
            sCalendar.set(Calendar.DAY_OF_MONTH, START_DAY);
            sCalendar.set(Calendar.HOUR_OF_DAY, START_HOUR);
            sCalendar.set(Calendar.MINUTE, START_MINUTE);

            eCalendar = Calendar.getInstance();
            eCalendar.set(Calendar.YEAR, END_YEAR);
            eCalendar.set(Calendar.MONTH, END_MONTH);
            eCalendar.set(Calendar.DAY_OF_MONTH, END_DAY);
            eCalendar.set(Calendar.HOUR_OF_DAY, END_HOUR);
            eCalendar.set(Calendar.MINUTE, END_MINUTE);

            long diff = eCalendar.getTimeInMillis() - sCalendar.getTimeInMillis();
            NO_OF_DAYS = (int) (diff / (24 * 60 * 60 * 1000)) + 1;
            System.out.println("No of Days :: " + NO_OF_DAYS);
            long increament = 24 * 60 * 60 * 1000;



            Random random = new Random();
            for (int n = 0; n < NO_OF_DAYS; n++) {
                long timeInMillis = sCalendar.getTimeInMillis() + n*increament;

                calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timeInMillis);

                int random_number = random.nextInt(6); // choose a random condition for the day

                switch (random_number) {
                    case 0: CONDITION = C1; SAMPLING_RATE = 5;
                        break;
                    case 1: CONDITION = C2; SAMPLING_RATE = 10;
                        break;
                    case 2: CONDITION = C3; SAMPLING_RATE = 15;
                        break;
                    case 3: CONDITION = C4; SAMPLING_RATE = 5;
                        break;
                    case 4: CONDITION = C5; SAMPLING_RATE = 10;
                        break;
                    case 5: CONDITION = C6; SAMPLING_RATE = 15;
                        break;
                    default: CONDITION = "Invalid";
                        break;
                }

                Log.d(TAG, "Condition for " + calendar.getTime() + ":: " + CONDITION);
                System.out.println("Condition for " + calendar.getTime() + ":: " + CONDITION);
                DIFF = (END_HOUR * 60 + END_MINUTE) - (START_HOUR * 60 + START_MINUTE); // ESM time window in minutes
                INTERVAL = DIFF / (SAMPLING_RATE-1); // fixed-spaced interval between 2 ESM triggers in minutes

                if (random_number < 3) { // fixed-interval conditions

                    long time = calendar.getTimeInMillis();
                    Calendar c = Calendar.getInstance();
                    for(int i = 0; i < SAMPLING_RATE; i++) {
                        c.setTimeInMillis(time);
                        editor.putLong("ALARM_" + alarm_counter++, c.getTimeInMillis());
                        editor.putString("CONDITION_" + condition_counter++, CONDITION);
                        time += INTERVAL * 60 * 1000; // interval converted to milliseconds
                    }

                }
                else { // random-interval conditions

                    alarm_time.clear();
                    long time1 = calendar.getTimeInMillis();

                    int i;
                    int j;
                    long time_diff;
                    long[] time = new long[15];

                    // generate random time and create alarms
                    for (i = 0; i < SAMPLING_RATE; i++) {
                        j = 0;
                        time[i] = random.nextInt(DIFF * 60 *1000); // random time in milliseconds
                        while(i > j) {
                            time_diff = Math.abs(time[i] - time[j]);
                            if (time_diff < (MIN_TIME * 60 *1000)) {
                                time[i] = random.nextInt(DIFF * 60 *1000); // milliseconds
                                j = 0;
                            }
                            else {
                                j++;
                            }
                        }
                        alarm_time.add(time1 + time[i]);
                    }

                    // sorting the list
                    Collections.sort(alarm_time);

                    for (i = 0; i < SAMPLING_RATE; i++) {
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(alarm_time.get(i));
                        editor.putLong("ALARM_" + alarm_counter++, c.getTimeInMillis());
                        editor.putString("CONDITION_" + condition_counter++, CONDITION);
                    }

                }
            }
            editor.putInt("SIZE", alarm_counter-1);
            editor.commit();
        }
        System.out.println("adding completed, now generating alarms ... ");
        generateAlarms();
    }

    void generateAlarms() {


        long now = System.currentTimeMillis();
        System.out.println("generating Alarms...");
        intentArray = new ArrayList<PendingIntent>();
        intentArray.clear();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int size = preferences.getInt("SIZE", 0);
        Calendar c = Calendar.getInstance();
        System.out.println("size :: " + size);
        TAG = preferences.getString("TAG", null);

        for (int i = 0; i <= size; i++) {
            c.setTimeInMillis(preferences.getLong("ALARM_" + i, 0));
            if (now < c.getTimeInMillis()) {

                Log.d(TAG, i + ". Alarm time chosen :: " + c.getTime() + " SELECTED");
                System.out.println(i + ". Alarm time chosen :: " + c.getTime() + " SELECTED");
                fcontent.add(i + ". Alarm time chosen :: " + c.getTime() + " SELECTED [" + preferences.getString("CONDITION_" + i, null) + "]");

                Intent intent1 = new Intent(this, CreateESM.class);
                intent1.putExtra("CONDITION_" + i, preferences.getString("CONDITION_" + i, null));
                intent1.putExtra("requestCode", i);
                //pendingIntent = PendingIntent.getService(this, i, intent1, 0);
                pendingIntent = PendingIntent.getService(getApplicationContext(), i, intent1, 0);
                alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
                intentArray.add(pendingIntent);

            }
            else {
                Log.d(TAG, i + ". Alarm time chosen :: " + c.getTime() + " REJECTED");
                System.out.println(i + ". Alarm time chosen :: " + c.getTime() + " REJECTED");
                fcontent.add(i + ". Alarm time chosen :: " + c.getTime() + " REJECTED [" + preferences.getString("CONDITION_" + i, null) + "]");
            }
        }

        writeFile(fcontent);
        notifyUser(001, "Successfully joined the ESM study.");
    }

    void writeFile(ArrayList<String> fcontent) {

        String fpath = Environment.getExternalStorageDirectory() + "/AWARE/ESMalarms" + ".txt";
        System.out.println(fpath);
        try {

            File file = new File(fpath);
            // If file does not exists, then create it
            //if (!file.exists()) {
            file.createNewFile();
            //}
            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);

            System.out.println("File successfully created");
            for (int i = 0; i < fcontent.size(); i++) {
                bw.write(fcontent.get(i));
                bw.newLine();
            }

            bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        notifyUser(002, "Successfully created file ESMalarms.txt");


    }

    @SuppressLint("NewApi") void notifyUser(int Id, String note) {
        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(drawable.ic_notify_user);
        mBuilder.setContentText(note);
        if (Id == 001) {
            mBuilder.setContentTitle("Thank You!");
        }
        else if (Id == 002) {
            mBuilder.setContentTitle("File Created");

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            String fpath = Environment.getExternalStorageDirectory() + "/AWARE/ESMalarms" + ".txt";
            File file = new File(fpath); // set your audio path
            intent.setDataAndType(Uri.fromFile(file), "text/*");

            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
            mBuilder.setContentIntent(pIntent).build();

        }

        int mNotificationId = Id;
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public static class ESMStatusListener extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            System.out.println("received from CreateESM");
            if(intent.getAction().equals(ESM.ACTION_AWARE_ESM_EXPIRED)) {
                System.out.println("ESM expired :: #" + (counter - 1));
                System.out.println("counter set to 999");
                counter = 999; // if ESM question expires, then the entire questionnaire set is aborted
            }
            if (intent.getAction().equals(ESM.ACTION_AWARE_ESM_DISMISSED)) {
                System.out.println("ESM dismissed :: #" + (counter - 1));
                System.out.println("counter set to 999");
                counter = 999; // if user dismisses one ESM question, then the entire questionnaire set is aborted
            }
            if(intent.getAction().equals(ESM.ACTION_AWARE_ESM_QUEUE_COMPLETE)) {
                System.out.println("ESM complete :: #" + (counter - 1));
                if (counter < 5) {
                    CreateESM.displayESM(context, CreateESM.ESMqueue[counter++]);
                }
                else {
                    counter = 1;
                }

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(esm_statuses);
        Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_ESM, false);
        //Aware.setSetting(getContentResolver(), Aware_Preferences.STATUS_ESM, false);

        sendBroadcast(new Intent(Aware.ACTION_AWARE_REFRESH));
    }
}

