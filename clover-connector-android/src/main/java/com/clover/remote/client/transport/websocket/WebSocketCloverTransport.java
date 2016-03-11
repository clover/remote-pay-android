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

import android.util.Log;

import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.CloverTransportObserver;
import com.clover.remote.client.transport.websocket.CloverWebSocketClient;
import com.clover.remote.client.transport.websocket.CloverWebSocketClientListener;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocketCloverTransport extends CloverTransport implements CloverWebSocketClientListener {

  //private static final Framedata PING = new PingFramedata();

  /*
  These hold the configurable options
   */
  private int maxPingRetriesBeforeDisconnect = 4;
  private long heartbeatInterval = 5000L;
  private long reconnectDelay = 3000L;
  URI endpoint;

  Gson gson = new Gson();
  CloverWebSocketClient webSocket;        // TODO: investigate whether the assignment in the onClose callback could cause problems
  String status = "Disconnected";   // TODO: investigate whether the assignment in the onClose callback could cause problems
  /**
   * prevent reconnects if shutdown was requested
   */
  boolean shutdown = false;

  /**
   * A single thread/queue to process reconnect requests
   */
  ScheduledThreadPoolExecutor reconnectPool = new ScheduledThreadPoolExecutor(1);


  private final Runnable reconnector = new Runnable() {
    @Override public void run() {
      if(!shutdown) {
        try {
          initialize(endpoint);
        } catch (Exception e) {
          reconnect();
        }
      }
    }
  };

  public WebSocketCloverTransport(URI endpoint, long heartbeatInterval, long reconnectDelay, int retriesUntilDisconnect) {

    this.endpoint = endpoint;
    this.heartbeatInterval = Math.max(10, heartbeatInterval);
    this.reconnectDelay = Math.max(0, reconnectDelay);
    this.maxPingRetriesBeforeDisconnect = Math.max(0, retriesUntilDisconnect);
    initialize(endpoint);
  }

  @Override
  public int sendMessage(final String message) {
    // let's see if we have connectivity

    if (webSocket != null && webSocket.getConnection().isOpen()) {
      try {
        Log.d(getClass().getName(), "Sending message to WebSocket: " + message);
        webSocket.send(message);
      } catch (WebsocketNotConnectedException e) {
        Log.i(getClass().getSimpleName(), String.format("Error sending message: %s", message), e);
        reconnect();
      }

      return 0;
    } else {
      //clearWebsocket(); // socket may be in a CLOSING state
      reconnect();
    }
    return -1;
  }

  private synchronized void clearWebsocket() {
    if(webSocket != null) {
      webSocket.clearListener();
    }
    webSocket = null;
  }

  private synchronized void initialize(URI deviceEndpoint) {
    if(webSocket != null) {
      if(webSocket.getConnection().isOpen() || webSocket.getConnection().isConnecting()) {
        return;
      } else {
        clearWebsocket();
      }
    }

    webSocket = new CloverWebSocketClient(deviceEndpoint, this, heartbeatInterval);

    webSocket.connect();
    /*try {
      webSocket.connectBlocking();
    } catch (Throwable t) {
      clearWebsocket();
      reconnect();
    }*/
    Log.d(getClass().getSimpleName(), "connection attempt done.");
  }

  public void dispose() {
    shutdown = true;
    if (webSocket != null) {
      webSocket.setNotifyClose(true);   // <-- need to notify because close was requested
      webSocket.close();
    }
    clearWebsocket();
  }


  public void reconnect() {
    if (shutdown) {
      Log.d(getClass().getSimpleName(), "Not attempting to reconnect, shutdown...");
      return;
    }
    //      Log.i(WebSocketCloverTransport.class.getName(), "reconnecting");

    reconnectPool.schedule(reconnector, reconnectDelay, TimeUnit.MILLISECONDS);
  }

  public synchronized void onClose(CloverWebSocketClient ws) {
    if(webSocket == ws) {
      clearWebsocket();
      reconnect();
    }
  }

  @Override
  public void onNotResponding(WebSocketClient ws) {
    if(webSocket == ws) {
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "onNotResponding");
        observer.onDeviceDisconnected(this);
      }
    }
  }

  @Override
  public void onPingResponding(WebSocketClient ws) {
    if(webSocket == ws) {
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "onPingResponding");
        observer.onDeviceReady(this);
      }
    }
  }

  @Override
  public void onOpen(WebSocketClient ws, ServerHandshake handshakedata) {
    if(webSocket == ws) {
      // notify connected
      // notify ready
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "onOpen, Connected");
        observer.onDeviceConnected(this);
      }
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "onOpen, Ready");
        observer.onDeviceReady(this);
      }
    }
  }

  @Override
  public void onClose(WebSocketClient ws, int code, String reason, boolean remote) {
    if(webSocket == ws) {
      clearWebsocket();
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "onClose");
        observer.onDeviceDisconnected(this);
      }

      reconnect();
      // notify disconnected
    }
  }

  @Override
  public void onMessage(WebSocketClient ws, String message) {
    if(webSocket == ws) {
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "Got message: " + message);
        observer.onMessage(message);
      }
    }

  }
}