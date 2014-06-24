package com.infolands.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;


public class BootBroadcastReceiver extends BroadcastReceiver {
  static final String action_boot="android.intent.action.BOOT_COMPLETED";

  @Override
  public void onReceive(Context context, Intent intent) {
      if (action_boot.equals(intent.getAction())){
        
        //创建一个PendingIntent用于启动一个service
//        PendingIntent amSendIntent = PendingIntent.getService(
//          context,
//          0,  // request code (not used)
//          new Intent("LOCATION_ACTION"),  // A new Service intent
//          0   // flags (none are required for a service)
//        );
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        am.setRepeating(
//            AlarmManager.RTC_WAKEUP, // 定时方式，表示在睡眠状态下也会启动
//            SystemClock.elapsedRealtime() + 1000,  //1s後启动
//            10 * 60 * 1000,  //重复周期
//            amSendIntent  //到时后触发的Intent
//        );
        //启动LocationService
        Intent locIntent = new Intent("LOCATION_ACTION");
        context.startService(locIntent);
        
        //启动PhoneInfoService
        Intent infoIntent = new Intent("PHONEINFO_ACTION");
        context.startService(infoIntent);
      }
  }

}
