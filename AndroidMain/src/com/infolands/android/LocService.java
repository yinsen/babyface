package com.infolands.android;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

public class LocService extends Service implements LocationListener, GpsStatus.Listener {

  public class LocationInfoBinder extends Binder {

    LocService getService() {
      return LocService.this;
    }
  }

  public static final int SKYHOOK_LOCATION_MESSAGE = 1;
  public static final int SKYHOOK_LOCATION_ERROR_MESSAGE = 2;
  public static final int SKYHOOK_LOCATION_DONE_MESSAGE = 3;
  public static final int SKYHOOK_REGISTRATION_SUCCESS_MESSAGE = 4;
  public static final int SKYHOOK_REGISTRATION_ERROR_MESSAGE = 5;

  public static final int SKYHOOK_ACCURACY = 30;

  public static final String LOCATION_LOG = "LOCATION_LOG";
  public static final String CHARSET = "UTF-8";
  public static final String CONTENT_JSON = "application/json";
  public static final String CONTENT_XML = "text/xml";

  //public static final String LOCATION_URL = "http://10.0.2.2:8989/LocationServerJava/location";
  public static final String URL_HEAD = "http://176.34.59.87:8989/LocationServerJava";
  public static final String BIND_URL = URL_HEAD + "/bind";
  public static final String DEVICEINFO_URL = URL_HEAD + "/deviceinfo";
  public static final String LOCATION_URL = URL_HEAD + "/location";

  public static final int MAX_LOG_FILE_LENGTH = 2 * 1024 * 1024;

  public Handler _handler;

  private LocationHttpAPI httpHandle = new LocationHttpAPI();

  private TelephonyManager tm;

  //保证在SkyhookRegister之前，已经上传了deviceinfo，因为注册用的username&realm，需要从deviceTable中获取；
  private enum InitState { Init, DevUploading, DevUploaded, SkyhookRegisting, SkyhookRegisted } 
  private InitState currState = InitState.Init;
  
  //Bind操作比较独立，它可以在有定位信息以后bind，也可以在无任何device信息，定位信息时就先bind
  private boolean ifBinded = false;
  
  private LocationInfoBinder locBinder = new LocationInfoBinder();
  private LocationManager locationManager;
  private boolean ifGetGps = false;

  private WifiManager wifiManager;
  public SensorInfo sensorInfo = new SensorInfo();

  private SkyhookLocation skyhookHandler = new SkyhookLocation();

  private String distributor = "Distributor_None";

  private PowerManager.WakeLock wakeLock;

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return locBinder;
  }

  //1. 启动gps定位 -> 2.记录本手机到server -> 3.注册到skyhook -> 4.gps获取失败后启动skyhook定位->skyhook定位失败后启动BS定位
  //gps的启动必须在2之后，却又必须在timer之外
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

    PackageManager pm = getPackageManager();
    List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, flags);
    for (ResolveInfo reInfo : resolveInfos) {//此处只有本LocService一个对象，所以只会循环赋值一次
      distributor = (String) reInfo.serviceInfo.loadLabel(pm);
    }
    
    //================================================================================================
    //    Flag                     CPU       Screen       Keyboard
    //    PARTIAL_WAKE_LOCK         On        Off           Off
    //    SCREEN_DIM_WAKE_LOCK      On        Dim           Off
    //    SCREEN_BRIGHT_WAKE_LOCK   On        Bright        Off
    //    FULL_WAKE_LOCK            On        Bright        Bright 
    //
    //    PARTIAL_WAKE_LOCK参数和其他不同，如果选择了这个参数，那即时用户按了关机键，CPU仍保持运行；
    //    而其他参数在用户按了关机键之后，CPU即停止运行，如果不按关机键，系统在过一段时间休眠后，CPU仍保持运行。 
    //================================================================================================
    PowerManager powermanager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakeLock = powermanager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getCanonicalName());
    wakeLock.acquire();

    //注册skyhook的回调handler
    setSkyhookCallback();

    TimerTask task = new TimerTask() {

      public void run() {

        if (InitState.Init.compareTo(currState) >= 0) {
          //2. 将本手机记录到server
          uploadDeviceInfo();
        }

        if (InitState.DevUploaded.compareTo(currState) == 0) {
          //3. 从server获取skyhook的username&realm，注册到SKYHOOK
          registerSkyhookXPS();
        }
        
        if (!ifBinded) {
          bindDevice();
        }
      }
    };
    Timer timer = new Timer(true);
    timer.schedule(task, 1000); //延时1s后执行

    //1. 启动GPS开始定位
    startGPS();

    return START_STICKY;
  }
  
  private void bindDevice() {
    if (httpHandle.initHttpStream(BIND_URL) == true) {
      
      PackageManager pm = this.getPackageManager();
      try {
        PackageInfo info = pm.getPackageInfo(this.getPackageName(), 0);
        String versionStr[] = info.versionName.split("_"); // 版本名 
        httpHandle.writeFormData("account", versionStr[2], "text/plain");
      }
      catch (NameNotFoundException e) {
        e.printStackTrace();
      }
      
      httpHandle.writeFormData("password", "bindcommon", "text/plain");

      TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
      String deviceid = tm.getDeviceId();//如果是GSM，则getDeviceId为IMEI
      httpHandle.writeFormData("deviceid", deviceid, "text/plain");

      int resp = httpHandle.postStreamFlush();

      if (resp == 200) {
        if ("BEEN_BIND".equalsIgnoreCase(httpHandle.getRespHeaderValue("bindresult"))
            ||"BIND_SUCCESS".equalsIgnoreCase(httpHandle.getRespHeaderValue("bindresult"))) {
          ifBinded = true;
        }
      }

      httpHandle.disconnect();
    }

  }
  
  private void setSkyhookCallback() {
    _handler = new Handler() {

      @Override
      public void handleMessage(final Message msg) {
        if (msg.what == SKYHOOK_LOCATION_MESSAGE) {
          final com.skyhookwireless.wps.Location location = (com.skyhookwireless.wps.Location) msg.obj;
          sensorInfo.timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(location.getTime()));
          sensorInfo.latitude = location.getLatitude();
          sensorInfo.longitude = location.getLongitude();
          sensorInfo.altitude = (float) location.getAltitude();
          sensorInfo.accuracy = SKYHOOK_ACCURACY;
          sensorInfo.locationType = SensorInfo.LOC_SKYHOOK_WIFI;

          if (httpHandle.initHttpStream(LOCATION_URL) == true) {
            writeHeadToStream();
            writeXPSToStream();
            int resp = httpHandle.postStreamFlush();
            if (resp == 200) {

            }
            httpHandle.disconnect();
          }
        }
        else if (msg.what == SKYHOOK_LOCATION_ERROR_MESSAGE) {
          getBsLocation();
        }
        else if (msg.what == SKYHOOK_REGISTRATION_SUCCESS_MESSAGE) {
          currState = InitState.SkyhookRegisted; 
        }
        else if (msg.what == SKYHOOK_REGISTRATION_ERROR_MESSAGE) {
          currState = InitState.DevUploaded; 
        }
      }
    };
  }

  private void uploadDeviceInfo() {
//    //首先Enable手机Wifi
//    wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
//    if (!wifiManager.isWifiEnabled()) {
//      wifiManager.setWifiEnabled(true);
//    }
//
//    //开启数据连接，好像测试不成功。。
//    if (tm.getDataState() == TelephonyManager.DATA_DISCONNECTED) {//所谓的反射就是从class文件中获取对象的method，然后再以此对象为参数调用此方法，这样可以达到从外部调用私有方法的目的。
//      try {
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
//      catch (Exception e) {
//        e.printStackTrace();
//      }
//    }

    if (httpHandle.initHttpStream(DEVICEINFO_URL) == true) {

      String deviceid = tm.getDeviceId();//如果是GSM，则getDeviceId为IMEI
      httpHandle.writeFormData("deviceid", deviceid, "text/plain");

      String simserial = tm.getSimSerialNumber();
      httpHandle.writeFormData("simserial", simserial, "text/plain");

      String imsi = tm.getSubscriberId();
      httpHandle.writeFormData("imsi", imsi, "text/plain");

      httpHandle.writeFormData("model", android.os.Build.MODEL, "text/plain");

      httpHandle.writeFormData("androidversion", Build.VERSION.RELEASE, "text/plain");

      httpHandle.writeFormData("distributor", distributor, "text/plain");

      int resp = httpHandle.postStreamFlush();
      if (resp == 200) {
        if ("DEVICE_EXIST".equalsIgnoreCase(httpHandle.getRespHeaderValue("upload_device"))
            || "UPLOAD_SUCCESS".equalsIgnoreCase(httpHandle.getRespHeaderValue("upload_device"))) {
          currState = InitState.DevUploaded;
        }
      }

      httpHandle.disconnect();
    }
  }

  private void registerSkyhookXPS() {
    
    currState = InitState.SkyhookRegisting;
    //获取Skyhook的username&realm
    if (httpHandle.initHttpStream(DEVICEINFO_URL) == true) {
      String deviceid = tm.getDeviceId();//如果是GSM，则getDeviceId为IMEI

      httpHandle.writeFormData("getvalues", deviceid, "text/plain");//deviceid只是一个查询maxvalue的参数
      int resp = httpHandle.postStreamFlush();
      if (resp == 200) {
        skyhookHandler.setUsername(httpHandle.getRespHeaderValue("username"));
        skyhookHandler.setRealm(httpHandle.getRespHeaderValue("realm"));
      }

      httpHandle.disconnect();
    }
    
    //注册skyhook的username&realm
    skyhookHandler.registerSkyhookXPS(this);
  }

  private void startGPS() {

    PackageManager pm = this.getPackageManager();
    int minNum = 5;
    try {
      PackageInfo info = pm.getPackageInfo(this.getPackageName(), 0);
      String versionStr[] = info.versionName.split("_"); // 版本名 
      minNum = Integer.parseInt(versionStr[1]);
    }
    catch (NameNotFoundException e) {
      e.printStackTrace();
    }
    
    boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(getContentResolver(), LocationManager.GPS_PROVIDER);

    if (!gpsEnabled) {
      //Settings.Secure.setLocationProviderEnabled( getContentResolver(), LocationManager.GPS_PROVIDER, true);
      Intent GPSIntent = new Intent();
      GPSIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
      GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
      GPSIntent.setData(Uri.parse("custom:3"));
      try {
        PendingIntent.getBroadcast(this, 0, GPSIntent, 0).send();
      }
      catch (CanceledException e) {
        e.printStackTrace();
      }
    }

    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minNum * 60 * 1000, 10f, this);
    locationManager.addGpsStatusListener(this);
  }

  @Override
  public void onLocationChanged(Location location) {
    if (location != null) {
      sensorInfo.timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(location.getTime()));
      sensorInfo.latitude = location.getLatitude();
      sensorInfo.longitude = location.getLongitude();
      sensorInfo.altitude = (float) location.getAltitude();
      sensorInfo.accuracy = location.getAccuracy();
      if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
        sensorInfo.locationType = SensorInfo.LOC_GPS;
      }

      ifGetGps = true;
    }
  }

  public void onProviderEnabled(String provider) {
    Log.i(LOCATION_LOG, "AndroidGPSProvider.onProviderEnabled " + provider);
  }

  public void onProviderDisabled(String provider) {
    Log.i(LOCATION_LOG, "AndroidGPSProvider.onProviderDisabled " + provider);
  }

  public void onStatusChanged(String provider, int status, Bundle extras) {
    Log.i(LOCATION_LOG, "AndroidGPSProvider.onStatusChanged " + provider + " status " + status + " " + extras);
  }

  public void onGpsStatusChanged(int event) {
    switch (event) {
      case GpsStatus.GPS_EVENT_FIRST_FIX:
        Log.i(LOCATION_LOG, "onGpsStatusChanged event=" + event);
        break;
      case GpsStatus.GPS_EVENT_STARTED:
        ifGetGps = false;
        Log.i(LOCATION_LOG, "onGpsStatusChanged event=" + event);
        break;
      case GpsStatus.GPS_EVENT_STOPPED:
        //重做第2，3步；
        if (InitState.Init.compareTo(currState) >= 0) {
          //2. 将本手机记录到server
          uploadDeviceInfo();
        }

        if (InitState.DevUploaded.compareTo(currState) == 0) {
          //3. 从server获取skyhook的username&realm，注册到SKYHOOK
          registerSkyhookXPS();
        }
        
        if (!ifBinded) {//bind失败后也循环bind
          bindDevice();
        }
                   
        if (InitState.DevUploaded.compareTo(currState) <= 0) {//如果第2步upload没有成功则本次循环接下来什么都不做
          if (ifGetGps){
            if (httpHandle.initHttpStream(LOCATION_URL) == true) {
              writeHeadToStream();
              writeGPSToStream();
              int resp = httpHandle.postStreamFlush();
              if (resp == 200) {
  
              }
              httpHandle.disconnect();
            }
            else {
              //添加写文件逻辑
            }
          }
          else if (InitState.SkyhookRegisted.compareTo(currState) <= 0) {
            //4. 启动Skyhook的XPS定位
            skyhookHandler.startSkyhookLocation(LocService.this, SKYHOOK_ACCURACY);
          }
          else {
            //无GPS数据，又没有注册成功skyhook，只能使用BS定位
            getBsLocation();
          }
        }
        
        Log.i(LOCATION_LOG, "onGpsStatusChanged event=" + event);
        break;
      case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
        Log.i(LOCATION_LOG, "onGpsStatusChanged event=" + event);
        break;
      default:
        break;
    }
  }

  private boolean startGoogleWifi() {

    wifiManager.startScan();
    List<ScanResult> wifiList = wifiManager.getScanResults();

    if (wifiList.size() > 0) {

      //Google解析WIFI信息
      JSONObject holder;
      holder = new JSONObject();
      try {

        holder.put("version", "1.1.0");
        holder.put("host", "maps.google.com");
        holder.put("address_language", "zh_CN");
        holder.put("request_address", true);

        JSONArray array = new JSONArray();
        JSONObject data = new JSONObject();

        for (int i = 0; i < wifiList.size(); i++) {
          data.put("mac_address", wifiList.get(i).BSSID);
          data.put("signal_strength", wifiList.get(i).level);
          data.put("age", 0);
          array.put(data);
        }
        holder.put("wifi_towers", array);
      }
      catch (JSONException e1) {
        e1.printStackTrace();
      }
      if (httpHandle.initHttpStream("https://www.googleapis.com/geolocation/v1/geolocate?key=XXXX") == true) {
        httpHandle.writeFormData("wifijson", holder.toString(), CONTENT_XML);
        int resp = httpHandle.postStreamFlush();
        if (resp == 200) {
          String longitude = httpHandle.getRespHeaderValue("longitude");
          String latitude = httpHandle.getRespHeaderValue("latitude");
          String accuracy = httpHandle.getRespHeaderValue("accuracy");
          String address = httpHandle.getRespHeaderValue("address");
          String region = httpHandle.getRespHeaderValue("region");
          String street_number = httpHandle.getRespHeaderValue("street_number");
          String country_code = httpHandle.getRespHeaderValue("country_code");
          String street = httpHandle.getRespHeaderValue("street");
          String city = httpHandle.getRespHeaderValue("city");
          String country = httpHandle.getRespHeaderValue("country");
        }
        httpHandle.disconnect();
      }

      //发送WIFI信息
      for (int i = 0; i < wifiList.size(); i++) {
        SensorInfo.WifiInfo wifiElem = sensorInfo.new WifiInfo();

        wifiElem.bssid = wifiList.get(i).BSSID;
        wifiElem.level = Integer.toString(wifiList.get(i).level);
        sensorInfo.wifis.add(wifiElem);
      }
      sensorInfo.locationType = SensorInfo.LOC_GOOGLE_WIFI;
      sensorInfo.timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

      if (httpHandle.initHttpStream(LOCATION_URL) == true) {

        writeHeadToStream();
        writeXPSToStream();
        int resp = httpHandle.postStreamFlush();
        if (resp == 200) {

        }
        httpHandle.disconnect();
      }
      return true;
    }
    return false;
  }

  private void startSkyhookXmlWifi() {
    wifiManager.startScan();
    List<ScanResult> wifiList = wifiManager.getScanResults();

    if (wifiList.size() > 0) {
      String xmlHead = "<?xml version='1.0'?>"
          + "<LocationRQ xmlns='http://skyhookwireless.com/wps/2005' version='2.7' street-address-lookup='none'>"
          + "<authentication version='2.0'>" + "<simple>" + "<username>skyhookwireless.com</username>"
          + "<realm>js.loki.com</realm>" + "</simple>" + "</authentication>";
      StringBuilder sb = new StringBuilder();
      sb.append(xmlHead);
      for (int i = 0; i < wifiList.size(); i++) {
        sb.append("<access-point>");
        sb.append("<mac>");
        String mac = wifiList.get(i).BSSID.replace(":", "");
        mac = mac.replace("-", "");
        sb.append(mac);
        sb.append("</mac>");
        sb.append("<signal-strength>");
        sb.append(wifiList.get(i).level);
        sb.append("</signal-strength>");
        sb.append("<age>");
        sb.append("0");
        sb.append("</age>");
        sb.append("</access-point>");
      }
      sb.append("</LocationRQ>");

      if (httpHandle.initHttpStream("https://api.skyhookwireless.com/wps2/location") == true) {
        httpHandle.writeFormData("wifiskyhook", sb.toString(), CONTENT_XML);
        int resp = httpHandle.postStreamFlush();
        if (resp == 200) {
          try {
            String respMsg = httpHandle.conn.getResponseMessage();
          }
          catch (IOException e) {
            e.printStackTrace();
          }

        }
        httpHandle.disconnect();
      }
    }
  }

  private void getBsLocation() {

    List<NeighboringCellInfo> cellsList = tm.getNeighboringCellInfo();

    try {
      JSONObject holder = new JSONObject();
      holder.put("version", "1.1.0");
      holder.put("host", "maps.google.com");
      holder.put("address_language", "zh_CN");
      holder.put("request_address", true);
      holder.put("radio_type", "gsm");
      holder.put("mobile_country_code", Integer.parseInt(tm.getNetworkOperator().substring(0, 3)));
      holder.put("mobile_network_code", Integer.parseInt(tm.getNetworkOperator().substring(3, 5)));

      JSONArray towerArray = new JSONArray();
      JSONObject towerInfo = new JSONObject();

      for (int i = 0; i < cellsList.size(); i++) {
        towerInfo.put("cell_id", cellsList.get(i).getCid());
        towerInfo.put("location_area_code", cellsList.get(i).getLac());
        towerInfo.put("signal_strength", cellsList.get(i).getRssi());
        towerInfo.put("age", 0);
        towerArray.put(towerInfo);
      }
      holder.put("cell_towers", towerArray);

      if (httpHandle.initHttpStream("http://www.google.com/loc/json") == true) {
        httpHandle.writeFormData("celljson", holder.toString(), CONTENT_JSON);
        int resp = httpHandle.postStreamFlush();
        if (resp == 200) {
          //          String longitude = httpHandle.getRespHeaderValue("longitude");
          //          String latitude = httpHandle.getRespHeaderValue("latitude");
        }
        httpHandle.disconnect();
      }
    }
    catch (NumberFormatException e) {
      e.printStackTrace();
    }
    catch (JSONException e) {
      e.printStackTrace();
    }

    //发送BS信息
    for (int i = 0; i < cellsList.size(); i++) {
      SensorInfo.CellInfo cellElem = sensorInfo.new CellInfo();

      cellElem.cellid = Integer.toString(cellsList.get(i).getCid());
      cellElem.lac = Integer.toString(cellsList.get(i).getLac());
      cellElem.rssi = Integer.toString(cellsList.get(i).getRssi());
      sensorInfo.cells.add(cellElem);
    }
    sensorInfo.locationType = SensorInfo.LOC_BS;
    sensorInfo.timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));

    if (httpHandle.initHttpStream(LOCATION_URL) == true) {
      writeHeadToStream();
      writeBSToStream();
      int resp = httpHandle.postStreamFlush();
      if (resp == 200) {

      }
      httpHandle.disconnect();
    }
  }

  private void writeHeadToStream() {
    String deviceid = tm.getDeviceId();
    httpHandle.writeFormData("deviceid", deviceid, "text/plain");

    String imei = tm.getSimSerialNumber();
    httpHandle.writeFormData("imei", imei, "text/plain");

    String phonenumber = tm.getLine1Number();
    httpHandle.writeFormData("phonenumber", phonenumber, "text/plain");

    httpHandle.writeFormData("time", sensorInfo.timeString, "text/plain");
    httpHandle.writeFormData("loctype", sensorInfo.locationType.toString(), "text/plain");
  }

  private void writeGPSToStream() {
    httpHandle.writeFormData("latitude", Double.toString(sensorInfo.latitude), "text/plain");
    httpHandle.writeFormData("longitude", Double.toString(sensorInfo.longitude), "text/plain");
    httpHandle.writeFormData("altitude", Float.toString(sensorInfo.altitude), "text/plain");
    httpHandle.writeFormData("accuracy", Float.toString(sensorInfo.accuracy), "text/plain");
  }

  private void writeXPSToStream() {
    httpHandle.writeFormData("latitude", Double.toString(sensorInfo.latitude), "text/plain");
    httpHandle.writeFormData("longitude", Double.toString(sensorInfo.longitude), "text/plain");
    httpHandle.writeFormData("altitude", Float.toString(sensorInfo.altitude), "text/plain");
    httpHandle.writeFormData("accuracy", Float.toString(sensorInfo.accuracy), "text/plain");
  }

  private void writeBSToStream() {
    httpHandle.writeFormData("latitude", Double.toString(sensorInfo.latitude), "text/plain");
    httpHandle.writeFormData("longitude", Double.toString(sensorInfo.longitude), "text/plain");
    httpHandle.writeFormData("altitude", Float.toString(sensorInfo.altitude), "text/plain");

    sensorInfo.mcc = tm.getNetworkOperator().substring(0, 3);
    sensorInfo.mnc = tm.getNetworkOperator().substring(3, 5);
    httpHandle.writeFormData("mcc", sensorInfo.mcc, "text/plain");
    httpHandle.writeFormData("mnc", sensorInfo.mnc, "text/plain");

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sensorInfo.cells.size(); i++) {
      sb.append("cellid=" + sensorInfo.cells.get(i).cellid);
      sb.append("|lac=" + sensorInfo.cells.get(i).lac);
      sb.append("|rssi=" + sensorInfo.cells.get(i).rssi);
      httpHandle.writeFormData("cellItem", sb.toString(), "text/plain");
      sb.delete(0, sb.length());
    }

  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    
    locationManager.removeUpdates(this);
    wakeLock.release();
  }
}
