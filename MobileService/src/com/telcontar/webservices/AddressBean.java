/*
 * MapBean.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.webservices;

import java.util.List;

/**
 * Extends MapBean and adds the address candidates for the geocode.
 * 
 * @version September 27, 2005
 * @author Brent Hamby 
 */
public class AddressBean extends MapBean {

    private List                addressCandidates; 
    
    /**
     * @return Returns the addressCandidates.
     */
    public List getAddressCandidates() {
        return addressCandidates;
    }
    /**
     * @param addressCandidates The addressCandidates to set.
     */
    public void setAddressCandidates(List addressCandidates) {
        this.addressCandidates = addressCandidates;
    }
}
