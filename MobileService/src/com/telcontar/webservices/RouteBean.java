/*
 * RouteBean.java
 * author Brent Hamby (brenthamby@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.webservices;

import java.util.List;

/**
 * Sub class of MapBean, adding the route fields.
 * 
 * @version Sep 23, 2005 4:21:44 PM
 * @author Brent Hamby 
 */
public class RouteBean extends MapBean {

    private List                directions;
    private String              totalTime;
    private String              totalDistance;
    private List                startAddressCandidates; 
    private List                endAddressCandidates; 

    /**
     * @return Returns the directions.
     */
    public List getDirections() {
        return directions;
    }
    /**
     * @param directions The directions to set.
     */
    public void setDirections(List directions) {
        this.directions = directions;
    }
    /**
     * @return Returns the totalDistance.
     */
    public String getTotalDistance() {
        return totalDistance;
    }
    /**
     * @param totalDistance The totalDistance to set.
     */
    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }
    /**
     * @return Returns the totalTime.
     */
    public String getTotalTime() {
        return totalTime;
    }
    /**
     * @param totalTime The totalTime to set.
     */
    public void setTotalTime(String totalTime) {
        this.totalTime = totalTime;
    }

    /**
     * @return Returns the endAddressCandidates.
     */
    public List getEndAddressCandidates() {
        return endAddressCandidates;
    }
    /**
     * @param endAddressCandidates The endAddressCandidates to set.
     */
    public void setEndAddressCandidates(List endAddressCandidates) {
        this.endAddressCandidates = endAddressCandidates;
    }
    /**
     * @return Returns the startAddressCandidates.
     */
    public List getStartAddressCandidates() {
        return startAddressCandidates;
    }
    /**
     * @param startAddressCandidates The startAddressCandidates to set.
     */
    public void setStartAddressCandidates(List startAddressCandidates) {
        this.startAddressCandidates = startAddressCandidates;
    }
}
