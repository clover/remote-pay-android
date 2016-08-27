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

package com.clover.remote.client.transport.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public interface CloverWebSocketClientListener {
  public void onOpen(WebSocketClient ws, ServerHandshake handshakedata);

  public void onNotResponding(WebSocketClient ws);

  public void onPingResponding(WebSocketClient ws);

  public void onClose(WebSocketClient ws, int code, String reason, boolean remote);

  public void onMessage(WebSocketClient ws, String message);

}
