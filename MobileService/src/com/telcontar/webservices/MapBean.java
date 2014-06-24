/*
 * MapBean.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.webservices;

import com.telcontar.openls.xml.PortrayMapRequest;

/**
 * Basic items retrieved from map requests.
 * 
 * @version Sep 23, 2005 7:04:58 PM
 * @author Brent Hamby 
 */
public class MapBean {

    private String              url;
    private PortrayMapRequest   mapReq;
    private String              reqXml;
    private String              resXml;
    
    /**
     * @return Returns the mapReq.
     */
    public PortrayMapRequest getMapReq() {
        return mapReq;
    }
    /**
     * @param mapReq The mapReq to set.
     */
    public void setMapReq(PortrayMapRequest mapReq) {
        this.mapReq = mapReq;
    }
    /**
     * @return Returns the url.
     */
    public String getUrl() {
        return url;
    }
    /**
     * @param url The url to set.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    /**
     * @return Returns the reqXml.
     */
    public String getReqXml() {
        return reqXml;
    }
    /**
     * @param reqXml The reqXml to set.
     */
    public void setReqXml(String reqXml, String clientname, String clientpassword) {
        reqXml=reqXml.replaceAll(clientname,"XXX");
        reqXml=reqXml.replaceAll(clientpassword,"XXX");
        this.reqXml = reqXml;
    }
    /**
     * @return Returns the resXml.
     */
    public String getResXml() {
        return resXml;
    }
    /**
     * @param resXml The resXml to set.
     */
    public void setResXml(String resXml) {
        this.resXml = resXml;
    }
}
