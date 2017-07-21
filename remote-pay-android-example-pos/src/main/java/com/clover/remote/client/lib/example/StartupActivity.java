/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.remote.client.lib.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;

public class StartupActivity extends Activity {

  public static final String TAG = StartupActivity.class.getSimpleName();
  public static final String EXAMPLE_APP_NAME = "EXAMPLE_APP";
  public static final String LAN_PAY_DISPLAY_URL = "LAN_PAY_DISPLAY_URL";
  public static final String CONNECTION_MODE = "CONNECTION_MODE";
  public static final String USB = "USB";
  public static final String LAN = "LAN";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_startup);

    loadBaseURL();

    getActionBar().hide();

    RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
    group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(RadioGroup group, int checkedId) {
        TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
        textView.setEnabled(checkedId == R.id.lanRadioButton);
      }
    });

    // initialize...
    TextView textView = (TextView) findViewById(R.id.lanPayDisplayAddress);
    String url = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(LAN_PAY_DISPLAY_URL,  "wss://192.168.1.101:12345/remote_pay");

    textView.setText(url);
    textView.setEnabled(((RadioGroup)findViewById(R.id.radioGroup)).getCheckedRadioButtonId() == R.id.lanRadioButton);

    String mode = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE).getString(CONNECTION_MODE, USB);

    ((RadioButton)findViewById(R.id.lanRadioButton)).setChecked(LAN.equals(mode));
    ((RadioButton)findViewById(R.id.usbRadioButton)).setChecked(!LAN.equals(mode));
  }

  private boolean loadBaseURL() {

    String _serverBaseURL = PreferenceManager.getDefaultSharedPreferences(this).getString(ExamplePOSActivity.EXAMPLE_POS_SERVER_KEY, "wss://10.0.0.101:12345/remote_pay");

    TextView tv = (TextView)findViewById(R.id.lanPayDisplayAddress);
    tv.setText(_serverBaseURL);

    Log.d(TAG, _serverBaseURL);
    return true;
  }



  public void connect(View view) {

    RadioGroup group = (RadioGroup)findViewById(R.id.radioGroup);
    Intent intent = new Intent();
    intent.setClass(this, ExamplePOSActivity.class);

    SharedPreferences prefs = this.getSharedPreferences(EXAMPLE_APP_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefs.edit();
    URI uri = null;
    String config = null;


    if(group.getCheckedRadioButtonId() == R.id.usbRadioButton) {
      config = "USB";
      editor.putString(CONNECTION_MODE, USB);
      editor.commit();
    } else { // (group.getCheckedRadioButtonId() == R.id.lanRadioButton)
      String uriStr = ((TextView)findViewById(R.id.lanPayDisplayAddress)).getText().toString();
      config = "WS";
      try {
        uri = new URI(uriStr);
        editor.putString(LAN_PAY_DISPLAY_URL, uriStr);
        editor.putString(CONNECTION_MODE, LAN);
        editor.commit();
      } catch(URISyntaxException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Invalid URL");
        builder.show();
      }
    }

    if(config.equals("USB") || (config.equals("WS") && uri != null)) {
      intent.putExtra(ExamplePOSActivity.EXTRA_CLOVER_CONNECTOR_CONFIG, config);
      intent.putExtra(ExamplePOSActivity.EXTRA_WS_ENDPOINT, uri);
      startActivity(intent);
    }

  }

}
