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

import com.clover.common2.payments.PayIntent;
import com.clover.remote.Challenge;
import com.clover.remote.KeyPress;
import com.clover.remote.client.transport.ICloverTransport;
import com.clover.remote.order.DisplayOrder;
import com.clover.sdk.v3.customers.CustomerInfo;
import com.clover.sdk.v3.loyalty.LoyaltyDataConfig;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.printer.PrintCategory;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class CloverDevice {
  protected final List<CloverDeviceObserver> deviceObservers = new CopyOnWriteArrayList<>();

  private final ICloverTransport transport;
  protected final String packageName;
  private final String applicationId;
  private boolean supportsAcks;
  private boolean supportsVoidPaymentResponse;

  public CloverDevice(String packageName, ICloverTransport transport, String applicationId) {
    this.transport = transport;
    this.packageName = packageName;
    this.applicationId = applicationId;
  }

  public void subscribe(CloverDeviceObserver observer) {
    deviceObservers.add(observer);
  }

  public void unsubscribe(CloverDeviceObserver observer) {
    deviceObservers.remove(observer);
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void setSupportsAcks(boolean supportsAcks) {
    this.supportsAcks = supportsAcks;
  }

  protected boolean supportsAcks() {
    return this.supportsAcks;
  }

  public void setSupportsVoidPaymentResponse(boolean supportsVoidPaymentResponse) {
    this.supportsVoidPaymentResponse = supportsVoidPaymentResponse;
  }

  protected boolean supportsVoidPaymentResponse() {
    return this.supportsVoidPaymentResponse;
  }

  public void initializeConnection() {
    transport.initializeConnection();
  }

  public void dispose() {
    deviceObservers.clear();
    if (transport != null) {
      transport.dispose();
    }
  }

  protected void sendRemoteMessage(String message) {
    Log.d(getClass().getSimpleName(), "Sending: " + message);
    transport.sendMessage(message);
  }

  public abstract void doDiscoveryRequest();

  public abstract void doTxStart(PayIntent payIntent, Order order, String messageInfo);

  public abstract void doKeyPress(KeyPress keyPress);

  public abstract void doVoidPayment(Payment payment, VoidReason reason, boolean disablePrinting, boolean disableReceiptSelection);

  public abstract void doVoidPaymentRefund(String orderId, String refundId, boolean disablePrinting, boolean disableReceiptSelection);

  public abstract void doCaptureAuth(String paymentID, long amount, long tipAmount);

  public abstract void doOrderUpdate(DisplayOrder order, Object orderOperation);

  public abstract void doSignatureVerified(Payment payment, boolean verified);

  public abstract void doTerminalMessage(String text);

  public abstract void doSendDebugLog(String message);

  public abstract void doPaymentRefund(String orderId, String paymentId, long amount, boolean fullRefund, boolean disablePrinting, boolean disableReceiptSelection);

  public abstract void doTipAdjustAuth(String orderId, String paymentId, long amount);

  public abstract void doPrintText(List<String> textLines, String printRequestId, String printDeviceId);

  public abstract void doShowWelcomeScreen();

  public abstract void doShowPaymentReceiptScreen(String orderId, String paymentId, boolean disablePrinting);

  public abstract void doShowReceiptScreen(String orderId, String paymentId, String refundId, String creditId, boolean disablePrinting);

  public abstract void doShowThankYouScreen();

  public abstract void doOpenCashDrawer(String reason, String deviceId);

  public abstract void doPrintImage(Bitmap bitmap, String printRequestId, String printDeviceId);

  public abstract void doPrintImage(String url, String printRequestId, String printDeviceId);

  public abstract void doPrint(List<Bitmap> img, List<String> urls, List<String> text, String printRequestId, String deviceId);

  public abstract void doRetrievePrinters(PrintCategory category);

  public abstract void doRetrievePrintJobStatus(String requestId);

  public abstract void doCloseout(boolean allowOpenTabs, String batchId);

  public abstract void doVaultCard(int cardEntryMethods);

  public abstract void doResetDevice();

  public abstract void doAcceptPayment(Payment payment);

  public abstract void doRejectPayment(Payment payment, Challenge challenge);

  public abstract void doRetrievePendingPayments();

  public abstract void doReadCardData(PayIntent payment);

  public abstract void doSendMessageToActivity(String actionId, String payload);

  public abstract void doStartActivity(String action, String payload, boolean nonBlocking);

  public abstract void doRetrieveDeviceStatus(boolean sendLastResponse);

  public abstract void doRetrievePayment(String externalPaymentId);

  public abstract void doRegisterForCustomerProvidedData(ArrayList<LoyaltyDataConfig> configurations);

  public abstract void doSetCustomerInfo(CustomerInfo customerInfo);
}