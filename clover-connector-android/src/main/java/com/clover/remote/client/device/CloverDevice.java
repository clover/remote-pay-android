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

package com.clover.remote.client.device;

import com.clover.remote.KeyPress;
import com.clover.remote.client.CloverDeviceObserver;
import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.order.DisplayOrder;
import com.clover.sdk.internal.PayIntent;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Payment;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public abstract class CloverDevice {
  protected List<CloverDeviceObserver> deviceObservers = new ArrayList<CloverDeviceObserver>();

  protected CloverTransport transport;
  protected String packageName;

  public CloverDevice(String packageName, CloverTransport transport) {
    this.transport = transport;
    this.packageName = packageName;
  }

  /// <summary>
  /// Adds a observer for transport events to the member transport object to notify
  /// </summary>
  /// <param name="observer"></param>
        /*public void Subscribe(CloverTransportObserver observer)
        {
            this.transport.Subscribe(observer);
        }*/

  public void Subscribe(CloverDeviceObserver observer) {
    deviceObservers.add(observer);
  }

  public void Unsubscribe(CloverDeviceObserver observer) {
    deviceObservers.remove(observer);
  }

  public abstract void doDiscoveryRequest();

  /// <summary>
  ///
  /// </summary>
  /// <param name="payIntent"></param>
  /// <param name="order">can be null.  If it is, an order will implicitly be created on the other end</param>
  public abstract void doTxStart(PayIntent payIntent, Order order, boolean suppressTipScreen);

  public abstract void doKeyPress(KeyPress keyPress);

  public abstract void doVoidPayment(Payment payment, VoidReason reason);

  public abstract void doCaptureAuth(String paymentID, long amount, long tipAmount);

  public abstract void doOrderUpdate(DisplayOrder order, Object orderOperation); //OrderDeletedOperation, LineItemsDeletedOperation, LineItemsAddedOperation, DiscountsDeletedOperation, DiscountsAddedOperation,

  public abstract void doSignatureVerified(Payment payment, boolean verified);

  public abstract void doTerminalMessage(String text);

  public abstract void doPaymentRefund(String orderId, String paymentId, long amount); // manual refunds are handled via doTxStart

  public abstract void doTipAdjustAuth(String orderId, String paymentId, long amount);

  //void doBreak();
  public abstract void doPrintText(List<String> textLines);

  public abstract void doShowWelcomeScreen();

  public abstract void doShowReceiptScreen();

  public abstract void doShowThankYouScreen();

  public abstract void doOpenCashDrawer(String reason);

  public abstract void doPrintImage(Bitmap bitmap);

  public abstract void dispose();

  public abstract void doCloseout(boolean allowOpenTabs, String batchId);

  public abstract void doVaultCard(int cardEntryMethods);


}