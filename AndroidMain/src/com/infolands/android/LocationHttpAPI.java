package com.infolands.android;

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import android.util.Log;

public class LocationHttpAPI {


  public static final String HTTP_API_LOG = "HTTP_API_LOG";
  public static final String PREFIX = "--";
  public static final String LINEND = "\r\n";
  public static final String MULTIPART_FROM_DATA = "multipart/form-data";
  public static final String CHARSET = "UTF-8";
  //public static final String CHARSET = "ISO-8859-1";
  public static final int CONNECT_TIMEOUT = 10000;
  public static final int READ_TIMEOUT = 30000;
  public static final int MAX_LOG_FILE_LENGTH = 2 * 1024 * 1024;

  public static final String HTTPCONTENT_TEXT = "text/plain";
  public static final String HTTPCONTENT_MIXED = "multipart/mixed";

  public HttpURLConnection conn;
  private OutputStream httpOs;
  private String boundary = UUID.randomUUID().toString();
  private String subBoundary = "BbC04y";

  public boolean initHttpStream(String url) {
    try {
      URL uri = new URL(url);
      conn = (HttpURLConnection) uri.openConnection();
      if (conn != null) {
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(true);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");//长连接设置无效，因为Tomcat的connector也会设置connectionTimeout参数
        conn.setRequestProperty("charset", CHARSET);
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + boundary);

        httpOs = conn.getOutputStream();
        if (httpOs != null) {
          return true;
        }
      }
    }
    catch (ProtocolException e) {
      Log.i(HTTP_API_LOG, "ProtocolException", e);
    }
    catch (MalformedURLException e) {
      Log.i(HTTP_API_LOG, "MalformedURLException", e);
    }
    catch (IOException e) {
      Log.i(HTTP_API_LOG, "IOException", e);
    }

    return false;
  }

  public void writeFormData(String dataName, String dataValue, String dataType) {
    StringBuilder sb = new StringBuilder();
    sb.append(PREFIX + boundary + LINEND);
    sb.append("Content-Disposition: form-data; name=\"" + dataName + "\"" + LINEND);
    sb.append("Content-Type: " + dataType + "; charset=" + CHARSET + LINEND);
    sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
    sb.append(LINEND);
    sb.append(dataValue);
    sb.append(LINEND);

    try {
      httpOs.write(sb.toString().getBytes(CHARSET));
    }
    catch (UnsupportedEncodingException e) {
      Log.i(HTTP_API_LOG, "UnsupportedEncodingException", e);
    }
    catch (IOException e) {
      Log.i(HTTP_API_LOG, "IOException", e);
    }
  }

  public void writeMultiFormData(String dataType, HashMap<String, String> params) {
    StringBuilder sb = new StringBuilder();

    for (Entry<String, String> entry : params.entrySet()) {
      sb.append(PREFIX + boundary + LINEND);
      sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
      sb.append("Content-Type: " + dataType + "; charset=" + CHARSET + LINEND);
      sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
      sb.append(LINEND);
      sb.append(entry.getValue());
      sb.append(LINEND);
    }

    try {
      httpOs.write(sb.toString().getBytes(CHARSET));
    }
    catch (UnsupportedEncodingException e) {
      Log.i(HTTP_API_LOG, "UnsupportedEncodingException", e);
    }
    catch (IOException e) {
      Log.i(HTTP_API_LOG, "IOException", e);
    }
  }

  public void writeFileData(String filePath, String fileType) {
    StringBuilder sb = new StringBuilder();
    sb.append(PREFIX + boundary + LINEND);
    sb.append("Content-Disposition: attachment; filename=\"" + filePath + "\"" + LINEND);
    sb.append("Content-Type: " + fileType + LINEND);
    sb.append("Content-Transfer-Encoding: binary" + LINEND);
    sb.append(LINEND);

    try {
      httpOs.write(sb.toString().getBytes(CHARSET));
    }
    catch (UnsupportedEncodingException e) {
      Log.i(HTTP_API_LOG, "UnsupportedEncodingException", e);
    }
    catch (IOException e) {
      Log.i(HTTP_API_LOG, "IOException", e);
    }
  }

  public void writeMixData(String MixDataName, HashMap<String, String> params) {
    StringBuilder sb = new StringBuilder();
    sb.append(PREFIX + boundary + LINEND);
    sb.append("Content-Disposition: form-data; name=\"" + MixDataName + "\"" + LINEND);
    sb.append("Content-Type: multipart/mixed; charset=" + CHARSET + "; boundary=" + subBoundary + LINEND);
    sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
    sb.append(LINEND);
    for (Entry<String, String> entry : params.entrySet()) {
      sb.append(PREFIX + subBoundary + LINEND);
      sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
      sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
      sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
      sb.append(LINEND);
      sb.append(entry.getValue());
      sb.append(LINEND);
    }
    sb.append(LINEND);

    try {
      httpOs.write(sb.toString().getBytes(CHARSET));
    }
    catch (UnsupportedEncodingException e) {
      Log.i(HTTP_API_LOG, "UnsupportedEncodingException", e);
    }
    catch (IOException e) {
      Log.i(HTTP_API_LOG, "IOException", e);
    }
  }

  public int postStreamFlush() {
    int res = 0;
    try {
      /** Request msg end */
      byte[] end_data = (PREFIX + boundary + PREFIX + LINEND).getBytes(CHARSET);
      httpOs.write(end_data);
      httpOs.flush();
      res = conn.getResponseCode();

    }
    catch (UnsupportedEncodingException e) {
      Log.i(HTTP_API_LOG, "UnsupportedEncodingException", e);
    }
    catch (IOException e) {
      Log.i(HTTP_API_LOG, "IOException", e);
    }
    return res;
  }

  public String getRespHeaderValue(String key) {

    return conn.getHeaderField(key);
  }

  public void disconnect() {

    try {
      httpOs.close();
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    conn.disconnect();
  }
}
