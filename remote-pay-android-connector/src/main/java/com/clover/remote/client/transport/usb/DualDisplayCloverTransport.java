package com.clover.remote.client.transport.usb;

import android.annotation.TargetApi;
import android.content.ContentProviderClient;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import com.clover.remote.client.transport.CloverTransport;

import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DualDisplayCloverTransport extends CloverTransport {

  private static final String RESET_METHOD = "reset";

  private static final String POS_SEND_METHOD = "posSendString";
  private static final String POS_RECEIVE_METHOD = "posReceiveString";

  private static final String TERMINAL_SEND_METHOD = "terminalSendString";
  private static final String TERMINAL_RECEIVE_METHOD = "terminalReceiveString";

  private static final String EXTRA_SUCCESS = "result";
  private static final String EXTRA_INTERRUPTED = "interrupted";

  private static final long SEND_TIMEOUT = 3000;

  private static final String EXTRA_PAYLOAD = "com.clover.remote.terminal.remotecontrol.extra.EXTRA_PAYLOAD";

  private static final String TAG = DualDisplayCloverTransport.class.getSimpleName();

  final Context context;
  Executor executor = Executors.newSingleThreadExecutor();



  private boolean running = true;
  private ContentProviderClient contentProviderClient;


  public DualDisplayCloverTransport(Context context) {
    this.context = context;
  }

  @Override public void initializeConnection() {
    running = true;
    contentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient("com.clover.remote.conduit");

    try {
      contentProviderClient.call(RESET_METHOD, null, null);

      Runnable readRunnable = new Runnable() {
        @Override public void run() {
          while(running) {
            try {
              readNextMessage();
            } catch (Exception e) {
              Log.e(TAG, "Error reading message", e);
              return;
            }
          }
        }

        private void readNextMessage() throws RemoteException {
            String payload = contentProviderClient.call(POS_RECEIVE_METHOD, null, null).getString(EXTRA_PAYLOAD);
            if (payload != null) {
              onMessage(payload);
            } else {
              Log.w(TAG, "Got null message");
            }
        }
      };
      notifyDeviceConnected();
      executor.execute(readRunnable);
      notifyDeviceReady();

    } catch (Exception e) {
      contentProviderClient = null;
      Log.e(TAG, "Failed to reset", e);

    }

  }


  @Override public void dispose() {
    super.dispose();
    running = false;
    try {
      contentProviderClient.call(RESET_METHOD, null, null);
    } catch (Exception e) {
      Log.w("Error calling reset...", e);
    }
    closeContentClient();
    contentProviderClient = null;
  }

  @TargetApi(24)
  public void closeContentClient() {
    if (contentProviderClient != null) {
      contentProviderClient.close();
      notifyDeviceDisconnected();
    } else {
      //
    }
  }

  @Override public int sendMessage(String message) throws NotYetConnectedException {
    if (contentProviderClient != null) {
      try {
        contentProviderClient.call(POS_SEND_METHOD, message, null);
      } catch (Exception e) {
        notifyDeviceDisconnected(); // TODO: ?
        Log.e(TAG, "Failed to send message", e);
      }
    }
    return 0;
  }

}
