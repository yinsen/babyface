/*
 * MapAction.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.actions; 

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.telcontar.openls.xml.PortrayMapRequest;
import com.telcontar.webservices.Client;
import com.telcontar.webservices.MapBean;

/** 
 * Basic mapping actions include zooming in and zooming out
 * or clicking the map to recenter.
 * 
 * @version Sep 10, 2005
 * @author Brent Hamby 
 */

public class MapAction extends Action {
    
    private static final Category log = Category.getInstance(MapAction.class.getName());
    
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
                                 throws java.lang.Exception {
        
        HttpSession session;
        session = request.getSession(true);
        PortrayMapRequest mapReq = (PortrayMapRequest)session.getAttribute("mapReq");
        //  --  grab the Client from the session scope
        Client client = (Client)session.getAttribute("client");
        
        MapBean mapBean = new MapBean();
        
        try {
            // ******** MAP ZOOM 
            if (request.getParameter("zoom")!=null){
                
                if (request.getParameter("zoom").equals("in"))
                    mapReq.setZoom(-0.4F);
                else 
                    mapReq.setZoom(0.4F);
                mapBean = client.executeMapRequest(mapReq);
                String mapUrl = mapBean.getUrl();
                mapReq.setZoom(0.0F);
                
            //  ******** PAN
            } else if (request.getParameter("pan")!=null){
                
                mapReq.setPan(request.getParameter("pan"));
                mapBean = client.executeMapRequest(mapReq);
                mapReq.setPan(null);
                
            // ******** MAP CLICK 
            // uses the implicit x/y variables when <INPUT TYPE=IMAGE> is used.
            } else  if (request.getParameter("map.x")!=null){
                
                double x = Double.parseDouble(request.getParameter("map.x"));
                double y = Double.parseDouble(request.getParameter("map.y")); 
                mapBean = client.queryByMouseClick(mapReq,x,y);

            // ******** CHANGE MAP CONFIGURATION  
            } else if (request.getParameter("configuration")!=null){
                
                client.setDataSetConfiguration(request.getParameter("configuration"));
                session.setAttribute("configuration",request.getParameter("configuration"));
                mapBean = client.executeMapRequest(mapReq);
                //String mapUrl = mapBean.getUrl();                
            }
            
            // set the stateful variables
            session.setAttribute("mapReq",mapBean.getMapReq());
            request.setAttribute("mapUrl",mapBean.getUrl());
            request.setAttribute("reqXml",mapBean.getReqXml());
            request.setAttribute("resXml",mapBean.getResXml());
            
        } catch (Exception e){
            //TODO add better error handling
            log.debug(e.getMessage());
        }
        return mapping.findForward("success");
    }
}