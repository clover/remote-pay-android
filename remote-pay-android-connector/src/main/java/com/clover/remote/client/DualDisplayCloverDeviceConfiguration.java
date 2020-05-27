package com.clover.remote.client;

import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import com.clover.remote.client.device.DefaultCloverDevice;
import com.clover.remote.client.transport.ICloverTransport;
import com.clover.remote.client.transport.usb.DualDisplayCloverTransport;
import com.clover.remote.client.transport.usb.USBCloverTransport;

public class DualDisplayCloverDeviceConfiguration implements CloverDeviceConfiguration {
  private static final String TAG = DualDisplayCloverDeviceConfiguration.class.getSimpleName();
  private static final int MAX_CHAR_IN_MESSAGE = 10000;
  private String appId;

  Context context;

  public DualDisplayCloverDeviceConfiguration(Context ctx, String appId) {
    this.context = ctx;
    this.appId = appId;
  }

  @Override public String getCloverDeviceTypeName() {
    return DefaultCloverDevice.class.getCanonicalName();
  }

  @Override public String getMessagePackageName() {
    return "com.clover.remote.protocol.local";
  }

  @Override public String getName() {
    return "Clover Dual Display Connector";
  }

  @Override public int getMaxMessageCharacters() {
    return MAX_CHAR_IN_MESSAGE;
  }

  @Override public ICloverTransport getCloverTransport() {

    try {
      ContentProviderClient contentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient("com.clover.remote.protocol.dualdisplay.terminal.set_enabled");
      if (contentProviderClient != null) {
        contentProviderClient.call("setPosEnabled", "false", null);
      }
    } catch (Exception e) {
      // just to prevent some unforeseen exception from preventing the transport from initializing
      Log.i(TAG, "Unexpected error trying to check for and disable DualDisplay Pay Display: ", e);
    }

    return new DualDisplayCloverTransport(context);
  }

  @Override public String getApplicationId() {
    return appId;
  }
}
