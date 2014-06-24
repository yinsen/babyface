package com.infolands.android;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {

  public static final String URL_HEAD = "http://176.34.59.87:8989/LocationServerJava";
  public static final String BIND_URL = URL_HEAD + "/bind";
  public static final String DEVICEINFO_URL = URL_HEAD + "/deviceinfo";

  public static final String REGISTER_LOG = "REGISTER_LOG";
  public static final String CHARSET = "UTF-8";

  public static final int MAX_LOG_FILE_LENGTH = 2 * 1024 * 1024;

  public static final int DIALOG_REGISTER = 0;

  private LocationHttpAPI httpHandle = new LocationHttpAPI();

  private TextView howtoView = null;
  private TextView deviceidView = null;
  private EditText passwordView = null;
  private Button locationButton = null;
  private Button registerButton = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.setting);

    howtoView = (TextView) this.findViewById(R.id.howtoview);
    howtoView.setText(R.string.introduce);
    
    TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    String deviceid = tm.getDeviceId();//如果是GSM，则getDeviceId为IMEI
    deviceidView = (TextView) this.findViewById(R.id.deviceiView);
    deviceidView.setText(deviceid);
    deviceidView.setTextSize(deviceidView.getTextSize()*5/4);
    
    passwordView = (EditText) this.findViewById(R.id.passwordinput);
    passwordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

    locationButton = (Button) findViewById(R.id.locationBtn);
    if (locationButton != null) {
      locationButton.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          //创建一个PendingIntent用于启动一个service
//          PendingIntent amSendIntent = PendingIntent.getService(SettingActivity.this, 0,
//              new Intent("LOCATION_ACTION"), // A new Service intent
//              0 // flags (none are required for a service)
//              );
//          AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//          am.setRepeating(AlarmManager.RTC_WAKEUP, // 定时方式，表示在睡眠状态下也会启动
//              SystemClock.elapsedRealtime() + 1000, //1s后启动
//              10 * 60 * 1000, //重复周期
//              amSendIntent //到时后触发的Intent
//          );

          //启动LocationService
          Intent locIntent = new Intent("LOCATION_ACTION");
          startService(locIntent);
          
          Intent intent2 = new Intent("PHONEINFO_ACTION");
          startService(intent2);

          Toast.makeText(getApplicationContext(), getResources().getString(R.string.dialog_begin_locate),
              Toast.LENGTH_SHORT).show();

          finish();
        }
      });
    }

    registerButton = (Button) findViewById(R.id.registerBtn);
    registerButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {

        //        ProgressDialog regDialog = new ProgressDialog(v.getContext());
        //        regDialog.setMessage(getString(R.string.dialog_waiting));
        //        regDialog.setCancelable(false);
        //        regDialog.show();

        bindDevice();

        //        regDialog.dismiss();
      }
    });

  }

  private void bindDevice() {
    if (httpHandle.initHttpStream(BIND_URL) == true) {
      EditText account = (EditText) findViewById(R.id.accountinput);
      String accountStr = account.getText().toString();
      httpHandle.writeFormData("account", accountStr, "text/plain");

      EditText password = (EditText) findViewById(R.id.passwordinput);
      String passwordStr = password.getText().toString();
      httpHandle.writeFormData("password", passwordStr, "text/plain");

      TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
      String deviceid = tm.getDeviceId();//如果是GSM，则getDeviceId为IMEI
      httpHandle.writeFormData("deviceid", deviceid, "text/plain");

      int resp = httpHandle.postStreamFlush();

      if (resp == 200) {
        if ("NO_ACCOUNT".equalsIgnoreCase(httpHandle.getRespHeaderValue("bindresult"))) {
          Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_account), Toast.LENGTH_SHORT)
              .show();
        }
        else if ("PWD_FAIL".equalsIgnoreCase(httpHandle.getRespHeaderValue("bindresult"))) {
          Toast.makeText(getApplicationContext(), getResources().getString(R.string.pwd_fail), Toast.LENGTH_SHORT)
              .show();
        }
        else if ("BEEN_BIND".equalsIgnoreCase(httpHandle.getRespHeaderValue("bindresult"))) {
          Toast.makeText(getApplicationContext(), getResources().getString(R.string.been_bind), Toast.LENGTH_SHORT)
              .show();
        }
        else if ("BIND_SUCCESS".equalsIgnoreCase(httpHandle.getRespHeaderValue("bindresult"))) {
          Toast.makeText(getApplicationContext(), getResources().getString(R.string.bind_success), Toast.LENGTH_SHORT)
              .show();
        }
        else if ("BIND_FAIL".equalsIgnoreCase(httpHandle.getRespHeaderValue("bindresult"))) {
          Toast.makeText(getApplicationContext(), getResources().getString(R.string.bind_fail), Toast.LENGTH_SHORT)
              .show();
        }
      }

      httpHandle.disconnect();
    }
    else {
      Toast.makeText(getApplicationContext(), getResources().getString(R.string.dialog_network_fail),
          Toast.LENGTH_SHORT).show();
    }

    return;
  }

}
