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
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;


public class RemoteUsbManager extends UsbCloverManager<Void> {

  private final String TAG = getClass().getSimpleName();

  private static final boolean VERBOSE = true;

  private static final Charset UTF_8 = Charset.forName("UTF-8");

  private static final int REMOTE_STRING_MAGIC_START_TOKEN = 0xcc771122;
  private static final int REMOTE_STRING_LENGTH_MAX = 4 * 1024 * 1024;
  private static final int REMOTE_STRING_HEADER_BYTE_COUNT = 4 + 4; // 2 ints

  // Defined by AOA
  private static final int MAX_PACKET_BYTES = 16384;
  // Size of a short
  private static final int PACKET_HEADER_SIZE = 2;

  public RemoteUsbManager(Context context) {
    super(context);
  }

  @Override
  protected int getReadSize() {
    return MAX_PACKET_BYTES;
  }

  @Override
  protected int getMaxWriteDataSize() {
    return MAX_PACKET_BYTES - PACKET_HEADER_SIZE;
  }

  public static boolean isUsbDeviceAttached(Context context) {
    UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    return findDevice(usbManager, VENDOR_PRODUCT_IDS) != null;
  }

  // See init.maplecutter.usb.rc in platform for more info
  public static final Pair<Integer, Integer>[] VENDOR_PRODUCT_IDS = new Pair[]{
      // Somehow related to the Mobile - Don't remove
      Pair.create(0x18d1, 0x2d00), // google accessory usb device
      Pair.create(0x18d1, 0x2d01), // google adb,accessory usb device

      // Production devices
      Pair.create(0x28f3, 0x2002), // leafcutter accessory usb device
      Pair.create(0x28f3, 0x3002), // maplecutter accessory usb device
      Pair.create(0x28f3, 0x4002), // bayleaf accessory usb device

      // Development devices
      Pair.create(0x28f3, 0x2004), // leafcutter adb,accessory usb device
      Pair.create(0x28f3, 0x3004), // maplecutter adb,accessory usb device
      Pair.create(0x28f3, 0x4004), // bayleaf adb,accessory usb device
  };

  @Override
  protected Pair<Integer, Integer>[] getVendorProductIds() {
    return VENDOR_PRODUCT_IDS;
  }

  @Override
  protected boolean isInterfaceMatch(UsbInterface usbInterface) {
    if (VERBOSE) {
      Log.d(TAG, String.format("Checking interface match: %s", usbInterface));
    }

    // Specified to avoid using ADB interface
    if (usbInterface.getInterfaceClass() == 0xff
        && usbInterface.getInterfaceSubclass() == 0xff
        && usbInterface.getInterfaceProtocol() == 0) {
      return true;
    }

    return false;
  }

  @Override
  protected int getReadTimeOut() {
    return -1; // No timeout, wait forever
  }

  @Override
  protected byte[] wrapWritePacket(ByteBuffer outDataBuffer, int numBytesToTransfer) {
    ByteBuffer writePacketBuffer = ByteBuffer.allocate(numBytesToTransfer + PACKET_HEADER_SIZE);
    writePacketBuffer.putShort((short) numBytesToTransfer);
    outDataBuffer.get(writePacketBuffer.array(), PACKET_HEADER_SIZE, numBytesToTransfer);
    return writePacketBuffer.array();
  }

  @Override
  protected byte[] unwrapReadPacket(ByteBuffer inDataBuffer) {
    short inputSize = inDataBuffer.getShort();
    if (inputSize <= 0) {
      Log.w(TAG, String.format("Error, packet too small: %d bytes", inputSize));
      return null;
    }

    if (VERBOSE) {
      Log.v(TAG, String.format("Input packet size: %d bytes", inputSize));
    }

    byte[] inputData = new byte[inputSize];
    inDataBuffer.get(inputData);
    return inputData;
  }

  @Override
  protected byte[] processOutputData(byte[] outputData, Void aVoid) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    try {
      dos.writeInt(REMOTE_STRING_MAGIC_START_TOKEN);
      dos.writeInt(outputData.length);
      dos.write(outputData);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    byte[] processedOutput = baos.toByteArray();
    if (VERBOSE) {
      Log.v(TAG, String.format("Processed output: %s bytes", processedOutput.length));
    }

    return processedOutput;
  }

  private int mCurrentIncomingStringLength;

  @Override
  protected InputResult processInputData(byte[] inputData, ByteArrayOutputStream outputStream, Void params) {
    try {
      if (inputData == null) {
        throw new IOException("Read error");
      }

      int numBytesRead = inputData.length;

      if (numBytesRead == 0) {
        throw new IOException("Read zero bytes");
      }

      DataInputStream dis = new DataInputStream(new ByteArrayInputStream(inputData));

      if (outputStream.size() == 0) {
        // Start packet
        final int startInt = dis.readInt();
        if (startInt != REMOTE_STRING_MAGIC_START_TOKEN) {
          Log.e(TAG, "Invalid start token: " + Integer.toHexString(startInt));
          throw new IOException("Unexpected start token: 0x" + Integer.toHexString(startInt));
        }

        final int totalStringLength = dis.readInt();
        if (totalStringLength <= 0 || totalStringLength > REMOTE_STRING_LENGTH_MAX) {
          Log.e(TAG, "Invalid length of message: " + totalStringLength + " bytes");
          throw new IOException("Illegal string length: " + totalStringLength + " bytes");
        }

        mCurrentIncomingStringLength = totalStringLength;

        outputStream.write(inputData, REMOTE_STRING_HEADER_BYTE_COUNT, numBytesRead - REMOTE_STRING_HEADER_BYTE_COUNT);
      } else {
        // Continuation packet
        outputStream.write(inputData, 0, numBytesRead);
      }

      int remainingBytes = mCurrentIncomingStringLength - outputStream.size();
      if (remainingBytes > 0) {
        return InputResult.CONTINUE;
      } else {
        mCurrentIncomingStringLength = 0;
        return InputResult.COMPLETE;
      }
    } catch (Exception e) {
      Log.w(TAG, "Unable to process USB input data", e);
      mCurrentIncomingStringLength = 0;
      return InputResult.ERROR;
    }
  }

  public void sendString(String string) throws IOException, InterruptedException {
    if (VERBOSE) {
      Log.v(TAG, String.format("Sending: %s", string));
    }

    byte[] stringBytes = string.getBytes(UTF_8);

    final int stringByteLength = stringBytes.length;
    if (stringByteLength <= 0 || stringByteLength > REMOTE_STRING_LENGTH_MAX) {
      if (stringByteLength <= 0) {
        Log.w(TAG, "sending a 0 lenght message");
      } else {
        Log.w(TAG, "message exceeds max length");
      }

      throw new IllegalArgumentException("String byte length " + stringByteLength + " bytes outside limits");
    }

    int numWrittenBytes = write(stringBytes, null);
    if (numWrittenBytes <= 0) {
      throw new IOException("USB bulk data write failed");
    }
  }

  public String receiveString() throws IOException, InterruptedException {
    byte[] stringBytes = read(null);
    if (stringBytes == null) {
      throw new IOException("USB bulk data read failed");
    }

    String string = new String(stringBytes, UTF_8);
    if (VERBOSE) {
      Log.v(TAG, String.format("Received: %s", string));
    }

    return string;
  }

}
