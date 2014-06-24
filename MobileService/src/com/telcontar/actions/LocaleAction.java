/*
 * LocaleAction.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.actions; 

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Category;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Change Locale.
 * 
 * @version 1.0 20 Apr 2005
 * @author Brent Hamby 
 */

public class LocaleAction extends Action {

    private static final Category log = Category.getInstance(LocaleAction.class.getName());

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws java.lang.Exception {
        HttpSession session;
        session = request.getSession(true);
        setLocale(request, new Locale(request.getParameter("country"),request.getParameter("language")));
        return mapping.findForward("success");
    }
}
