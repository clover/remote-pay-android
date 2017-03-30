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

import android.os.AsyncTask;
import android.util.Log;
import com.clover.remote.client.messages.PairingCodeMessage;
import com.clover.remote.client.messages.remote.PairingCodeRemoteMessage;
import com.clover.remote.client.messages.remote.PairingRequest;
import com.clover.remote.client.messages.remote.PairingRequestMessage;
import com.clover.remote.client.messages.remote.PairingResponse;
import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.CloverTransportObserver;
import com.clover.remote.client.transport.PairingDeviceConfiguration;
import com.clover.remote.message.Method;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.security.KeyStore;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocketCloverTransport extends CloverTransport implements CloverNVWebSocketClientListener {

  private final Gson GSON = new Gson();
  private final String posName;
  private final String serialNumber;
  private String authToken;
  /*
    These hold the configurable options
     */
  private int maxPingRetriesBeforeDisconnect = 4;
  private long heartbeatInterval = 2000L;
  private long reconnectDelay = 3000L;
  URI endpoint;

  PairingDeviceConfiguration pairingDeviceConfiguration;

  CloverNVWebSocketClient webSocket;

  String status = "Disconnected";
  /**
   * prevent reconnects if shutdown was requested
   */
  boolean shutdown = false;

  KeyStore trustStore;

  boolean isPairing = true;

  /**
   * A single thread/queue to process reconnect requests
   */
  ScheduledThreadPoolExecutor reconnectPool = new ScheduledThreadPoolExecutor(1);


  private final Runnable reconnector = new Runnable() {
    @Override
    public void run() {
      if (!shutdown) {
        try {
          initialize(endpoint);
        } catch (Exception e) {
          reconnect();
        }
      }
    }
  };
  public static final String METHOD = "method";
  public static final String PAYLOAD = "payload";

  public WebSocketCloverTransport(URI endpoint, long heartbeatInterval, long reconnectDelay, int retriesUntilDisconnect, KeyStore trustStore, String posName, String serialNumber, String authToken) {

    this.endpoint = endpoint;
    this.heartbeatInterval = Math.max(10, heartbeatInterval);
    this.reconnectDelay = Math.max(0, reconnectDelay);
    this.maxPingRetriesBeforeDisconnect = Math.max(0, retriesUntilDisconnect);
    this.trustStore = trustStore;
    this.posName = posName;
    this.serialNumber = serialNumber;
    this.authToken = authToken;
    initialize(endpoint);
  }

  @Override
  public int sendMessage(final String message) {
    // let's see if we have connectivity

    if(webSocket != null && webSocket.isOpen()) {
      try {
        webSocket.send(message);
      } catch(Exception e){
        reconnect();;
      }
      return 0;
    } else {
      reconnect();
    }
    return -1;
  }

  private synchronized void clearWebsocket() {
    if (webSocket != null) {
      webSocket.clearListener();
    }
    webSocket = null;
  }

  private synchronized void initialize(URI deviceEndpoint) {

    if (webSocket != null) {
      if (webSocket.isOpen() || webSocket.isConnecting()) {
        return;
      /*} else if (webSocket.getReadyState() == WebSocket.READYSTATE.NOT_YET_CONNECTED) {
        webSocket.connect();
        return;*/
      } else {
        clearWebsocket();
      }
    }

    webSocket = new CloverNVWebSocketClient(deviceEndpoint, this, 5000, trustStore);

    webSocket.connect();
    Log.d(getClass().getSimpleName(), "connection attempt done.");
  }

  public void dispose() {
    shutdown = true;
    if (webSocket != null) {
      notifyDeviceDisconnected();
      try {
        webSocket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    clearWebsocket();

  }


  public void reconnect() {
    if (shutdown) {
      Log.d(getClass().getSimpleName(), "Not attempting to reconnect, shutdown...");
      return;
    }
    reconnectPool.schedule(reconnector, reconnectDelay, TimeUnit.MILLISECONDS);
  }


  @Override public void connectionError(CloverNVWebSocketClient ws) {
    Log.d(getClass().getSimpleName(), "Not Responding...");
    if (webSocket == ws) {
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "onNotResponding");
        observer.onDeviceDisconnected(this);
      }
    }
    reconnect();
  }

  public void onNotResponding(CloverNVWebSocketClient ws) {
    Log.d(getClass().getSimpleName(), "Not Responding...");
    if (webSocket == ws) {
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "onNotResponding");
        observer.onDeviceDisconnected(this);
      }
    }
  }


  public void onPingResponding(CloverNVWebSocketClient ws) {
    Log.d(getClass().getSimpleName(), "Ping Responding");
    if (webSocket == ws) {
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "onPingResponding");
        observer.onDeviceReady(this);
      }
    }
  }



  @Override
  public void onOpen(CloverNVWebSocketClient ws) {

    Log.d(getClass().getSimpleName(), "Open...");
    if (webSocket == ws) {
      // notify connected
      notifyDeviceConnected();
      sendPairRequest();
    }
  }

  private void sendPairRequest() {
    isPairing = true;
    PairingRequest pr = new PairingRequest(posName, serialNumber, authToken);
    PairingRequestMessage prm = new PairingRequestMessage(pr);

    webSocket.send(new Gson().toJson(prm));
  }


  @Override
  public void onClose(CloverNVWebSocketClient ws, int code, String reason, boolean remote) {
    Log.d(getClass().getSimpleName(), "onClose: " + reason + ", remote? " + remote);

    if (webSocket == ws) {
      if(!webSocket.isClosing()) {
        webSocket.clearListener();
        webSocket.close();
      }
      clearWebsocket();
      for (CloverTransportObserver observer : observers) {
        Log.d(getClass().getName(), "onClose");
        observer.onDeviceDisconnected(this);
      }
      if(!shutdown) {
        reconnect();
      }
    }
  }


  @Override
  public void onMessage(CloverNVWebSocketClient ws, String message) {
    if (webSocket == ws) {
      if(isPairing) {
        JsonObject obj = GSON.fromJson(message, JsonObject.class);
        JsonElement method = obj.get(METHOD);
        String payload = obj.get(PAYLOAD).getAsString();
        if (PairingCodeMessage.PAIRING_CODE.equals(method.getAsString())) {
          Log.d(getClass().getName(), "Got PAIRING_CODE");
          PairingCodeRemoteMessage pcm = GSON.fromJson(payload, PairingCodeRemoteMessage.class);
          String pairingCode = pcm.getPairingCode();
          pairingDeviceConfiguration.onPairingCode(pairingCode);
        } else if (PairingCodeMessage.PAIRING_RESPONSE.equals(method.getAsString())) {
          Log.d(getClass().getName(), "Got PAIRING_RESPONSE");
          PairingResponse response = GSON.fromJson(payload, PairingResponse.class);
          if (PairingCodeMessage.PAIRED.equals(response.pairingState) || PairingCodeMessage.INITIAL.equals(response.pairingState)) {
            Log.d(getClass().getName(), "Got PAIRED pair response");
            isPairing = false;
            authToken = response.authenticationToken;

            new AsyncTask<Void, Void, Void>() {
              @Override protected Void doInBackground(Void... params) {
                try {
                  pairingDeviceConfiguration.onPairingSuccess(authToken);
                } catch (Exception e) {
                  Log.e(pairingDeviceConfiguration.getClass().getSimpleName(), "Error", e);
                } finally {
                  notifyDeviceReady();
                }
                return null;
              }
            }.execute();

          } else if (PairingCodeMessage.FAILED.equals(method.getAsString())) {
            Log.d(getClass().getName(), "Got FAILED pair response");
            isPairing = true;
            sendPairRequest();
          }
        } else if (!Method.ACK.name().equals(method) || !Method.UI_STATE.name().equals(method)) {
          Log.w(getClass().getName(), "Unexpected method: '" + method + "' while in pairing mode.");
        }
      } else {
        for (CloverTransportObserver observer : observers) {
          Log.d(getClass().getName(), "Got message: " + message);
          observer.onMessage(message);
        }
      }
    }
  }

  @Override public void onSendError(String payloadText) {
    // TODO:
    /*for (CloverTransportObserver observer : observers) {
      CloverDeviceErrorEvent errorEvent = new CloverDeviceErrorEvent();
    }*/
  }

  public void setPairingDeviceConfiguration(PairingDeviceConfiguration pairingDeviceConfiguration) {
    this.pairingDeviceConfiguration = pairingDeviceConfiguration;
  }
}