package com.infolands.locationserver;

public class LocationInfo {

  public static final String LOC_GPS = "LOC_GPS";
  public static final String LOC_WIFI = "LOC_WIFI";
  public static final String LOC_BS = "LOC_BS";

  private String deviceId;
  private String locType;
  private double latitude;
  private double longitude;
  private float altitude;
  private float accuracy;
  private String address;
  private String mcc;
  private String mnc;
  private String cellId;
  private String lac;
  private String wifimac;
  private String time;


  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }
  
  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getLongitude() {
    return (this.longitude);
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLatitude() {
    return (this.latitude);
  }

  public void setAltitude(float altitude) {
    this.altitude = altitude;
  }

  public float getAltitude() {
    return (this.altitude);
  }
  
  public float getAccuracy() {
    return accuracy;
  }

  public void setAccuracy(float accuracy) {
    this.accuracy = accuracy;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public String getTime() {
    return (this.time);
  }

  public String getLocType() {
    return locType;
  }

  public void setLocType(String locType) {
    this.locType = locType;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getMcc() {
    return mcc;
  }

  public void setMcc(String mcc) {
    this.mcc = mcc;
  }

  public String getMnc() {
    return mnc;
  }

  public void setMnc(String mnc) {
    this.mnc = mnc;
  }

  public String getCellId() {
    return cellId;
  }

  public void setCellId(String cellId) {
    this.cellId = cellId;
  }

  public String getLac() {
    return lac;
  }

  public void setLac(String lac) {
    this.lac = lac;
  }


  public String getWifimac() {
    return wifimac;
  }

  public void setWifimac(String wifimac) {
    this.wifimac = wifimac;
  }
  
  public LocationInfo() {
    // TODO Auto-generated constructor stub
  }

  public LocationInfo(String paraDeviceId, String paraPhonenumber, String paraLocType, 
      double paraLongitude, double paraLatitude, float paraAltitude, String paraAddress,
      String paraMcc,String paraMnc,String paraCellId,String paraLac, String paraTime) {
    
    deviceId = paraDeviceId;
    locType = paraLocType;
    
    longitude = paraLongitude;
    latitude = paraLatitude;
    altitude = paraAltitude;
    address = paraAddress;
    
    mcc = paraMcc;
    mnc = paraMnc;
    cellId = paraCellId;
    lac = paraLac;
    
    time = paraTime;
  }

}
