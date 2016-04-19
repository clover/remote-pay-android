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

import android.app.Service;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * This service runs on the POS device.
 */
public abstract class PosRemoteProtocolService extends Service {

  private final String TAG = getClass().getSimpleName();

  private final Handler mMainHandler = new Handler(Looper.getMainLooper());

//  private final Set<RemoteTerminalEventListener> mListeners
//      = Collections.newSetFromMap(new WeakHashMap<RemoteTerminalEventListener, Boolean>());

  private RemoteTerminalStatus mRemoteTerminalStatus = RemoteTerminalStatus.TERMINAL_DISCONNECTED;

  private static boolean sConduitConnected = false;

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public void onDestroy() {
    sConduitConnected = false;
    super.onDestroy();
  }

  public static boolean isConduitConnected() {
    return sConduitConnected;
  }

  public void onConduitConnected() {
    sConduitConnected = true;
  }

  //  @Override
  public void onConduitDisconnected() {
    sConduitConnected = false;
  }

  //  @Override
  public void onMessageTransferError(final Exception e) {

  }

  private void handleTerminalStatusChanged(final RemoteTerminalStatus status) {
    if (mRemoteTerminalStatus == status) {
      return;
    }

    mRemoteTerminalStatus = status;

  }

  public RemoteTerminalStatus getRemoteTerminalStatus() {
    return mRemoteTerminalStatus;
  }

  private interface Invokable<P> {
    void invoke(P p);
  }

  private Context getContext() {
    return this;
  }

  /**
   * Returns true if this POS supports order modifications.
   */
  protected abstract boolean isOrderModificationSupported();

}
