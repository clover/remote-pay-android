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

public interface CloverTransportObserver {
  /// <summary>
  /// Device is there but not yet ready for use
  /// </summary>
  void onDeviceConnected(CloverTransport transport);

  /// <summary>
  /// Device is there and ready for use
  /// </summary>
  void onDeviceReady(CloverTransport transport);

  /**
   * <p>
   *     Device has either been told to disconnect or the keepalive has timed out
   * </p>
   * @param transport for talking to the clover device
   */
  void onDeviceDisconnecting(CloverTransport transport);

  /// <summary>
  /// Device is not there anymore
  /// </summary>
  /// <param name="transport"></param>
  void onDeviceDisconnected(CloverTransport transport);

  void onMessage(String message);
}