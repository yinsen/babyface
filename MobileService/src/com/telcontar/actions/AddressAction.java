/*
 * AddressAction.java
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

import com.telcontar.webservices.AddressBean;
import com.telcontar.webservices.Client;

/**
 * Free form address form submissions.
 * @version Sep 23, 2005
 * @author Brent Hamby 
 */
public class AddressAction extends Action {

    private static final Category log = Category.getInstance(AddressAction.class.getName());
    
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws java.lang.Exception {

        HttpSession session;
        session = request.getSession(true);
        session.removeAttribute("startAddressCandidates");
        session.removeAttribute("endAddressCandidates");
        
        try {
            
            Client client = (Client)session.getAttribute("client");
            
            AddressBean addressBean = client.queryByAddress(request.getParameter("address"));
            // set variables in scope
            log.info(addressBean.getUrl());
            request.setAttribute("mapUrl",addressBean.getUrl());
            request.setAttribute("reqXml",addressBean.getReqXml());
            request.setAttribute("resXml",addressBean.getResXml());
            session.setAttribute("addressCandidates",addressBean.getAddressCandidates());
            session.setAttribute("mapReq",addressBean.getMapReq());
            
            // clear route info if any
            session.removeAttribute("directions");
            session.removeAttribute("totalTime");
            session.removeAttribute("totalDistance");
            
        } catch (Exception e){
            //TODO add better error handling
            log.info(e.getMessage());
        }
        return mapping.findForward("success");
    }
}