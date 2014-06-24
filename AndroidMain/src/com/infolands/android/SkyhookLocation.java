package com.infolands.android;

import android.content.Context;
import android.os.Handler;

import com.skyhookwireless.wps.RegistrationCallback;
import com.skyhookwireless.wps.WPSAuthentication;
import com.skyhookwireless.wps.WPSContinuation;
import com.skyhookwireless.wps.WPSLocation;
import com.skyhookwireless.wps.WPSPeriodicLocationCallback;
import com.skyhookwireless.wps.WPSReturnCode;
import com.skyhookwireless.wps.XPS;

public class SkyhookLocation {

  public static int requestTimes = 0;
  public static int registerTimes = 0;

  public void stop() {
    // make sure WPS is stopped
    _xps.abort();
  }

  private class MyLocationCallback implements WPSPeriodicLocationCallback {

    public void done() {
//      _handler.sendMessage(_handler.obtainMessage(LocService.SKYHOOK_LOCATION_DONE_MESSAGE));
    }

    public WPSContinuation handleError(final WPSReturnCode error) {
      
      //定位失败后再尝试两次，都失败了才上报失败
      if (++ requestTimes % 3 != 0) {
        return WPSContinuation.WPS_CONTINUE;
      }
      else {
        _handler.sendMessage(_handler.obtainMessage(LocService.SKYHOOK_LOCATION_ERROR_MESSAGE, error));
        return WPSContinuation.WPS_STOP;
      }
    }

    @Override
    public WPSContinuation handleWPSPeriodicLocation(final WPSLocation location) {
      
      //成功后清0。防止两次成功后，下次就只测试一次的情况
      requestTimes = 0;
      //成功后直接上报
      _handler.sendMessage(_handler.obtainMessage(LocService.SKYHOOK_LOCATION_MESSAGE, location));
      return WPSContinuation.WPS_STOP;
    }

  }

  private class MyRegistrationCallback implements RegistrationCallback {

    public void done() {
    }

    public void handleSuccess() {
      registerTimes = 0;
      _handler.sendMessage(_handler.obtainMessage(LocService.SKYHOOK_REGISTRATION_SUCCESS_MESSAGE));
    }

    public WPSContinuation handleError(final WPSReturnCode error) {
      if (++ registerTimes % 3 != 0) {
        return WPSContinuation.WPS_CONTINUE;
      }
      else {
        _handler.sendMessage(_handler.obtainMessage(LocService.SKYHOOK_REGISTRATION_ERROR_MESSAGE, error));
        return WPSContinuation.WPS_STOP;
      }
    }
  }

  public static final int SKYHOOK_TYPE_WPS = 1;
  public static final int SKYHOOK_TYPE_XPS = 2;
  
  private final MyLocationCallback _callback = new MyLocationCallback();
  private final MyRegistrationCallback _registrationCallback = new MyRegistrationCallback();

  private String _username = "0", _realm = "0";
  private XPS _xps;
  private Handler _handler;

  public void setUsername(String username) {
    _username = username;
  }
  public void setRealm(String realm) {
    _realm = realm;
  }
  
  public void registerSkyhookXPS(Context context) {
    _handler = ((LocService) context)._handler;

    _xps = new XPS(context);
    _xps.registerUser(new WPSAuthentication(_username, _realm), null, _registrationCallback);
  }

  //accurance为精确度，单位为米
  public void startSkyhookLocation(Context context, int accurance) {
    final WPSAuthentication auth = new WPSAuthentication(_username, _realm);
    
    _xps.getXPSLocation(auth, 5, accurance, _callback);
  }

}
