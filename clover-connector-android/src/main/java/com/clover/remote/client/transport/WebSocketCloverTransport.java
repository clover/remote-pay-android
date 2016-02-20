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

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocketCloverTransport extends CloverTransport {

    private static final Framedata PING = new PingFramedata();
    private static final long HEARTBEAT_INTERVAL = 5000L;

    /**
     * <p>
     *     task to perform disconnection that should be canceled in the case of a timely
     *     pong message
     * </p>
     */
    private ScheduledFuture<?> disconnectFuture;

    /**
     * <p>
     *     In the case of a ping/pong timeout, notify the observers of the connection closing
     *     before cleanup has happened so that observers can react in a timely manner.
     * </p>
     */
    private volatile boolean notifyClose = true;

    private final Runnable disconnector = new Runnable() {
        @Override
        public void run() {
            if (webSocket == null) {
                return;
            }

            setNotifyClose(false);
            if (observers != null) {
                for (CloverTransportObserver observer : observers) {
                    observer.onDeviceDisconnected(WebSocketCloverTransport.this);
                }
            }

            try {
                webSocket.closeBlocking();  // <-- synchronous close keeps from opening multiple connections to the same device
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    };

    private final Runnable pinger = new Runnable() {
        @Override
        public void run() {
            if (webSocket == null) {
                return;
            }
//            Log.i(WebSocketCloverTransport.class.getName(), "pinging");
            webSocket.getConnection().sendFrame(PING);
            disconnectFuture = timerPool.schedule(disconnector, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
        }
    };

  Gson gson = new Gson();
  WebSocketClient webSocket;        // TODO: investigate whether the assignment in the onClose callback could cause problems
  String status = "Disconnected";   // TODO: investigate whether the assignment in the onClose callback could cause problems
  boolean shutdown = false;
  URI endpoint;
  Timer pingTimer = new Timer();
  ScheduledThreadPoolExecutor timerPool = new ScheduledThreadPoolExecutor(1);

  CloverTransportObserver tempObs = null;

  //List<CloverTransportObserver> listeners = new ArrayList<CloverTransportObserver>();

  public WebSocketCloverTransport(URI endpoint) {
    this.endpoint = endpoint;
    initialize(endpoint);
  }

  @Override
  public int sendMessage(final String message) {
    // let's see if we have connectivity

    if (webSocket != null) {
      try {
        Log.d(getClass().getName(), "Sending message to WebSocket: " + message);
        webSocket.send(message);
                /*new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] params) {
                        ALog.d(this, "%s", "Sending message");
                        return null;
                    }
                }.execute();*/
      } catch (WebsocketNotConnectedException e) {
        // maybe it closed, so let's try to re-open and then send...
                /*webSocket = null;
                tempObs = new CloverTransportObserver() {
                    @Override
                    public void onDeviceConnected(CloverTransport transport) {

                    }

                    @Override
                    public void onDeviceReady(CloverTransport transport) {
                        webSocket.send(message);
                        observers.remove(tempObs);
                    }

                    @Override
                    public void onDeviceDisconnected(CloverTransport transport) {
                        observers.remove(tempObs);
                    }

                    @Override
                    public void onMessage(String message) {

                    }
                };
                observers.add(tempObs);*/
        initialize(endpoint);
      }

      return 0;
    } else {
      reconnect();
    }
    return -1;
  }


  public void initialize(URI deviceEndpoint) {
    onDeviceConnected();
    final WebSocketClient tempWebSocket = new WebSocketClient(deviceEndpoint) {

      @Override
      public void onOpen(ServerHandshake handshakedata) {
        status = "Connected";
        webSocket = this;
          timerPool.schedule(pinger, HEARTBEAT_INTERVAL / 2L, TimeUnit.MILLISECONDS);
        for (CloverTransportObserver listener : observers) {
          listener.onDeviceReady(WebSocketCloverTransport.this);
        }
      }

      @Override
      public void onWebsocketPing(WebSocket conn, Framedata f) {
        super.onWebsocketPing(conn, f);
      }

      @Override
      public void onWebsocketPong(WebSocket conn, Framedata f) {
        super.onWebsocketPong(conn, f);
          disconnectFuture.cancel(true);
          timerPool.schedule(pinger, HEARTBEAT_INTERVAL / 2L, TimeUnit.MILLISECONDS);
      }

      @Override
      public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
        super.onWebsocketClosing(conn, code, reason, remote);
      }

      @Override
      public void onMessage(String message) {
        for (CloverTransportObserver observer : observers) {
          Log.d(getClass().getName(), "Got message: " + message);
          observer.onMessage(message);
        }
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
        Log.d(getClass().getName(), reason);
        status = "Disconnected";
          if (shouldNotifyClose()) {
              for (CloverTransportObserver listener : observers) {
                  listener.onDeviceDisconnected(WebSocketCloverTransport.this);
              }
          }
        webSocket = null;
        reconnect();
      }

      @Override
      public void onError(Exception ex) {
          Log.e(getClass().getName(), "onError", ex);
        for (CloverTransportObserver listener : observers) {
          //listener.onDevice
        }
      }
    };
    //reconnect();
    tempWebSocket.connect();
  }

  public void dispose() {
    shutdown = true;
    clearListeners();
    if (webSocket != null) {
        setNotifyClose(true);   // <-- need to notify because close was requested
      webSocket.close();
    }
  }


  public void reconnect() {
    if (shutdown) {
      return;
    }
//      Log.i(WebSocketCloverTransport.class.getName(), "reconnecting");
    AsyncTask task = new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        try {
          Thread.sleep(3000);
        } catch (InterruptedException ie) {
          // ignore and try to connect
        }
                    /*
                    for(ICloverConnectorListener listener : listeners){
                        listener.onDebug("Trying to connect again...");
                    }*/


        return null;
      }

      @Override
      protected void onPostExecute(Object o) {
        if (shutdown) {
          return;
        }
        try {
          //webSocket.connect();
          initialize(endpoint);

        } catch (Exception e) {
          reconnect();
        }
      }

      @Override
      protected void onPreExecute() {

      }
    };
    task.execute();
  }

    private void setNotifyClose(boolean notifyClose) {
        synchronized (this) {
            this.notifyClose = notifyClose;
        }
    }

    private boolean shouldNotifyClose() {
        synchronized (this) {
            return notifyClose;
        }
    }

    /**
     * <p>
     *     PingFramedata is the minimal {@link Framedata} necessary to elicit a response of
     *     {@link org.java_websocket.framing.Framedata.Opcode#PONG} from the other end of the
     *     web socket connection.
     * </p>
     */
    private static final class PingFramedata implements Framedata {

        private final ByteBuffer buf = ByteBuffer.allocate(0);

        @Override
        public boolean isFin() {
            return true;  // <-- data is less than 127 bytes, so this is the final message in the payload
        }

        @Override
        public boolean getTransfereMasked() {
            return true;  // <-- mask all frames being sent to server unless closing the connection
        }

        @Override
        public Opcode getOpcode() {
            return Opcode.PING;
        }

        @Override
        public ByteBuffer getPayloadData() {
            return buf; // <-- an empty buffer because this is just a heartbeat
        }

        @Override
        public void append(Framedata nextframe) throws InvalidFrameException {
            //do nothing
        }
    }
}
