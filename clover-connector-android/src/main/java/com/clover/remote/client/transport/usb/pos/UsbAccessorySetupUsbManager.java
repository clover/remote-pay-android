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

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.util.Pair;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * The purpose of this class is to put the attached USB Android device into "Accessory Mode".
 * <p>
 * See https://source.android.com/accessories/aoa.html#determine-accessory-mode-support
 */
public class UsbAccessorySetupUsbManager extends UsbCloverManager<Void> {

  private final String TAG = getClass().getSimpleName();

  private static final Charset US_ASCII = Charset.forName("US-ASCII");

  public UsbAccessorySetupUsbManager(Context context) {
    super(context);
  }

  @Override
  protected int getReadSize() {
    return 0;
  }

  @Override
  protected int getMaxWriteDataSize() {
    return 0;
  }

  public static boolean isUsbDeviceAttached(Context context) {
    UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    return findDevice(usbManager, VENDOR_PRODUCT_IDS) != null;
  }

  // See init.maplecutter.usb.rc in platform for more info
  public static final Pair<Integer, Integer>[] VENDOR_PRODUCT_IDS = new Pair[]{
      Pair.create(0x28f3, 0x2000), // leafcutter adb device
      Pair.create(0x28f3, 0x2001), // leafcutter rndis,adb device
      Pair.create(0x28f3, 0x2003), // leafcutter cloverusb device

      Pair.create(0x28f3, 0x3000), // maplecutter adb device
      Pair.create(0x28f3, 0x3001), // maplecutter rndis,adb device
      Pair.create(0x28f3, 0x3003), // maplecutter cloverusb device
  };

  @Override
  protected Pair<Integer, Integer>[] getVendorProductIds() {
    return VENDOR_PRODUCT_IDS;
  }

  public void startAccessoryMode() throws UsbConnectException {
    if (isConnected()) {
      Log.w(TAG, "Unexpectedly already connected");
      disconnect();
    }

    open();

    try {
      if (isConnected()) {
        if (!sendAccessoryModeCommands()) {
          throw new UsbConnectException("Unable to start accessory mode, sending commands failed");
        } else {
          Log.d(TAG, "Successfully started accessory mode");
        }
      } else {
        throw new UsbConnectException("Unable to start accessory mode, open failed");
      }
    } finally {
      // Always disconnect immediately
      disconnect();
    }
  }

  @Override
  protected boolean isBulkInterface() {
    return false;
  }

  private static final int ACCESSORY_STRING_MANUFACTURER = 0;
  private static final int ACCESSORY_STRING_MODEL = 1;
  private static final int ACCESSORY_STRING_DESCRIPTION = 2;
  private static final int ACCESSORY_STRING_VERSION = 3;
  private static final int ACCESSORY_STRING_URI = 4;
  private static final int ACCESSORY_STRING_SERIAL = 5;

  private static final int ACCESSORY_GET_PROTOCOL = 51;
  private static final int ACCESSORY_SEND_STRING = 52;
  private static final int ACCESSORY_START = 53;

  private boolean sendAccessoryModeCommands() {
    ByteBuffer dataBuf = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
    byte[] data = dataBuf.array();

    int result;

    result = mConnection.controlTransfer(UsbConstants.USB_DIR_IN | UsbConstants.USB_TYPE_VENDOR,
        ACCESSORY_GET_PROTOCOL, 0, 0, data, data.length, 2000);

    if (result <= 0) {
      Log.w(TAG, String.format("Get protocol failed: %d", result));
      return false;
    }

    short protocol = dataBuf.getShort();
    if (protocol < 1) {
      Log.w(TAG, String.format("Get protocol returned %d", protocol));
      return false;
    }

    if (!sendAccessoryString(ACCESSORY_STRING_MANUFACTURER, "Clover")) {
      return false;
    }
    if (!sendAccessoryString(ACCESSORY_STRING_MODEL, "Adapter")) {
      return false;
    }
    if (!sendAccessoryString(ACCESSORY_STRING_DESCRIPTION, "Android point of sale device")) {
      return false;
    }
    if (!sendAccessoryString(ACCESSORY_STRING_VERSION, Build.VERSION.INCREMENTAL)) {
      return false;
    }
    // For now the Android platform won't accept this string because it doesn't start with http or https and
    // the popup won't show up
    if (!sendAccessoryString(ACCESSORY_STRING_URI, "market://details?id=com.clover.remote.protocol.usb")) {
      return false;
    }
    if (!sendAccessoryString(ACCESSORY_STRING_SERIAL, Build.SERIAL)) {
      return false;
    }

    result = mConnection.controlTransfer(UsbConstants.USB_DIR_OUT | UsbConstants.USB_TYPE_VENDOR,
        ACCESSORY_START, 0, 0, null, 0, 2000);

    if (result < 0) {
      Log.w(TAG, String.format("Start accessory mode failed: %d", result));
      return false;
    }

    return true;
  }

  protected boolean sendAccessoryString(int index, String value) {
    byte[] strBytes = value.getBytes(US_ASCII);
    int result = mConnection.controlTransfer(UsbConstants.USB_DIR_OUT | UsbConstants.USB_TYPE_VENDOR,
        ACCESSORY_SEND_STRING, 0, index, strBytes, strBytes.length, 2000);
    boolean success = result > 0;

    if (!success) {
      Log.w(TAG, String.format("Send string failed: %s", value));
    }

    return success;
  }

}
