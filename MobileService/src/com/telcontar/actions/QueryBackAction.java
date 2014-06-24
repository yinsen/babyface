/*
 * QueryBackAction author Brent Hamby (brent@telcontar.com) For use only with
 * Telcontar products. Copyright (c) 1997-2005 Telcontar Inc. All Rights
 * Reserved. US and International Patents Pending.
 */

package com.telcontar.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import net.sf.json.*;

import com.telcontar.webservices.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @version Oct 10, 2005
 * @author Brent Hamby 
 */
public class QueryBackAction extends Action {

  private static final Category log = Category.getInstance(QueryBackAction.class.getName());

  public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws java.lang.Exception {

    String deviceId = request.getParameter("deviceid");
    String dateStr = request.getParameter("date");
    
    //long time_mills = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS").parse(dateStr).getTime();
    //String dateStart = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS").format(new Date(time_mills));
    //String dateEnd = new SimpleDateFormat("yyyy-MM-dd HH:MM:SS").format(new Date(time_mills + 24 * 3600 * 1000));
    String dateStart = dateStr + " 00:00:00";
    String dateEnd = dateStr + " 23:59:59";
        
    DataSource ds = this.getDataSource(request, "mobile");//获得mobile数据库的连接。"mobile"要与struts-config.xml中data-source的key值对应
    Connection conn = ds.getConnection();
    PreparedStatement stat = null;
    ResultSet rs = null;

    try {
      stat = conn
          .prepareStatement("select loctype,latitude,longitude,altitude,accuracy,date from locationTable where deviceId=? AND date>? AND date<?");
      stat.setString(1, deviceId);
      stat.setString(2, dateStart);
      stat.setString(3, dateEnd);
      rs = stat.executeQuery();

      JSONObject holder = new JSONObject();
      JSONArray array = new JSONArray();
      JSONObject point = new JSONObject();
      try {
        while (rs.next()) {
          point.put("loctype", rs.getString("loctype"));
          point.put("latitude", rs.getString("latitude"));
          point.put("longitude", rs.getString("longitude"));
          point.put("altitude", rs.getString("altitude"));
          point.put("accuracy", rs.getString("accuracy"));
          point.put("date", rs.getString("date"));
          
          array.add(point);
        }
        holder.put("points", array);
        
        response.getWriter().write(holder.toString());
      }
      catch (JSONException e1) {
        e1.printStackTrace();
      }

      if (stat != null) {
        stat.close();
      }

    }
    catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}