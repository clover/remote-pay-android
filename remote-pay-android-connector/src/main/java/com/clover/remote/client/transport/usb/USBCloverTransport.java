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

package com.clover.remote.client.transport.usb;

import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.usb.pos.PosUsbRemoteProtocolService;
import com.clover.remote.client.transport.usb.pos.RemoteUsbManager;
import com.clover.remote.client.transport.usb.pos.UsbAccessorySetupUsbManager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.nio.channels.NotYetConnectedException;

/**
 * Implements the Clover Transport for USB
 */
public class USBCloverTransport extends CloverTransport {

  public static final String ACTION_USB_PERMISSION = "com.clover.USB_PERMISSION";
  public static final String TAG = USBCloverTransport.class.getSimpleName();

  private final Context context;

  private BroadcastReceiver connectionBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      try {
        switch (action) {
          case DEVICE_DISCONNECTED: {
            notifyDeviceDisconnected();
            break;
          }
          case DEVICE_CONNECTED: {
            notifyDeviceConnected();
            break;
          }
          case DEVICE_READY: {
            notifyDeviceReady();
            break;
          }
        }
      } catch (Exception e) {
        Log.e(TAG, "Couldn't parse intent: " + intent.getAction());
      }
    }
  };

  private BroadcastReceiver messageBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if (action.equals(PosUsbRemoteProtocolService.ACTION_USB_RECEIVE_MESSAGE)) {
        String msg = intent.getStringExtra(PosUsbRemoteProtocolService.EXTRA_MESSAGE);
        Log.d(TAG, String.format("Got message in Transport: %s", msg));
        onMessage(msg);
      }
    }
  };

  public USBCloverTransport(Context ctx) {
    context = ctx;
  }

  @Override
  public synchronized void initializeConnection() {
    context.registerReceiver(connectionBroadcastReceiver, getConnectionIntentFilter());
    context.registerReceiver(messageBroadcastReceiver, getMessageIntentFilter());

    findAndOpenDevice();

    Intent sendIntent = new Intent(PosUsbRemoteProtocolService.ACTION_USB_REQUEST_MESSAGE);
    context.sendBroadcast(sendIntent);
  }

  private IntentFilter getConnectionIntentFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(CloverTransport.DEVICE_DISCONNECTED);
    filter.addAction(CloverTransport.DEVICE_READY);
    filter.addAction(CloverTransport.DEVICE_CONNECTED);
    return filter;
  }

  private IntentFilter getMessageIntentFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(PosUsbRemoteProtocolService.ACTION_USB_RECEIVE_MESSAGE);
    return filter;
  }

  private void findAndOpenDevice() {

    if (RemoteUsbManager.isUsbDeviceAttached(context)) {
      Log.d(TAG, "Start pos usb connect from USBTransport");

      Intent posUsbServiceIntent = new Intent()
          .setClass(context, PosUsbRemoteProtocolService.class)
          .setAction(PosUsbRemoteProtocolService.ACTION_USB_CONNECT);
      context.startService(posUsbServiceIntent);
    } else if (UsbAccessorySetupUsbManager.isUsbDeviceAttached(context)) {
      Log.d(TAG, "Start pos usb setup from USBTransport");

      Intent posUsbServiceIntent = new Intent()
          .setClass(context, PosUsbRemoteProtocolService.class)
          .setAction(PosUsbRemoteProtocolService.ACTION_USB_SETUP);
      context.startService(posUsbServiceIntent);
    }

  }

  @Override
  protected void finalize() throws Throwable {
    dispose();
  }

  @Override
  public void dispose() {
    super.dispose();
    if(context != null) {
      context.unregisterReceiver(messageBroadcastReceiver);
      context.unregisterReceiver(connectionBroadcastReceiver);
    }
  }

  @Override
  public synchronized int sendMessage(String message) throws NotYetConnectedException {
    Intent sendIntent = new Intent(PosUsbRemoteProtocolService.ACTION_USB_SEND_MESSAGE);
    sendIntent.putExtra(PosUsbRemoteProtocolService.EXTRA_MESSAGE, message);
    context.sendBroadcast(sendIntent);

    return 0;
  }

  private boolean isMyServiceRunning(Class<?> serviceClass) {
    ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (serviceClass.getName().equals(service.service.getClassName())) {
        return true;
      }
    }
    return false;
  }
}
