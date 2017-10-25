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

package com.clover.remote.client.transport;

import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CloverTransport implements ICloverTransport {

  public static final String DEVICE_CONNECTED = "com.clover.remotepay.DEVICE_CONNECTED";
  public static final String DEVICE_READY = "com.clover.remotepay.DEVICE_READY";
  public static final String DEVICE_DISCONNECTED = "com.clover.remotepay.DEVICE_DISCONNECTED";

  private final List<ICloverTransportObserver> observers = new CopyOnWriteArrayList<>();

  /**
   * Should be called by subclasses (super.notifyDeviceConnected) when the device connects (but is not ready)
   * in order to forward to all observers
   */
  protected void notifyDeviceConnected() {
    for (ICloverTransportObserver obs : observers) {
      try {
        obs.onDeviceConnected(this);
      } catch (Exception ex) {
        Log.e(getClass().getName(), "Error notifying observer", ex);
      }
    }
  }

  /**
   * Should be called by subclasses (super.notifyDeviceReady) when the device is ready to process messages
   * in order to forward to all observers
   */
  protected void notifyDeviceReady() {
    for (ICloverTransportObserver obs : observers) {
      try {
        obs.onDeviceReady(this);
      } catch (Exception ex) {
        Log.e(getClass().getName(), "Error notifying observer", ex);
      }
    }
  }

  /**
   * Should be called by subclasses (super.notifyDeviceDisconnected) when the device disconnects
   * in order to forward to all observers
   */
  protected void notifyDeviceDisconnected() {
    for (ICloverTransportObserver obs : observers) {
      try {
        obs.onDeviceDisconnected(this);
      } catch (Exception ex) {
        Log.e(getClass().getName(), "Error notifying observer", ex);
      }
    }
  }

  /**
   * Should be called by subclasses (super.onMessage) when a message is received
   * in order to forward to all observers
   * @param message message to forward
   */
  protected void onMessage(String message) {
    for (ICloverTransportObserver obs : observers) {
      try {
        obs.onMessage(message);
      } catch (Exception ex) {
        Log.e(getClass().getName(), "Error processing message: " + message, ex);
      }
    }
  }

  public int getRemoteMessageVersion(){
    return 1;
  }

  public void addObserver(ICloverTransportObserver observer) {
    observers.add(observer);
  }

  public void removeObserver(ICloverTransportObserver observer) {
    observers.remove(observer);
  }

  public void dispose() {
    observers.clear();
  }
}

