/*
 * RouteAction.java
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

import com.telcontar.webservices.Client;
import com.telcontar.webservices.RouteBean;

/**
 * Driving Directions Controller servlet.
 * 
 * @version Sep 16, 2005
 * @author Brent Hamby 
 */
public class RouteAction extends Action {

    private static final Category log = Category.getInstance(RouteAction.class.getName());

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws java.lang.Exception {

        HttpSession session;
        session = request.getSession(true);
        // clear old route info if any
        session.removeAttribute("directions");
        session.removeAttribute("totalTime");
        session.removeAttribute("totalDistance");
        session.removeAttribute("addressCandidates");
        
        try {
            // grab the Client Singleton from the application scope
            Client client = (Client)session.getAttribute("client");
            
            RouteBean routeBean = client.queryRouteByAddress(request.getParameter("address1"),request.getParameter("address2"));
            request.setAttribute("mapUrl",routeBean.getUrl());
            session.setAttribute("mapReq",routeBean.getMapReq());
            session.setAttribute("directions",routeBean.getDirections());
            session.setAttribute("totalTime",routeBean.getTotalTime());
            session.setAttribute("totalDistance",routeBean.getTotalDistance());
            
            session.setAttribute("startAddressCandidates",routeBean.getStartAddressCandidates());
            session.setAttribute("endAddressCandidates",routeBean.getEndAddressCandidates());
            
            request.setAttribute("reqXml",routeBean.getReqXml());
            request.setAttribute("resXml",routeBean.getResXml());

        } catch (Exception e){
            //TODO add better error handling
            log.info(e.getMessage());
        }
        return mapping.findForward("success");
    }
}