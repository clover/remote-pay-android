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

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.clover.common2.Signature2;
import com.clover.common2.payments.PayIntent;
import com.clover.remote.CardData;
import com.clover.remote.Challenge;
import com.clover.remote.InputOption;
import com.clover.remote.KeyPress;
import com.clover.remote.PendingPaymentEntry;
import com.clover.remote.ResultStatus;
import com.clover.remote.TxStartResponseResult;
import com.clover.remote.TxState;
import com.clover.remote.UiState;
import com.clover.remote.client.device.CloverDevice;
import com.clover.remote.client.device.CloverDeviceConfiguration;
import com.clover.remote.client.device.CloverDeviceFactory;
import com.clover.remote.client.messages.CustomActivityRequest;
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CloseoutRequest;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.PrintManualRefundDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintManualRefundReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentMerchantCopyReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentReceiptMessage;
import com.clover.remote.client.messages.PrintRefundPaymentReceiptMessage;
import com.clover.remote.client.messages.ReadCardDataRequest;
import com.clover.remote.client.messages.ReadCardDataResponse;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.TransactionRequest;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.client.messages.remote.PairingCodeRemoteMessage;
import com.clover.remote.message.DiscoveryResponseMessage;
import com.clover.remote.order.DisplayOrder;
import com.clover.remote.order.operation.OrderDeletedOperation;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Batch;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;
import com.clover.sdk.v3.payments.TipMode;
import com.clover.sdk.v3.payments.TransactionSettings;
import com.clover.sdk.v3.payments.VaultedCard;
import com.google.gson.Gson;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CloverConnector implements ICloverConnector {

  private static final int KIOSK_CARD_ENTRY_METHODS = 1 << 15;
  public static final int CARD_ENTRY_METHOD_MAG_STRIPE = 0b0001 | 0b0001_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33026
  public static final int CARD_ENTRY_METHOD_ICC_CONTACT = 0b0010 | 0b0010_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33282
  public static final int CARD_ENTRY_METHOD_NFC_CONTACTLESS = 0b0100 | 0b0100_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33796
  public static final int CARD_ENTRY_METHOD_MANUAL = 0b1000 | 0b1000_00000000 | KIOSK_CARD_ENTRY_METHODS; // 34824

  public static final InputOption CANCEL_INPUT_OPTION = new InputOption(KeyPress.ESC, "Cancel");

  Gson gson = new Gson();
  private Exception lastException = null;
  private Object lastRequest;

  // manual is not enabled by default
  private final int cardEntryMethods = CARD_ENTRY_METHOD_MAG_STRIPE | CARD_ENTRY_METHOD_ICC_CONTACT | CARD_ENTRY_METHOD_NFC_CONTACTLESS;// | CARD_ENTRY_METHOD_MANUAL;

  protected CloverDevice device;
  private InnerDeviceObserver deviceObserver;

  private CloverConnectorBroadcaster broadcaster = new CloverConnectorBroadcaster();

  private MerchantInfo merchantInfo;

  private CloverDeviceConfiguration configuration;

  boolean isReady = false;

  private Executor executor = Executors.newSingleThreadExecutor();

  public CloverConnector() {

  }

  /**
   * CloverConnector constructor
   *
   * @param config - A CloverDeviceConfiguration object; TestDeviceConfiguration can be used for testing
   */
  public CloverConnector(CloverDeviceConfiguration config) {
    this.configuration = config;
  }

  public void addCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.add(connectorListener);
  }

  public void removeCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.remove(connectorListener);
  }

  /**
   * Initialize the connector with a given configuration
   *
   * @param config - A CloverDeviceConfiguration object; TestDeviceConfiguration can be used for testing
   */
  private void initialize(final CloverDeviceConfiguration config) {
    this.configuration = config;
    deviceObserver = new InnerDeviceObserver(this);

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

  @Override
  public void initializeConnection() {
    if(device == null) {
      initialize(configuration);
    }
  }

  public void sale(SaleRequest request) {
    lastRequest = request;
    if(device == null || !isReady) {
      deviceObserver.onFinishCancel(ResultCode.ERROR, "Device Connection Error", "In sale: SaleRequest - The Clover device is not connected.");
    } else if(request == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In sale: SaleRequest - The request that was passed in for processing is null.");
    } else if(request.getAmount() <= 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Request Validation Error", "In sale: SaleRequest - The request amount cannot be zero. Original Request = " + request);
    } else if(request.getTipAmount() != null && request.getTipAmount() < 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Request Validation Error", "In sale: SaleRequest - The tip amount cannot be less than zero. Original Request = " + request);
    } else if (request.getExternalId() == null || request.getExternalId().trim().length() == 0 || request.getExternalId().trim().length() > 32){
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In sale: SaleRequest - The externalId is required and the max length is 32 characters. Original Request = " + request);
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In sale: SaleRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request);
    } else if (request.getTipMode() != null &&
               !request.getTipMode().equals(SaleRequest.TipMode.TIP_PROVIDED) &&
               request.getTipAmount() != null &&
               request.getTipAmount() > 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In sale: SaleRequest - TipMode must be null or TIP_PROVIDED when TipAmount is greater than zero.  Original Request = " + request);
    } else if (request.getTipMode() != null &&
               request.getTipMode().equals(SaleRequest.TipMode.TIP_PROVIDED) &&
               request.getTipAmount() == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In sale: SaleRequest - TipAmount cannot be null when TipMode is TIP_PROVIDED.    Original Request = " + request);
    } else {

      if (request.getTipAmount() == null) {
        request.setTipAmount(0L);
      }
      try {
        saleAuth(request, false);
      } catch (Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        deviceObserver.onFinishCancel(ResultCode.ERROR, e.getMessage(), sw.toString());
      }

    }
  }

  /**
   * A common PayIntent builder method for Sale, Auth and PreAuth
   *
   * @param request
   */
  private void saleAuth(TransactionRequest request, boolean suppressTipScreen) throws Exception {
    if (device != null && isReady) {
      lastRequest = request;

      PayIntent.Builder builder = new PayIntent.Builder();
      TransactionSettings transactionSettings = new TransactionSettings();

      builder.transactionType(request.getType()); // difference between sale, auth and auth(preAuth)
      builder.amount(request.getAmount());
      builder.vaultedCard(request.getVaultedCard());
      builder.externalPaymentId(request.getExternalId().trim());
      builder.requiresRemoteConfirmation(true);
      if(request.getCardNotPresent() != null) {
        builder.cardNotPresent(request.getCardNotPresent());
      }
      transactionSettings.setCardEntryMethods(request.getCardEntryMethods() != null ? request.getCardEntryMethods() : cardEntryMethods);
      if(request.getDisablePrinting() != null) {
        transactionSettings.setCloverShouldHandleReceipts(!request.getDisablePrinting());
      }
      if(request.getDisableRestartTransactionOnFail() != null) {
        transactionSettings.setDisableRestartTransactionOnFailure(request.getDisableRestartTransactionOnFail());
      }
      transactionSettings.setSignatureEntryLocation(request.getSignatureEntryLocation());
      transactionSettings.setSignatureThreshold(request.getSignatureThreshold());
      transactionSettings.setDisableReceiptSelection(request.getDisableReceiptSelection());
      transactionSettings.setDisableDuplicateCheck(request.getDisableDuplicateChecking());
      transactionSettings.setAutoAcceptPaymentConfirmations(request.getAutoAcceptPaymentConfirmations());
      transactionSettings.setAutoAcceptSignature(request.getAutoAcceptSignature());

      if (request instanceof PreAuthRequest) {
        // nothing extra as of now
      }
      else if (request instanceof AuthRequest) {

        AuthRequest req = (AuthRequest)request;
        if(req.getTaxAmount() != null) {
          builder.taxAmount(req.getTaxAmount());
        }

        if(req.getTippableAmount() != null) {
          transactionSettings.setTippableAmount(req.getTippableAmount());
        }
        if(req.getAllowOfflinePayment() != null) {
          transactionSettings.setAllowOfflinePayment(req.getAllowOfflinePayment());
        }
        if(req.getApproveOfflinePaymentWithoutPrompt() != null) {
          transactionSettings.setApproveOfflinePaymentWithoutPrompt(req.getApproveOfflinePaymentWithoutPrompt());
        }
        if(req.getDisableCashback() != null) {
          transactionSettings.setDisableCashBack(req.getDisableCashback());
        }
        transactionSettings.setTipMode(com.clover.sdk.v3.payments.TipMode.ON_PAPER); // overriding TipMode, since it's an Auth request
      }
      else if (request instanceof SaleRequest) {

        SaleRequest req = (SaleRequest) request;

        // shared with AuthRequest
        if(req.getAllowOfflinePayment() != null) {
          transactionSettings.setAllowOfflinePayment(req.getAllowOfflinePayment());
        }
        if(req.getApproveOfflinePaymentWithoutPrompt() != null) {
          transactionSettings.setApproveOfflinePaymentWithoutPrompt(req.getApproveOfflinePaymentWithoutPrompt());
        }
        if(req.getDisableCashback() != null) {
          transactionSettings.setDisableCashBack(req.getDisableCashback());
        }
        if(req.getTaxAmount() != null) {
          builder.taxAmount(req.getTaxAmount());
        }

        // SaleRequest
        if(req.getTippableAmount() != null) {
          transactionSettings.setTippableAmount(req.getTippableAmount());
        }
        if(req.getTipAmount() != null) {
          builder.tipAmount(req.getTipAmount());
        }
        if (req.getTipMode() != null) {
          transactionSettings.setTipMode(getV3TipModeFromRequestTipMode(req.getTipMode()));
        }
      }

      builder.transactionSettings(transactionSettings);
      PayIntent payIntent = builder.build();

      device.doTxStart(payIntent, null); //

    }
  }

  private TipMode getV3TipModeFromRequestTipMode(SaleRequest.TipMode saleTipMode) {
    TipMode tipMode = null;

    for (TipMode tm: TipMode.values()) {
      if (saleTipMode.toString().equals(tm.toString())) {
        tipMode = tm;
        break;
      }
    }
    return tipMode;
  }

  public void acceptSignature(VerifySignatureRequest request) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptSignature: Device is not connected."));
    } else if(request == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptSignature: VerifySignatureRequest cannot be null."));
    } else if(request.getPayment() == null || request.getPayment().getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptSignature: VerifySignatureRequest. Payment must have anID."));
    } else {
      device.doSignatureVerified(request.getPayment(), true);
    }
  }

  public void rejectSignature(VerifySignatureRequest request) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In rejectSignature: Device is not connected."));
    } else if(request == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In rejectSignature: VerifySignatureRequest cannot be null."));
    } else if(request.getPayment() == null || request.getPayment().getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In rejectSignature: VerifySignatureRequest.Payment must have an ID."));
    } else {
      device.doSignatureVerified(request.getPayment(), false);
    }
  }

  @Override
  public void acceptPayment(Payment payment) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptPayment: Device is not connected."));
    } else if(payment == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptPayment: Payment cannot be null."));
    } else if(payment.getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In acceptPayment: Payment must have an ID."));
    } else {
      device.doAcceptPayment(payment);
    }
  }

  @Override
  public void rejectPayment(Payment payment, Challenge challenge) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In rejectPayment: Device is not connected."));
    } else if(payment == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In rejectPayment: Payment cannot be null."));
    } else if(payment.getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In rejectPayment: Payment must have an ID."));
    } else if(challenge == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In rejectPayment: Challenge cannot be null."));
    } else {
      device.doRejectPayment(payment, challenge);
    }
  }

  public void auth(AuthRequest request) {
    lastRequest = request;
    if(device == null || !isReady) {
      deviceObserver.onFinishCancel(ResultCode.ERROR, "Device connection Error", "In auth: Auth Request - The Clover device is not connected.");
    } else if (!merchantInfo.supportsAuths) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In auth: AuthRequest - Auths are not enabled for the payment gateway. Original Request = " + request);
    } else if(request == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In auth: AuthRequest - The request that was passed in for processing is null.");
    } else if(request.getAmount() <= 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Request Validation Error", "In auth: AuthRequest - The request amount cannot be zero. Original Request = " + request);
    } else if (request.getExternalId() == null || request.getExternalId().trim().length() == 0 || request.getExternalId().trim().length() > 32){
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In auth: AuthRequest - The externalId is invalid. It is required and the max length is 32. Original Request = " + request);
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In auth: AuthRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request);
    } else {

      try {
        saleAuth(request, true);
      } catch (Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        deviceObserver.onFinishCancel(ResultCode.ERROR, e.getMessage(), sw.toString());
      }

    }
  }


  public void preAuth(PreAuthRequest request) {
    lastRequest = request;
    if(device == null || !isReady) {
      deviceObserver.onFinishCancel(ResultCode.ERROR, "Device connection Error", "In preAuth: PreAuthRequest - The Clover device is not connected.");
    } else if (!merchantInfo.supportsPreAuths) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In preAuth: PreAuthRequest - PreAuths are not enabled for the payment gateway. Original Request = " + request);
    } else if(request == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In preAuth: PreAuthRequest - The request that was passed in for processing is null.");
    } else if(request.getAmount() <= 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Request Validation Error", "In preAuth: PreAuthRequest - The request amount cannot be zero. Original Request = " + request);
    } else if (request.getExternalId() == null || request.getExternalId().trim().length() == 0 || request.getExternalId().trim().length() > 32){
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In preAuth: PreAuthRequest - The externalId is invalid. It is required and the max length is 32. Original Request = " + request);
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In preAuth: PreAuthRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request);
    } else {

      try {
        saleAuth(request, true);
      } catch (Exception e) {
        lastRequest = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        deviceObserver.onFinishCancel(ResultCode.ERROR, e.getMessage(), sw.toString());
      }

    }
  }

  public void capturePreAuth(CapturePreAuthRequest request) {
    if(device == null || !isReady) {
      deviceObserver.onCapturePreAuth(ResultCode.ERROR, "Device connection Error", "In capturePreAuth: CapturePreAuth - The Clover device is not connected.", null, null);
    } else if (!merchantInfo.supportsPreAuths) {
      deviceObserver.onCapturePreAuth(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In capturePreAuth: PreAuth Captures are not enabled for the payment gateway. Original Request = " + request, null, null);
    } else if(request == null) {
      deviceObserver.onCapturePreAuth(ResultCode.FAIL, "Invalid Argument.", "In capturePreAuth: CapturePreAuth - The request that was passed in for processing is null.", null, null);
    } else if(request.getAmount() < 0 || request.getTipAmount() < 0) {
      deviceObserver.onCapturePreAuth(ResultCode.FAIL, "Request Validation Error", "In capturePreAuth: CapturePreAuth - The request amount must be greater than zero and the tip must be greater than or equal to zero. Original Request = " + request, null, null);
    } else {
      try {
        device.doCaptureAuth(request.paymentID, request.amount, request.tipAmount);
      } catch(Exception e) {
        CapturePreAuthResponse response = new CapturePreAuthResponse(false, ResultCode.UNSUPPORTED);
        response.setReason("Pre Auths unsupported");
        response.setMessage("The currently configured merchant gateway does not support Capture Auth requests.");
        broadcaster.notifyOnCapturePreAuth(response);
      }
    }

  }

  public void tipAdjustAuth(TipAdjustAuthRequest request) {
    if(device == null || !isReady) {
      deviceObserver.onAuthTipAdjusted(ResultCode.ERROR, "Device connection Error", "In tipAdjustAuth: TipAdjustAuthRequest - The Clover device is not connected.");
    } else if (!merchantInfo.supportsTipAdjust) {
      deviceObserver.onAuthTipAdjusted(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In tipAdjustAuth: TipAdjustAuthRequest - Tip Adjustments are not enabled for the payment gateway. Original Request = " + request);
    } else if(request == null) {
      deviceObserver.onAuthTipAdjusted(ResultCode.FAIL, "Invalid Argument.",
      "In tipAdjustAuth: TipAdjustAuthRequest - The request that was passed in for processing is null.");
    } else if(request.getPaymentId() == null) {
      deviceObserver.onAuthTipAdjusted(ResultCode.FAIL, "Invalid Argument.",
      "In tipAdjustAuth: TipAdjustAuthRequest - The paymentId is required.");
    } else if(request.getTipAmount() < 0) {
      deviceObserver.onAuthTipAdjusted(ResultCode.FAIL, "Request Validation Error", "In tipAdjustAuth: TipAdjustAuthRequest - The request amount cannot be less than zero. Original Request = " + request);
    } else {
      device.doTipAdjustAuth(request.getOrderId(), request.getPaymentId(), request.getTipAmount());
    }
  }

  public void vaultCard(Integer cardEntryMethods) {
    if(device == null || !isReady) {
      deviceObserver.onVaultCardResponse(false, ResultCode.ERROR, "Device connection Error", "In vaultCard: The Clover device is not connected.", null);
    } else if (!merchantInfo.supportsVaultCards) {
      deviceObserver.onVaultCardResponse(false, ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In vaultCard: VaultCard/Payment Tokens are not enabled for the payment gateway.", null);
    } else {
      device.doVaultCard(cardEntryMethods != null ? cardEntryMethods : getCardEntryMethods());
    }

  }

  public void voidPayment(VoidPaymentRequest request)
  {
    if(device == null || !isReady) {
      deviceObserver.onPaymentVoided(ResultCode.ERROR, "Device connection Error", "In voidPayment: VoidPaymentRequest - The Clover device is not connected.");
    } else if(request == null) {
      deviceObserver.onPaymentVoided(ResultCode.FAIL, "Invalid Argument.", "In voidPayment: VoidPaymentRequest - The request that was passed in for processing is null.");
    } else if(request.getPaymentId() == null) {
      deviceObserver.onPaymentVoided(ResultCode.FAIL, "Invalid Argument.", "In voidPayment: VoidPaymentRequest - The paymentId is required.");
    } else {
      Payment payment = new Payment();
      payment.setId(request.getPaymentId());
      payment.setOrder(new Reference());
      payment.getOrder().setId(request.getOrderId());
      payment.setEmployee(new Reference());
      payment.getEmployee().setId(request.getEmployeeId());
      VoidReason reason = VoidReason.valueOf(request.getVoidReason());
      device.doVoidPayment(payment, reason);
    }

  }

  public void refundPayment(RefundPaymentRequest request) {
    if (device == null || !isReady)
    {
      RefundPaymentResponse prr = new RefundPaymentResponse(false, ResultCode.ERROR);
      prr.setRefund(null);
      prr.setReason("Device Connection Error");
      prr.setMessage("In refundPayment: RefundPaymentRequest - The Clover device is not connected.");
      deviceObserver.lastPRR = prr;
      deviceObserver.onFinishCancel();
    } else if(request == null) {
      RefundPaymentResponse prr = new RefundPaymentResponse(false, ResultCode.FAIL);
      prr.setRefund(null);
      prr.setReason("Request Validation Error");
      prr.setMessage("In refundPayment: RefundPaymentRequest - The request that was passed in for processing is empty.");
      deviceObserver.lastPRR = prr;
      deviceObserver.onFinishCancel();
    } else if(request.getPaymentId() == null) {
      RefundPaymentResponse prr = new RefundPaymentResponse(false, ResultCode.FAIL);
      prr.setRefund(null);
      prr.setReason("Request Validation Error");
      prr.setMessage("In refundPayment: RefundPaymentRequest PaymentID cannot be empty. " + request);
      deviceObserver.lastPRR = prr;
      deviceObserver.onFinishCancel();
    } else if(request.getAmount() <= 0 && !request.isFullRefund()) {
      RefundPaymentResponse prr = new RefundPaymentResponse(false, ResultCode.FAIL);
      prr.setRefund(null);
      prr.setReason("Request Validation Error");
      prr.setMessage("In refundPayment: RefundPaymentRequest Amount must be greater than zero when FullRefund is set to false. " + request);
      deviceObserver.lastPRR = prr;
      deviceObserver.onFinishCancel();
    } else {
      device.doPaymentRefund(request.getOrderId(), request.getPaymentId(), request.getAmount(), request.isFullRefund());
    }
  }

  public void manualRefund(ManualRefundRequest request) // NakedRefund is a Transaction, with just negative amount
  {
    TransactionSettings transactionSettings = new TransactionSettings();
    lastRequest = request;
    if(device == null || !isReady) {
      deviceObserver.onFinishCancel(ResultCode.ERROR, "Device connection Error", "In manualRefund: ManualRefundRequest - The Clover device is not connected.");
    } else if(!merchantInfo.supportsManualRefunds) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In manualRefund: ManualRefundRequest - Manual Refunds are not enabled for the payment gateway. Original Request = " + request);
    } else if(request == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In manualRefund: ManualRefundRequest - The request that was passed in for processing is null.");
    } else if(request.getAmount() <= 0) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Request Validation Error", "In manualRefund: ManualRefundRequest - The request amount cannot be zero. Original Request = " + request);
    } else if (request.getExternalId() == null || request.getExternalId().trim().length() == 0 || request.getExternalId().trim().length() > 32){
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In manualRefund: ManualRefundRequest - The externalId is invalid. It is required and the max length is 32. Original Request = " + request);
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In manualRefund: ManualRefundRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request);
    } else {
      PayIntent.Builder builder = new PayIntent.Builder();
      builder.amount(-Math.abs(request.getAmount()))
          .transactionType(PayIntent.TransactionType.PAYMENT.CREDIT)
          .vaultedCard(request.getVaultedCard())
          .externalPaymentId(request.getExternalId());

      transactionSettings.setCardEntryMethods(request.getCardEntryMethods() != null ? request.getCardEntryMethods() : cardEntryMethods);
      if(request.getDisablePrinting() != null) {
        transactionSettings.setCloverShouldHandleReceipts(!request.getDisablePrinting());
      }
      if(request.getDisableRestartTransactionOnFail() != null) {
        transactionSettings.setDisableRestartTransactionOnFailure(request.getDisableRestartTransactionOnFail());
      }
      if(request.getSignatureEntryLocation() != null) {
        transactionSettings.setSignatureEntryLocation(request.getSignatureEntryLocation());
      }
      if(request.getSignatureThreshold() != null) {
        transactionSettings.setSignatureThreshold(request.getSignatureThreshold());
      }
      if(request.getDisableReceiptSelection() != null) {
        transactionSettings.setDisableReceiptSelection(request.getDisableReceiptSelection());
      }
      builder.transactionSettings(transactionSettings);
      PayIntent payIntent = builder.build();
      device.doTxStart(payIntent, null);
    }

  }


  @Override public void retrievePendingPayments() {
    if(device == null || !isReady) {
      deviceObserver.onPendingPaymentsResponse(ResultCode.ERROR, "Device connection Error", "In retrievePendingPayments: The Clover device is not connected.");
    } else {
      device.doRetrievePendingPayments();
    }
  }

  @Override public void readCardData(ReadCardDataRequest request) {
    if(device == null || !isReady) {
      deviceObserver.onReadCardDataResponse(ResultCode.ERROR, "Device connection Error", "In readCardData: The Clover device is not connected.");
    } else if (request == null) {
      deviceObserver.onFinishCancel(ResultCode.FAIL, "Invalid Argument.", "In readCardData: ReadCardDataRequest - The request that was passed in for processing is null.");
    } else {

      // create pay intent...
      PayIntent.Builder builder = new PayIntent.Builder();
      builder.transactionType(PayIntent.TransactionType.DATA);
      builder.cardEntryMethods(request.getCardEntryMethods() != null ? request.getCardEntryMethods() : cardEntryMethods);
      builder.forceSwipePinEntry(request.isForceSwipePinEntry());

      PayIntent pi = builder.build();
      device.doReadCardData(pi);
    }
  }



  public void closeout(CloseoutRequest request) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In closeout: CloseoutRequest - The Clover device is not connected."));
    } else {
      device.doCloseout(request.isAllowOpenTabs(), request.getBatchId());
    }
  }


  public void cancel() {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In cancel: The Clover device is not connected."));
    } else {
      invokeInputOption(CANCEL_INPUT_OPTION);
    }
  }

  public void printText(List<String> messages) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In printText: The Clover device is not connected."));
    } else if(messages == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In printText: Invalid argument. Null is not allowed."));
    } else {
      device.doPrintText(messages);
    }
  }

  public void printImage(Bitmap bitmap) //Bitmap img
  {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In printImage: The Clover device is not connected."));
    } else if(bitmap == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In printImage: Invalid argument.  Null is not allowed."));
    } else {
      device.doPrintImage(bitmap);
    }
  }

  public void printImageFromURL(String url) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In printImageFromURL: The Clover device is not connected."));
    } else if(url == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In printImageFromURL: Invalid argument.  Null is not allowed."));
    } else {
      device.doPrintImage(url);
    }
  }

  public void showMessage(String message) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In showMessage: The Clover device is not connected."));
    } else if(message == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In showMessage: Invalid argument.  Null is not allowed."));
    } else {
      device.doTerminalMessage(message);
    }
  }

  public void showWelcomeScreen() {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In showWelcomeScreen: The Clover device is not connected."));
    } else {
      device.doShowWelcomeScreen();
    }
  }

  public void showThankYouScreen() {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In showThankYouScreen: The Clover device is not connected."));
    } else {
      device.doShowThankYouScreen();
    }
  }

  public void displayPaymentReceiptOptions(String orderId, String paymentId) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In displayPaymentReceiptOptions: The Clover device is not connected."));
    } else if(orderId == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In displayPaymentReceiptOptions: Invalid argument.  The orderId cannot be null."));
    } else if(paymentId == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In displayPaymentReceiptOptions: Invalid argument.  The paymentId cannot be null."));
    } else {
      device.doShowPaymentReceiptScreen(orderId, paymentId);
    }
  }

  public void openCashDrawer(String reason) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In displayPaymentReceiptOptions: The Clover device is not connected."));
    } else {
      device.doOpenCashDrawer(reason);
    }
  }

  public void showDisplayOrder(DisplayOrder order) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In showDisplayOrder: The Clover device is not connected."));
    } else if(order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In showDisplayOrder: Invalid argument.  The order cannot be null."));
    } else {
      device.doOrderUpdate(order, null);
    }
  }

  public void removeDisplayOrder(DisplayOrder order) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In removeDisplayOrder: The Clover device is not connected."));
    } else if(order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, "In removeDisplayOrder: Invalid argument.  The order cannot be null."));
    } else {
      OrderDeletedOperation dao = new OrderDeletedOperation();
      dao.setId(order.getId());
      device.doOrderUpdate(order, dao);
    }

  }

  public void dispose() {
    broadcaster.clear();
    if (device != null) {
      device.dispose();
    }
  }

  public void invokeInputOption(InputOption io) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In invokeInputOption: The Clover device is not connected."));
    } else {
      device.doKeyPress(io.keyPress);
    }
  }

  @Override
  public void resetDevice() {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In resetDevice: The Clover device is not connected."));
    } else {
      device.doResetDevice();
    }
  }

  private int getCardEntryMethods() {
    return cardEntryMethods;
  }

  @Override public void startCustomActivity(CustomActivityRequest request) {
    if(device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, "In promptForTip: The Clover device is not connected."));
    } else {
      device.doStartActivity(request.action, request.getPayload(), request.isNonBlocking());
    }
  }

  private class InnerDeviceObserver implements CloverDeviceObserver {

    private RefundPaymentResponse lastPRR;
    CloverConnector cloverConnector;

    class SVR extends VerifySignatureRequest {
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

    }

    @Override
    public void onTxStartResponse(TxStartResponseResult result, String externalId) {
      boolean success = result.equals(TxStartResponseResult.SUCCESS) ? true : false;
      if (success)
      {
        return;
      }
      boolean duplicate = result.equals(TxStartResponseResult.DUPLICATE);
      try
      {

        if (cloverConnector.lastRequest instanceof PreAuthRequest)
        {
          PreAuthResponse response = new PreAuthResponse(false,ResultCode.FAIL);
          if (duplicate)
          {
            response.setResult(ResultCode.CANCEL);
            response.setReason(result.toString());
            response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
          }
          else
          {
            response.setResult(ResultCode.FAIL);
            response.setReason(result.toString());
          }
          cloverConnector.broadcaster.notifyOnPreAuthResponse(response);
        }
        else if (cloverConnector.lastRequest instanceof AuthRequest)
        {
          AuthResponse response = new AuthResponse(false, ResultCode.FAIL);
          if (duplicate)
          {
            response.setResult(ResultCode.CANCEL);
            response.setReason(result.toString());
            response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
          }
          else
          {
            response.setResult(ResultCode.FAIL);
            response.setReason(result.toString());
          }
          cloverConnector.broadcaster.notifyOnAuthResponse(response);
        }
        else if (cloverConnector.lastRequest instanceof SaleRequest)
        {
          SaleResponse response = new SaleResponse(false, ResultCode.FAIL);
          if (duplicate)
          {
            response.setResult(ResultCode.CANCEL);
            response.setReason(result.toString());
            response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
          }
          else
          {
            response.setResult(ResultCode.FAIL);
            response.setReason(result.toString());
          }
          cloverConnector.broadcaster.notifyOnSaleResponse(response);
        }
        else if (cloverConnector.lastRequest instanceof ManualRefundRequest)
        {
          ManualRefundResponse response = new ManualRefundResponse(false, ResultCode.FAIL);
          if (duplicate)
          {
            response.setResult(ResultCode.CANCEL);
            response.setReason(result.toString());
            response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
          }
          else
          {
            response.setResult(ResultCode.FAIL);
            response.setReason(result.toString());
          }
          cloverConnector.broadcaster.notifyOnManualRefundResponse(response);
        }
      }
      finally
      {
        cloverConnector.lastRequest = null;
      }
    }

    public void onPendingPaymentsResponse(ResultCode code, String category, String message) {
      RetrievePendingPaymentsResponse rppr = new RetrievePendingPaymentsResponse(code, category, Collections.EMPTY_LIST);
      rppr.setMessage(message);
      cloverConnector.broadcaster.notifyOnRetrievePendingPaymentResponse(rppr);
    }

    public void onPendingPaymentsResponse(boolean success, List<PendingPaymentEntry> payments) {
      RetrievePendingPaymentsResponse rppr = new RetrievePendingPaymentsResponse(success ? ResultCode.SUCCESS : ResultCode.FAIL, "", payments);
      cloverConnector.broadcaster.notifyOnRetrievePendingPaymentResponse(rppr);
    }

    @Override public void onReadCardResponse(ResultStatus status, String reason, CardData cardData) {
      boolean success = status == ResultStatus.SUCCESS;
      if(success) {
        onReadCardDataResponse(success, cardData);
      } else if (status == ResultStatus.CANCEL) {
        onReadCardDataResponse(ResultCode.CANCEL, reason, "");
      } else {
        onReadCardDataResponse(ResultCode.FAIL, reason, "");
      }
    }

    public void onReadCardDataResponse(ResultCode code, String category, String message) {
      boolean success = code == ResultCode.SUCCESS;
      ReadCardDataResponse rcdr = new ReadCardDataResponse(success, code);
      rcdr.setMessage(message);
      cloverConnector.device.doShowWelcomeScreen();
      cloverConnector.broadcaster.notifyOnReadCardDataResponse(rcdr);
    }

    public void onActivityResponse(ResultStatus status, String action, String payload, String failReason) {
      boolean success = status == ResultStatus.SUCCESS;
      CustomActivityResponse car = new CustomActivityResponse(success, success ? ResultCode.SUCCESS : ResultCode.CANCEL, action, payload, failReason);
      cloverConnector.broadcaster.notifyOnActivityResponse(car);
    }

    public void onReadCardDataResponse(boolean success, CardData cardData) {
      ReadCardDataResponse rcdr = new ReadCardDataResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      rcdr.setCardData(cardData);
      cloverConnector.device.doShowWelcomeScreen();
      cloverConnector.broadcaster.notifyOnReadCardDataResponse(rcdr);
    }

    public void onPartialAuth(long partialAmount) {
      //TODO: For future use
    }

    public void onTipAdded(long tip) {
      cloverConnector.broadcaster.notifyOnTipAdded(tip);
    }

    public void onAuthTipAdjusted(String paymentId, long amount, boolean success) {
      TipAdjustAuthResponse response = new TipAdjustAuthResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setPaymentId(paymentId);
      response.setTipAmount(amount);
      if(!success) {
        response.setReason("Failure");
        response.setMessage("TipAdjustAuth failed to process for payment ID: " + paymentId);
      }
      cloverConnector.broadcaster.notifyOnTipAdjustAuthResponse(response);
    }

    // convenience, used for failure only
    private void onAuthTipAdjusted(ResultCode resultCode, String reason, String message) {
      TipAdjustAuthResponse taar = new TipAdjustAuthResponse(resultCode == ResultCode.SUCCESS, resultCode);
      taar.setPaymentId(null);
      taar.setTipAmount(0);
      taar.setReason(reason);
      taar.setMessage(message);

      cloverConnector.broadcaster.notifyOnTipAdjustAuthResponse(taar);
    }

    public void onCashbackSelected(long cashbackAmount) {
      //TODO: For future use
    }

    public void onKeyPressed(KeyPress keyPress) {
      //TODO: For future use
    }

    public void onPaymentRefundResponse(String orderId, String paymentId, Refund refund, TxState code) {
      // hold the response for finishOk for the refund. See comments in onFinishOk(Refund)
      boolean success = code == TxState.SUCCESS;
      RefundPaymentResponse prr = new RefundPaymentResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      prr.setOrderId(orderId);
      prr.setPaymentId(paymentId);
      prr.setRefund(refund);
      lastPRR = prr; // set this so we have the appropriate information for when onFinish(Refund) is called
    }

    public void onCloseoutResponse(ResultStatus status, String reason, Batch batch) {
      CloseoutResponse cr = new CloseoutResponse(status == ResultStatus.SUCCESS, status.toString() == "SUCCESS" ? ResultCode.SUCCESS : ResultCode.FAIL);
      cr.setReason(reason);
      cr.setBatch(batch);
      cloverConnector.broadcaster.notifyCloseout(cr);
    }

    public void onUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption[] inputOptions) {
      CloverDeviceEvent deviceEvent = new CloverDeviceEvent();
      deviceEvent.setInputOptions(inputOptions);
      try {
        CloverDeviceEvent.DeviceEventState eventState = CloverDeviceEvent.DeviceEventState.valueOf(uiState.toString());
        deviceEvent.setEventState(eventState);
        deviceEvent.setMessage(uiText);
        if (uiDirection == UiState.UiDirection.ENTER) {
          cloverConnector.broadcaster.notifyOnDeviceActivityStart(deviceEvent);
        } else if (uiDirection == UiState.UiDirection.EXIT) {
          cloverConnector.broadcaster.notifyOnDeviceActivityEnd(deviceEvent);
          if (uiState.toString().equals(CloverDeviceEvent.DeviceEventState.RECEIPT_OPTIONS.toString())) {
            cloverConnector.device.doShowWelcomeScreen();
          }
        }
      } catch(IllegalArgumentException iae) {
        Log.w(getClass().getSimpleName(), "Unsupported UI event type: " + uiState);
      }
    }

    public void onFinishOk(Payment payment, Signature2 signature2) {
      try {
        cloverConnector.device.doShowThankYouScreen(); //need to do this first, so Listener implementation can replace the screen as desired
        Object lastReq = cloverConnector.lastRequest;
        cloverConnector.lastRequest = null;
        if (lastReq instanceof PreAuthRequest) {
          PreAuthResponse response = new PreAuthResponse(true, ResultCode.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnPreAuthResponse(response);
        } else if (lastReq instanceof AuthRequest) {
          AuthResponse response = new AuthResponse(true, ResultCode.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnAuthResponse(response);
        } else if (lastReq instanceof SaleRequest) {
          SaleResponse response = new SaleResponse(true, ResultCode.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnSaleResponse(response);
        } else {
          Log.e(getClass().getSimpleName(), String.format("Failed to pair this response: %s", payment));
        }
      } finally {
        // do nothing for now...
      }
    }

    public void onFinishOk(Credit credit) {

      try {
        cloverConnector.device.doShowWelcomeScreen();
        cloverConnector.lastRequest = null;
        ManualRefundResponse response = new ManualRefundResponse(true, ResultCode.SUCCESS);
        response.setCredit(credit);
        cloverConnector.broadcaster.notifyOnManualRefundResponse(response);
      } finally {
      }
    }

    public void onFinishOk(Refund refund) {
      try {
        cloverConnector.device.doShowWelcomeScreen();
        cloverConnector.lastRequest = null;
        RefundPaymentResponse lastRefundResponse = lastPRR;
        lastPRR = null;
        // Since finishOk is the more appropriate/consistent location in the "flow" to
        // publish the RefundResponse (like SaleResponse, AuthResponse, etc., rather
        // than after the server call, which calls onPaymetRefund),
        // we will hold on to the response from
        // onRefundResponse (Which has more information than just the refund) and publish it here
        if (lastRefundResponse != null) {
          if (lastRefundResponse.getRefund().getId().equals(refund.getId())) {
            cloverConnector.broadcaster.notifyOnRefundPaymentResponse(lastRefundResponse);
          } else {
            Log.e(this.getClass().getName(), "The last PaymentRefundResponse has a different refund than this refund in finishOk");
          }
        } else {
          Log.e(this.getClass().getName(), "Shouldn't get an onFinishOk with having gotten an onPaymentRefund!");
        }
      } finally {
      }
    }

    private void onFinishCancel(ResultCode result, String reason, String message) {
      try {
        if(device != null) {
          device.doShowWelcomeScreen();
        }
        Object lastReq = lastRequest;
        lastRequest = null;
        if (lastReq instanceof PreAuthRequest) {
          PreAuthResponse preAuthResponse = new PreAuthResponse(false, result != null? result : ResultCode.CANCEL);
          preAuthResponse.setReason(reason != null ? reason : "Request Canceled");
          preAuthResponse.setMessage(message != null ? message : "The PreAuth Request was canceled.");
          preAuthResponse.setPayment(null);
          broadcaster.notifyOnPreAuthResponse(preAuthResponse);
        } else if (lastReq instanceof SaleRequest) {
          SaleResponse saleResponse = new SaleResponse(false, result != null? result : ResultCode.CANCEL);
          saleResponse.setReason(reason != null ? reason : "Request Canceled");
          saleResponse.setMessage(message != null ? message : "The Sale Request was canceled.");
          saleResponse.setPayment(null);
          broadcaster.notifyOnSaleResponse(saleResponse);
        } else if (lastReq instanceof AuthRequest) {
          AuthResponse authResponse = new AuthResponse(false, result != null? result : ResultCode.CANCEL);
          authResponse.setReason(reason != null ? reason : "Request Canceled");
          authResponse.setMessage(message != null ? message : "The Auth Request was canceled.");
          authResponse.setPayment(null);
          broadcaster.notifyOnAuthResponse(authResponse);
        } else if (lastReq instanceof ManualRefundRequest) {
          ManualRefundResponse refundResponse = new ManualRefundResponse(false, result != null? result : ResultCode.CANCEL);
          refundResponse.setReason(reason != null ? reason : "Request Canceled");
          refundResponse.setMessage(message != null ? message : "The Manual Refund Request was canceled.");
          refundResponse.setCredit(null);
          broadcaster.notifyOnManualRefundResponse(refundResponse);
        } else if (lastPRR instanceof RefundPaymentResponse) {
          broadcaster.notifyOnRefundPaymentResponse(lastPRR);
          lastPRR = null;
        }
      } finally {
        // do nothing
      }
    }
    public void onFinishCancel() {
      onFinishCancel(ResultCode.CANCEL, null, null);
    }

    public void onVerifySignature(Payment payment, Signature2 signature) {
      SVR request = new SVR(cloverConnector.device);
      request.setSignature(signature);
      request.setPayment(payment);
      broadcaster.notifyOnVerifySignatureRequest(request);
    }

    @Override
    public void onConfirmPayment(Payment payment, Challenge[] challenges) {
      ConfirmPaymentRequest cpr = new ConfirmPaymentRequest();
      cpr.setPayment(payment);
      cpr.setChallenges(challenges);
      broadcaster.notifyOnConfirmPaymentRequest(cpr);
    }

    @Override
    public void onPaymentVoided(Payment payment, VoidReason voidReason, ResultStatus result, String reason, String message) {
      boolean success = result == ResultStatus.SUCCESS;

      VoidPaymentResponse response = new VoidPaymentResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setReason(reason != null ? reason : result.toString());
      response.setMessage(message != null ? message : "No extended information provided.");
      response.setPaymentId(payment != null ? payment.getId() : null);
      cloverConnector.broadcaster.notifyOnVoidPaymentResponse(response);
    }

    public void onPaymentVoided(ResultCode code, String reason, String message) {
      VoidPaymentResponse response = new VoidPaymentResponse(code == ResultCode.SUCCESS, code);
      response.setReason(reason != null ? reason : code.toString());
      response.setMessage(message != null ? message : "No extended information provided.");
      response.setPaymentId(null);
      cloverConnector.broadcaster.notifyOnVoidPaymentResponse(response);
    }

    public void onCapturePreAuth(ResultStatus status, String reason, String paymentId, long amount, long tipAmount) {
      boolean success = ResultStatus.SUCCESS == status;
      CapturePreAuthResponse response = new CapturePreAuthResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setReason(reason);
      response.setPaymentID(paymentId);
      response.setAmount(amount);
      response.setTipAmount(tipAmount);

      cloverConnector.broadcaster.notifyOnCapturePreAuth(response);
    }

    public void onCapturePreAuth(ResultCode code, String reason, String paymentId, Long amount, Long tipAmount) {
      boolean success = ResultCode.SUCCESS == code;
      CapturePreAuthResponse response = new CapturePreAuthResponse(success, code);
      response.setReason(reason);
      response.setPaymentID(paymentId);
      if(amount != null) {
        response.setAmount(amount);
      }
      if(tipAmount != null) {
        response.setTipAmount(tipAmount);
      }

      cloverConnector.broadcaster.notifyOnCapturePreAuth(response);
    }

    private void onVaultCardResponse(boolean success, ResultCode code, String reason, String message, VaultedCard vaultedCard) {
      device.doShowWelcomeScreen();
      VaultCardResponse ccr = new VaultCardResponse(success, code, vaultedCard);
      ccr.setReason(reason);
      ccr.setMessage(message);
      cloverConnector.broadcaster.notifyOnVaultCardRespose(ccr);
    }

    public void onVaultCardResponse(VaultedCard vaultedCard, String code, String reason) {
      boolean success = "SUCCESS".equals(code);
      onVaultCardResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL, null, null, vaultedCard);
    }

    public void onDeviceConnected(CloverDevice device) {
      Log.d(getClass().getSimpleName(), "Connected");
      cloverConnector.isReady = false;
      cloverConnector.broadcaster.notifyOnConnect();
    }

    public void onDeviceReady(CloverDevice device, DiscoveryResponseMessage drm) {
      Log.d(getClass().getSimpleName(), "Ready");
      cloverConnector.isReady = drm.ready;

      MerchantInfo merchantInfo = new MerchantInfo(drm);
      cloverConnector.merchantInfo = merchantInfo;
      device.setSupportsAcks(merchantInfo.deviceInfo.supportsAcks);

      if (drm.ready) {
        cloverConnector.device.doShowWelcomeScreen();
        cloverConnector.broadcaster.notifyOnReady(merchantInfo);
      } else {
        cloverConnector.broadcaster.notifyOnConnect();
      }
    }

    @Override public void onDeviceError(CloverDeviceErrorEvent errorEvent) {
      cloverConnector.broadcaster.notifyOnDeviceError(errorEvent);
    }

    @Override public void onPrintRefundPayment(Payment payment, Order order, Refund refund) {
      cloverConnector.broadcaster.notifyOnPrintRefundPaymentReceipt(new PrintRefundPaymentReceiptMessage(payment, order, refund));
    }

    @Override public void onPrintMerchantReceipt(Payment payment) {
      cloverConnector.broadcaster.notifyOnPrintPaymentMerchantCopyReceipt(new PrintPaymentMerchantCopyReceiptMessage(payment));
    }

    @Override public void onPrintPaymentDecline(Payment payment, String reason) {
      cloverConnector.broadcaster.notifyOnPrintPaymentDeclineReceipt(new PrintPaymentDeclineReceiptMessage(payment, reason));
    }

    @Override public void onPrintPayment(Payment payment, Order order) {
      cloverConnector.broadcaster.notifyOnPrintPaymentReceipt(new PrintPaymentReceiptMessage(payment, order));
    }

    @Override public void onPrintCredit(Credit credit) {
      cloverConnector.broadcaster.notifyOnPrintCreditReceipt(new PrintManualRefundReceiptMessage(credit));
    }

    @Override public void onPrintCreditDecline(Credit credit, String reason) {
      cloverConnector.broadcaster.notifyOnPrintCreditDeclineReceipt(new PrintManualRefundDeclineReceiptMessage(credit, reason));
    }

    public void onDeviceDisconnected(CloverDevice device) {
      Log.d(getClass().getSimpleName(), "Disconnected");
      cloverConnector.isReady = false;
      cloverConnector.broadcaster.notifyOnDisconnect();
    }
    @Override
    public void onMessageAck(String messageId) {
      // TODO: for future use
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


