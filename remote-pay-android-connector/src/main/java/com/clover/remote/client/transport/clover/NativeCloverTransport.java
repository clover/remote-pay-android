package com.clover.remote.client.transport.clover;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.clover.remote.client.transport.CloverTransport;

import java.nio.channels.NotYetConnectedException;

public class NativeCloverTransport extends CloverTransport {

  BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {
      String payload = intent.getStringExtra("com.clover.remote.EXTRA_PAYLOAD");
      onMessage(payload);
    }
  };

  Context context = null;

  public NativeCloverTransport(Context context) {
    this.context = context;
  }

  @Override public void initializeConnection() {
    context.registerReceiver(receiver, getIntentFilter());//, "com.clover.remote.terminal", null);
    notifyDeviceConnected();
    notifyDeviceReady();
  }

  @Override public void dispose() {
    try {
      context.unregisterReceiver(receiver);
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), "Error unregistering receiver", e);
    }
  }

  private IntentFilter getIntentFilter() {
    IntentFilter filter = new IntentFilter("com.clover.remote.SEND_TO_POS");
    return filter;
  }

  @Override public int sendMessage(String message) throws NotYetConnectedException {
    if (context == null) {
      throw new NotYetConnectedException();
    }
    Intent intent = new Intent("com.clover.remote.SEND_TO_TERMINAL");
    intent.putExtra("com.clover.remote.EXTRA_PAYLOAD", message);

    context.sendBroadcast(intent);//, "com.clover.remote.terminal");
    return 0;
  }
}
