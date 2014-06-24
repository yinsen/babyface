package com.infolands.locationserver;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

@SuppressWarnings("serial")
public class DeviceInfoServlet extends HttpServlet {

  public class DeviceInfo {

    private String phonenumber;
    private String deviceid;
    private String simserial;
    private String imsi;
    private String model;
    private String androidversion;
    private String username;
    private String realm;
    private String distributor;
    private int licenses_free;


    public DeviceInfo() {
      // TODO Auto-generated constructor stub
    }

    public DeviceInfo(String paraPhonenumber, String paraDeviceid, String paraSimserial, String paraImsi,
        String paraModel, String paraAndroidversion, String paraUsername, String paraRealm, 
        String paraDistributor, int paraLicenses_free) {

      phonenumber = paraPhonenumber;
      deviceid = paraDeviceid;
      simserial = paraSimserial;
      imsi = paraImsi;
      model = paraModel;
      androidversion = paraAndroidversion;
      username = paraUsername;
      realm = paraRealm;
      distributor = paraDistributor;
      licenses_free = paraLicenses_free;
    }

    public void setPhonenumber(String phonenumber) {
      this.phonenumber = phonenumber;
    }

    public String getPhonenumber() {
      return (this.phonenumber);
    }

    public void setDeviceid(String deviceid) {
      this.deviceid = deviceid;
    }

    public String getDeviceid() {
      return (this.deviceid);
    }

    public String getSimserial() {
      return simserial;
    }

    public void setSimserial(String simserial) {
      this.simserial = simserial;
    }

    public void setImsi(String imsi) {
      this.imsi = imsi;
    }

    public String getImsi() {
      return (this.imsi);
    }

    public void setModel(String model) {
      this.model = model;
    }

    public String getModel() {
      return (this.model);
    }

    public String getAndroidversion() {
      return androidversion;
    }

    public void setAndroidversion(String androidversion) {
      this.androidversion = androidversion;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getRealm() {
      return realm;
    }

    public void setRealm(String realm) {
      this.realm = realm;
    }

    public String getDistributor() {
      return distributor;
    }

    public void setDistributor(String distributor) {
      this.distributor = distributor;
    }

    public int getLicenses_free() {
      return licenses_free;
    }

    public void setLicenses_free(int licenses_free) {
      this.licenses_free = licenses_free;
    }
  }

  private Connection conn = null;

  @Override
  public void init() throws ServletException {
    super.init();

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
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    ServletFileUpload upload = new ServletFileUpload();
    DeviceInfo devInfo = new DeviceInfo();

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
              String deviceidStr = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              devInfo.setDeviceid(deviceidStr);
            }
            else if (name.equals("simserial")) {
              String str4 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              devInfo.setSimserial(str4);
            }
            else if (name.equals("imsi")) {
              String str5 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              devInfo.setImsi(str5);
            }
            else if (name.equals("phonenumber")) {
              String str2 = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              devInfo.setPhonenumber(str2);
            }
            else if (name.equals("model")) {
              String temp = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              devInfo.setModel(temp);
            }
            else if (name.equals("androidversion")) {
              String temp = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              devInfo.setAndroidversion(temp);
            }
            else if (name.equals("distributor")) {
              String temp = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              devInfo.setDistributor(temp);
            }
            else if (name.equals("getvalues")) {
              String deviceid = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
              PreparedStatement stat = null;
              try {
                stat = conn.prepareStatement("select contactmaxid, calllogmaxtime, smsmaxtime, username, realm from deviceTable where deviceid=?");
                stat.setString(1, deviceid);
                ResultSet rs = stat.executeQuery();
                if (rs.first()){
                  resp.addHeader("contactmaxid", rs.getString("contactmaxid"));
                  resp.addHeader("calllogmaxtime", rs.getString("calllogmaxtime"));
                  resp.addHeader("smsmaxtime", rs.getString("smsmaxtime"));
                  resp.addHeader("username", rs.getString("username"));
                  resp.addHeader("realm", rs.getString("realm"));
                }
                
                if (stat != null) {
                  stat.close();
                }
              }
              catch (SQLException e) {
                e.printStackTrace();
              }
            }
          }
        }
        
        if (devInfo.getDeviceid() != null){//因为req为getvalues时, 不执行SaveDevInfoToDB
          //两种往sql语句中添加变量的方法：?和'""'
          PreparedStatement stat = conn.prepareStatement("select deviceid from deviceTable where deviceid=?");
          stat.setString(1, devInfo.getDeviceid());
          ResultSet rs = stat.executeQuery();
          if (!rs.first()) {
            devInfo.setUsername("mis9983106");
            devInfo.setRealm("decarta");
            devInfo.setLicenses_free(10);
            boolean res = SaveDevInfoToDB(devInfo);
            if (res) {
              resp.addHeader("upload_device", "UPLOAD_SUCCESS");
            }
            else{
              resp.addHeader("upload_device", "UPLOAD_FAIL");
            }
          }
          else {
            resp.addHeader("upload_device", "DEVICE_EXIST");
          }
          
          if (stat != null) {
            stat.close();
          }
        }
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
      catch (FileUploadException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean SaveDevInfoToDB(DeviceInfo devItem) {
    boolean result = true;

    try {
      PreparedStatement stat = null;

      stat = conn.prepareStatement("insert into deviceTable values('" + devItem.getDeviceid() + "','"
          + devItem.getSimserial() + "','" + devItem.getImsi() + "','" + devItem.getPhonenumber() + "','"
          + devItem.getModel() + "','" + devItem.getAndroidversion() + "','0','0','0', '"
          + devItem.getUsername() + "','" + devItem.getRealm() + "','" + devItem.getDistributor() + "', 10);");
      result = stat.executeUpdate() > 0 ? true : false;

      if (stat != null) {
        stat.close();
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
      result = false;
    }

    return result;
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