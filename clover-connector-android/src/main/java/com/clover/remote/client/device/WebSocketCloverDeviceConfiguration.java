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

package com.clover.remote.client.device;

import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.websocket.WebSocketCloverTransport;

import java.net.URI;

public class WebSocketCloverDeviceConfiguration implements CloverDeviceConfiguration {
  private URI uri = null;
  /**
   * ping heartbeat interval in milliseconds
   */
  private long heartbeatInterval = 1000L;

  /**
   * delay before attempting a reconnect in milliseconds, so after a disconnect, the client will
   * try to establish a connection every <i>reconnectDelay</i> milliseconds
   */
  private long reconnectDelay = 3000L;

  /**
   * the number of missed pong response periods before a reconnect is executed.
   * Effectively, it will timeout after pingRetryCountBeforeReconnect * heartbeatInterval
   */
  private int pingRetryCountBeforeReconnect = 4;

  public WebSocketCloverDeviceConfiguration(URI endpoint) {
    uri = endpoint;
  }

  public WebSocketCloverDeviceConfiguration(URI endpoint, long heartbeatInterval, long reconnectDelay) {
    this(endpoint);
    this.heartbeatInterval = Math.max(100, heartbeatInterval);
    this.reconnectDelay = Math.max(0, reconnectDelay);
  }

  public Long getHeartbeatInterval() {
    return heartbeatInterval;
  }

  public void setHeartbeatInterval(Long heartbeatInterval) {
    this.heartbeatInterval = heartbeatInterval;
  }

  public Long getReconnectDelay() {
    return reconnectDelay;
  }

  public void setReconnectDelay(Long reconnectDelay) {
    this.reconnectDelay = reconnectDelay;
  }

  public int getPingRetryCountBeforeReconnect() {
    return pingRetryCountBeforeReconnect;
  }

  public void setPingRetryCountBeforeReconnect(int pingRetryCountBeforeReconnect) {
    this.pingRetryCountBeforeReconnect = pingRetryCountBeforeReconnect;
  }

  @Override
  public String getCloverDeviceTypeName() {
    return DefaultCloverDevice.class.getCanonicalName();
  }

  @Override
  public String getMessagePackageName() {
    return "com.clover.remote.protocol.lan";
  }

  @Override
  public String getName() {
    return "Clover WebSocket Connector";
  }

  @Override
  public CloverTransport getCloverTransport() {
    return new WebSocketCloverTransport(uri, heartbeatInterval, reconnectDelay, pingRetryCountBeforeReconnect);
  }
}
