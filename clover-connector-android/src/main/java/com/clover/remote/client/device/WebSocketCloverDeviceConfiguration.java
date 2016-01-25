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
import com.clover.remote.client.transport.WebSocketCloverTransport;

import java.net.URI;

public class WebSocketCloverDeviceConfiguration implements CloverDeviceConfiguration {
  private URI uri = null;

  public WebSocketCloverDeviceConfiguration(URI endpoint) {
    uri = endpoint;
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
    return new WebSocketCloverTransport(uri);
  }
}
