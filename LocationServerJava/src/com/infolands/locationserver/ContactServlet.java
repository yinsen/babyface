package com.infolands.locationserver;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
public class ContactServlet extends HttpServlet {

  private Connection conn = null;

  private String deviceId = "";
  private Vector<String> contact;

  @Override
  public void init() throws ServletException {
    super.init();

    contact = new Vector<String>();

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
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    ServletFileUpload upload = new ServletFileUpload();

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
              deviceId = new String(Streams.asString(stream).getBytes("ISO-8859-1"), "utf-8");
            }
            if (name.equals("contactitem")) {
              String temp = new String(Streams.asString(stream).getBytes("UTF-8"), "utf-8");
              contact.add(temp);
            }
          }
        }

        if (!contact.isEmpty()){
          SaveContactToDB();
        }
        
        deviceId = "";
        contact.clear();
      }
      catch (FileUploadException e) {
        e.printStackTrace();
      }
    }
  }

  private void SaveContactToDB() {
    try {
      PreparedStatement stat = null;
      int number = contact.size();

      //保存contact信息并更新contact标志
      String maxID = "0";//用于记录每次同步的最后一条contact的ID值。
      for (int i = 0; i < number; i++) {
        String item = contact.get(i);
        String items[] = item.split("\\|");
        if (items.length < 4)//将缺少字段的contact card忽略掉
          continue;

        String contactId = items[0].substring(items[0].indexOf('=') + 1);
        if (contactId!=null && contactId.length()>0 && Long.parseLong(contactId) > Long.parseLong(maxID)) {
          maxID = contactId;
          
          stat = conn.prepareStatement("update deviceTable set contactmaxid=? where deviceid=? ;");
          stat.setString(1, maxID);
          stat.setString(2, deviceId);
          stat.executeUpdate();
        }
        
        stat = conn.prepareStatement("insert into contactTable values(null,'" + deviceId + "','"
            + items[0].substring(items[0].indexOf('=') + 1) + "','"
            + items[1].substring(items[1].indexOf('=') + 1) + "','"
            + items[2].substring(items[2].indexOf('=') + 1) + "','" 
            + items[3].substring(items[3].indexOf('=') + 1) + "');");
        stat.executeUpdate();
        
      }

      if (stat != null) {
        stat.close();
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
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
      e.printStackTrace();
    }
  }
}