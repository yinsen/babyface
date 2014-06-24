/*
 * DecartaReverseGeocoder.java
 *
 * Created on September 22, 2004, 3:22 PM
 */

package com.infolands.locationserver;

import javax.xml.bind.*;
import java.util.*;

import com.telcontar.openls.xml.*;
import com.telcontar.openls.server.Util;
import com.decarta.shifting.ShiftTool;

/**
 *
 * @author  ghendrey
 */
public class DecartaReverseGeocoder{
    
	String SESSION_ID = "123";
	String DEVICE_TYPE = "phone/G1 xxx";
	final String CLIENT_NAME = "map-sample-app";
	final String CLIENT_PASSWORD = "letmein";
	final String SERVICE_CONFIGURATION = "global-decarta";        
	final String MAX_RESPONSES = "10";
	final String VERSION = "1.0";
	final String LANG = "en";
	
	com.telcontar.openls.xml.ObjectFactory factory = null;    
	com.telcontar.openls.client.HttpDataSource ds = null;
	Marshaller marshaller;
	int requestId;
	JAXBContext jc;
	
    
    
    /** Creates a new instance of DecartaReverseGeocoderTest */
    public DecartaReverseGeocoder() {

        String oldurl = System.setProperty("url", "http://hws-staging.en.cn.decartahws.com/openls/openls");
        ds = new com.telcontar.openls.client.HttpDataSource(System.getProperties());
        factory = new com.telcontar.openls.xml.ObjectFactory();  
        
        try{
            jc = JAXBContext.newInstance( "com.telcontar.openls.xml" );
            marshaller = jc.createMarshaller();
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );        
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
    
    protected XLSMessage wrapParametersInXLSMessage(AbstractRequestParametersType params, String methodName) throws Exception{
        XLSMessage msg = factory.createXLSMessage();
        RequestHeader header = factory.createRequestHeader();
        header.setClientName(System.getProperties().getProperty("clientName",CLIENT_NAME));
        header.setClientPassword(System.getProperties().getProperty("clientPassword",CLIENT_PASSWORD));
        header.setConfiguration(System.getProperties().getProperty("serviceConfiguration",SERVICE_CONFIGURATION));
        header.setDeviceType(DEVICE_TYPE);
        header.setSessionID(SESSION_ID);
        msg.setHeader((AbstractHeaderType)header); 

        Request request = factory.createRequest();
        request.setRequestParameters(params);
        request.setMethodName(methodName);
        request.setMaximumResponses(new java.math.BigInteger(MAX_RESPONSES));
        request.setRequestID(String.valueOf(++requestId));
        request.setVersion(VERSION);

        msg.getBody().add(request);
        msg.setVersion(new java.math.BigDecimal(VERSION));
        msg.setLang(LANG);   
        
        return msg;
    }    
     protected ReverseGeocodeRequestType getReverseGeocodeRequest(DirectPositionType dirPos) throws Exception{
        ReverseGeocodeRequest geo = factory.createReverseGeocodeRequest();
        PositionType position = factory.createPositionType();
        PointType point = factory.createPointType();
        point.setPos(dirPos);
        position.setPoint(point);
        geo.setPosition(position); 
        return geo;
     }     
     
    protected XLSMessage formulateRevGeoMessage(DirectPositionType dirPos) throws Exception{
        ReverseGeocodeRequestType geo = getReverseGeocodeRequest(dirPos);
        return wrapParametersInXLSMessage(geo, "ReverseGeocodeRequest");
     }
    
    
    protected AddressType revGeo(DirectPositionType dirPos, com.telcontar.openls.client.DataSource ds) throws Exception{
        XLSMessage msg = formulateRevGeoMessage(dirPos);
        XLSMessage resp = (XLSMessage) ds.executeRequest(msg);
        marshaller.marshal(resp, System.out);
        ReverseGeocodeResponseType respParams = (ReverseGeocodeResponseType)((Response)resp.getBody().get(0)).getResponseParameters();
        List revGeoLocations = respParams.getReverseGeocodedLocation();
        if (revGeoLocations.isEmpty()){
            return null;
        }else{
            return ((ReverseGeocodedLocationType)revGeoLocations.get(0)).getAddress();
        }
    }
   
    
     
     private boolean streetNameMissing(AddressType addr){
            if (null ==addr){ //if could not geocode the origin
                return true; //skip to next lat/long pair
            }
            if(null != addr.getStreetAddress()){
                if (null != addr.getStreetAddress().getStreet() && null != addr.getStreetAddress().getStreet().getValue()){
                    if (addr.getStreetAddress().getStreet().getValue().equals("") || addr.getStreetAddress().getStreet().getValue().equals(" ")){
                        return true;
                    }                    
                }else{
                    return true;
                }
            }else{
                return true;
            }
            return false;
     }
     
	public String doShifting(double lati, double longi, float alti) {
	    // don't shift if lat/lon is out of China boundary
		if (lati > 53.55 || lati < 3.8667
		    || longi < 73.6667 || longi > 135.0417)
		  return Double.toString(lati)+","+Double.toString(longi);
		
		String rtn = ShiftTool.shift(1, longi, lati, (int) alti);
		
		// shift library will return "0.0,0.0" or "0,0", if shifting is failed.
		if (rtn.equals("0.0,0.0") || rtn.equals("0,0")) return null;
		
		double lat = Double.parseDouble(rtn.split(",")[0]);
		double lon = Double.parseDouble(rtn.split(",")[1]);
		
		// ignore if shifted lat/lon is out of China boundary
		if (lat > 53.55 || lat < 3.8667 || lon < 73.6667 || lon > 135.0417)
		  return doShifting(lat, lon, (int) alti);
		
		return Double.toString(lat)+","+Double.toString(lon);
	}
     
  public String revGeocoder(String latilong) { 
    
  	DirectPositionType requestPoint = Util.llStringToDirectPos(latilong);
  	AddressType originAddress=null;
		try {
			originAddress = revGeo(requestPoint, ds);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    if (streetNameMissing(originAddress)){
      return null;
    }
    else{
    	final int len2 = originAddress.getPlace().size();
    	String addr = "";
    	for(int i=0;i<len2;i++){
    		if(i>0){
    			String curr = ((Place)(originAddress.getPlace().get(i))).getValue();
    			String prio = ((Place)(originAddress.getPlace().get(i-1))).getValue();
    			if (curr.equals(prio))
    				continue;
    		}
    			
    		addr += ((Place)(originAddress.getPlace().get(i))).getValue();
    	}
    	addr += originAddress.getStreetAddress().getStreet().getValue();
    	return  addr; 
    }
        
  }
}
