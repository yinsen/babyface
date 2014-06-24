/** 
 *  deCarta confidential and proprietary.
 *  Copyright 2006-2009 deCarta. All rights reserved.
 *  
 *  Enter the clientName and ClientPassword you received when registering 
 *  on the developer zone.
 */

function CONFIG() {}
    

Credentials.url = "http://wsdds1.dz.cn.decartahws.com/openls/openls";
Credentials.configuration = "global-decarta";
CONFIG.clientName     ="map-sample-app";
CONFIG.clientPassword ="letmein";

//		Credentials.url = "http://hws-staging.en.cn.decartahws.com/openls/openls";
//		Credentials.configuration = "global-decarta";
//		CONFIG.clientName     ="map-sample-app";
//    CONFIG.clientPassword ="letmein";


//    Credentials.url = "http://localhost:8080/openls/openls";
//    Credentials.configuration = "global-decarta";

//		Credentials.url = "http://chameleon-dev1.decarta.com:80/openls/openls";
//		Credentials.configuration = "old-english-tile";

//    Credentials.url = "http://176.34.59.87:80/openls/openls";
//    Credentials.configuration = "global-decarta";

//    CONFIG.clientName     ="someclient";
//   	CONFIG.clientPassword ="abc123";

//		Credentials.url = "http://localhost:80/openls/openls";
//		Credentials.configuration = "emapgo-china-decarta";

// By default, the JSAPI points to the Developer Zone.
// If you are using a self-hosted version of the DDS, the following
// are the most basic configuration settings that you should use.
// To have good looking maps, you'll need to modify your Web Services
// configuration to use a JSAPI appropriate DDS Image Settings.
// CONFIG.clientName = "someclient";
// CONFIG.clientPassword = "abc123";
// Credentials.url = "http://localhost:8080/openls/openls";
// Credentials.configuration = "old-english-tile";
// Following settings will need to be changed if you are using hybrid maps.
// In order to access the SATELLITE layer of images, you will need a Digital
// Globe access key. Please contact sales or support about getting this key.
// Credentials.dgkey = "";
// In order to access the HYBRID layer of images, you will need to set a
// valid transparent overlay configuration:
// Credentials.transparentConfiguration = "transparent-tile";
// To push down the fleet features to PND client via FCS Gateway from fleet
// sample applications, an URL for FCS gateway is set here. 
// Before turning on this value please make sure the fleet extension has been
// enabled in web services and FCS gateway has been launched.
// CONFIG.fcsUrl = "http://localhost:8088/openls/JSON";
