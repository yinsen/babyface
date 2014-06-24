package com.infolands.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {

  private TextView statesView = null;
  private RadioGroup statesRadioGrp = null;
  private Button nextButton = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    statesView = (TextView) this.findViewById(R.id.statesview);
    statesView.setText(R.string.statesment);

    statesRadioGrp = (RadioGroup) this.findViewById(R.id.statesRadioGrp);
    final RadioButton radio1 = (RadioButton) findViewById(R.id.radioDisagree);
    radio1.setChecked(true);
    final RadioButton radio2 = (RadioButton) findViewById(R.id.radioAgree);
    statesRadioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        // TODO Auto-generated method stub
        if (checkedId == radio1.getId()) {
          nextButton.setEnabled(false);
        }
        else if (checkedId == radio2.getId()) {
          nextButton.setEnabled(true);
        }
      }
    });

    nextButton = (Button) findViewById(R.id.nextBtn);
    nextButton.setEnabled(false);
    if (nextButton != null) {
      nextButton.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          Intent intent = new Intent();
          intent.setAction(Intent.ACTION_VIEW);
          String url = "setting://";
          intent.setData(Uri.parse(url));
          startActivity(intent);
          
          finish();
        }
      });
    }
  }

  @Override
  public void onStart() {
    super.onStart();
  }

}