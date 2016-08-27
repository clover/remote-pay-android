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

package com.clover.remote.client.device;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.usb.USBCloverTransport;

import android.content.Context;

import java.io.Serializable;

/**
 * Default configuration to communicate with the Mini via USB connection
 */
public class USBCloverDeviceConfiguration implements CloverDeviceConfiguration, Serializable {
  public static final String TAG = USBCloverDeviceConfiguration.class.getSimpleName();
  Context context;
  String appId;

  public USBCloverDeviceConfiguration(Context ctx, String appId) {
    context = ctx;
    this.appId = appId;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  @Override
  public String getCloverDeviceTypeName() {
    return DefaultCloverDevice.class.getCanonicalName();
  }

  @Override
  public String getMessagePackageName() {
    return "com.clover.remote.protocol.usb";
  }

  @Override
  public String getName() {
    return "Clover USB Connector";
  }

  @Override
  public CloverTransport getCloverTransport() {
    PackageManager pm = context.getPackageManager();
    try {
      pm.getPackageInfo("com.clover.remote.protocol.usb", PackageManager.GET_ACTIVITIES);
      // getPackageInfo will throw an exception if it isn't found
      Intent disableIntent = new Intent();
      disableIntent.setComponent(new ComponentName("com.clover.remote.protocol.usb", "com.clover.remote.protocol.usb.pos.EnablePosReceiver"));
      disableIntent.putExtra("enabled", false);
      context.sendBroadcast(disableIntent);
    } catch (PackageManager.NameNotFoundException nnfe) {
      // com.clover.remote.protocol.usb isn't installed, so we don't have to disable the USB Pay Display components
      Log.d(TAG, "USB Pay Display not, found");
    } catch (Exception e) {
      // just to prevent some unforeseen exception from preventing the transport from initializing
      Log.e(TAG, "Unexpected error trying to check for, and disable, USB Pay Display: ", e);
    }

    return new USBCloverTransport(context);
  }

  @Override public String getApplicationId() {
    return this.appId;
  }
}
