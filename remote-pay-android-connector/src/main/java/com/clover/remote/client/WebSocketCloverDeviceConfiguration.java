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

import com.clover.remote.client.device.DefaultCloverDevice;
import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.PairingDeviceConfiguration;
import com.clover.remote.client.transport.websocket.WebSocketCloverTransport;

import java.io.Serializable;
import java.net.URI;
import java.security.KeyStore;

/**
 * Default configuration to communicate with the Mini via WebSockets to the LAN Pay Display
 */
public abstract class WebSocketCloverDeviceConfiguration implements PairingDeviceConfiguration, CloverDeviceConfiguration, Serializable {

  private static final long RECONNECT_DELAY = 2000;
  private static final long PING_FREQUENCY = 3000;
  private static final long PONG_TIMEOUT = 6000;
  private static final long REPORT_CONNECTION_PROBLEM_AFTER = 6000;

  private final String posName;
  private final String serialNumber;
  private final String authToken;
  private final URI uri;
  private final KeyStore trustStore;
  private final String appId;
  private final long pongTimeout;
  private final long pingFrequency;
  private final long reconnectDelay;
  private final long reportConnectionProblemAfter;

  /**
   * Constructor
   *
   * @param endpoint network endpoint of the device to connect to
   * @param applicationId remote application ID
   * @param trustStore certificate keystore used to support the secure websockets protocol (wss)
   * @param posName point of sale name
   * @param serialNumber serial number of the POS terminal/device attaching to the clover device
   * @param authToken cached authentication token provided from a previous {@link PairingDeviceConfiguration#onPairingSuccess(String)} call
   */
  public WebSocketCloverDeviceConfiguration(URI endpoint, String applicationId, KeyStore trustStore, String posName, String serialNumber, String authToken) {
    this(endpoint, applicationId, trustStore, posName, serialNumber, authToken,
      PONG_TIMEOUT, PING_FREQUENCY, RECONNECT_DELAY, REPORT_CONNECTION_PROBLEM_AFTER);
  }

  /**
   * Constructor
   *
   * @param endpoint network endpoint of the device to connect to
   * @param applicationId remote application ID
   * @param trustStore certificate keystore used to support the secure websockets protocol (wss)
   * @param posName point of sale name
   * @param serialNumber serial number of the POS terminal/device attaching to the clover device
   * @param authToken cached authentication token provided from a previous {@link PairingDeviceConfiguration#onPairingSuccess(String)} call
   * @param pongTimeout amount of time, in milliseconds, before closing the connection, but still wait
   * @param pingFrequency amount of time, in milliseconds, between pings
   * @param reconnectDelay amount of time, in milliseconds, to wait before attempting to reconnect
   * @param reportConnectionProblemAfter amount of time, in milliseconds, in which a disconnected client is reported if a pong hasn't come back,
   *                                     before it is actually disconnected
   */
  public WebSocketCloverDeviceConfiguration(URI endpoint, String applicationId, KeyStore trustStore, String posName, String serialNumber, String authToken,
                                            long pongTimeout, long pingFrequency, long reconnectDelay, long reportConnectionProblemAfter) {
    this.uri = endpoint;
    this.appId = applicationId;
    this.trustStore = trustStore;
    this.posName = posName;
    this.serialNumber = serialNumber;
    this.authToken = authToken;

    this.pongTimeout = pongTimeout;
    this.pingFrequency = pingFrequency;
    this.reconnectDelay = reconnectDelay;
    this.reportConnectionProblemAfter = reportConnectionProblemAfter;
  }

  @Override
  public String getApplicationId() {
    return appId;
  }

  @Override
  public String getCloverDeviceTypeName() {
    return DefaultCloverDevice.class.getCanonicalName();
  }

  @Override
  public String getMessagePackageName() {
    return "com.clover.remote_protocol_broadcast.app";
  }

  @Override
  public String getName() {
    return "Clover WebSocket Connector";
  }

  @Override
  public CloverTransport getCloverTransport() {
    return new WebSocketCloverTransport(uri, this, trustStore, posName, serialNumber, authToken,
        pongTimeout, pingFrequency, reconnectDelay, reportConnectionProblemAfter);
  }
}
