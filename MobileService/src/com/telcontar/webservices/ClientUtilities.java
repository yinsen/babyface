/*
 * ClientUtilities.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.webservices;

import java.math.BigInteger;
import java.util.*;

import com.telcontar.openls.xml.AddressType;
import com.telcontar.openls.xml.CenterAddressType;
import com.telcontar.openls.xml.CenterContextType;
import com.telcontar.openls.xml.DetermineRouteRequest;
import com.telcontar.openls.xml.DetermineRouteResponseType;
import com.telcontar.openls.xml.DirectPositionType;
import com.telcontar.openls.xml.ObjectFactory;
import com.telcontar.openls.xml.OutputType;
import com.telcontar.openls.xml.OverlayType;
import com.telcontar.openls.xml.PointType;
import com.telcontar.openls.xml.PortrayMapRequest;
import com.telcontar.openls.xml.PositionType;
import com.telcontar.openls.xml.RadiusType;
import com.telcontar.openls.xml.RouteMap;
import com.telcontar.openls.xml.RouteMapOutputType;
import com.telcontar.openls.xml.RouteMapRequestType;
import com.telcontar.openls.xml.ScreenCoordinateType;
import com.telcontar.openls.xml.StyleType;

/**
 * Basic client utilities for navigating and creating JAXB xml types.
 * 
 * @version Sep 23, 2005
 * @author Brent Hamby 
 */
public class ClientUtilities {
    
    protected ObjectFactory factory = new ObjectFactory();
    
    public PortrayMapRequest setMapReqFromRouteMap(DetermineRouteResponseType respParams ) throws Exception {
        
        PortrayMapRequest mapReq = factory.createPortrayMapRequest();
        OutputType desiredOutput = factory.createOutputType();
        desiredOutput.setBBoxContext(((RouteMap)respParams.getRouteMap().get(respParams.getRouteMap().size()-1)).getBBoxContext());
        desiredOutput.setWidth(new java.math.BigInteger(String.valueOf(500)));
        desiredOutput.setHeight(new java.math.BigInteger(String.valueOf(500))); 
        mapReq.getOutput().add(desiredOutput);
        //  --  add the route geometry to the overlays
        OverlayType overlay = factory.createOverlayType();
        overlay.setRouteGeometry(respParams.getRouteGeometry());
        mapReq.getOverlay().add(overlay);
        mapReq.setFitOverlays(false);
        return mapReq;
    }
    
    public AddressType createFreeFormAddress(String freeformAddr) throws Exception{
        AddressType addr = factory.createAddress();
        addr.setFreeFormAddress(freeformAddr);
        addr.setCountryCode("US");
        return addr;
    }
    
    public void includeMapsInResponse(DetermineRouteRequest routeReq) throws Exception{
        RouteMapRequestType mapReq = factory.createRouteMapRequestType();
        RouteMapOutputType output = factory.createRouteMapOutputType();
        output.setHeight(new BigInteger("500"));
        output.setWidth(new BigInteger("500"));
        output.setStyle("Overview");
        mapReq.getOutput().add(output); //request default map behavior by specifying an "empty" output specification
        
        routeReq.setRouteMapRequest(mapReq);                 
    }
    public PortrayMapRequest recenterOnMapClick(double x, double y, PortrayMapRequest mapReq) throws Exception {
        //center the mapReq on the x,y pixels
        ScreenCoordinateType click = factory.createScreenCoordinateType();
        click.setX((short)x);
        click.setY((short)y);
        mapReq.setScreenCoordinate(click);
        return mapReq;
    }

    protected CenterAddressType createCenterAddress(String address, String km) throws Exception{
        AddressType addr = newAddress(address);
        CenterAddressType centerAddress = factory.createCenterAddressType();
        centerAddress.setAddress(addr);
        RadiusType radius = factory.createRadiusType();
        radius.setUnit("KM");
        radius.setValue(new java.math.BigDecimal(km));
        centerAddress.setRadius(radius);        
        return centerAddress;
    }

    protected AddressType newAddress(String line) throws Exception{
        AddressType address  = factory.createAddressType();
        address.setFreeFormAddress(line);
        address.setCountryCode("US");
        return address;
    }

    protected PortrayMapRequest getPortrayMapParams(String address, String km, String width, String height) throws Exception{
        PortrayMapRequest mapReq = factory.createPortrayMapRequest();
        CenterAddressType centerAddress = createCenterAddress(address, km);
        OutputType desiredOutput = factory.createOutputType();
        desiredOutput.setCenterAddress(centerAddress);
        desiredOutput.setWidth(new java.math.BigInteger(width));
        desiredOutput.setHeight(new java.math.BigInteger(height)); 
        mapReq.getOutput().add(desiredOutput);

        //  this sets an icon on the center of the map.
        
        OverlayType overylayType = factory.createOverlayType();
        StyleType styleType = factory.createStyleType();
        styleType.setStyleContent("red-dot.gif:,Font3,TL");
        overylayType.setStyle(styleType);
        mapReq.getOverlay().add(overylayType);        

        return mapReq;
    }    

    protected PortrayMapRequest getPortrayMapParams(String lat, String lon, String km, String width, String height) throws Exception{
        
        PortrayMapRequest mapReq = factory.createPortrayMapRequest();

        CenterContextType centerContext = factory.createCenterContextType();
        PointType center = factory.createPointType();
        center.setPos(factory.createDirectPositionType());
        center.getPos().getValue().add(new Double(lat));
        center.getPos().getValue().add(new Double(lon));        
        centerContext.setCenterPoint(center);
        centerContext.setSRS("WGS-84");
        RadiusType radiusType = factory.createRadiusType();
        radiusType.setUnit("KM");
        radiusType.setValue(new java.math.BigDecimal(km));
        centerContext.setRadius(radiusType);
        OutputType desiredOutput = factory.createOutputType();
        desiredOutput.setCenterContext(centerContext);
        desiredOutput.setWidth(new java.math.BigInteger(width));
        desiredOutput.setHeight(new java.math.BigInteger(height)); 
        mapReq.getOutput().add(desiredOutput);
        
        //  this sets an icon on the lat / lon
        addIconWithLatLon(lat,lon,mapReq);

        return mapReq;
    }    

    protected void addIconWithLatLon(String lat, String lon, PortrayMapRequest mapReq) throws Exception{
        OverlayType overlay = factory.createOverlayType();
        DirectPositionType pos1 = factory.createDirectPositionType();
        pos1.getValue().add(new Double(lat));
        pos1.getValue().add(new Double(lon));   
        PointType pointType = factory.createPointType();
        pointType.setPos(pos1);
        PositionType positionType = factory.createPositionType();
        positionType.setPoint(pointType);
        overlay.setPosition(positionType); 
        StyleType style = factory.createStyleType();
        style.setName("red-dot.gif");
        overlay.setStyle(style);
        mapReq.getOverlay().add(overlay);
    }

    protected PortrayMapRequest getPortrayMapParamsWithOverlays(List points, String width, String height) throws Exception{

        PortrayMapRequest mapReq = factory.createPortrayMapRequest();
        OverlayType overlay = factory.createOverlayType();
        OutputType output = factory.createOutputType();

        //      Iterator itr = points.iterator();
        //	  while (itr.hasNext()) {
        //String position = (String)itr.next();
        
        
        String position = (String)points.get(0);
        StringTokenizer strTok = new StringTokenizer(position, ",");
        String lat = strTok.nextToken();
        String lng = strTok.nextToken();
        
        
        DirectPositionType pos1 = factory.createDirectPositionType();
        pos1.getValue().add(new Double(lat));
        pos1.getValue().add(new Double(lng));   
        PointType pointType = factory.createPointType();
        pointType.setPos(pos1);
        PositionType positionType = factory.createPositionType();
        positionType.setPoint(pointType);
        overlay.setPosition(positionType); 
        
        StyleType style = factory.createStyleType();
        style.setStyleContent("blue-dot.gif:,FONT3,TL");

        overlay.setStyle(style);
        mapReq.getOverlay().add(overlay);
        //mapReq.setFitOverlays(true);
        
        CenterContextType centerContext = factory.createCenterContextType();
        PointType center = factory.createPointType();
        center.setPos(factory.createDirectPositionType());
        center.getPos().getValue().add(new Double(lat));
        center.getPos().getValue().add(new Double(lng));        
        centerContext.setCenterPoint(center);
        centerContext.setSRS("WGS-84");
        RadiusType radiusType = factory.createRadiusType();
        radiusType.setUnit("KM");
        radiusType.setValue(new java.math.BigDecimal("1"));
        centerContext.setRadius(radiusType);
        OutputType desiredOutput = factory.createOutputType();
        desiredOutput.setCenterContext(centerContext);
        desiredOutput.setWidth(new java.math.BigInteger(width));
        desiredOutput.setHeight(new java.math.BigInteger(height)); 
        mapReq.getOutput().add(desiredOutput);
        return mapReq;
    }        
}
