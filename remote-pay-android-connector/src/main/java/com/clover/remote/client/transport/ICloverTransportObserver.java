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


/**
 * Asynchronous callback interface to handle device event and message notifications.
 */
public interface ICloverTransportObserver {
  /**
   * Device is there but not yet ready for use
   *
   * @param transport transport on which the event was received
   */
  void onDeviceConnected(ICloverTransport transport);

  /**
   * Device is there and ready for use
   *
   * @param transport transport on which the event was received
   */
  void onDeviceReady(ICloverTransport transport);

  /**
   * Device is not there anymore
   *
   * @param transport transport on which the event was received
   */
  void onDeviceDisconnected(ICloverTransport transport);

  /**
   * Called when a raw message is received from the device
   *
   * @param message message
   */
  void onMessage(String message);
}