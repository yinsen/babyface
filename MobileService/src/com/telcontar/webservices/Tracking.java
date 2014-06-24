/*
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
import com.telcontar.openls.xml.*;
import com.telcontar.openls.xml.ObjectFactory;
import com.telcontar.openls.xml.Request;
import com.telcontar.openls.xml.RequestHeader;
import com.telcontar.openls.xml.ResponseType;
import com.telcontar.openls.xml.XLSMessage;


/**
 * Contains high level connection info and query methods.  
 * @version Sep 21, 2005
 * @author Brent Hamby 
 */

public class Tracking {
    
    private static final Category log = Category.getInstance(Tracking.class.getName());
    private DataSource         dataSource;  
    private ObjectFactory      factory;
    private Marshaller         marshaller;
    private ObjectFactory      openlsObjectFactory;
    
    private String             requestId = "OQ3";
    private String             language = "en";
    private String             version = "1.0";

    
    private String             host = "http://www.us.sensornet.gov:8080/OLSTracker/track";
    private String             clientName;
    private String             clientPassword;
    private String             configuration;
    private String             sessionId ;
    private String             maximumResponses;
    private String             id;

    /** 
     * initializes dataSource marshaller
     * @param clientName
     * @param clientPassword
     * @param sessionId
     * @param maximumResponses
     */
    
    public Tracking(String clientName, String clientPassword,
            	    String sessionId, String maximumResponses, String phoneNumber) {
        super();
        this.clientName = clientName;
        this.clientPassword = clientPassword; 
        this.sessionId = sessionId;
        this.maximumResponses = maximumResponses;
        this.id=phoneNumber;
        marshaller = newMarshaller();
        openlsObjectFactory = new ObjectFactory();
        factory = new ObjectFactory();
        dataSource = newDataSource();
        dataSource.setHeader(newHeader());
    }
    
    public Tracking() {
    }
    

    /** 
     * creates data source from specified DataSourceClass and URL
     */
    private DataSource newDataSource(){
        java.util.Properties props = new java.util.Properties();
        String dataSourceClassname= System.getProperties().getProperty("com.telcontar.openls.properties.DataSourceClass", "com.telcontar.openls.client.HttpDataSource");
        props.setProperty("com.telcontar.openls.properties.DataSourceClass", dataSourceClassname);
        props.setProperty("url", host);
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
            header.setClientName(clientName);
            header.setClientPassword(clientPassword);
            header.setConfiguration(configuration);
            header.setSessionID(sessionId); 
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
            request.setMaximumResponses(new java.math.BigInteger(maximumResponses));
            request.setRequestID(requestId);
            request.setVersion(version);            
            XLSMessage msg = factory.createXLSMessage();
            msg.getBody().add(request);
            msg.setVersion(new java.math.BigDecimal(version));
            msg.setLang(language);       
            msg.setHeader(newHeader());
            return msg;
        }catch(JAXBException jaxbe){
            throw new RuntimeException(jaxbe.getMessage());
         }        
    }
    

    /**
     * synchronized method for executing map requests
     * @param mapReq
     * @return MapBean
     * @throws Exception
     */
    public synchronized List executeTrackingRequest(String phone) throws Exception{

        TrackingQueryRequest track = factory.createTrackingQueryRequest();
        track.setID(phone);
        
        XLSMessage msg = newXLSMessage(track, "TrackingQueryRequest");                                   
        //show what gets sent to server 
        //marshaller.marshal( msg, System.out );

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        marshaller.marshal( msg, b );
        
        //post to http server
        XLSMessage resp = (XLSMessage) dataSource.executeRequest(msg);
        //show what was returned from server
        //marshaller.marshal( resp, System.out );
        
        b = new ByteArrayOutputStream();
        marshaller.marshal( resp, b );
        
        TrackingQueryResponseType tqrt = (TrackingQueryResponseType)((ResponseType)resp.getBody().get(0)).getResponseParameters();
        
        ArrayList list = new ArrayList();
        log.info(((TrackingQueryResponseType.EntityType)tqrt.getEntity().get(0)).getWGS84().getLat()+","+((TrackingQueryResponseType.EntityType)tqrt.getEntity().get(0)).getWGS84().getLon());
        list.add(((TrackingQueryResponseType.EntityType)tqrt.getEntity().get(0)).getWGS84().getLat()+","+((TrackingQueryResponseType.EntityType)tqrt.getEntity().get(0)).getWGS84().getLon());
        list.add(((TrackingQueryResponseType.EntityType)tqrt.getEntity().get(0)).getTimespan().getStart() );
        return list;
    }
    
    
    public static void main(String args[]) throws Exception{
        Tracking tracking = new Tracking("Telcontar", "hendrey", "-594002901", "1", "408-294-8400");
        tracking.executeTrackingRequest("865-740-0628");
    }
    
   /** 
     * close dataSource
     */
    protected void close(){
        dataSource.close();
    }
}