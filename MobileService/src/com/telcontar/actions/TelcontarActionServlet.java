/*
 * TelcontarActionServlet.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.actions;

import javax.servlet.ServletException;

import org.apache.log4j.Category;
import org.apache.struts.action.ActionServlet;

/**
 * Overrides ActionServlet to start up associated applications
 * when the web container (e.g. TOMCAT) starts.  These options
 * are configured in <code>web.xml</code>
 *
 * @version Sep 16, 2005
 * @author Brent Hamby 
 */
public class TelcontarActionServlet extends ActionServlet {

    private static final Category log = Category.getInstance(TelcontarActionServlet.class.getName());

    public void init() throws ServletException {
        super.init();
        // set any application level variables here, singletons etc. etc.
    }
}
