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

package com.clover.remote.client;

import com.clover.remote.InputOption;
import com.clover.remote.KeyPress;
import com.clover.remote.ResultStatus;
import com.clover.remote.TxState;
import com.clover.remote.UiState;
import com.clover.remote.client.device.CloverDevice;
import com.clover.remote.client.device.CloverDeviceConfiguration;
import com.clover.remote.client.device.CloverDeviceFactory;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CaptureAuthRequest;
import com.clover.remote.client.messages.CaptureAuthResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.SignatureVerifyRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.TransactionResponse;
import com.clover.remote.client.messages.TxRequest;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.DiscoveryResponseMessage;
import com.clover.remote.order.DisplayDiscount;
import com.clover.remote.order.DisplayLineItem;
import com.clover.remote.order.DisplayOrder;
import com.clover.remote.order.operation.DiscountsAddedOperation;
import com.clover.remote.order.operation.DiscountsDeletedOperation;
import com.clover.remote.order.operation.LineItemsAddedOperation;
import com.clover.remote.order.operation.LineItemsDeletedOperation;
import com.clover.remote.order.operation.OrderDeletedOperation;
import com.clover.sdk.internal.PayIntent;
import com.clover.sdk.internal.Signature2;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Batch;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;
import com.clover.sdk.v3.payments.VaultedCard;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class CloverConnector implements ICloverConnector {

  private static final int KIOSK_CARD_ENTRY_METHODS = 1 << 15;
  public static final int CARD_ENTRY_METHOD_MAG_STRIPE = 0b0001 | 0b0001_00000000 | KIOSK_CARD_ENTRY_METHODS;
  public static final int CARD_ENTRY_METHOD_ICC_CONTACT = 0b0010 | 0b0010_00000000 | KIOSK_CARD_ENTRY_METHODS;
  public static final int CARD_ENTRY_METHOD_NFC_CONTACTLESS = 0b0100 | 0b0100_00000000 | KIOSK_CARD_ENTRY_METHODS;
  public static final int CARD_ENTRY_METHOD_MANUAL = 0b1000 | 0b1000_00000000 | KIOSK_CARD_ENTRY_METHODS;

  public static final InputOption CANCEL_INPUT_OPTION = new InputOption(KeyPress.ESC, "Cancel");

  //List<ICloverConnectorListener> listeners = new ArrayList<>();
  Gson gson = new Gson();
  private Exception lastException = null;
  private Object lastRequest;

  // manual is not enabled by default
  private int cardEntryMethods = CARD_ENTRY_METHOD_MAG_STRIPE | CARD_ENTRY_METHOD_ICC_CONTACT | CARD_ENTRY_METHOD_NFC_CONTACTLESS;// | CARD_ENTRY_METHOD_MANUAL;

  protected CloverDevice device;
  private CloverDeviceObserver deviceObserver;

  private CloverConnectorBroadcaster broadcaster = new CloverConnectorBroadcaster();

  /// <summary>
  /// set to true to disable printing on the Clover Mini
  /// </summary>
  private boolean disablePrinting;
  /// <summary>
  /// set to true to disable cashback on the Clover Mini
  /// </summary>
  private boolean disableCashBack;

  /// <summary>
  /// set to true to disable tip on the Clover Mini
  /// </summary>
  private boolean disableTip;

  /// <summary>
  /// set to true, so when a transaction fails the Clover Mini returns to the welcome screen,
  /// otherwise it restarts the payment transaction
  /// </summary>
  private boolean disableRestartTransactionOnFail;
  private MerchantInfo merchantInfo;

  private boolean allowOfflinePayment;
  private boolean approveOfflinePaymentWithoutPrompt;

  public CloverConnector() {

  }

  /**
   * CloverConnector constructor
   *
   * @param config - A CloverDeviceConfiguration object; TestDeviceConfiguration can be used for testing
   */
  public CloverConnector(CloverDeviceConfiguration config) {
    this(config, null);
  }

  /**
   * @param config            - A CloverDeviceConfiguration object; TestDeviceConfiguration can be used for testing
   * @param connectorListener - Connector listener that will be added before the device is initialized
   */

  public CloverConnector(CloverDeviceConfiguration config, ICloverConnectorListener connectorListener) {
    if (connectorListener != null) {
      addCloverConnectorListener(connectorListener);
    }
    initialize(config);
  }

  public void addCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.add(connectorListener);
  }

  public void removeCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.remove(connectorListener);
  }

  /// <summary>
  /// Initialize the connector with a given configuration
  /// </summary>
  /// <param name="config">A CloverDeviceConfiguration object; TestDeviceConfiguration can be used for testing</param>
  public void initialize(final CloverDeviceConfiguration config) {
    if (device != null) {
      device.dispose();
    }
    deviceObserver = new InnerDeviceObserver(this);
    //transportObserver = new InnerTransportObserver(this);

    disableCashBack = false;
    disablePrinting = false;
    disableRestartTransactionOnFail = false;
    disableTip = false;

    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        device = CloverDeviceFactory.get(config); // network access, so needs to be off UI thread
        if (device != null) {
          device.Subscribe(deviceObserver);
        }
        return null;
      }
    }.execute();
  }

  /**
   * Sale method, aka "purchase"
   *
   * @param request
   */
  public int sale(SaleRequest request) {
    if (request.getTipAmount() == null) {
      request.setTipAmount(0L);
    }
    saleAuth(request, false);
    return 0;
  }

  /**
   * A common PayIntent builder method for Sale and Auth
   *
   * @param request
   */
  private void saleAuth(TxRequest request, boolean suppressTipScreen) {
    //payment, finishOK(payment), finishCancel, onPaymentVoided
    if (device != null) {
      try {
        lastRequest = request;

        PayIntent.Builder builder = new PayIntent.Builder();

        builder.transactionType(request.getType()); // difference between sale, auth and auth(preAuth)
        builder.remotePrint(disablePrinting);
        //builder.disableCashBack(DisableCashBack);
        builder.cardEntryMethods(request.getCardEntryMethods() != null ? request.getCardEntryMethods() : cardEntryMethods);
        builder.amount(request.getAmount());

        if (request instanceof SaleRequest) { // Sale or Auth because Auth extends SaleRequest
          if (request.getTipAmount() != null) {
            builder.tipAmount(request.getTipAmount()); // can't just set to zero because zero has a specific meaning
          }
          SaleRequest sr = (SaleRequest) request;
          if (sr.getTaxAmount() != null) {
            builder.taxAmount(sr.getTaxAmount());
          }
          if (sr.getTippableAmount() != null) {
            builder.tippableAmount(sr.getTippableAmount());
          }
          if (request.getVaultedCard() != null) {
            builder.vaultedCard(request.getVaultedCard());
          }
          builder.cardNotPresent(request.isCardNotPresent());
          Boolean allowOffline = ((SaleRequest) request).getAllowOfflinePayment();
          builder.allowOfflinePayment(allowOffline == null ? isAllowOfflinePayment() : allowOffline.booleanValue()); // use connector value if request doesn't define one
          Boolean approveOfflinePaymentWithoutPrompt = ((SaleRequest) request).getApproveOfflinePaymentWithoutPrompt();
          builder.approveOfflinePaymentWithoutPrompt(approveOfflinePaymentWithoutPrompt == null ? isApproveOfflinePaymentWithoutPrompt() : approveOfflinePaymentWithoutPrompt); // use connector value if request doesn't define one
        }

        String externalPaymentId = request.getExternalPaymentId();// == null ? getNextId() : request.getExternalPaymentId();
        if (externalPaymentId != null) {
          builder.externalPaymentId(externalPaymentId);
        }

        PayIntent payIntent = builder.build();

        device.doTxStart(payIntent, null, suppressTipScreen); //
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
//    return null;
  }

  /**
   * If signature is captured during a Sale, this method accepts the signature as entered
   *
   * @param request
   */
  public void acceptSignature(SignatureVerifyRequest request) {
    device.doSignatureVerified(request.getPayment(), true);
  }

  /**
   * If signature is captured during a Sale, this method rejects the signature as entered
   *
   * @param request
   */
  public void rejectSignature(SignatureVerifyRequest request) {
    device.doSignatureVerified(request.getPayment(), false);
  }

  /**
   * Auth method to obtain an Auth or Pre-Auth, based on the AuthRequest IsPreAuth flag
   *
   * @param request
   */
  public int auth(AuthRequest request) {
    request.setTipAmount(null);
    saleAuth(request, true);
    return 0;
  }


  public int preAuth(PreAuthRequest request) {
    if (device != null) {
      try {
        saleAuth(request, true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return 0;
  }

  /**
   * Capture a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request
   */

  public void captureAuth(CaptureAuthRequest request) {
    device.doCaptureAuth(request.paymentID, request.amount, request.tipAmount);
  }


  /**
   * Adjust the tip for a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request
   */
  public void tipAdjustAuth(TipAdjustAuthRequest request) {
    if (device == null) {
      return;
    }
    lastRequest = request;
    device.doTipAdjustAuth(request.getOrderID(), request.getPaymentID(), request.getTipAmount());
  }

  public void vaultCard(Integer cardEntryMethods) {
    if (device == null) {
      return;
    }
    device.doVaultCard(cardEntryMethods != null ? cardEntryMethods : getCardEntryMethods());
  }

  /**
   * Void a transaction, given a previously used order ID and/or payment ID
   * TBD - defining a payment or order ID to be used with a void without requiring a response from Sale()
   *
   * @param request
   */
  public void voidPayment(VoidPaymentRequest request) // SaleResponse is a Transaction? or create a Transaction from a SaleResponse
  {
    if (device == null) {
      return;
    }
    lastRequest = request;

    Payment payment = new Payment();
    payment.setId(request.getPaymentId());
    payment.setOrder(new Reference());
    payment.getOrder().setId(request.getOrderId());
    payment.setEmployee(new Reference());
    payment.getEmployee().setId(request.getEmployeeId());
    VoidReason reason = VoidReason.valueOf(request.getVoidReason());
    device.doVoidPayment(payment, reason);
  }

  /**
   * called when requesting a payment be voided when only the request UUID is available
   * @param request
   */
    /*public void VoidTransaction(VoidTransactionRequest request) {
        return 0;
    }*/

  /**
   * Refund a specific payment
   *
   * @param request
   */
  public void refundPayment(RefundPaymentRequest request) {
    if (device == null) {
      return;
    }
    lastRequest = request;
    device.doPaymentRefund(request.getOrderId(), request.getPaymentId(), request.getAmount());
  }

  /**
   * Manual refund method, aka "naked credit"
   *
   * @param request
   */
  public void manualRefund(ManualRefundRequest request) // NakedRefund is a Transaction, with just negative amount
  {
    //payment, finishOK(credit), finishCancel, onPaymentVoided
    if (device == null) {
      return;
    }
    lastRequest = request;
    PayIntent.Builder builder = new PayIntent.Builder();
    builder.amount(-Math.abs(request.getAmount()))
        .cardEntryMethods(15)
        .transactionType(PayIntent.TransactionType.PAYMENT.CREDIT);

    PayIntent payIntent = builder.build();
    device.doTxStart(payIntent, null, true);
  }

  /**
   * Send a request to the server to closeout all orders.
   */
  public void closeout(boolean allowOpenTabs, String batchID) {
    device.doCloseout(allowOpenTabs, batchID);
  }


  /**
   * Cancels the device from waiting for payment card
   */
  public void cancel() {
    invokeInputOption(CANCEL_INPUT_OPTION);
  }

  /**
   * Print simple lines of text to the Clover Mini printer
   *
   * @param messages - list of messages that will be printed, one per line
   */
  public void printText(List<String> messages) {
    if (device != null) {
      device.doPrintText(messages);
    }
  }

  /**
   * Print an image on the Clover Mini printer
   *
   * @param bitmap
   */
  public void printImage(Bitmap bitmap) //Bitmap img
  {
    if (device != null) {
      device.doPrintImage(bitmap);
    }
  }

  /**
   * Show a message on the Clover Mini screen
   *
   * @param message
   */
  public void showMessage(String message) {
    if (device != null) {
      device.doTerminalMessage(message);
    }
  }

  /**
   * Return the device to the Welcome Screen
   */
  public void showWelcomeScreen() {
    if (device != null) {
      device.doShowWelcomeScreen();
    }
  }

  /**
   * Show the thank you screen on the device
   */
  public void showThankYouScreen() {
    if (device != null) {
      device.doShowThankYouScreen();
    }
  }

  /**
   * Show the customer facing receipt option screen for the last order only.
   */
  public void displayReceiptOptions(String orderId, String paymentId) {
    if (device != null) {
      device.doShowReceiptScreen();
    }
  }

  /**
   * Will trigger cash drawer to open that is connected to Clover Mini
   *
   * @param reason
   */

  public void openCashDrawer(String reason) {
    if (device != null) {
      device.doOpenCashDrawer(reason);
    }
  }

  /// <summary>
  /// Show the DisplayOrder on the device. Replaces the existing DisplayOrder on the device.
  /// </summary>
  /// <param name="order"></param>
  public void displayOrder(DisplayOrder order) {
    if (device != null) {
      device.doOrderUpdate(order, null);
    }
  }

  /// <summary>
  /// Notify the device of a DisplayLineItem being added to a DisplayOrder
  /// </summary>
  /// <param name="order"></param>
  /// <param name="lineItem"></param>
  public void displayOrderLineItemAdded(DisplayOrder order, final DisplayLineItem lineItem) {
    if (device != null) {
      LineItemsAddedOperation liao = new LineItemsAddedOperation();
      liao.setOrderId(order.getId());
      List<String> lineItemIds = new ArrayList<String>();
      lineItemIds.add(lineItem.getId());
      liao.setIds(lineItemIds);

      device.doOrderUpdate(order, liao);
    }
  }

  /// <summary>
  /// Notify the device of a DisplayLineItem being removed from a DisplayOrder
  /// </summary>
  /// <param name="order"></param>
  /// <param name="lineItem"></param>
  public void displayOrderLineItemRemoved(DisplayOrder order, DisplayLineItem lineItem) {
    if (device != null) {
      LineItemsDeletedOperation lido = new LineItemsDeletedOperation();
      lido.setOrderId(order.getId());
      List<String> lineItemIds = new ArrayList<String>();
      lineItemIds.add(lineItem.getId());
      lido.setIds(lineItemIds);

      device.doOrderUpdate(order, lido);
    }
  }

  /// <summary>
  /// Notify device of a discount being added to the order.
  /// Note: This is independent of a discount being added to a display line item.
  /// </summary>
  /// <param name="order"></param>
  /// <param name="discount"></param>
  public void displayOrderDiscountAdded(DisplayOrder order, DisplayDiscount discount) {
    if (device != null) {
      DiscountsAddedOperation dao = new DiscountsAddedOperation();
      dao.setOrderId(order.getId());
      List<String> discountIds = new ArrayList<String>();
      discountIds.add(discount.getId());
      dao.setIds(discountIds);

      device.doOrderUpdate(order, dao);
    }
  }

  /// <summary>
  /// Notify the device that a discount was removed from the order.
  /// Note: This is independent of a discount being removed from a display line item.
  /// </summary>
  /// <param name="order"></param>
  /// <param name="discount"></param>
  public void displayOrderDiscountRemoved(DisplayOrder order, DisplayDiscount discount) {
    if (device != null) {
      DiscountsDeletedOperation dao = new DiscountsDeletedOperation();
      dao.setOrderId(order.getId());
      List<String> discountIds = new ArrayList<String>();
      discountIds.add(discount.getId());
      dao.setIds(discountIds);

      device.doOrderUpdate(order, dao);
    }
  }

  /// <summary>
  /// Remove the DisplayOrder from the device.
  /// </summary>
  /// <param name="order"></param>
  public void displayOrderDelete(DisplayOrder order) {
    if (device != null) {
      OrderDeletedOperation dao = new OrderDeletedOperation();
      dao.setId(order.getId());
      device.doOrderUpdate(order, dao);
    }
  }

  /// <summary>
  /// return the Merchant object for the Merchant configured for the Clover Mini
  /// </summary>
  /// <returns></returns>
    /*
    public void getMerchantInfo() {

    }
    */

  public void dispose() {
    broadcaster.clear();
    if (device != null) {
      device.dispose();
    }
  }

  /// <summary>
  /// Invoke the InputOption on the device
  /// </summary>
  /// <param name="io"></param>
  public void invokeInputOption(InputOption io) {
    if (device != null) {
      device.doKeyPress(io.keyPress);
    }
  }

  public void setCardEntryMethods(int entryMethods) {
    cardEntryMethods = entryMethods;
  }

  public int getCardEntryMethods() {
    return cardEntryMethods;
  }

  public boolean isAllowOfflinePayment() {
    return allowOfflinePayment;
  }

  public void setAllowOfflinePayment(boolean allowOfflinePayment) {
    this.allowOfflinePayment = allowOfflinePayment;
  }

  public boolean isApproveOfflinePaymentWithoutPrompt() {
    return approveOfflinePaymentWithoutPrompt;
  }

  public void setApproveOfflinePaymentWithoutPrompt(boolean approveOfflinePaymentWithoutPrompt) {
    this.approveOfflinePaymentWithoutPrompt = approveOfflinePaymentWithoutPrompt;
  }

  /*public void setMerchantInfo(MerchantInfo merchantInfo) {
    this.merchantInfo = merchantInfo;
  }*/

  public MerchantInfo getMerchantInfo() {
    return merchantInfo;
  }

  private class InnerDeviceObserver implements CloverDeviceObserver {

    private RefundPaymentResponse lastPRR;
    CloverConnector cloverConnector;

    class SVR extends SignatureVerifyRequest {
      CloverDevice _device;

      public SVR(CloverDevice device) {
        _device = device;
      }

      public void Accept() {
        _device.doSignatureVerified(getPayment(), true);
      }

      public void Reject() {
        _device.doSignatureVerified(getPayment(), false);
      }
    }

    public InnerDeviceObserver(CloverConnector cc) {
      this.cloverConnector = cc;
    }

    public void onTxState(TxState txState) {
      //Console.WriteLine("onTxTstate: " + txState.ToString());
      cloverConnector.broadcaster.notifyOnTxState(txState);
    }

    public void onPartialAuth(long partialAmount) {
      //TODO: Implement
    }

    public void onTipAdded(long tip) {
      cloverConnector.broadcaster.notifyOnTipAdded(tip);
    }

    public void onAuthTipAdjusted(String paymentId, long amount, boolean success) {
      TipAdjustAuthResponse response = new TipAdjustAuthResponse();
      response.setPaymentId(paymentId);
      response.setAmount(amount);
      response.setSuccess(success);

      cloverConnector.broadcaster.notifyOnTipAdjustAuthResponse(response);
    }

    public void onCashbackSelected(long cashbackAmount) {
      //TODO: Implement
    }

    public void onKeyPressed(KeyPress keyPress) {
      //TODO: Implement
    }

    public void onPaymentRefundResponse(String orderId, String paymentId, Refund refund, TxState code) {
      // hold the response for finishOk for the refund. See comments in onFinishOk(Refund)
      RefundPaymentResponse prr = new RefundPaymentResponse();
      prr.setOrderId(orderId);
      prr.setPaymentId(paymentId);
      prr.setRefundObj(refund);
      prr.setCode(code.toString());
      lastPRR = prr; // set this so we have the appropriate information for when onFinish(Refund) is called
      //cloverConnector.broadcaster.notifyOnRefundPaymentResponse(prr);
    }

    public void onCloseoutResponse(ResultStatus status, String reason, Batch batch) {
      CloseoutResponse cr = new CloseoutResponse();
      cr.setCode(status.toString());
      cr.setReason(reason);
      cr.setBatch(batch);
      cloverConnector.broadcaster.notifyCloseout(cr);
    }

    public void onUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption[] inputOptions) {
      //Console.WriteLine(uiText  + " inputOptions: " + inputOptions.Length);
      CloverDeviceEvent deviceEvent = new CloverDeviceEvent();
      deviceEvent.setInputOptions(inputOptions);
      deviceEvent.setEventState(CloverDeviceEvent.DeviceEventState.valueOf(uiState.toString()));
      deviceEvent.setMessage(uiText);
      if (uiDirection == UiState.UiDirection.ENTER) {
        cloverConnector.broadcaster.notifyOnDeviceActivityStart(deviceEvent);
      } else if (uiDirection == UiState.UiDirection.EXIT) {
        cloverConnector.broadcaster.notifyOnDeviceActivityEnd(deviceEvent);
      }
    }

    public void onFinishOk(Payment payment, Signature2 signature2) {
      try {
        if (cloverConnector.lastRequest instanceof PreAuthRequest) {
          PreAuthResponse response = new PreAuthResponse();
          response.setCode(TransactionResponse.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnPreAuthResponse(response);
        } else if (cloverConnector.lastRequest instanceof AuthRequest) {
          AuthResponse response = new AuthResponse();
          response.setCode(TransactionResponse.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnAuthResponse(response);
        } else if (cloverConnector.lastRequest instanceof SaleRequest) {
          SaleResponse response = new SaleResponse();
          response.setCode(TransactionResponse.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnSaleResponse(response);
        } else {
          Log.e(getClass().getSimpleName(), String.format("Failed to pair this response: %s", payment));
        }
      } finally {
        cloverConnector.device.doShowThankYouScreen();
      }
    }

    public void onFinishOk(Credit credit) {
      try {
        ManualRefundResponse response = new ManualRefundResponse();
        response.setCode(TransactionResponse.SUCCESS);
        response.setCredit(credit);
        cloverConnector.broadcaster.notifyOnManualRefundResponse(response);
      } finally {
        cloverConnector.device.doShowWelcomeScreen();
      }
    }

    public void onFinishOk(Refund refund) {
      try {
        // Since finishOk is the more appropriate/consistent location in the "flow" to
        // publish the RefundResponse (like SaleResponse, AuthResponse, etc., rather
        // than after the server call, which calls onPaymetRefund),
        // we will hold on to the response from
        // onRefundResponse (Which has more information than just the refund) and publish it here
        if (lastPRR != null) {
          if (lastPRR.getRefundObj().getId().equals(refund.getId())) {
            cloverConnector.broadcaster.notifyOnRefundPaymentResponse(lastPRR);
          } else {
            Log.e(this.getClass().getName(), "The last PaymentRefundResponse has a different refund than this refund in finishOk");
          }
        } else {
          Log.e(this.getClass().getName(), "Shouldn't get an onFinishOk with having gotten an onPaymentRefund!");
        }
      } finally {
        cloverConnector.device.doShowWelcomeScreen();
      }
    }

    public void onFinishCancel() {
      try {
        if (cloverConnector.lastRequest instanceof PreAuthRequest) {
          PreAuthResponse preAuthResponse = new PreAuthResponse();
          preAuthResponse.setPayment(null);
          preAuthResponse.setCode(TransactionResponse.CANCEL);
          cloverConnector.broadcaster.notifyOnPreAuthResponse(preAuthResponse);
        } else if (cloverConnector.lastRequest instanceof SaleRequest) {
          SaleResponse saleResponse = new SaleResponse();
          saleResponse.setPayment(null);
          saleResponse.setCode(TransactionResponse.CANCEL);
          cloverConnector.broadcaster.notifyOnSaleResponse(saleResponse);
        } else if (cloverConnector.lastRequest instanceof ManualRefundRequest) {
          ManualRefundResponse refundResponse = new ManualRefundResponse();
          refundResponse.setCode(TransactionResponse.CANCEL);
          cloverConnector.broadcaster.notifyOnManualRefundResponse(refundResponse);
        }
      } finally {
        cloverConnector.device.doShowWelcomeScreen();
      }
    }

    public void onVerifySignature(Payment payment, Signature2 signature) {
      SVR request = new SVR(cloverConnector.device);
      request.setSignature(signature);
      request.setPayment(payment);
      cloverConnector.broadcaster.notifyOnSignatureVerifyRequest(request);
    }

    public void onPaymentVoided(Payment payment, VoidReason reason) {
      VoidPaymentResponse response = new VoidPaymentResponse();
      response.setCode(TransactionResponse.SUCCESS);
      response.setPaymentId(payment.getId());
      response.setTransactionNumber((payment.getCardTransaction() != null) ? payment.getCardTransaction().getTransactionNo() : "");
      //response.setResponseCode(""+payment.getResult()); //TODO: verify this value

      cloverConnector.broadcaster.notifyOnVoidPaymentResponse(response);
      cloverConnector.device.doShowWelcomeScreen();
    }

    public void onCapturePreAuth(ResultStatus status, String reason, String paymentId, long amount, long tipAmount) {
      CaptureAuthResponse response = new CaptureAuthResponse();
      response.setCode(status.toString());
      response.setReason(reason);
      response.setPaymentID(paymentId);
      response.setAmount(amount);
      response.setTipAmount(tipAmount);

      cloverConnector.broadcaster.notifyOnCapturePreAuth(response);
    }

    public void onVaultCardResponse(VaultedCard vaultedCard, String code, String reason) {
      device.doShowWelcomeScreen();
      VaultCardResponse ccr = new VaultCardResponse(vaultedCard, code, reason);
      cloverConnector.broadcaster.notifyOnCaptureCardRespose(ccr);
    }

    public void onTxStartResponse(boolean success) {
      //Console.WriteLine("Tx Started? " + success);
      // TODO: when don't we get this? if a transaction has already begun and we try a 2nd?
    }

    public void onDeviceConnected(CloverDevice device) {
      Log.d(getClass().getSimpleName(), "Connected");
      cloverConnector.broadcaster.notifyOnConnect();
    }

    public void onDeviceReady(CloverDevice device, DiscoveryResponseMessage drm) {
      Log.d(getClass().getSimpleName(), "Ready");
      cloverConnector.device.doShowWelcomeScreen();
      MerchantInfo merchantInfo = new MerchantInfo();

      merchantInfo.merchantID = drm.merchantId;
      merchantInfo.merchantMID = drm.merchantMId;
      merchantInfo.merchantName = drm.merchantName;

      merchantInfo.deviceInfo.name = drm.name;
      merchantInfo.deviceInfo.model = drm.model;
      merchantInfo.deviceInfo.serial = drm.serial;
      cloverConnector.merchantInfo = merchantInfo;

      if (drm.ready) { //TODO: is this a valid check?
        cloverConnector.broadcaster.notifyOnReady(merchantInfo);
      } else {
        Log.e(CloverConnector.class.getName(), "DiscoveryResponseMessage, not ready...");
      }
    }

    public void onDeviceDisconnected(CloverDevice device) {
      Log.d(getClass().getSimpleName(), "Disconnected");
      cloverConnector.broadcaster.notifyOnDisconnect();
    }

    public void onMessage(String message) {
      //Console.WriteLine("onMessage: " + message);
    }
  }

  private static final SecureRandom random = new SecureRandom();
  private static final char[] vals = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'}; // Crockford's base 32 chars

  // providing a simplified version so we don't have a dependency on common's Ids
  private String getNextId() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 13; i++) {
      int idx = random.nextInt(vals.length);
      sb.append(vals[idx]);
    }
    return sb.toString();
  }
}


