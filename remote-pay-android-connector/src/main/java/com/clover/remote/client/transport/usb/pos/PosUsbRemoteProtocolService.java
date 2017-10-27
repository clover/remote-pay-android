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

package com.clover.remote.client.transport.usb.pos;

import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.usb.USBCloverTransportService;
import com.clover.remote.client.transport.usb.UsbCloverManager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * This service runs on the Clover POS device and connects to a Clover Terminal via USB. It stays up and running
 * as long as possible so that it can deliver terminal status changed events to bound clients.
 */
public class PosUsbRemoteProtocolService extends PosRemoteProtocolService implements USBCloverTransportService {

  private static final String TAG = PosUsbRemoteProtocolService.class.getSimpleName();

  public static final String ACTION_USB_SETUP = "com.clover.remote.usb.intent.ACTION_USB_SETUP";
  public static final String ACTION_USB_CONNECT = "com.clover.remote.usb.intent.ACTION_USB_CONNECT";
  public static final String ACTION_USB_DISCONNECT = "com.clover.remote.usb.intent.ACTION_USB_DISCONNECT";

  public static final String ACTION_USB_REQUEST_MESSAGE = "com.clover.remote.usb.intent.CLOVER_USB_TRANSPORT_REQUEST";// this just asks for the usb device status. Asks to resend status if it is listening
  public static final String ACTION_USB_SEND_MESSAGE = "com.clover.remote.usb.intent.CLOVER_USB_TRANSPORT_SEND";
  public static final String ACTION_USB_RECEIVE_MESSAGE = "com.clover.remote.usb.intent.CLOVER_USB_TRANSPORT_RECEIVE";
  public static final String EXTRA_MESSAGE = "com.clover.remote.Message";

  private final Handler mBgHandler;
  private final HandlerThread mBgHandlerThread;

  RemoteTerminalStatus currentStatus = RemoteTerminalStatus.TERMINAL_DISCONNECTED;

  BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(ACTION_USB_SEND_MESSAGE)) {
        String msg = intent.getStringExtra(EXTRA_MESSAGE);
        Log.d(getClass().getSimpleName(), "Sending: " + msg);
        sendMessage(msg);
      } else if (intent.getAction().equals(ACTION_USB_REQUEST_MESSAGE)) {
        broadcastStatus();
      }
    }
  };

  private SendQueue sendQueue = new SendQueue();
  private ReadQueue readQueue = new ReadQueue();

  {
    mBgHandlerThread = new HandlerThread(TAG + "-Bg-Thread");
    mBgHandlerThread.start();
    mBgHandler = new Handler(mBgHandlerThread.getLooper());
  }

  private IntentFilter getIntentFilter() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(ACTION_USB_SEND_MESSAGE);
    intentFilter.addAction(ACTION_USB_REQUEST_MESSAGE);
    return intentFilter;
  }

  private RemoteUsbManager mRemoteUsbManager;

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    Log.d(getClass().getSimpleName(), "onDestroy Clover USB Service.");

    mBgHandler.removeCallbacks(mSetupUsbRunnable);
    mBgHandler.removeCallbacks(mConnectUsbRunnable);
    mBgHandler.removeCallbacks(mDisconnectUsbRunnable);

    mBgHandler.post(new Runnable() {
      @Override
      public void run() {
        disconnectUsb();
        mBgHandlerThread.quit();
      }
    });

    if(sendQueue != null) {
      sendQueue.stop();
    }
    if(readQueue != null) {
      readQueue.stop();
    }

    super.onDestroy();

    try {
      getContext().unregisterReceiver(broadcastReceiver);
    } catch (IllegalArgumentException iae) {
      // not registered here?
    }
  }

  public class PosUsbClientServiceBinder extends ServiceBinder<PosUsbRemoteProtocolService> {
    @Override
    public PosUsbRemoteProtocolService getService() {
      return PosUsbRemoteProtocolService.this;
    }
  }

  private final IBinder mBinder = new PosUsbClientServiceBinder();

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);
    Log.d(TAG, String.format("onStartCommand, intent: %s, flags: %d, startId: %d", intent, flags, startId));

    if (intent != null) {
      final String action = intent.getAction();

      if (ACTION_USB_SETUP.equals(action)) {
        mBgHandler.post(mSetupUsbRunnable);
      } else if (ACTION_USB_CONNECT.equals(action) || action == null) {
        mBgHandler.post(mConnectUsbRunnable);
      } else if (ACTION_USB_DISCONNECT.equals(action)) {
        mBgHandler.removeCallbacks(mSetupUsbRunnable);
        mBgHandler.removeCallbacks(mConnectUsbRunnable);
        mBgHandler.removeCallbacks(mDisconnectUsbRunnable);
        mBgHandler.post(mDisconnectUsbRunnable);
      }
    } else {
      // We need to re-establish connectivity.  A null intent signifies that the service was killed by the OS
      // and was automatically restarted when system resources were available.  This call performs the same
      // reconnect logic that is invoked when you start/run UsbPayDisplay activity manually and assumes the
      // payment device is still connected.  Otherwise, we would have received a disconnect call from the USB
      // device service and would have terminated normally.
      mBgHandler.post(mConnectUsbRunnable);
      Log.d(TAG, "onStartCommand, The intent was null. Calling mConnectUsbRunnable to re-initiate the usb connection"
                   + " after system kill/restart of service.");
    }
    return START_STICKY; // if the service is killed by the OS, it will restart automatically
  }

  private final Runnable mSetupUsbRunnable = new Runnable() {
    @Override
    public void run() {
      if (setupUsb()) {
        // need to check the connect...
      }
    }
  };

  private final Runnable mConnectUsbRunnable = new Runnable() {
    @Override
    public void run() {
      connectUsb();
    }
  };

  private final Runnable mDisconnectUsbRunnable = new Runnable() {
    @Override
    public void run() {
      disconnectUsb();
    }
  };

  private synchronized boolean setupUsb() {
    if (mRemoteUsbManager != null && mRemoteUsbManager.isConnected()) {
      return false;
    }

    Log.d(TAG, "setupUsb");

    try {
      UsbAccessorySetupUsbManager setupUsbManager = new UsbAccessorySetupUsbManager(getContext());
      setupUsbManager.startAccessoryMode();

      currentStatus = RemoteTerminalStatus.TERMINAL_CONNECTED_NOT_READY;
      getContext().sendBroadcast(new Intent(CloverTransport.DEVICE_CONNECTED));

      return true;
    } catch (Exception e) {
      Log.w(TAG, "Terminal setup failed", e);
    }

    return false;
  }

  private synchronized void connectUsb() {
    if (mRemoteUsbManager != null && mRemoteUsbManager.isConnected()) {
      Log.d(TAG, "Already have a connection, just return.");
      return; // ready!
    }

    Log.d(TAG, "connectUsb");

    mRemoteUsbManager = new RemoteUsbManager(getContext());

    try {
      mRemoteUsbManager.open();

      getContext().registerReceiver(broadcastReceiver, getIntentFilter());

      // Give the terminal time to prepare itself to receive messages after the connection is open
      SystemClock.sleep(1000);

      // setup the processing queues for usb...
      sendQueue.start();
      readQueue.start();

      currentStatus = RemoteTerminalStatus.TERMINAL_CONNECTED_READY;

      Log.d(TAG, "send ready message");
      getContext().sendBroadcast(new Intent(CloverTransport.DEVICE_READY));

    } catch (Exception e) {
      boolean quiet = e instanceof UsbCloverManager.UsbDeviceNotFoundException;
      if (quiet) {
        Log.d(TAG, "USB connect failed, this is expected when the device is not attached");
      } else {
        Log.w(TAG, "USB connect failed", e);
      }

      disconnectUsb();
    }
  }

  private synchronized void disconnectUsb() {
    if(sendQueue != null) {
      sendQueue.stop();
    }
    if(readQueue != null) {
      readQueue.stop();
    }
    if (mRemoteUsbManager == null) {
      return;
    }

    Log.d(TAG, "disconnectUsb");

    if (mRemoteUsbManager != null) {
      mRemoteUsbManager.disconnect();
      mRemoteUsbManager = null;
    }

    currentStatus = RemoteTerminalStatus.TERMINAL_DISCONNECTED;
    Log.d(TAG, "send disconnect message");
    getContext().sendBroadcast(new Intent(CloverTransport.DEVICE_DISCONNECTED));

    stopSelf();
  }


  @Override
  public void onConduitConnected() {
    super.onConduitConnected();
    getContext().sendBroadcast(new Intent(CloverTransport.DEVICE_CONNECTED));
  }

  @Override
  public void onConduitDisconnected() {
    super.onConduitDisconnected();
    disconnectUsb();
  }

  @Override
  protected boolean isOrderModificationSupported() {
    return true;
  }

  private Context getContext() {
    return this;
  }


  public void sendMessage(String remoteMessageJSON) {
    sendQueue.send(remoteMessageJSON);
  }

  private void broadcastStatus() {
    String msg = CloverTransport.DEVICE_DISCONNECTED;
    switch(currentStatus) {
      case TERMINAL_DISCONNECTED: {
        msg = CloverTransport.DEVICE_DISCONNECTED;
        break;
      }
      case TERMINAL_CONNECTED_NOT_READY: {
        msg = CloverTransport.DEVICE_CONNECTED;
        break;
      }
      case TERMINAL_CONNECTED_READY: {
        msg = CloverTransport.DEVICE_READY;
        break;
      }
    }
    getContext().sendBroadcast(new Intent(msg));
  }

  private class SendQueue {
    ExecutorService svc;

    public synchronized void send(final String msg) {
      if (svc == null) {
        Log.e(TAG, "USB Device isn't ready, as the send queue hasn't been started.");
        return;
      }
      svc.submit(new Runnable() {
        @Override
        public void run() {
          try {
            if (mRemoteUsbManager != null) {
              mRemoteUsbManager.sendString(msg);
            }
          } catch (IOException | InterruptedException e) {
            // Do nothing
          }
        }
      });
    }

    public synchronized void start() {
      if (svc == null || svc.isShutdown()) {
        svc = Executors.newSingleThreadExecutor();
      }
    }

    public void stop() {
      ExecutorService temp = svc;
      svc = null;
      if(temp != null) {
        temp.shutdown();
      }
    }
  }

  private class ReadQueue {
    ExecutorService svc;
    boolean shutdown = false;

    public void start() {
      shutdown = false;
      svc = Executors.newSingleThreadExecutor();
      svc.execute(new Runnable() {
        @Override
        public void run() {
          do {
            try {
              if (mRemoteUsbManager != null) {
                String message = mRemoteUsbManager.receiveString();
                Log.d(TAG, String.format("Got message from device: %s", message));
                Intent intent = new Intent(ACTION_USB_RECEIVE_MESSAGE);
                intent.putExtra(EXTRA_MESSAGE, message);
                getContext().sendBroadcast(intent);
              }
            } catch (IOException | InterruptedException ie) {
              //
            }
          } while (!shutdown);
        }
      });
    }

    public void stop() {
      shutdown = true;
      svc = null;
    }
  }
}


