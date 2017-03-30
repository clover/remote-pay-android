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

package com.clover.remote.client.transport.usb.pos;

import com.clover.remote.client.transport.usb.UsbCloverManager;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;


public class PosUsbBroadcastReceiver extends BroadcastReceiver {

  public static final String ACTION_USB_PERMISSION = "com.clover.USB_PERMISSION";
  private static final String TAG = PosUsbBroadcastReceiver.class.getSimpleName();
  private UsbManager mUsbManager;

  public void onReceive(final Context context, Intent intent) {
    String action = intent.getAction();
    Bundle extras = intent.getExtras();


    final Intent serviceIntent = new Intent().setClass(context, PosUsbRemoteProtocolService.class).putExtras(extras);

    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

    mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);


    if (UsbCloverManager.isMatch(device, UsbAccessorySetupUsbManager.VENDOR_PRODUCT_IDS)) {
      if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            serviceIntent.setAction(PosUsbRemoteProtocolService.ACTION_USB_SETUP);
            Log.d(TAG, String.format("Starting service: %s", serviceIntent));
            context.startService(serviceIntent);
          }
        };

        if (mUsbManager.hasPermission(device)) {
          runnable.run();
        } else {
          requestPermission(device, runnable, context); // this probably needs to be removed, as this will probably fail in a BroadcastReceiver
        }
        return;
      }
    }

    if (UsbCloverManager.isMatch(device, RemoteUsbManager.VENDOR_PRODUCT_IDS)) {
      if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            serviceIntent.setAction(PosUsbRemoteProtocolService.ACTION_USB_CONNECT);
            Log.d(TAG, String.format("Starting service: %s", serviceIntent));
            context.startService(serviceIntent);
          }
        };
        if (mUsbManager.hasPermission(device)) {
          runnable.run();
        } else {
          requestPermission(device, runnable, context); // this probably needs to be removed, as this will probably fail in a BroadcastReceiver
        }
        return;
      } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            serviceIntent.setAction(PosUsbRemoteProtocolService.ACTION_USB_DISCONNECT);
            Log.d(TAG, String.format("Starting service: %s", serviceIntent));
            context.startService(serviceIntent);
          }
        };
        if (true || mUsbManager.hasPermission(device)) {
          runnable.run();
        } else {
          requestPermission(device, runnable, context); // this probably needs to be removed, as this will probably fail in a BroadcastReceiver
        }
        return;

      }
    }
  }

  private void requestPermission(UsbDevice device, final Runnable runnable, Context context) {
    BroadcastReceiver usbReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        synchronized (this) {
          UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            if (device != null) {
              runnable.run();
            }
          } else {
            Log.d(TAG, "permission denied for device " + device);
          }
        }
      }
    };
    PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    context.registerReceiver(usbReceiver, filter);
    mUsbManager.requestPermission(device, permissionIntent);
  }

}
