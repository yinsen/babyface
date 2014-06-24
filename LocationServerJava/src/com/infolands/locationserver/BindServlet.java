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
public class BindServlet extends HttpServlet {

  private class BindInfo {
    private String account;
    private String password;
    private String deviceid;
    
    public String getAccount() {
      return account;
    }
    public void setAccount(String account) {
      this.account = account;
    }
    public String getPassword() {
      return password;
    }
    public void setPassword(String password) {
      this.password = password;
    }
    public String getDeviceid() {
      return deviceid;
    }
    public void setDeviceid(String deviceid) {
      this.deviceid = deviceid;
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
    BindInfo bindInfo = new BindInfo();
    PreparedStatement stat = null;
    
    // file1.2.2的api已经改变了，不是使用直接存文件的DiskFileUpload，而是换成了更灵活简单的ServletFileUpload
    if (ServletFileUpload.isMultipartContent(req)) {// 判断是否文件上传请求
      try {
        FileItemIterator iter = upload.getItemIterator(req);
        while (iter.hasNext()) {
          FileItemStream item = iter.next();
          String name = item.getFieldName();// 获取表单项name值
          InputStream stream = item.openStream();
          
          if (item.isFormField() && "account".equals(name)) {
            String accountStr = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
            bindInfo.setAccount(accountStr);
          }
          else if (item.isFormField() && "password".equals(name)){
            String passwordStr = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
            bindInfo.setPassword(passwordStr);
          }
          else if (item.isFormField() && "deviceid".equals(name)){
            String deviceidStr = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
            bindInfo.setDeviceid(deviceidStr);
          }
        }
        
        //两种往sql语句中添加变量的方法：?和'""'
        stat = conn.prepareStatement("select account, password from accountTable where account=? ");
        stat.setString(1, bindInfo.getAccount());
        ResultSet rs = stat.executeQuery();
        if (!rs.first()){
          resp.addHeader("bindresult", "NO_ACCOUNT");
        }
        else {
          String pwd = rs.getString("password");
          if (!bindInfo.getPassword().equals(pwd)
             && !bindInfo.getPassword().equals("bindcommon")){//"bindcommon"作为通用密码只在bind时有效
            resp.addHeader("bindresult", "PWD_FAIL");
          }
          else{
            stat = conn.prepareStatement("select * from bindTable where account=? AND deviceid=?");
            stat.setString(1, bindInfo.getAccount());
            stat.setString(2, bindInfo.getDeviceid());
            rs = stat.executeQuery();
            if (rs.first()) {
              resp.addHeader("bindresult", "BEEN_BIND");
            }
            else {
              boolean saved = SaveBindInfoToDB( bindInfo );
              if (saved){
                //bind成功
                resp.addHeader("bindresult", "BIND_SUCCESS");
              }
              else {
                resp.addHeader("bindresult", "BIND_FAIL");
              }
            }
          }
        }
        
        
        
        if (stat != null) {
          stat.close();
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
  
  private boolean SaveBindInfoToDB ( BindInfo paraItem ) {
    boolean result = true;
    
    try {
      PreparedStatement stat = null;

      stat = conn.prepareStatement("insert into bindTable values('" + paraItem.getAccount() + "','" + paraItem.getDeviceid() + "');");
      result = stat.executeUpdate() > 0 ? true:false;
      
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