package com.infolands.android;

import java.util.Vector;


class SensorInfo {

  public class WifiInfo {
    public String bssid;
    public String level;
    public String age;
  }

  public class CellInfo {
    public String cellid;
    public String lac;
    public String rssi;
  }
  public static final String LOC_GPS = "GPS";
  public static final String LOC_SKYHOOK_WIFI = "SKYHOOK";
  public static final String LOC_GOOGLE_WIFI = "GOOGLE";
  public static final String LOC_BS = "BS";

  String timeString;

  // GPS data
  String locationType = "";
  double latitude;
  double longitude;
  float altitude;
  float accuracy;

  //Wifi data
  Vector<WifiInfo> wifis = new Vector<WifiInfo>();

  // BS data
  String mcc = null;
  String mnc = null;
  Vector<CellInfo> cells = new Vector<CellInfo>();
  
}
