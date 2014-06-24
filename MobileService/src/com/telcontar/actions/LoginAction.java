/*
 * LoginAction.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
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

import com.telcontar.webservices.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

/**
 * Authenticate the user upon login.
 * 
 * @version Sep 13, 2005
 * @author Brent Hamby 
 */

public class LoginAction extends Action {

    private static final Category log = Category.getInstance(LoginAction.class.getName());

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws java.lang.Exception {
        HttpSession session;
        session = request.getSession(true);
        session.removeAttribute("date");
        session.removeAttribute("phone");

        String account = request.getParameter("account");
        String password = request.getParameter("password");
        
        DataSource ds = this.getDataSource(request,"mobile");//获得mobile数据库的连接。"mobile"要与struts-config.xml中data-source的key值对应
        Connection conn = ds.getConnection();
        PreparedStatement stat = null;
        ResultSet rs = null;
        boolean accExist = false;
        
        try {
          stat = conn.prepareStatement("select account,password from accountTable where account=? AND password=?");
          stat.setString(1, account);
          stat.setString(2, password);
          rs = stat.executeQuery();
          accExist = rs.first();
          
          if (accExist){
            Vector<String> deviceIds = new Vector<String>();
            
            stat = conn.prepareStatement("select deviceId from bindTable where account=?");
            stat.setString(1, account);
            rs = stat.executeQuery();
            
            while (rs.next()){
              String deviceId = rs.getString("deviceId");
              deviceIds.add(deviceId);
            }
            request.setAttribute("device_list", deviceIds);
          }
          
          if (stat != null) {
            stat.close();
          }
          
        }
        catch (SQLException e) {
          e.printStackTrace();
        }
        
        if (accExist){
          Client client = new Client();
          session.setAttribute("client",client);
          session.setAttribute("configuration",client.getDataSetConfiguration());
          
          return mapping.findForward("success");
        }
        else {
          return mapping.findForward("index");
        }
        
        
        /*
        String queryString = "http://www.us.sensornet.gov:8080/OLSTracker/track?ID=aaa&address=query&badge="+request.getParameter("login")+"&pin="+request.getParameter("passWord");
        
        // get session ID from server
        URL url = new URL(queryString);
        
        URLConnection uc = url.openConnection();
        HttpURLConnection urlc = (HttpURLConnection)uc;
        uc = url.openConnection();
        urlc = (HttpURLConnection) uc;
        urlc.setRequestMethod("GET");
        urlc.setDoInput(true);
        urlc.connect();      
        log.info("Getting  data..." + urlc.getContentType());
        
        InputStream is = null;
        try {
            is = urlc.getInputStream();
        } catch (Exception e){
            return mapping.findForward("index");
        }
   
        
        byte[] bytes = new byte[256];
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0)
            offset += numRead;
        // Close the input stream and return bytes
        is.close();
        
        String reponseString = new String(bytes);
        String xlsSessionId = reponseString.substring(0,reponseString.indexOf("#"));
        log.info("full response String  "+reponseString);
        log.info("parsed out session ID "+xlsSessionId);
             
        //  session the tracking connector
        
        Tracking tracking = new Tracking("abc", xlsSessionId, xlsSessionId, 
                						 "1", (request.getParameter("phone")));
        log.info(((String)request.getAttribute("phone")));
        session.setAttribute("tracking",tracking);
        
        //  session the regular connector

        
        //  Add any of your own authentication mechanisms here!
        */
    }
}