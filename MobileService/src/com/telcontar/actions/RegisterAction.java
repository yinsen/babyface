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

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

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

public class RegisterAction extends Action {

    private static final Category log = Category.getInstance(LoginAction.class.getName());

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws java.lang.Exception {
        HttpSession session;
        session = request.getSession(true);
        session.removeAttribute("date");
        session.removeAttribute("phone");


        String account=request.getParameter("account");//只能够获得以“get”形式发过来的http数据
				String password=request.getParameter("password");
				String email=request.getParameter("email");
				boolean regOk = false;
					
				DataSource ds = this.getDataSource(request,"mobile");//获得mobile数据库的连接。"mobile"要与struts-config.xml中data-source的key值对应
				Connection conn = ds.getConnection();
				PreparedStatement stat = null;
        try {
          stat = conn.prepareStatement("select account from accountTable where account=?");
          stat.setString(1, account);
	
          ResultSet rs = stat.executeQuery();
          JSONObject holder = new JSONObject();
          if (rs.first()){
            holder.put("register", "exist");
            response.getWriter().write(holder.toString());
          }
          else {
						stat=conn.prepareStatement("insert into accountTable values('"+account+"','"+password+"','"+email+"',10);");
						regOk = stat.executeUpdate()>0?true:false;
						if (regOk){
						  holder.put("register", "success");
	            response.getWriter().write(holder.toString());
						}
						else{
						  holder.put("register", "fail");
              response.getWriter().write(holder.toString());
						}
          }
          
          if (stat != null) {
            stat.close();
          }
        }
        catch (JSONException e1) {
          e1.printStackTrace();
        }
        catch (SQLException e) {
          e.printStackTrace();
        }
        
        if (regOk){
          //设置成为decarta的client端
          Client client = new Client();
          session.setAttribute("client",client);
          session.setAttribute("configuration",client.getDataSetConfiguration());
          
          //设置下拉列表框为空
//          Vector<String> deviceIds = new Vector<String>();
//          request.setAttribute("device_list", deviceIds);
          
          //因为是用js访问此action，所以不从这里跳转到map.jsp而由js跳转
          return null;
        }
        else {
          return null;
        }
    }
}
