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
import com.clover.remote.client.messages.remote.PairingCodeMessage;
import com.clover.remote.client.messages.remote.PairingCodeRemoteMessage;
import com.clover.remote.client.messages.remote.PairingRequest;
import com.clover.remote.client.messages.remote.PairingRequestMessage;
import com.clover.remote.client.messages.remote.PairingResponse;
import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.PairingDeviceConfiguration;
import com.clover.remote.message.Method;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.security.KeyStore;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocketCloverTransport extends CloverTransport implements CloverNVWebSocketClientListener {

  private static final String METHOD = "method";
  private static final String PAYLOAD = "payload";

  private long reconnectDelay; // delay before attempting reconnect
  private final Runnable reconnectThread = new Runnable() {
    @Override
    public void run() {
      if (!shutdown) {
        try {
          initializeConnection();
        } catch (Exception e) {
          reconnect();
        }
      }
    }
  };
  private long pingFrequency; // period between pings in seconds
  private long pongTimeout; // how long to wait for a pong before closing connection
  // but still wait
  private long reportConnectionProblemAfter; // if pong hasn't come back in this time, report as disconnected
  // client, before it is actually disconnected so
  // if the pong is received before disconnect timeout, a deviceReady
  // needs to be sent

  private final Gson GSON = new Gson();

  private final URI endpoint;
  private final PairingDeviceConfiguration pairingDeviceConfiguration;
  private final KeyStore trustStore;
  private final String posName;
  private final String serialNumber;
  private String authToken;
  private boolean reportedDisconnect = false; // keeps track if a deviceDisconnected message has been sent to the
  private Timer pingTimer;
  private TimerTask pingTimerTask;
  private TimerTask reportDisconnectTimerTask;
  private TimerTask disconnectTimerTask;

  private CloverNVWebSocketClient webSocket;

  // NOTE:  We are using this library to synchronize the websocket and the timer tasks to eliminate lock ordering issues
  // Synchronization on the tasks must be done to prevent a race condition where a thread can cancel a newly created
  // task PRIOR to actually scheduling it.
  private final Object webSocketLock = new Object();

  /**
   * prevent reconnects if shutdown was requested
   */
  private boolean shutdown = false;

  private boolean isPairing = true;

  /**
   * A single thread/queue to process reconnect requests
   */
  private final ScheduledThreadPoolExecutor reconnectPool = new ScheduledThreadPoolExecutor(1);

  public WebSocketCloverTransport(URI endpoint, PairingDeviceConfiguration pairingConfig, KeyStore trustStore, String posName, String serialNumber, String authToken,
                                  long pongTimeout, long pingFrequency, long reconnectDelay, long reportConnectionProblemAfter) {
    if (endpoint == null) {
      throw new IllegalArgumentException("Endpoint cannot be null!");
    }
    this.endpoint = endpoint;
    this.pairingDeviceConfiguration = pairingConfig;
    this.trustStore = trustStore;
    this.posName = posName;
    this.serialNumber = serialNumber;
    this.authToken = authToken;

    this.pongTimeout = pongTimeout;
    this.pingFrequency = pingFrequency;
    this.reconnectDelay = reconnectDelay;
    this.reportConnectionProblemAfter = reportConnectionProblemAfter;

    this.pingTimer = new Timer("Remote-Pay Ping Timer");
  }

  /**
   * Sends the provided encoded message.  If a connection does not exist or an error occurs during transmission,
   * the message is NOT resent and a negative value is returned.
   *
   * @param message encoded message to send
   * @return 0 if the message was sent successfully, -1 if the send fails
   */
  @Override
  public int sendMessage(final String message) {
    // let's see if we have connectivity
    synchronized (webSocketLock) {
      if (webSocket != null && webSocket.isOpen()) {
        try {
          webSocket.send(message);
          return 0;
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }
    reconnect();
    return -1;
  }

  private void clearWebsocket() {
    synchronized (webSocketLock) {
      if (webSocket != null) {
        webSocket.clearListener();
        webSocket = null;
      }
    }
  }

  @Override
  public void initializeConnection() {
    synchronized (webSocketLock) {
      if (webSocket != null) {
        if (webSocket.isOpen() || webSocket.isConnecting()) {
          return;
        }
        clearWebsocket();
      }
      webSocket = new CloverNVWebSocketClient(endpoint, this, trustStore);
    }

    // This connect call is outside the synchronized block intentionally because this is a blocking call
    // Potential race condition is handled by try/catch
    try {
      webSocket.connect();
      Log.d(getClass().getSimpleName(), "connection attempt done.");
    } catch (Exception ex) {
      ex.printStackTrace();
      reconnect();
    }
  }

  @Override
  public void dispose() {
    super.dispose();
    shutdown = true;
    close();
  }

  private void close() {
    boolean notify = true;
    synchronized (webSocketLock) {
      if (webSocket != null) {
        if (!webSocket.isClosing()) {
          try {
            webSocket.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        clearWebsocket();
      } else {
        notify = false;
      }

      // Timer task cancel occurs in synchronization block to prevent canceling a newly created task prior to schedul
      if (disconnectTimerTask != null) {
        disconnectTimerTask.cancel();
      }
      if (reportDisconnectTimerTask != null) {
        reportDisconnectTimerTask.cancel();
      }
    }

    if (notify) {
      notifyDeviceDisconnected();
    }
  }

  private void reconnect() {
    if (shutdown) {
      Log.d(getClass().getSimpleName(), "Not attempting to reconnect, shutdown...");
      return;
    }
    reconnectPool.schedule(reconnectThread, reconnectDelay, TimeUnit.MILLISECONDS);
  }

  @Override
  public void connectionError(CloverNVWebSocketClient ws) {
    Log.d(getClass().getSimpleName(), "Connection Error...");
    if (webSocket == ws) {
      notifyDeviceDisconnected();
    }
    reconnect();
  }

  @Override
  public void onNotResponding(CloverNVWebSocketClient ws) {
    Log.d(getClass().getSimpleName(), "Not Responding...");
    if (webSocket == ws) {
      notifyDeviceDisconnected();
    }
  }

  @Override
  public void onPingResponding(CloverNVWebSocketClient ws) {
    Log.d(getClass().getSimpleName(), "Ping Responding");
    if (webSocket == ws) {
      notifyDeviceReady();
    }
  }

  @Override
  public void onOpen(CloverNVWebSocketClient ws) {
    Log.d(getClass().getSimpleName(), "Open...");
    if (webSocket == ws) {
      // notify connected
      notifyDeviceConnected();
      schedulePing();
      sendPairRequest();
    }
  }

  private void sendPairRequest() {
    isPairing = true;
    PairingRequest pr = new PairingRequest(posName, serialNumber, authToken);
    PairingRequestMessage prm = new PairingRequestMessage(pr);
    String message = new Gson().toJson(prm);

    synchronized (webSocketLock) {
      webSocket.send(message);
    }
  }

  @Override
  public void onClose(CloverNVWebSocketClient ws, int code, String reason, boolean remote) {
    Log.d(getClass().getSimpleName(), "onClose: " + reason + ", remote? " + remote);

    if (webSocket == ws) {
      close();
      if (!shutdown) {
        reconnect();
      }
    }
  }

  private void resetPong() {
    synchronized (webSocketLock) {
      // Timer task cancel occurs in synchronization block to prevent canceling a newly created task prior to scheduling
      if (disconnectTimerTask != null) {
        disconnectTimerTask.cancel(); //Subsequent calls have no effect.
      }
      if (reportDisconnectTimerTask != null) {
        reportDisconnectTimerTask.cancel();//Subsequent calls have no effect.
      }
    }

    if (reportedDisconnect) {
      AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void[] params) {
          notifyDeviceReady();
          return null;
        }
      };
      task.execute();
    }
    reportedDisconnect = false;
    schedulePing();
  }

  private void schedulePing() {
    reportedDisconnect = false;
    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void[] params) {
        synchronized (webSocketLock) {
          // Timer task creation/cancel occurs in synchronization block to prevent canceling a newly created task prior to scheduling
          if (pingTimerTask != null) {
            pingTimerTask.cancel();
          }
          pingTimerTask = new TimerTask() {
            @Override
            public void run() {
              sendPing();
            }
          };
          pingTimer.schedule(pingTimerTask, pingFrequency);
        }
        return null;
      }
    };
    task.execute();
  }

  private void sendPing() {
    synchronized (webSocketLock) {
      if (webSocket != null) {
        webSocket.sendPing();
        scheduleDisconnect();
      }
    }
  }

  private void scheduleDisconnect() {
    if (reportConnectionProblemAfter < pongTimeout) {
      AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void[] params) {
          synchronized (webSocketLock) {
            // Timer task creation/cancel occurs in synchronization block to prevent canceling a newly created task prior to scheduling
            if (reportDisconnectTimerTask != null) {
              reportDisconnectTimerTask.cancel();
            }
            reportDisconnectTimerTask = new TimerTask() {
              @Override
              public void run() {
                reportDisconnect();
              }
            };
            pingTimer.schedule(reportDisconnectTimerTask, reportConnectionProblemAfter);
          }
          return null;
        }
      };
      task.execute();
    }
    final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void[] params) {
        synchronized (webSocketLock) {
          // Timer task creation/cancel occurs in synchronization block to prevent canceling a newly created task prior to scheduling
          if (disconnectTimerTask != null) {
            disconnectTimerTask.cancel();
          }
          disconnectTimerTask = new TimerTask() {
            @Override
            public void run() {
              disconnectMissedPong();
            }
          };
          pingTimer.schedule(disconnectTimerTask, pongTimeout);
        }
        return null;
      }
    };
    task.execute();
  }

  private void reportDisconnect() {
    reportedDisconnect = true;
    AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void[] params) {
        Log.w(getClass().getSimpleName(), "Notifying of disconnect");
        // This is equivalent to !ready
        notifyDeviceConnected();
        return null;
      }
    };
    task.execute();
  }

  private void disconnectMissedPong() {
    boolean dispose = false;
    synchronized (webSocketLock) {
      if (webSocket != null) {
        Log.w(getClass().getSimpleName(), "forcing disconnect");
        webSocket.disconnect();
      } else {
        dispose = true;
      }
    }

    if (dispose) {
      dispose();
    }
  }

  @Override
  public void onMessage(CloverNVWebSocketClient ws, String message) {
    if (webSocket == ws) {
      resetPong();
      if(isPairing) {
        JsonObject obj = GSON.fromJson(message, JsonObject.class);
        String method = obj.get(METHOD).getAsString();
        String payload = obj.get(PAYLOAD).getAsString();
        if (PairingCodeMessage.PAIRING_CODE.equals(method)) {
          Log.d(getClass().getName(), "Got PAIRING_CODE");
          PairingCodeRemoteMessage pcm = GSON.fromJson(payload, PairingCodeRemoteMessage.class);
          String pairingCode = pcm.getPairingCode();
          pairingDeviceConfiguration.onPairingCode(pairingCode);
        } else if (PairingCodeMessage.PAIRING_RESPONSE.equals(method)) {
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

          } else if (PairingCodeMessage.FAILED.equals(method)) {
            Log.d(getClass().getName(), "Got FAILED pair response");
            isPairing = true;
            sendPairRequest();
          }
        } else if (!Method.ACK.name().equals(method) || !Method.UI_STATE.name().equals(method)) {
          Log.w(getClass().getName(), "Unexpected method: '" + method + "' while in pairing mode.");
        }
      } else {
        Log.d(getClass().getName(), "Got message: " + message);
        onMessage(message);
      }
    }
  }

  @Override
  public void onPong(CloverNVWebSocketClient ws) {
    resetPong();
  }

  @Override
  public void onSendError(String payloadText) {
    // TODO
  }
}