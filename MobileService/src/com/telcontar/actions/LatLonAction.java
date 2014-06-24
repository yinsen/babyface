/*
 * LatLonAction.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.actions; 

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.telcontar.webservices.Client;
import com.telcontar.webservices.MapBean;

/**
 * For dealing with direct latitude longitude form submissions.
 * @version Sep 15, 2005 4:23:30 PM
 * @author Brent Hamby 
 */
public class LatLonAction extends Action {

    private static final Category log = Category.getInstance(LatLonAction.class.getName());
    
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws java.lang.Exception {

        HttpSession session;
        session = request.getSession(true);

        try {
            
            Client client = (Client)session.getAttribute("client");
            
            StringTokenizer stringTokenizer = new StringTokenizer(request.getParameter("latlon"),"|");
            String lat = stringTokenizer.nextToken();
            String lon = stringTokenizer.nextToken();     

            MapBean mapBean = client.queryByLatLon(lat,lon);
            // set variables in scope
            request.setAttribute("mapUrl",mapBean.getUrl());
            request.setAttribute("reqXml",mapBean.getReqXml());
            request.setAttribute("resXml",mapBean.getResXml());
            session.setAttribute("mapReq",mapBean.getMapReq());
                        
        } catch (Exception e){
            //TODO add better error handling
            log.info(e.getMessage());
        }
        return mapping.findForward("success");
    }
}