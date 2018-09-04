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

import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class is designed to be a reusable USB communication manager for devices that bulk transfer data.
 * Instances of this class should be singletons.
 */
public abstract class UsbCloverManager<T> {

  private final String TAG = getClass().getSimpleName();

  protected static final boolean VERBOSE = true;

  protected static final int CONNECTION_TIMEOUT_MS = 5000;
  protected static final int CONNECTION_RETRY_WAIT_MS = 100;

  protected static final int SETUP_TIMEOUT_MS = 2000;
  protected static final int SETUP_RETRY_WAIT_MS = 200;

  protected static final int READ_TIMEOUT_MS = 500;
  protected static final int WRITE_TIMEOUT_MS = 1000;

  enum DeviceStatus {
    STATUS_DEVICE_FOUND,
    STATUS_DEVICE_NOT_FOUND,

    STATUS_DEVICE_CONNECTING,
    STATUS_DEVICE_CONNECTED,
    STATUS_DEVICE_CONNECTION_CLOSED,
    STATUS_DEVICE_CONNECTION_FAILED,

    STATUS_DEVICE_SEARCHING,
    STATUS_DEVICE_ATTACHED,
    STATUS_DEVICE_DETACHED,
  }

  public static class UsbConnectException extends Exception {
    public UsbConnectException() {
      super();
    }

    public UsbConnectException(String detailMessage) {
      super(detailMessage);
    }
  }

  public static class UsbDeviceNotFoundException extends UsbConnectException {
    public UsbDeviceNotFoundException() {
    }

    public UsbDeviceNotFoundException(String detailMessage) {
      super(detailMessage);
    }
  }


  private final android.hardware.usb.UsbManager mUsbManager;

  private final byte[] mReadBuffer;

  private UsbDevice mUsbDevice;
  protected UsbDeviceConnection mConnection;
  private UsbInterface mInterface;
  private UsbEndpoint mEndpointIn;
  private UsbEndpoint mEndpointOut;

  public UsbCloverManager(Context context) {
    mReadBuffer = new byte[getReadSize()];
    mUsbManager = (android.hardware.usb.UsbManager) context.getSystemService(Context.USB_SERVICE);
  }

  /**
   * Open a connection to the USB device and begin a session. Only one session may be opened at a time.
   *
   * @throws UsbConnectException if connecting to the device failed
   */
  public void open() throws UsbConnectException {
    assertBackgroundThread();
    boolean connected = false;

    try {
      postStatusChange(DeviceStatus.STATUS_DEVICE_CONNECTING);

      onPreConnect();

      connect();
      connected = true;
    } finally {
      if (!connected) {
        disconnect();
      }
    }
  }

  protected abstract int getReadSize();

  protected abstract int getMaxWriteDataSize();

  /**
   * Called before a connection is attempted, does nothing by default and may be overridden.
   */
  protected void onPreConnect() {
    // Do nothing
  }

  private void connect() throws UsbConnectException {
    postStatusChange(DeviceStatus.STATUS_DEVICE_SEARCHING);

    final UsbDevice device = poll(new Pollee<UsbDevice>() {
      @Override
      public boolean attempt() {
        UsbDevice device = findDevice(mUsbManager, getVendorProductIds());
        if (device != null) {
          setResult(device);
          return true;
        }
        return false;
      }
    }, CONNECTION_TIMEOUT_MS, CONNECTION_RETRY_WAIT_MS);

    if (device == null) {
      postStatusChange(DeviceStatus.STATUS_DEVICE_NOT_FOUND);
      throw new UsbDeviceNotFoundException("Device not found");
    } else {
      postStatusChange(DeviceStatus.STATUS_DEVICE_FOUND);
    }

    if (!mUsbManager.hasPermission(device)) {
      throw new UsbConnectException("Permission denied");
    }

    boolean connected = poll(new Pollee<Boolean>() {
      @Override
      boolean attempt() {
        boolean success = setupDevice(device);
        setResult(success);
        return success;
      }
    }, SETUP_TIMEOUT_MS, SETUP_RETRY_WAIT_MS);

    if (connected) {
      postStatusChange(DeviceStatus.STATUS_DEVICE_CONNECTED);
    } else {
      postStatusChange(DeviceStatus.STATUS_DEVICE_CONNECTION_FAILED);
      throw new UsbConnectException("Device setup failed");
    }
  }

  public static UsbDevice findDevice(android.hardware.usb.UsbManager usbManager, Pair<Integer, Integer>[] vendorProductIds) {
    HashMap<String, UsbDevice> devices = usbManager.getDeviceList();
    if (devices == null) {
      return null;
    }

    for (UsbDevice device : devices.values()) {
      if (isMatch(device, vendorProductIds)) {
        return device;
      }
    }

    return null;
  }

  protected abstract Pair<Integer, Integer>[] getVendorProductIds();

  public static boolean isMatch(UsbDevice device, Pair<Integer, Integer>[] vendorProductIds) {
    if (device != null) {
      for (int i = 0; i < vendorProductIds.length; i++) {
        if (device.getVendorId() == vendorProductIds[i].first && device.getProductId() == vendorProductIds[i].second) {
          // Determine if this clover device is being configured for use as an RNDIS interface
          for (int j = 0; j < device.getInterfaceCount(); j++) {
            UsbInterface intf = device.getInterface(j);
            if (intf.getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {
              // Interface class is RNDIS Ethernet Data, don't set accessory mode
              Log.d("USBCloverManager", "Skipping accessory mode due to InterfaceClass: " + intf.getInterfaceClass());
              return false;
            }
          }
          Log.d("USBCloverManager", "Setting accessory mode");
          return true;
        }
      }
    }
    return false;
  }

  protected boolean isBulkInterface() {
    return true;
  }

  protected boolean isInterfaceMatch(UsbInterface usbInterface) {
    return true;
  }

  private boolean setupDevice(UsbDevice device) {
    int interfaceCount = device.getInterfaceCount();
    for (int i = 0; i < interfaceCount; i++) {
      mUsbDevice = device;
      mInterface = mUsbDevice.getInterface(i);

      if (!isInterfaceMatch(mInterface)) {
        continue;
      }

      final int endpointCount = mInterface.getEndpointCount();
      for (int j = 0; j < endpointCount; j++) {
        UsbEndpoint endpoint = mInterface.getEndpoint(j);
        if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
          if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
            mEndpointIn = endpoint;
          } else {
            mEndpointOut = endpoint;
          }
        }
      }

      mConnection = mUsbManager.openDevice(mUsbDevice);

      if (VERBOSE) {
        Log.v(TAG, String.format("USB Device: %s", mUsbDevice));
        Log.v(TAG, String.format("USB Interface: %s", mInterface));
        Log.v(TAG, String.format("USB Endpoint IN: %s", mEndpointIn));
        Log.v(TAG, String.format("USB Endpoint OUT: %s", mEndpointOut));
        Log.v(TAG, String.format("USB Connection: %s", mConnection));
      }

      if (!isConnected()) {
        Log.e(TAG, "Error, open device failed");
        return false;
      }

      if (!mConnection.claimInterface(mInterface, true)) {

        Log.e(TAG, "Error, claim interface failed");
        return false;
      }

      return true;
    }

    Log.e(TAG, "Error, bulk endpoints not found");
    return false;
  }

  public void disconnect() {
    if (mConnection != null) {
      if (mInterface != null) {
        mConnection.releaseInterface(mInterface);
      }
      mConnection.close();
    }

    mConnection = null;
    mInterface = null;
    mEndpointIn = null;
    mEndpointOut = null;
    mUsbDevice = null;

    Arrays.fill(mReadBuffer, (byte) 0);

    postStatusChange(DeviceStatus.STATUS_DEVICE_CONNECTION_CLOSED);

    onPostDisconnect();
  }

  /**
   * Called after disconnect is complete, does nothing by default and may be overridden.
   */
  protected void onPostDisconnect() {
    // Do nothing
  }

  public final boolean isConnected() {
    boolean endPointsOkay = !isBulkInterface() || (mEndpointIn != null && mEndpointOut != null);
    return mConnection != null && mInterface != null && endPointsOkay;
  }

  public int write(byte[] outputData, T params) throws IOException, InterruptedException {
    if (!isConnected() || outputData == null || outputData.length == 0) {
      Log.w(TAG, "Ignoring write request");
      return -1;
    }

    int numBytes = bulkWrite(outputData, params);
    if (numBytes <= 0) {
      onTransferError();
    }
    return numBytes;
  }

  public byte[] read(T params) throws IOException, InterruptedException {
    if (!isConnected()) {
      Log.w(TAG, "Ignoring read request");
      return null;
    }

    byte[] data = bulkRead(params);
    if (data == null) {
      onTransferError();
    }
    return data;
  }

  /**
   * Called when a transfer error occurs, does nothing by default and may be overridden.
   */
  protected void onTransferError() {
    // Do nothing
  }

  private int bulkWrite(byte[] data, T params) throws IOException, InterruptedException {
    data = processOutputData(data, params);

    if (data == null) {
      return -1;
    }

    final int totalDataBytes = data.length;
    int totalBytesTransferred = 0;

    ByteBuffer outDataBuffer = ByteBuffer.wrap(data);

    while (totalDataBytes > totalBytesTransferred) {
      if (Thread.interrupted()) {
        Log.d(TAG, "[write] interrupted");
        throw new InterruptedException();
      }

      final int numBytesToTransfer = Math.min(getMaxWriteDataSize(), totalDataBytes - totalBytesTransferred);
      byte[] writePacket = wrapWritePacket(outDataBuffer, numBytesToTransfer);

      if (VERBOSE) {
        Log.v(TAG, String.format("[write] requesting transfer of %s bytes", writePacket.length));
      }

      // Load member variable into local variable for handling asynchronous closing of the connection
      UsbDeviceConnection localConnection = mConnection;
      if (localConnection == null) {
        // Connection closed underneath
        throw new IOException("Connection closed - [write] interrupted");
      }

      int bulkTransferResultSize = localConnection.bulkTransfer(mEndpointOut, writePacket, writePacket.length,
          WRITE_TIMEOUT_MS + 2 * writePacket.length);

      if (VERBOSE) {
        Log.v(TAG, String.format("[write] bulkTransfer returned %s bytes", bulkTransferResultSize));
      }

      if (bulkTransferResultSize < 0) {
        // This may be due to physical disconnect, wait for an interrupt before erring out
        Thread.sleep(2000);
        totalBytesTransferred = -1;
        break;
      } else if (bulkTransferResultSize != writePacket.length) {
        Log.w(TAG, "[write] error data transferred less than requested");
        totalBytesTransferred = -1;
        break;
      } else {
        totalBytesTransferred += numBytesToTransfer;
      }
    }

    if (VERBOSE) {
      Log.v(TAG, String.format("[write] data transferred: %s of %s bytes", totalBytesTransferred, totalDataBytes));
    }

    return totalBytesTransferred;
  }

  protected byte[] wrapWritePacket(ByteBuffer outDataBuffer, int numBytesToTransfer) {
    byte[] writePacket = new byte[numBytesToTransfer];
    outDataBuffer.get(writePacket, 0, numBytesToTransfer);
    return writePacket;
  }

  private byte[] bulkRead(T params) throws IOException, InterruptedException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(getReadSize());

    while (true) {
      if (Thread.interrupted()) {
        if (VERBOSE) {
          Log.d(TAG, "[read] interrupted");
        }
        throw new InterruptedException();
      }

      if (VERBOSE) {
        Log.v(TAG, "[read] requesting transfer");
      }

      // Load member variable into local variable for handling asynchronous closing of the connection
      UsbDeviceConnection localConnection = mConnection;
      if (localConnection == null) {
        // Connection closed underneath
        throw new IOException("Connection closed - [read] interrupted");
      }
      int numBytesRead = localConnection.bulkTransfer(mEndpointIn, mReadBuffer, getReadSize(), getReadTimeOut());

      if (VERBOSE) {
        Log.v(TAG, String.format("[read] bulkTransfer returned %s bytes", numBytesRead));
      }

      if (numBytesRead < 0) {
        // This may be due to physical disconnect, wait for an interrupt before erring out
        Thread.sleep(2000);
        return null;
      } else if (numBytesRead == 0) {
        // Ignore ZLP (Zero Length Packet) sent by recent versions of USB accessory driver, shouldn't actually ever
        // happen since our protocol avoids it
        continue;
      }

      byte[] inputData = unwrapReadPacket(ByteBuffer.wrap(mReadBuffer, 0, numBytesRead));
      InputResult inputResult = processInputData(inputData, baos, params);

      switch (inputResult) {
        case COMPLETE:
          if (VERBOSE) {
            Log.v(TAG, String.format("[read] complete: %s bytes", baos.size()));
          }
          return baos.toByteArray();
        case ERROR:
          if (VERBOSE) {
            Log.v(TAG, "[read] data transfer error");
          }
          return null;
      }
    }
  }

  protected byte[] unwrapReadPacket(ByteBuffer inDataBuffer) {
    byte[] inputData = new byte[inDataBuffer.remaining()];
    inDataBuffer.get(inputData);
    return inputData;
  }

  /**
   * Returns the amount of time to wait before read fails, 0 means wait forever.
   */
  protected int getReadTimeOut() {
    return READ_TIMEOUT_MS;
  }

  /**
   * Optionally override this method to perform data processing on the output bytes before they are sent. The returned
   * byte array is used for the data transfer, the implementation may choose to modify the byte array parameter or
   * return a new byte array.
   */
  protected byte[] processOutputData(byte[] outputData, T params) {
    return outputData;
  }

  protected enum InputResult {
    CONTINUE, COMPLETE, ERROR
  }

  /**
   * Optionally override this method to perform data processing on the input data bytes before they are returned. The
   * implementation must write the desired output bytes to the ByteArrayOutputStream. Unlike
   * {@link #processOutputData(byte[], T)} this function may be called multiple times in a single transfer depending on the
   * return value of this function
   *
   * @param inputData    buffer containing bytes read from the USB device
   * @param outputStream stream containing bytes that will be returned to the caller
   * @param params       additional parameters which may allow the implementation to modify the way the byte array is interpreted
   * @return CONTINUE to request more USB device data, COMPLETE to return the received data or ERROR if something is wrong
   */
  protected InputResult processInputData(byte[] inputData, ByteArrayOutputStream outputStream, T params) {
    if (inputData == null || inputData.length == 0) {
      return InputResult.ERROR;
    }

    outputStream.write(inputData, 0, inputData.length);
    return InputResult.COMPLETE;
  }

  protected final DeviceStatus postStatusChange(DeviceStatus status) {
    if (VERBOSE) {
      Log.v(getClass().getSimpleName(), String.format("DeviceStatus change: %s", status));
    }
    return status;
  }

  private static void assertBackgroundThread() {
    if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
      throw new IllegalStateException("Invoking function from main thread not allowed");
    }
  }

  private static abstract class Pollee<T> {
    T result;

    abstract boolean attempt();

    void setResult(T result) {
      this.result = result;
    }

    T getResult() {
      return result;
    }

  }

  private static <T> T poll(Pollee<T> pollee, int timeoutMs, int waitMs) {
    final long connectStartTime = SystemClock.elapsedRealtime();

    boolean success;
    do {
      success = pollee.attempt();
      if (!success) {
        SystemClock.sleep(waitMs);
      }
    } while (!success && (SystemClock.elapsedRealtime() - connectStartTime) < timeoutMs);

    return pollee.getResult();
  }

}
