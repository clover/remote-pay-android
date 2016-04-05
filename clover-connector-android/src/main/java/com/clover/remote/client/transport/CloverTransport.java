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

import com.clover.remote.protocol.message.DiscoveryResponseMessage;

import java.nio.channels.NotYetConnectedException;
import java.util.ArrayList;
import java.util.List;

public abstract class CloverTransport {
  protected List<CloverTransportObserver> observers = new ArrayList<CloverTransportObserver>();
  boolean ready = false;
  private DiscoveryResponseMessage lastDiscoverResponseMessage;

  protected void onDeviceConnected() {
    for (CloverTransportObserver obs : observers) {
      obs.onDeviceConnected(this);
    }

  }

  protected void onDeviceReady(DiscoveryResponseMessage drm) {
    ready = true;
    for (CloverTransportObserver obs : observers) {
      obs.onDeviceReady(this);
    }
  }

  protected void onDeviceDisconnected() {
    ready = false;
    for (CloverTransportObserver obs : observers) {
      obs.onDeviceDisconnected(this);
    }
  }

  /// <summary>
  /// Should be called by subclasses when a message is received.
  /// </summary>
  /// <param name="message"></param>
  protected void onMessage(String message) {
    for (CloverTransportObserver obs : observers) {
      obs.onMessage(message);
    }
  }

  public void Subscribe(CloverTransportObserver observer) {
    CloverTransport me = this;
    // to notify if the device has already reported as ready
    if (ready) {
      for (CloverTransportObserver obs : observers) {
        obs.onDeviceReady(this);
      }
    }
    observers.add(observer);
  }

  public abstract void dispose();

  public void Unsubscribe(CloverTransportObserver observer) {
    observers.remove(observer);
  }

  public void clearListeners() {
    observers.clear();
  }


  // Implement this to send info
  public abstract int sendMessage(String message) throws NotYetConnectedException;
}

