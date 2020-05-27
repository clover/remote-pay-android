package com.clover.remote.client;

import android.content.Context;
import com.clover.remote.client.device.DefaultCloverDevice;
import com.clover.remote.client.transport.ICloverTransport;
import com.clover.remote.client.transport.clover.NativeCloverTransport;

public class NativeCloverDeviceConfiguration implements CloverDeviceConfiguration {

  private final Context context;
  private String appId;

  public NativeCloverDeviceConfiguration(Context context, String applicationId) {
    this.context = context;
    this.appId = applicationId;
  }
  @Override public String getCloverDeviceTypeName() {
    return DefaultCloverDevice.class.getCanonicalName();
  }

  @Override public String getMessagePackageName() {
    return "com.clover.remote.protocol.local";
  }

  @Override public String getName() {
    return "Native";
  }

  @Override public int getMaxMessageCharacters() {
    return Integer.MAX_VALUE;
  }

  @Override public ICloverTransport getCloverTransport() {
    return new NativeCloverTransport(context);
  }

  @Override public String getApplicationId() {
    return appId;
  }
}
