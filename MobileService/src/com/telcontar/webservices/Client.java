/*
 * Client.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.webservices;

import java.io.ByteArrayOutputStream;
import java.util.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Category;

import com.telcontar.openls.client.DataSource;
import com.telcontar.openls.client.DataSourceFactory;
import com.telcontar.openls.client.OpenlsDataSourceFactory;
import com.telcontar.openls.xml.AbstractRequestParametersType;
import com.telcontar.openls.xml.AddressType;
import com.telcontar.openls.xml.CenterContextType;
import com.telcontar.openls.xml.ContentType;
import com.telcontar.openls.xml.DetermineRouteRequest;
import com.telcontar.openls.xml.DetermineRouteResponseType;
import com.telcontar.openls.xml.EnvelopeType;
import com.telcontar.openls.xml.GeocodedAddressType;
import com.telcontar.openls.xml.MapType;
import com.telcontar.openls.xml.ObjectFactory;
import com.telcontar.openls.xml.OutputType;
import com.telcontar.openls.xml.PortrayMapRequest;
import com.telcontar.openls.xml.PortrayMapResponse;
import com.telcontar.openls.xml.Request;
import com.telcontar.openls.xml.RequestHeader;
import com.telcontar.openls.xml.ResponseType;
import com.telcontar.openls.xml.RouteGeometryRequestType;
import com.telcontar.openls.xml.RouteInstructionsRequestType;
import com.telcontar.openls.xml.RouteMap;
import com.telcontar.openls.xml.RoutePlanType;
import com.telcontar.openls.xml.RoutePreference;
import com.telcontar.openls.xml.WayPointListType;
import com.telcontar.openls.xml.WayPointType;
import com.telcontar.openls.xml.XLSMessage;

/**
 * Contains high level connection info and query methods.  
 * @version Sep 21, 2005
 * @author Brent Hamby 
 */

public class Client {
    
    private static final Category log = Category.getInstance(Client.class.getName());
    private DataSource         dataSource;  
    private ObjectFactory      factory;
    private Marshaller         marshaller;
    private Configuration      config;
    private ObjectFactory      openlsObjectFactory;
    private ClientUtilities    util;
    private String             height;
    private String             width;

    /** 
     * initializes dataSource marshaller
     */
    public Client() {
        config = Configuration.getInstance();
        marshaller = newMarshaller();
        height = config.getHeight();
        width = config.getWidth();
        util = new ClientUtilities();
        openlsObjectFactory = new ObjectFactory();
        factory = new ObjectFactory();
        dataSource = newDataSource();
        dataSource.setHeader(newHeader());
    }
    
    /**
     * client session configuration of <B>(data set / image settings)</B>
     */
    public void setDataSetConfiguration(String config){
        this.config.setConfiguration(config);
    }
    
    /**
     * client session configuration of <B>(data set / image settings)</B>
     * @return String
     */
    public String getDataSetConfiguration(){
        return this.config.getConfiguration();
    }
    
    /** 
     * creates data source from specified DataSourceClass and URL
     */
    private DataSource newDataSource(){
        java.util.Properties props = new java.util.Properties();
        String dataSourceClassname= System.getProperties().getProperty("com.telcontar.openls.properties.DataSourceClass", "com.telcontar.openls.client.HttpDataSource");
        props.setProperty("com.telcontar.openls.properties.DataSourceClass", dataSourceClassname);
        props.setProperty("url", config.getHost());
        DataSourceFactory dsFactory = new OpenlsDataSourceFactory(props); 
        return dsFactory.newDataSource();
    }
    
    /** 
     * creates JAXB marshaller from com.telcontar.openls.xml
     */
    private Marshaller newMarshaller(){
        try{
            JAXBContext jc = JAXBContext.newInstance( "com.telcontar.openls.xml" );
            marshaller = jc.createMarshaller();
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );   
            return marshaller;
        }catch(JAXBException jaxbe){
            throw new RuntimeException(jaxbe.getMessage());
        }
    }
    
    /** 
     * creates request header containing credentials found in webservices.properies
     */
    protected RequestHeader newHeader(){
        
        try{
            RequestHeader header = factory.createRequestHeader();
            header.setClientName(config.getClientName());
            header.setClientPassword(config.getClientPassword());
            header.setConfiguration(config.getConfiguration());
            header.setSessionID(config.getSessionId()); 
            return header;
        }catch(JAXBException jaxbe){
            throw new RuntimeException(jaxbe.getMessage());
        }
    }
    
    /** 
     * creates new XLSMessage from the request params, that will be sent to the server.
     */
    protected XLSMessage newXLSMessage(AbstractRequestParametersType params, String methodName){
        try{
            Request request = factory.createRequest();
            request.setRequestParameters(params);
            request.setMethodName(methodName);
            request.setMaximumResponses(new java.math.BigInteger(config.getMaximumResponses()));
            request.setRequestID(config.getRequestId());
            request.setVersion(config.getVersion());            
            XLSMessage msg = factory.createXLSMessage();
            msg.getBody().add(request);
            msg.setVersion(new java.math.BigDecimal(config.getVersion()));
            msg.setLang(config.getLanguage());       
            msg.setHeader(newHeader());
            return msg;
        }catch(JAXBException jaxbe){
            throw new RuntimeException(jaxbe.getMessage());
         }        
    }
    
    /**
     * Query Route with start and stop addresses.
     * @param origin_p
     * @param destination_p
     * @return RouteBean
     * @throws Exception
     */
    public RouteBean queryRouteByAddress(String origin_p, String destination_p) throws Exception{
        
        AddressType origin = util.createFreeFormAddress(origin_p);
        AddressType destination = util.createFreeFormAddress(destination_p);
        DetermineRouteRequest routeReq = factory.createDetermineRouteRequest();
        RoutePlanType routePlan = factory.createRoutePlanType();
        routeReq.setRoutePlan(routePlan);
        routeReq.setProvideRouteHandle(false);
        RoutePreference pref = factory.createRoutePreference("Fastest");
        routePlan.setRoutePreference(pref.getValue());
        WayPointType start = factory.createWayPointType();
        start.setLocation(origin);
        WayPointType end = factory.createWayPointType();
        end.setLocation(destination);
        WayPointListType waypoints = factory.createWayPointListType();
        waypoints.setStartPoint(start);
        waypoints.setEndPoint(end);
        routePlan.setWayPointList(waypoints);
        RouteInstructionsRequestType routeInstructions = factory.createRouteInstructionsRequestType();
        routeReq.setRouteInstructionsRequest(routeInstructions);
        RouteGeometryRequestType routeGeometryRequest = factory.createRouteGeometryRequest();
        routeReq.setRouteGeometryRequest(routeGeometryRequest);
        return executeRouteRequest(routeReq);
    }     
    
    /**
     * Query for map with an address (Street City, State Zip)
     * @param address
     * @return AddressBean
     * @throws Exception
     */
    public AddressBean queryByAddress(String address) throws Exception{
        PortrayMapRequest mapReq = util.getPortrayMapParams(address, "1", height, width);
        return executeMapGeocodeRequest(mapReq);
    }

    /**
     * Get mapbean from lat lon
     * @param lat
     * @param lon
     * @return MapBean
     * @throws Exception
     */
    public MapBean queryByLatLon(String lat, String lon) throws Exception{
        PortrayMapRequest mapReq = util.getPortrayMapParams(lat,lon,"1", height, width);
        return executeMapRequest(mapReq);
    }

    
    /**
     * Get a map with a pixel x and y from a mouse click.
     * @param mapReq
     * @param x
     * @param y
     * @return MapBean
     * @throws Exception
     */
    public MapBean queryByMouseClick(PortrayMapRequest mapReq, double x, double y) throws Exception{
        return executeMapRequest(util.recenterOnMapClick(x,y,mapReq));
    }
    
    
    
    
    
    /**
     * Query for map with an address (Street City, State Zip)
     * @param address
     * @return AddressBean
     * @throws Exception
     */
    public MapBean queryWithPositions(List points) throws Exception{
        PortrayMapRequest mapReq = util.getPortrayMapParamsWithOverlays(points, height, width);
        return executeMapPositionsRequest(mapReq);
    }

    
    
    
    
    /**
     * synchronized method for executing geocode requests
     * @param mapReq
     * @return AddressBean
     * @throws Exception
     */
    public synchronized MapBean executeMapPositionsRequest(PortrayMapRequest mapReq) throws Exception{

        MapBean mapBean = new MapBean();
        
        //mapReq.setFitOverlays(false);
        XLSMessage msg = newXLSMessage(mapReq, "PortrayMapRequest");                                   
        //show what gets sent to server 
        if (config.getDisplayRequestXML())
            marshaller.marshal( msg, System.out );

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        marshaller.marshal( msg, b );
        mapBean.setReqXml(new String(b.toByteArray()), config.getClientName(), config.getClientPassword());
        
        //post to http server
        XLSMessage resp = (XLSMessage) dataSource.executeRequest(msg);
        //show what was returned from server
        if(config.getDisplayResponseXML())
            marshaller.marshal( resp, System.out );
        
        b = new ByteArrayOutputStream();
        marshaller.marshal( resp, b );
        mapBean.setResXml(new String(b.toByteArray()));
        
        PortrayMapResponse pmr = (PortrayMapResponse)((ResponseType)resp.getBody().get(0)).getResponseParameters();

        //  get the geocoded address candidates
        //addressBean.setAddressCandidates(pmr.getGeocodeResponseList().getGeocodedAddress());
        
        //  Get center context & bounding box out of the response 
        //  and put it back in the sessioned PortrayMapRequest.
        //CenterContextType newCC  = ((CenterContextType)((MapType)pmr.getMap().get(0)).getCenterContext());
        //EnvelopeType newBB  = ((EnvelopeType)((MapType)pmr.getMap().get(0)).getBBoxContext());
        
        //((OutputType)mapReq.getOutput().get(0)).setCenterContext(newCC);
       // ((OutputType)mapReq.getOutput().get(0)).setBBoxContext(newBB);
        
       // ((OutputType)mapReq.getOutput().get(0)).setCenterAddress(null);
        
        //remove the old overlay that just centers the icon
       // mapReq.getOverlay().clear();

        //  add the lat lon from the response so the icon stays put at the same lat lon.
        //Double lat = (Double)((GeocodedAddressType)pmr.getGeocodeResponseList().getGeocodedAddress().get(0)).getPoint().getPos().getValue().get(0);
        //Double lon = (Double)((GeocodedAddressType)pmr.getGeocodeResponseList().getGeocodedAddress().get(0)).getPoint().getPos().getValue().get(1);
        //util.addIconWithLatLon(lat.toString(),lon.toString(),mapReq);
        
       // mapReq.setScreenCoordinate(null);
        mapReq.setFitOverlays(false);
        mapBean.setUrl(((ContentType)((MapType)pmr.getMap().get(0)).getContent()).getURL());
        mapBean.setMapReq(mapReq);
        return mapBean;
    }
    
    
    
    
    /**
     * synchronized method for executing geocode requests
     * @param mapReq
     * @return AddressBean
     * @throws Exception
     */
    public synchronized AddressBean executeMapGeocodeRequest(PortrayMapRequest mapReq) throws Exception{

        AddressBean addressBean = new AddressBean();
        
        //mapReq.setFitOverlays(false);
        XLSMessage msg = newXLSMessage(mapReq, "PortrayMapRequest");                                   
        //show what gets sent to server 
        if (config.getDisplayRequestXML())
            marshaller.marshal( msg, System.out );

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        marshaller.marshal( msg, b );
        addressBean.setReqXml(new String(b.toByteArray()), config.getClientName(), config.getClientPassword());
        
        //post to http server
        XLSMessage resp = (XLSMessage) dataSource.executeRequest(msg);
        //show what was returned from server
        if(config.getDisplayResponseXML())
            marshaller.marshal( resp, System.out );
        
        b = new ByteArrayOutputStream();
        marshaller.marshal( resp, b );
        addressBean.setResXml(new String(b.toByteArray()));
        
        PortrayMapResponse pmr = (PortrayMapResponse)((ResponseType)resp.getBody().get(0)).getResponseParameters();

        //  get the geocoded address candidates
        addressBean.setAddressCandidates(pmr.getGeocodeResponseList().getGeocodedAddress());
        
        //  Get center context & bounding box out of the response 
        //  and put it back in the sessioned PortrayMapRequest.
        CenterContextType newCC  = ((CenterContextType)((MapType)pmr.getMap().get(0)).getCenterContext());
        EnvelopeType newBB  = ((EnvelopeType)((MapType)pmr.getMap().get(0)).getBBoxContext());
        
        ((OutputType)mapReq.getOutput().get(0)).setCenterContext(newCC);
        ((OutputType)mapReq.getOutput().get(0)).setBBoxContext(newBB);
        
        ((OutputType)mapReq.getOutput().get(0)).setCenterAddress(null);
        
        //remove the old overlay that just centers the icon
        mapReq.getOverlay().clear();

        //  add the lat lon from the response so the icon stays put at the same lat lon.
        Double lat = (Double)((GeocodedAddressType)pmr.getGeocodeResponseList().getGeocodedAddress().get(0)).getPoint().getPos().getValue().get(0);
        Double lon = (Double)((GeocodedAddressType)pmr.getGeocodeResponseList().getGeocodedAddress().get(0)).getPoint().getPos().getValue().get(1);
        util.addIconWithLatLon(lat.toString(),lon.toString(),mapReq);
        
        mapReq.setScreenCoordinate(null);
        addressBean.setUrl(((ContentType)((MapType)pmr.getMap().get(0)).getContent()).getURL());
        addressBean.setMapReq(mapReq);
        return addressBean;
    }
    
    /**
     * synchronized method for executing map requests
     * @param mapReq
     * @return MapBean
     * @throws Exception
     */
    public synchronized MapBean executeMapRequest(PortrayMapRequest mapReq) throws Exception{

        MapBean mapBean = new MapBean();
        
        //mapReq.setFitOverlays(false);
        XLSMessage msg = newXLSMessage(mapReq, "PortrayMapRequest");                                   
        //show what gets sent to server 
        if (config.getDisplayRequestXML())
            marshaller.marshal( msg, System.out );

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        marshaller.marshal( msg, b );
        mapBean.setReqXml(new String(b.toByteArray()), config.getClientName(), config.getClientPassword());
        
        //post to http server
        XLSMessage resp = (XLSMessage) dataSource.executeRequest(msg);
        //show what was returned from server
        if(config.getDisplayResponseXML())
            marshaller.marshal( resp, System.out );
        
        b = new ByteArrayOutputStream();
        marshaller.marshal( resp, b );
        mapBean.setResXml(new String(b.toByteArray()));
        
        PortrayMapResponse pmr = (PortrayMapResponse)((ResponseType)resp.getBody().get(0)).getResponseParameters();

        //  Get center context & bounding box out of the response 
        //  and put it back in the sessioned PortrayMapRequest.
        CenterContextType newCC  = ((CenterContextType)((MapType)pmr.getMap().get(0)).getCenterContext());
        EnvelopeType newBB  = ((EnvelopeType)((MapType)pmr.getMap().get(0)).getBBoxContext());
        
        ((OutputType)mapReq.getOutput().get(0)).setCenterContext(newCC);
        ((OutputType)mapReq.getOutput().get(0)).setBBoxContext(newBB);
        
        ((OutputType)mapReq.getOutput().get(0)).setCenterAddress(null);
        mapReq.setScreenCoordinate(null);
        mapBean.setUrl(((ContentType)((MapType)pmr.getMap().get(0)).getContent()).getURL());
        mapBean.setMapReq(mapReq);
        return mapBean;
    }

    /**
     * synchronized method for executing route requests
     * @param params
     * @return RouteBean
     * @throws Exception
     */
    public synchronized RouteBean executeRouteRequest(DetermineRouteRequest params) throws Exception{
        
        try {
            
            RouteBean routeBean = new RouteBean();
            
            //configure the request so that it asks for default maps, which include one
            //map for each maneuver and a final map, which is for the overview
            util.includeMapsInResponse(params);
            //configure the request to return Geography Markup Language (GML) geometry for the route
            //includeGeometryInResponse(params);
            XLSMessage msg = newXLSMessage(params, "DetermineRouteRequest");                                   
            
            //show what gets sent to server
            if(config.getDisplayRequestXML())
                marshaller.marshal( msg, System.out );
            
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            marshaller.marshal( msg, b );
            routeBean.setReqXml(new String(b.toByteArray()), config.getClientName(), config.getClientPassword());
            
            //post to http server
            XLSMessage resp = (XLSMessage) dataSource.executeRequest(msg);
            //show what was returned from server
            if(config.getDisplayResponseXML())
                marshaller.marshal( resp, System.out );
            
            b = new ByteArrayOutputStream();
            marshaller.marshal( resp, b );
            routeBean.setResXml(new String(b.toByteArray()));
            
            //at this point we have a response containing the route handle. Extract the handle
            ResponseType response = (ResponseType)resp.getBody().get(0);
            DetermineRouteResponseType respParams = (DetermineRouteResponseType)response.getResponseParameters();

            //  get the geocoded address candidates
            routeBean.setEndAddressCandidates(respParams.getEndAddressCandidates().getGeocodedAddress());
            routeBean.setStartAddressCandidates(respParams.getStartAddressCandidates().getGeocodedAddress());
            
            routeBean.setUrl( ((RouteMap)respParams.getRouteMap().get(respParams.getRouteMap().size()-1)).getContent().getURL());
            routeBean.setDirections( respParams.getRouteInstructionsList().getRouteInstruction());
            routeBean.setTotalTime( respParams.getRouteSummary().getTotalTime());
            routeBean.setTotalDistance( respParams.getRouteSummary().getTotalDistance().getValue()+""+respParams.getRouteSummary().getTotalDistance().getUom());
            routeBean.setMapReq(util.setMapReqFromRouteMap(respParams));
            return routeBean;

        } finally {
            close(); //release resources, in this case, the dataSource
        }
    }

    /**
     * main methods which tests the three queryBy methods.
     * @param args
     */
    public static void main(String args[]){
        
        Client test = new Client();
        try {

            System.out.println("Connecting . . .\n");
            
            // test the lat/lng query 
            MapBean mapBean = test.queryByLatLon("37.8487","-122.265415");
            log.info("1.) lat lon test: 37.8487,-122.265415");
            log.info("  URL "+mapBean.getUrl());
            
            System.out.println("\n\n");

            // test the address query
            log.info("2.) geocode test: 6300 Shattuck Ave, Oakland 94609");
            AddressBean addressBean = test.queryByAddress("6300 Shattuck Ave, Oakland 94609");
            log.info("  URL "+addressBean.getUrl());
            log.info("  geocode top candidate "+(((GeocodedAddressType)addressBean.getAddressCandidates().get(0)).getAddress().getFreeFormAddress()));
            
            System.out.println("\n\n");
            
            // test the route query
            log.info("3.) route test: 6300 Shattuck Ave, Oakland 94609,  612 Howard Street, San Francisco, CA 94105");
            RouteBean routeBean = test.queryRouteByAddress("6300 Shattuck Ave, Oakland 94609","612 Howard Street, San Francisco, CA 94105");
            log.info("  URL "+routeBean.getUrl());
            log.info("  geocode top start candidate "+(((GeocodedAddressType)routeBean.getStartAddressCandidates().get(0)).getAddress().getFreeFormAddress()));
            log.info("  geocode top end candidate "+(((GeocodedAddressType)routeBean.getEndAddressCandidates().get(0)).getAddress().getFreeFormAddress()));
            System.out.println("\n\n");
            System.out.println("Test result: SUCCESS!");
            
        } catch (Exception e ) {
            System.out.println("Test result: FAIL!");
            System.out.println("\n");
            System.out.println("PLEASE CHECK YOUR webservices.properties FILE");
            System.out.println("This file is located in WEB-INF/src and deployed to WEB-INF/classes");
            System.out.println("Verify that you have the correct:");
            System.out.println("host=");
            System.out.println("clientname=");
            System.out.println("clientpassword=");
            System.out.println("configuration=");
            //e.printStackTrace();
        }
    }		
    
    /** 
     * close dataSource
     */
    protected void close(){
        dataSource.close();
    }
}