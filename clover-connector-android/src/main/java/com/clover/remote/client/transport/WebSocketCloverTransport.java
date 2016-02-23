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
import java.util.Iterator;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocketCloverTransport extends CloverTransport {

    /**
     * <p>
     *     PingFramedata is the minimal {@link Framedata} necessary to elicit a response of
     *     {@link org.java_websocket.framing.Framedata.Opcode#PONG} from the other end of the
     *     web socket connection.
     * </p>
     */
    private static final Framedata PING = new Framedata() {
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
    };
    private static final long HEARTBEAT_INTERVAL = 5000L;
    private static final int MAX_RETRIES = 4;

    /**
     * <p>
     *     Holds the tasks to perform disconnection should be canceled in the case of a timely
     *     pong message. Once a future is canceled, it should be removed from the list.
     * </p>
     */
    private CopyOnWriteArrayList<ScheduledFuture<?>> disconnectFutures = new CopyOnWriteArrayList<>();

    /**
     * <p>
     *     tracks when there has been a ping timeout. This allows the onPongMessage callback to
     *     notify ready whenever a late pong message arrives.
     * </p>
     */
    private volatile boolean pingTimeoutState = false;

    private final Runnable pinger = new Runnable() {

        @Override
        public void run() {
            if (webSocket == null) {
                return;
            }
            Log.i(WebSocketCloverTransport.class.getName(), "pinging");
            webSocket.getConnection().sendFrame(PING);
            disconnectFutures.add(timerPool.schedule(new DisconnectHandler(), HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS));
            Log.i(WebSocketCloverTransport.class.getName(), "Adding new DisconnectHandler; disconnectFutures.size() = " + disconnectFutures.size());
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
          setPingTimeoutState(false);
          timerPool.schedule(pinger, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
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
          cancelAllDisconnectHandlers();
          if (isPingTimeoutState()) {
              setPingTimeoutState(false);
              for (CloverTransportObserver observer : observers) {
                  observer.onDeviceReady(WebSocketCloverTransport.this);
              }
          }
          timerPool.schedule(pinger, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
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
          for (CloverTransportObserver listener : observers) {
              listener.onDeviceDisconnected(WebSocketCloverTransport.this);
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

    private void cancelAllDisconnectHandlers() {
        Log.i(WebSocketCloverTransport.class.getName(), "Canceling all disconnectFutures; disconnectFutures.size() = " + disconnectFutures.size());
        Iterator<ScheduledFuture<?>> it = disconnectFutures.iterator();
        while (it.hasNext()) {
            ScheduledFuture<?> disconnectFuture = it.next();
            disconnectFuture.cancel(true);
            disconnectFutures.remove(disconnectFuture); // <-- once canceled, there is no reason to keep the future
        }
    }

    public void dispose() {
    shutdown = true;
        for (CloverTransportObserver observer : observers) {
            observer.onDeviceDisconnecting(this);
        }
        // TODO: investigate why the listeners are cleared here
    clearListeners();
    if (webSocket != null) {
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

    private final class DisconnectHandler implements Runnable {

        private int retryCount = MAX_RETRIES;

        @Override
        public void run() {
            if (webSocket == null) {
                return;
            }

            // notify observers only on the first time through this disconnection handler's run method
            if (observers != null && retryCount == MAX_RETRIES) {
                setPingTimeoutState(true);
                for (CloverTransportObserver observer : observers) {
                    // sending the connected state should signify that the device is not ready
                    observer.onDeviceDisconnecting(WebSocketCloverTransport.this);
                }
            }

            if (retryCount > 0) {   // <-- retry mechanism allows for a tolerance before closing the connection
                Log.i(WebSocketCloverTransport.class.getName(), "disconnect handler retrying: retryCount = " + retryCount);
                retryCount--;
                disconnectFutures.add(timerPool.schedule(this, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS));
                return;
            }

            try {
                Log.i(WebSocketCloverTransport.class.getName(), "disconnect handler retryCount = " + retryCount + "; closing");
                webSocket.closeBlocking();  // <-- synchronous close keeps from opening multiple connections to the same device
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
}
