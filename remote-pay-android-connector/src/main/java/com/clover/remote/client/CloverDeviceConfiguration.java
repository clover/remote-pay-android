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

package com.clover.remote.client;

import com.clover.remote.client.transport.ICloverTransport;

import java.io.Serializable;

/**
 * Interface which defines the configuration of the underlying transport for connecting to a Clover device.
 */
public interface CloverDeviceConfiguration extends Serializable {

  public static final String REMOTE_SDK = "com.clover.cloverconnector.android:4.3.5-Public";

  /**
   * Retrieve the class name of the clover device instantiated during the {@link CloverConnector#initializeConnection()} call.
   *
   * @return the canonical class name
   */
  String getCloverDeviceTypeName();

  /**
   * Retrieve the package name broadcast on all messages sent to the remote device
   *
   * @return package name
   */
  String getMessagePackageName();

  /**
   * Retrieve the device configuration name
   *
   * @return name
   */
  String getName();

  /**
   * Retrieve the max message characters
   *
   * @return max numner characters
   */
  int getMaxMessageCharacters();


  /**
   * Retrieve the configuration specified transport (e.g. network, etc.) associated with this configuration.  Note
   * that the underlying connection for the transport is not made as a part of this call.
   *
   * @return disconnected transport
   */
  ICloverTransport getCloverTransport();

  /**
   * Retrieve the application ID broadcast on all messages sent to the remote device
   *
   * @return application ID
   */
  String getApplicationId();
}
