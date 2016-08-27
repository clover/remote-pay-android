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
import com.clover.remote.client.transport.CloverTransportObserver;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class CloverWebSocketClient extends WebSocketClient {

  public static final FramedataImpl1 PING = new FramedataImpl1(Framedata.Opcode.PING);
  public static final int maxPingRetriesBeforeDisconnect = 4;
  private final long heartbeatInterval;
  private CloverWebSocketClientListener listener;

  static {
    PING.setFin(true);
    PING.setTransferemasked(true);
    try {
      PING.setPayload(ByteBuffer.allocate(0));
    } catch (InvalidDataException e) {
      e.printStackTrace();
    }
  }

  /**
   * A single thread/queue to send a ping and disconnect if no pong response
   */
  ScheduledThreadPoolExecutor timerPool = new ScheduledThreadPoolExecutor(1);
  /**
   * <p>
   * task to perform disconnection that should be canceled in the case of a timely
   * pong message
   * </p>
   */
  private ScheduledFuture<?> disconnectFuture;

  /**
   * <p>
   * In the case of a ping/pong timeout, notify the observers of the connection closing
   * before cleanup has happened so that observers can react in a timely manner.
   * </p>
   */
  private volatile boolean notifyClose = true;
  /**
   * <p>
   * Holds the tasks to perform disconnection should be canceled in the case of a timely
   * pong message. Once a future is canceled, it should be removed from the list.
   * </p>
   */
  private CopyOnWriteArrayList<ScheduledFuture<?>> disconnectFutures = new CopyOnWriteArrayList<>();
  /**
   * holds the state if we didn't get the ping within the first interval.
   */
  boolean pingTimeoutState;

  public CloverWebSocketClient(URI endpoint, CloverWebSocketClientListener listener, long heartbeatInterval) {
    super(endpoint);
    this.listener = listener;
    this.heartbeatInterval = heartbeatInterval;

  }

  public void setListener(CloverWebSocketClientListener listener) {
    this.listener = listener;
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {

    if (listener != null) {
      listener.onOpen(this, handshakedata);
    }

    timerPool.schedule(pinger, heartbeatInterval, TimeUnit.MILLISECONDS);
  }

  @Override
  public void onWebsocketPing(WebSocket conn, Framedata f) {

    super.onWebsocketPing(conn, f);
  }

  @Override
  public void onWebsocketPong(WebSocket conn, Framedata f) {
    super.onWebsocketPong(conn, f);

    cancelAllDisconnectHandlers();
    if (isPingTimeoutState()) {
      listener.onPingResponding(this);
    }
    setPingTimeoutState(false);
    Log.d(WebSocketCloverTransport.class.getName(), "got Pong");
    disconnectFuture.cancel(true);


    timerPool.schedule(pinger, heartbeatInterval, TimeUnit.MILLISECONDS);
  }

  @Override
  public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
    super.onWebsocketClosing(conn, code, reason, remote);
    if (listener != null) {
      listener.onClose(this, code, reason, remote);
    }
  }

  @Override
  public void onMessage(String message) {
    if (listener != null) {
      listener.onMessage(this, message);
    }
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    if (listener != null) {
      listener.onClose(this, code, reason, remote);
    }
  }

  private void dispose() {
    clearListener();
  }

  public void clearListener() {
    listener = null;
    Log.d(CloverWebSocketClient.class.getSimpleName(), "Socket abandoned, no longer listening to it.");
  }

  private void setPingTimeoutState(boolean pingTimeoutState) {
    synchronized (this) {
      this.pingTimeoutState = pingTimeoutState;
    }
  }

  private boolean isPingTimeoutState() {
    synchronized (this) {
      return pingTimeoutState;
    }
  }

  void setNotifyClose(boolean notifyClose) {
    synchronized (this) {
      this.notifyClose = notifyClose;
    }
  }

  private boolean shouldNotifyClose() {
    synchronized (this) {
      return notifyClose;
    }
  }

  void cancelAllDisconnectHandlers() {
    Iterator<ScheduledFuture<?>> it = disconnectFutures.iterator();
    while (it.hasNext()) {
      ScheduledFuture<?> disconnectFuture = it.next();
      disconnectFuture.cancel(true);
      disconnectFutures.remove(disconnectFuture); // <-- once canceled, there is no reason to keep the future
    }
  }

  @Override
  public void onError(Exception ex) {
    Log.e(getClass().getName(), "onError", ex);
    // TODO: should be close and re-open?
  }

  @Override
  public void close() {
    super.close();
    dispose();
  }

  @Override
  public void closeBlocking() throws InterruptedException {
    super.closeBlocking();
    dispose();
  }

  private final Runnable pinger = new Runnable() {
    @Override
    public void run() {
      WebSocket connection = getConnection();
      if (connection != null && !connection.isOpen()) {
        return;
      }
      Log.d(WebSocketCloverTransport.class.getName(), "sending Ping...");

      disconnectFuture = timerPool.schedule(new DisconnectHandler(), heartbeatInterval, TimeUnit.MILLISECONDS);
      connection.sendFrame(PING);
    }
  };

  private final class DisconnectHandler implements Runnable {

    private int retryCount = maxPingRetriesBeforeDisconnect;

    @Override
    public void run() {
      if (listener != null && retryCount == maxPingRetriesBeforeDisconnect) {
        setPingTimeoutState(true);
        listener.onNotResponding(CloverWebSocketClient.this);
      }

      if (retryCount > 0) {
        retryCount--;
        Log.d(WebSocketCloverTransport.class.getSimpleName(), "");
        disconnectFutures.add(timerPool.schedule(this, heartbeatInterval, TimeUnit.MILLISECONDS));
        return;
      }

      Log.d(WebSocketCloverTransport.class.getSimpleName(), "No ping response, so closing");
      cancelAllDisconnectHandlers(); // just to be safe...
      close();
    }
  }
}
