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
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Timer;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class WebSocketCloverTransport extends CloverTransport {
  Gson gson = new Gson();
  WebSocketClient webSocket;
  String status = "Disconnected";
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
        //Toast.makeText(CloverConnector.this, "Socket Open", Toast.LENGTH_SHORT).show();
        status = "Connected";
        webSocket = this;
        // TODO: add PING/PONG timer thread...
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
      }

      @Override
      public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
        super.onWebsocketClosing(conn, code, reason, remote);
        pingTimer.cancel();
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
        //Toast.makeText(CloverConnector.this, "Socket Closed", Toast.LENGTH_SHORT).show();
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
      webSocket.close();
    }
  }


  public void reconnect() {
    if (shutdown) {
      return;
    }
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
}
