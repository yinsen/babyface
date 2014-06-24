package com.infolands.locationserver;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

@SuppressWarnings("serial")
public class LocationServlet extends HttpServlet {

  private Vector<LocationInfo> locInfoVector;
  private Connection conn = null;

  @Override
  public void init() throws ServletException {
    super.init();

    locInfoVector = new Vector<LocationInfo>();
    try {
      String url = "jdbc:mysql://localhost:3306/locationPublic?user=sen&password=sen";
      Class.forName("com.mysql.jdbc.Driver");
      conn = DriverManager.getConnection(url);
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    catch (SQLException e) {
      e.printStackTrace();
    }

    startParseThread();
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    ServletFileUpload upload = new ServletFileUpload();
    LocationInfo reqItem = new LocationInfo();

    // file1.2.2的api已经改变了，不是使用直接存文件的DiskFileUpload，而是换成了更灵活简单的ServletFileUpload
    if (ServletFileUpload.isMultipartContent(req)) {// 判断是否文件上传请求
      try {
        FileItemIterator iter = upload.getItemIterator(req);
        while (iter.hasNext()) {
          FileItemStream item = iter.next();
          String name = item.getFieldName();// 获取表单项name值
          InputStream stream = item.openStream();

          if (item.isFormField() && name != null) {
            if (name.equals("deviceid")) {
              String str2 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setDeviceId(str2);
            }
            else if (name.equals("loctype")) {
              String str2 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setLocType(str2);
            }
            else if (name.equals("latitude")) {
              String str4 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setLatitude(Double.parseDouble(str4));
            }
            else if (name.equals("longitude")) {
              String str3 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setLongitude(Double.parseDouble(str3));
            }
            else if (name.equals("altitude")) {
              String str5 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setAltitude(Float.parseFloat(str5));
            }
            else if (name.equals("accuracy")) {
              String str5 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setAccuracy(Float.parseFloat(str5));
            }
            else if (name.equals("address")) {
              String str2 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setAddress(str2);
            }
            else if (name.equals("mcc")) {
              String mcc = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setMcc(mcc);
            }
            else if (name.equals("mnc")) {
              String mnc = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setMnc(mnc);
            }
            else if (name.equals("cellid")) {
              String str2 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setLocType(str2);
            }
            else if (name.equals("lac")) {
              String lac = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setLac(lac);
            }
            else if (name.equals("time")) {
              String temp = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              reqItem.setTime(temp);
            }
          }
        }

        locInfoVector.add(reqItem);
      }
      catch (FileUploadException e) {
        e.printStackTrace();
      }

    }
  }

  private synchronized void startParseThread() {
    new Thread(new ParseThread()).start();
  }

  private final class ParseThread implements Runnable {

    public ParseThread() {
    }

    @Override
    public void run() {
      while (true) {
        if (!locInfoVector.isEmpty()) {
          LocationInfo item = locInfoVector.remove(0);
          if (item.getLatitude() != 0 && item.getLongitude() != 0) {
            try {
              DecartaReverseGeocoder revGeo = new DecartaReverseGeocoder();
              String latilong = revGeo.doShifting(item.getLatitude(), item.getLongitude(), item.getAltitude());
              double lati = Double.parseDouble(latilong.split(",")[0]);
              double longi = Double.parseDouble(latilong.split(",")[1]);
              item.setLatitude(lati);
              item.setLongitude(longi);
              if(latilong != null){
//                StringBuilder addressSb = new StringBuilder();
//                addressSb.append(revGeo.revGeocoder(latilong));
//                item.setAddress(addressSb.toString());
                SaveLocToDB(item);
              }
              
            }
            catch (Exception e1) {
              e1.printStackTrace();
            }
            
          }
        }
        try {
          Thread.sleep(10);
        }
        catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    private void SaveLocToDB(LocationInfo paraItem) {

      try {
        PreparedStatement stat = conn.prepareStatement("insert into locationTable values(null,'" + paraItem.getDeviceId()
            + "','" + paraItem.getLocType() + "','" + paraItem.getLatitude()
            + "','" + paraItem.getLongitude() + "','" + paraItem.getAltitude() + "','" + paraItem.getAccuracy()
            + "','" + paraItem.getAddress() 
            + "','" + paraItem.getMcc() + "','" + paraItem.getMnc() + "','" + paraItem.getCellId() + "','" + paraItem.getLac()
            + "','" + paraItem.getTime() + "');");
        stat.executeUpdate();

        if (stat != null) {
          stat.close();
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
  
  @Override
  public void destroy() {
    super.destroy();
    
    try {
      if (conn != null) {
        conn.close();
      }
    }
    catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
