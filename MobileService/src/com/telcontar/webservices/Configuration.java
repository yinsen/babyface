/*
 * Configuration.java
 * author Brent Hamby (brent@telcontar.com)
 * For use only with Telcontar products.
 * Copyright (c) 1997-2005 Telcontar Inc. 
 * All Rights Reserved. US and International Patents Pending.
 */

package com.telcontar.webservices;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * reads configuration settings from web services property file.
 * 
 * @version Sep 17, 2005 8:42:10 AM
 * @author Brent Hamby 
 */
public class Configuration {

	private static Configuration instance = null;
	private ResourceBundle    propertyFile = null;
	private String            configuration=null; // this is to allow a session only change of the dataset configuration
	
	
	public Configuration() throws IOException {
		setPropertyFile("webservices");
	}

	public static Configuration getInstance() {
		if (instance == null) {
		    try {
		        instance = new Configuration();
		    } catch (Exception e) {
		        
		    }
		}	
		return instance;
	}
	
	public String getProperty(String propertyName) {
		return getPropertyFile().getString(propertyName);
	}
	
	/**
	 * Gets host
	 * @return host
	 */
	public String getHost() {
		return getProperty("host");
	}
	/**
	 * Gets clientname
	 * @return clientname
	 */
	public String getClientName() {
		return getProperty("clientname");
	}
	/**
	 * Gets clientpassword
	 * @return clientpassword
	 */
	public String getClientPassword() {
		return getProperty("clientpassword");
	}
	
	/**
	 * Gets configuration
	 * @return dataset
	 */
	public String getConfiguration() {
	    if (configuration==null)
	        configuration = getProperty("configuration");
	    return configuration;
	}
	
	/**
	 * Set the configuration dataset for a particular user session.
	 * @param configuration
	 */
	public void setConfiguration(String configuration) {	
	    this.configuration=configuration;
	}

	/**
	 * Gets sessionid
	 * @return sessionid
	 */
	public String getSessionId() {
		return getProperty("sessionid");
	}	
	/**
	 * Gets maximumresponses
	 * @return maximumresponses
	 */
	public String getMaximumResponses() {
		return getProperty("maximumresponses");
	}	
	
	/**
	 * Gets requestid
	 * @return requestid
	 */
	public String getRequestId() {
		return getProperty("requestid");
	}	
	
	/**
	 * Gets height
	 * @return height
	 */
	public String getHeight() {
		return getProperty("height");
	}		
	
	/**
	 * Gets width
	 * @return width
	 */
	public String getWidth() {
		return getProperty("width");
	}
	/**
	 * Gets version
	 * @return version
	 */
	public String getVersion() {
		return getProperty("version");
	}	

	/**
	 * Gets displayRequestXML
	 * @return displayRequestXML
	 */
	public boolean getDisplayRequestXML() {
		if((getProperty("displayRequestXML").equals("true")))
		    return true;	
		else 
		    return false;
	}	
	/**
	 * Gets displayResponseXML
	 * @return displayResponseXML
	 */
	public boolean getDisplayResponseXML() {
		if((getProperty("displayResponseXML").equals("true")))
		    return true;	
		else 
		    return false;
	}	

	/**
	 * Gets language
	 * @return language
	 */
	public String getLanguage() {
		return getProperty("language");
	}	
	private void setPropertyFile(String fileName) throws IOException {
	    
	    //propertyFile = new Properties();
	    //InputStream in = new FileInputStream("webservices.properties");
	    //InputStream in = this.getClass().getClassLoader().getResourceAsStream("webservices.properties");
	    //propertyFile.load(in);
	    //in.close();
	    propertyFile = ResourceBundle.getBundle(fileName);
	}
	private ResourceBundle getPropertyFile() {
		return propertyFile;
	}	
}