package com.zhuyuan.mobilesimulator;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class SimulatorApp {

	public static final int POSITION_TIMEOUT = 120000;
	public SimulatorApp() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HashMap<String, String> params = new HashMap<String,String>();
				params.put("enterpriseid", "demo");
        params.put("staffname", "莹莹");
        params.put("phonenumber", "15921696562");
        params.put("longitude", "121.576530933377");
        params.put("latitude", "31.2519729137412");
        params.put("altitude", "8.2");
        params.put("time", "20121006aabbcc");
        
        HashMap <String,File> files = null;// = new HashMap<String,File>();
        
        try {
			postViaHttpConnection("http://localhost:8080/MobileServer/",params,files);
			//postViaHttpConnection("http://176.34.59.87:8080/MobileServer/",params,files);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

  @SuppressWarnings("unused")
  public static String postViaHttpConnection(String actionUrl, HashMap<String, String> params,HashMap<String, File> files) throws IOException {

    String BOUNDARY = java.util.UUID.randomUUID().toString();
    String PREFIX = "--" , LINEND = "\r\n";
    String MULTIPART_FROM_DATA = "multipart/form-data";
    String CHARSET = "UTF-8";

    URL uri = new URL(actionUrl);
    HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
    conn.setConnectTimeout(POSITION_TIMEOUT);
    conn.setReadTimeout(POSITION_TIMEOUT); 
    conn.setDoInput(true);
    conn.setDoOutput(true);
    conn.setUseCaches(true); 
    conn.setRequestMethod("POST");
    conn.setRequestProperty("connection", "keep-alive");
    conn.setRequestProperty("charset", "UTF-8");
    conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

    //conn.connect(); 
    // First add the content
    StringBuilder sb = new StringBuilder();
    java.util.Iterator it = params.entrySet().iterator();
    while(it.hasNext()){
      java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
      sb.append(PREFIX);
      sb.append(BOUNDARY);
      sb.append(LINEND);
      sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
      sb.append("Content-Type: text/plain; charset=" + CHARSET+LINEND);
      sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
      sb.append(LINEND);
      sb.append(entry.getValue());
      sb.append(LINEND);
    }
    

      
      DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
      outStream.write(sb.toString().getBytes());
      // add the log files
      if(files!=null){
        int i = 0;
        java.util.Iterator it2 = files.entrySet().iterator();
        while(it2.hasNext()){
          StringBuilder sb1 = new StringBuilder();
          java.util.Map.Entry file = (java.util.Map.Entry)it2.next();
          sb1.append(PREFIX);
          sb1.append(BOUNDARY);
          sb1.append(LINEND);
          sb1.append("Content-Disposition: form-data; name=\"file"+(i++)+"\"; filename=\""+ file.getKey() +"\""+LINEND);
          sb1.append("Content-Type: text/plain"+LINEND);
          sb1.append(LINEND);
          outStream.write(sb1.toString().getBytes());
          InputStream is = new FileInputStream(((File) file.getValue()).getPath());
          byte[] buffer = new byte[1024];
          int len = 0;
          while ((len = is.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
          }
  
          is.close();
          outStream.write(LINEND.getBytes());
        }
  
      }
    
    //Request msg end
    byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
    outStream.write(end_data);
    
    outStream.flush();

    //Get the response code
    int res = conn.getResponseCode();
    InputStream in = null;
    if (res == 200) {
      in = conn.getInputStream();
      int ch;
      StringBuilder sb2 = new StringBuilder();
      while ((ch = in.read()) != -1) {
        sb2.append((char) ch);
      }
    }
    return in == null ? null : in.toString();
  }
}
