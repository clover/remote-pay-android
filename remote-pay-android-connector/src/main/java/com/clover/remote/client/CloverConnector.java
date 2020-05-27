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

import com.clover.common2.Signature2;
import com.clover.common2.payments.PayIntent;
import com.clover.remote.CardData;
import com.clover.remote.Challenge;
import com.clover.remote.ErrorCode;
import com.clover.remote.ExternalDeviceState;
import com.clover.remote.ExternalDeviceStateData;
import com.clover.remote.InputOption;
import com.clover.remote.KeyPress;
import com.clover.remote.PendingPaymentEntry;
import com.clover.remote.QueryStatus;
import com.clover.remote.ResultStatus;
import com.clover.remote.TxStartResponseResult;
import com.clover.remote.TxState;
import com.clover.remote.UiState;
import com.clover.remote.client.device.CloverDevice;
import com.clover.remote.client.device.CloverDeviceFactory;
import com.clover.remote.client.device.CloverDeviceObserver;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.BaseTransactionRequest;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CheckBalanceRequest;
import com.clover.remote.client.messages.CheckBalanceResponse;
import com.clover.remote.client.messages.CloseoutRequest;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.IncrementPreauthRequest;
import com.clover.remote.client.messages.IncrementPreauthResponse;
import com.clover.remote.client.messages.SignatureRequest;
import com.clover.remote.client.messages.SignatureResponse;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.CustomActivityRequest;
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.CustomerProvidedDataEvent;
import com.clover.remote.client.messages.DataProviderConfig;
import com.clover.remote.client.messages.DisplayReceiptOptionsResponse;
import com.clover.remote.client.messages.InvalidStateTransitionResponse;
import com.clover.remote.client.messages.OpenCashDrawerRequest;
import com.clover.remote.client.messages.PrintJobStatusRequest;
import com.clover.remote.client.messages.PrintJobStatusResponse;
import com.clover.remote.client.messages.PrintRequest;
import com.clover.remote.client.messages.DisplayReceiptOptionsRequest;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.MessageFromActivity;
import com.clover.remote.client.messages.MessageToActivity;
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
import com.clover.remote.client.messages.RegisterForCustomerProvidedDataRequest;
import com.clover.remote.client.messages.ResetDeviceResponse;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.client.messages.RetrieveDeviceStatusRequest;
import com.clover.remote.client.messages.RetrieveDeviceStatusResponse;
import com.clover.remote.client.messages.RetrievePaymentRequest;
import com.clover.remote.client.messages.RetrievePaymentResponse;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.RetrievePrintersRequest;
import com.clover.remote.client.messages.RetrievePrintersResponse;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.SetCustomerInfoRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.TipRequest;
import com.clover.remote.client.messages.TipResponse;
import com.clover.remote.client.messages.TransactionRequest;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRefundRequest;
import com.clover.remote.client.messages.VoidPaymentRefundResponse;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.DiscoveryResponseMessage;
import com.clover.remote.message.TxStartRequestMessage;
import com.clover.remote.order.DisplayOrder;
import com.clover.remote.order.operation.OrderDeletedOperation;
import com.clover.sdk.v3.base.Reference;
import com.clover.sdk.v3.loyalty.LoyaltyDataConfig;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Authorization;
import com.clover.sdk.v3.payments.Batch;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;
import com.clover.sdk.v3.payments.Result;
import com.clover.sdk.v3.payments.TransactionSettings;
import com.clover.sdk.v3.payments.VaultedCard;
import com.clover.sdk.v3.printer.PrintJobStatus;
import com.clover.sdk.v3.printer.Printer;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides the default implementation of the {@link ICloverConnector} interface, connecting to the device specified
 * in the constructor.  This implementation supports the registration of one or more {@link ICloverConnectorListener}
 * interfaces that are notified asynchronously of events and responses from the underlying connected Clover device.
 */
public class CloverConnector implements ICloverConnector {

  private static final int KIOSK_CARD_ENTRY_METHODS = 1 << 15;
  public static final int CARD_ENTRY_METHOD_MAG_STRIPE = 0b0001 | 0b0001_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33026
  public static final int CARD_ENTRY_METHOD_ICC_CONTACT = 0b0010 | 0b0010_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33282
  public static final int CARD_ENTRY_METHOD_NFC_CONTACTLESS = 0b0100 | 0b0100_00000000 | KIOSK_CARD_ENTRY_METHODS; // 33796
  public static final int CARD_ENTRY_METHOD_MANUAL = 0b1000 | 0b1000_00000000 | KIOSK_CARD_ENTRY_METHODS; // 34824
  public static final int MAX_PAYLOAD_SIZE = 10000000; // maximum size of the payload of a full message.  if the payload exceeds this, the message will not be sent.

  public static final InputOption CANCEL_INPUT_OPTION = new InputOption(KeyPress.ESC, "Cancel");

  // This field maintains state for some deprecated logic and should be removed at some point in the future
  // when we are comfortable that there are no longer any backward compatibility issues
  @Deprecated
  private Object lastRequest;

  // manual is not enabled by default
  private final int cardEntryMethods = CARD_ENTRY_METHOD_MAG_STRIPE | CARD_ENTRY_METHOD_ICC_CONTACT | CARD_ENTRY_METHOD_NFC_CONTACTLESS;// | CARD_ENTRY_METHOD_MANUAL;

  private CloverDevice device;
  private final InnerDeviceObserver deviceObserver;

  private final CloverConnectorBroadcaster broadcaster = new CloverConnectorBroadcaster();

  private MerchantInfo merchantInfo;

  private CloverDeviceConfiguration configuration;

  boolean isReady = false;


  /**
   * CloverConnector constructor
   *
   * @param config A CloverDeviceConfiguration object containing the configuration for the device
   */
  public CloverConnector(CloverDeviceConfiguration config) {
    this.configuration = config;
    deviceObserver = new InnerDeviceObserver(this);
  }

  /**
   * Registers the provided listener to receive callbacks and events from the underlying device connection.  Note that
   * listeners MUST be registered prior to calling {@link #initializeConnection()} to guarantee notification of all
   * connection related callbacks.
   *
   * @param connectorListener The listener to register
   */
  public void addCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.add(connectorListener);
  }

  /**
   * Remove a previously added listener.  If the provided listener is not registered, this call has no effect.
   *
   * @param connectorListener The listener to remove
   */
  public void removeCloverConnectorListener(ICloverConnectorListener connectorListener) {
    broadcaster.remove(connectorListener);
  }

  @Override
  public void initializeConnection() {
    if (device == null) {
      device = CloverDeviceFactory.get(configuration);
      device.subscribe(deviceObserver);
    }

    device.initializeConnection();
  }

  @Override
  public void sale(SaleRequest request) {
    lastRequest = request;
    if (device == null || !isReady) {
      deviceObserver.onFinishCancelSale(ResultCode.ERROR, "Device Connection Error", "In sale: SaleRequest - The Clover device is not connected.");
    }
    else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In sale: SaleRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request, TxStartRequestMessage.SALE_REQUEST);
    }
    else {
      if (request.getTipAmount() == null) {
        request.setTipAmount(0L);
      }
      try {
        setupSaleRequest(request);
      } catch (Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        deviceObserver.onFinishCancel(ResultCode.ERROR, e.getMessage(), sw.toString(), TxStartRequestMessage.SALE_REQUEST);
      }

    }
  }

  private void setupPreauthRequest(PreAuthRequest request) {
    setupBaseTransactionRequest(request, new TransactionSettings(), new PayIntent.Builder(), TxStartRequestMessage.PREAUTH_REQUEST);
  }

  private void setupAuthRequest(AuthRequest request) {
    TransactionSettings transactionSettings = new TransactionSettings();
    transactionSettings.setTipMode(com.clover.sdk.v3.payments.TipMode.ON_PAPER); // overriding TipMode, since it's an Auth request
    setupTransactionRequest(request, transactionSettings, new PayIntent.Builder(), TxStartRequestMessage.AUTH_REQUEST);
  }

  private void setupSaleRequest(SaleRequest request) {
    TransactionSettings transactionSettings = new TransactionSettings();
    if(request.getTipMode() != null) {
      transactionSettings.setTipMode(com.clover.sdk.v3.payments.TipMode.valueOf(request.getTipMode().toString()));
    }
    PayIntent.Builder builder = new PayIntent.Builder();
    if(request.getTipAmount() != null) {
      builder.tipAmount(request.getTipAmount());
    }
    setupTransactionRequest(request, transactionSettings, builder, TxStartRequestMessage.SALE_REQUEST);
  }

  private void setupTransactionRequest(TransactionRequest request, TransactionSettings transactionSettings, PayIntent.Builder builder, String paymentRequestType) {
    lastRequest = request;
    if(request.getSignatureEntryLocation() != null) {
      transactionSettings.setSignatureEntryLocation(request.getSignatureEntryLocation());
    }
    if(request.getSignatureThreshold() != null) {
      transactionSettings.setSignatureThreshold(request.getSignatureThreshold());
    }
    if(request.getAutoAcceptSignature() != null) {
      transactionSettings.setAutoAcceptSignature(request.getAutoAcceptSignature());
    }
    if(request.getDisableCashback() != null) {
      transactionSettings.setDisableCashBack(request.getDisableCashback());
    }
    if(request.getAllowOfflinePayment() != null) {
      transactionSettings.setAllowOfflinePayment(request.getAllowOfflinePayment());
    }
    if(request.getForceOfflinePayment() != null) {
      transactionSettings.setForceOfflinePayment(request.getForceOfflinePayment());
    }
    if(request.getApproveOfflinePaymentWithoutPrompt() != null) {
      transactionSettings.setApproveOfflinePaymentWithoutPrompt(request.getApproveOfflinePaymentWithoutPrompt());
    }
    if (request.getTaxAmount() != null) {
      builder.taxAmount(request.getTaxAmount());
    }
    if(request.getTippableAmount() != null) {
      transactionSettings.setTippableAmount(request.getTippableAmount());
    }
    if(request.getTipSuggestions() != null){
      transactionSettings.setTipSuggestions(Arrays.asList(request.getTipSuggestions()));
    }
    setupBaseTransactionRequest(request, transactionSettings, builder, paymentRequestType);
  }


  private void setupBaseTransactionRequest(BaseTransactionRequest request, TransactionSettings transactionSettings, PayIntent.Builder builder, String paymentRequestType) {
    if (device != null && isReady) {
      builder.transactionType(request.getType());
      builder.amount(request.getAmount());
      builder.vaultedCard(request.getVaultedCard());
      builder.externalPaymentId(request.getExternalId() != null ? request.getExternalId().trim() : null);
      builder.requiresRemoteConfirmation(true);
      if (request.getCardNotPresent() != null) {
        builder.cardNotPresent(request.getCardNotPresent());
      }
      transactionSettings.setCardEntryMethods(request.getCardEntryMethods() != null ? request.getCardEntryMethods() : cardEntryMethods);
      if (request.getDisablePrinting() != null) {
        transactionSettings.setCloverShouldHandleReceipts(!request.getDisablePrinting());
      }
      if (request.getDisableRestartTransactionOnFail() != null) {
        transactionSettings.setDisableRestartTransactionOnFailure(request.getDisableRestartTransactionOnFail());
      }
      if(request.getDisableReceiptSelection() != null) {
        transactionSettings.setDisableReceiptSelection(request.getDisableReceiptSelection());
      }
      if (request.getDisableDuplicateChecking() != null) {
        transactionSettings.setDisableDuplicateCheck(request.getDisableDuplicateChecking());
      }
      if (request.getAutoAcceptPaymentConfirmations() != null) {
        transactionSettings.setAutoAcceptPaymentConfirmations(request.getAutoAcceptPaymentConfirmations());
      }
      if (request.getRegionalExtras() != null && !request.getRegionalExtras().isEmpty()) {
        transactionSettings.setRegionalExtras(request.getRegionalExtras());
      }
      if (request.getExtras() != null && !request.getExtras().isEmpty()) {
        builder.passThroughValues(request.getExtras());
      }
      if (request.getExternalReferenceId() != null) {
        builder.externalReferenceId(request.getExternalReferenceId());
      }
      builder.transactionSettings(transactionSettings);
      PayIntent payIntent = builder.build();

      device.doTxStart(payIntent, null, paymentRequestType); //
    }
  }

  @Override
  public void acceptSignature(VerifySignatureRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In acceptSignature: Device is not connected."));
    } else if (request == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In acceptSignature: VerifySignatureRequest cannot be null."));
    } else if (request.getPayment() == null || request.getPayment().getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In acceptSignature: VerifySignatureRequest. Payment must have anID."));
    } else {
      device.doSignatureVerified(request.getPayment(), true);
    }
  }

  @Override
  public void rejectSignature(VerifySignatureRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In rejectSignature: Device is not connected."));
    } else if (request == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In rejectSignature: VerifySignatureRequest cannot be null."));
    } else if (request.getPayment() == null || request.getPayment().getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In rejectSignature: VerifySignatureRequest.Payment must have an ID."));
    } else {
      device.doSignatureVerified(request.getPayment(), false);
    }
  }

  @Override
  public void acceptPayment(Payment payment) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In acceptPayment: Device is not connected."));
    } else if (payment == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In acceptPayment: Payment cannot be null."));
    } else if (payment.getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In acceptPayment: Payment must have an ID."));
    } else {
      device.doAcceptPayment(payment);
    }
  }

  @Override
  public void rejectPayment(Payment payment, Challenge challenge) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In rejectPayment: Device is not connected."));
    } else if (payment == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In rejectPayment: Payment cannot be null."));
    } else if (payment.getId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In rejectPayment: Payment must have an ID."));
    } else if (challenge == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In rejectPayment: Challenge cannot be null."));
    } else {
      device.doRejectPayment(payment, challenge);
    }
  }

  @Override
  public void auth(AuthRequest request) {
    lastRequest = request;
    if (device == null || !isReady) {
      deviceObserver.onFinishCancelAuth(ResultCode.ERROR, "Device connection Error", "In auth: Auth Request - The Clover device is not connected.");
    } else if (!merchantInfo.supportsAuths) {
      deviceObserver.onFinishCancelAuth(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In auth: AuthRequest - Auths are not enabled for the payment gateway. Original Request = " + request);
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In auth: AuthRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request, TxStartRequestMessage.AUTH_REQUEST);
    }
    else {
      try {
        setupAuthRequest(request);
      } catch (Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        deviceObserver.onFinishCancel(ResultCode.ERROR, e.getMessage(), sw.toString(), TxStartRequestMessage.AUTH_REQUEST);
      }
    }
  }

  @Override
  public void preAuth(PreAuthRequest request) {
    lastRequest = request;
    if (device == null || !isReady) {
      deviceObserver.onFinishCancelPreAuth(ResultCode.ERROR, "Device connection Error", "In preAuth: PreAuthRequest - The Clover device is not connected.");
    } else if (!merchantInfo.supportsPreAuths) {
      deviceObserver.onFinishCancelPreAuth(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In preAuth: PreAuthRequest - PreAuths are not enabled for the payment gateway. Original Request = " + request);
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In preAuth: PreAuthRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request, TxStartRequestMessage.PREAUTH_REQUEST);
    } else {
      try {
        setupPreauthRequest(request);
      } catch (Exception e) {
        lastRequest = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        deviceObserver.onFinishCancel(ResultCode.ERROR, e.getMessage(), sw.toString(), TxStartRequestMessage.PREAUTH_REQUEST);
      }

    }
  }

  @Override
  public void capturePreAuth(CapturePreAuthRequest request) {
    if (device == null || !isReady) {
      deviceObserver.onCapturePreAuth(ResultCode.ERROR, "Device connection Error", "In capturePreAuth: CapturePreAuth - The Clover device is not connected.");
    } else if (!merchantInfo.supportsPreAuths) {
      deviceObserver.onCapturePreAuth(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In capturePreAuth: PreAuth Captures are not enabled for the payment gateway. Original Request = " + request);
    } else {
      try {
        PayIntent.Builder builder = new PayIntent.Builder();
        TransactionSettings transactionSettings = new TransactionSettings();

        builder.paymentId(request.getPaymentId());
        builder.amount(request.getAmount());
        builder.tipAmount(request.getTipAmount() != null ? request.getTipAmount() : 0L);
        builder.requiresRemoteConfirmation(true);

        builder.transactionSettings(transactionSettings);
        builder.transactionType(PayIntent.TransactionType.CAPTURE_PREAUTH);
        PayIntent payIntent = builder.build();

        device.doCaptureAuth(request.getPaymentId(), request.getAmount(), request.getTipAmount());
      } catch (Exception e) {
        e.printStackTrace();
        CapturePreAuthResponse response = new CapturePreAuthResponse(false, ResultCode.UNSUPPORTED);
        response.setReason("Pre Auths unsupported");
        response.setMessage("The currently configured merchant gateway does not support Capture Auth requests.");
        broadcaster.notifyOnCapturePreAuth(response);
      }
    }

  }

  @Override
  public void incrementPreAuth(IncrementPreauthRequest request) {
    if (device == null || !isReady) {
      deviceObserver.onIncrementPreAuthResponse(ResultStatus.FAIL, "Device connection Error", "In incrementPreAuth: IncrementPreAuth - The Clover device is not connected.", null);
    } else if (!merchantInfo.supportsPreAuths) {
      deviceObserver.onIncrementPreAuthResponse(ResultStatus.FAIL, "Merchant Configuration Validation Error", "In incrementPreAuth: PreAuth support is not enabled for the payment gateway.", null);
    } else {
      device.doIncrementAuth(request.getPaymentId(), request.getAmount());
    }
  }

  @Override
  public void tipAdjustAuth(TipAdjustAuthRequest request) {
    if (device == null || !isReady) {
      deviceObserver.onAuthTipAdjusted(ResultCode.ERROR, "Device connection Error", "In tipAdjustAuth: TipAdjustAuthRequest - The Clover device is not connected.");
    } else if (!merchantInfo.supportsTipAdjust) {
      deviceObserver.onAuthTipAdjusted(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In tipAdjustAuth: TipAdjustAuthRequest - Tip Adjustments are not enabled for the payment gateway. Original Request = " + request);
    } else {
      device.doTipAdjustAuth(request.getOrderId(), request.getPaymentId(), request.getTipAmount());
    }
  }

  @Override
  public void vaultCard(Integer cardEntryMethods) {
    if (device == null || !isReady) {
      deviceObserver.onVaultCardResponse(false, ResultCode.ERROR, "Device connection Error", "In vaultCard: The Clover device is not connected.", null);
    } else if (!merchantInfo.supportsVaultCards) {
      deviceObserver.onVaultCardResponse(false, ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In vaultCard: VaultCard/Payment Tokens are not enabled for the payment gateway.", null);
    } else {
      device.doVaultCard(cardEntryMethods != null ? cardEntryMethods : getCardEntryMethods());
    }

  }

  @Override
  public void voidPayment(VoidPaymentRequest request) {
    if (device == null || !isReady) {
      deviceObserver.onPaymentVoided(ResultCode.ERROR, "Device connection Error", "In voidPayment: VoidPaymentRequest - The Clover device is not connected.");
    } else {
      Payment payment = new Payment();
      payment.setId(request.getPaymentId());
      payment.setOrder(new Reference());
      payment.getOrder().setId(request.getOrderId());
      payment.setEmployee(new Reference());
      payment.getEmployee().setId(request.getEmployeeId());
      VoidReason reason = request.getVoidReason() != null ? VoidReason.valueOf(request.getVoidReason()) : null;
      device.doVoidPayment(payment, reason, request.getDisablePrinting(), request.getDisableReceiptSelection(), request.getExtras());
    }

  }

  @Override
  public void refundPayment(RefundPaymentRequest request) {
    if (device == null || !isReady) {
      RefundPaymentResponse prr = new RefundPaymentResponse(false, ResultCode.ERROR);
      prr.setRefund(null);
      prr.setReason("Device Connection Error");
      prr.setMessage("In refundPayment: RefundPaymentRequest - The Clover device is not connected.");
      deviceObserver.lastPRR = prr;
      deviceObserver.onFinishCancel(TxStartRequestMessage.REFUND_REQUEST);
    }  else {
      device.doPaymentRefund(request.getOrderId(), request.getPaymentId(), request.getAmount(), request.isFullRefund(), request.getDisablePrinting(), request.getDisableReceiptSelection(), request.getExtras());
    }
  }

  @Override
  public void voidPaymentRefund(VoidPaymentRefundRequest request) {
//    if (device == null || !isReady) {
//      Refund refund = new Refund();
//      refund.setId(request.getRefundId());
//      deviceObserver.onPaymentRefundVoided(refund, ResultCode.ERROR, "Device connection Error", "In voidPaymentRefund: VoidPaymentRefundRequest - The Clover device is not connected.");
//    } else {
//      device.doVoidPaymentRefund(request.getOrderId(), request.getRefundId(), request.getDisablePrinting(), request.getDisableReceiptSelection(), request.getExtras());
//    }
      deviceObserver.onPaymentRefundVoided(null, ResultCode.ERROR, "Cannot do Void Payment Refund", "This version of the SDK does not support voidPaymentRefunds");
  }

  @Override
  public void manualRefund(ManualRefundRequest request) // NakedRefund is a Transaction, with just negative amount
  {
    TransactionSettings transactionSettings = new TransactionSettings();
    lastRequest = request;
    if (device == null || !isReady) {
      deviceObserver.onFinishCancelManualRefund(ResultCode.ERROR, "Device connection Error", "In manualRefund: ManualRefundRequest - The Clover device is not connected.");
    } else if (!merchantInfo.supportsManualRefunds) {
      deviceObserver.onFinishCancelManualRefund(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In manualRefund: ManualRefundRequest - Manual Refunds are not enabled for the payment gateway. Original Request = " + request);
    } else if (request.getVaultedCard() != null && !merchantInfo.supportsVaultCards) {
      deviceObserver.onFinishCancel(ResultCode.UNSUPPORTED, "Merchant Configuration Validation Error", "In manualRefund: ManualRefundRequest - Vault Card support is not enabled for the payment gateway. Original Request = " + request, TxStartRequestMessage.CREDIT_REQUEST);
    } else {
      PayIntent.Builder builder = new PayIntent.Builder();
      builder.amount(-Math.abs(request.getAmount()))
          .transactionType(PayIntent.TransactionType.CREDIT)
          .vaultedCard(request.getVaultedCard())
          .externalPaymentId(request.getExternalId());

      transactionSettings.setCardEntryMethods(request.getCardEntryMethods() != null ? request.getCardEntryMethods() : cardEntryMethods);
      if (request.getDisablePrinting() != null) {
        transactionSettings.setCloverShouldHandleReceipts(!request.getDisablePrinting());
      }
      if (request.getDisableRestartTransactionOnFail() != null) {
        transactionSettings.setDisableRestartTransactionOnFailure(request.getDisableRestartTransactionOnFail());
      }
      if (request.getDisableReceiptSelection() != null) {
        transactionSettings.setDisableReceiptSelection(request.getDisableReceiptSelection());
      }
      if(request.getExtras() != null && !request.getExtras().isEmpty()) {
        builder.passThroughValues(request.getExtras());
      }
      builder.transactionSettings(transactionSettings);
      PayIntent payIntent = builder.build();
      device.doTxStart(payIntent, null, TxStartRequestMessage.CREDIT_REQUEST);
    }

  }

  @Override
  public void retrievePendingPayments() {
    if (device == null || !isReady) {
      deviceObserver.onPendingPaymentsResponse(ResultCode.ERROR, "Device connection Error", "In retrievePendingPayments: The Clover device is not connected.");
    } else {
      device.doRetrievePendingPayments();
    }
  }

  @Override
  public void readCardData(ReadCardDataRequest request) {
    if (device == null || !isReady) {
      deviceObserver.onFinishCancelReadCardData(ResultCode.ERROR, "Device connection Error", "In readCardData: The Clover device is not connected.");
    } else if (request == null) {
      deviceObserver.onFinishCancelReadCardData(ResultCode.FAIL, "Invalid Argument.", "In readCardData: ReadCardDataRequest - The request that was passed in for processing is null.");
    } else {
      TransactionSettings transactionSettings = new TransactionSettings();
      transactionSettings.setCardEntryMethods(request.getCardEntryMethods() != null ? request.getCardEntryMethods() : cardEntryMethods);
      transactionSettings.setForcePinEntryOnSwipe(request.isForceSwipePinEntry());
      // create pay intent...
      PayIntent.Builder builder = new PayIntent.Builder();
      builder.transactionType(PayIntent.TransactionType.DATA);
      builder.transactionSettings(transactionSettings);
      PayIntent pi = builder.build();
      device.doReadCardData(pi);
    }
  }

  @Override
  public void sendMessageToActivity(MessageToActivity request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In sendMessageToActivity: The Clover device is not connected."));
    } else if (request == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In sendMessageToActivity: Invalid argument. Null is not allowed."));
    } else {
      device.doSendMessageToActivity(request.getAction(), request.getPayload());
    }
  }

  @Override
  public void closeout(CloseoutRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In closeout: CloseoutRequest - The Clover device is not connected."));
    } else if (request == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In closeout: Invalid argument. Null is not allowed."));
    } else {
      device.doCloseout(request.isAllowOpenTabs(), request.getBatchId());
    }
  }





  @Override
  public void print(PrintRequest request) {
    if(device != null){
      if(!isReady){
        broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR,0, null, "Print: The Clover Device is not ready"));
      }
      else{
        device.doPrint(request.getImages(), request.getImageURLs(), request.getText(), request.getPrintRequestId(), request.getPrintDeviceId());
      }
    }
    else{
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR,0, null, "Print: The Clover Device is null"));
    }
  }


  @Override
  public void retrievePrinters(RetrievePrintersRequest request) {
    if(device != null){
      if(!isReady){
        broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR,0, null, "RetrievePrinters: The Clover Device is not ready"));
      }
      else{
        device.doRetrievePrinters(request.getCategory());
      }
    }
    else{
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR,0, null, "RetrievePrinters: The Clover Device is null"));
    }
  }

  @Override
  public void retrievePrintJobStatus(PrintJobStatusRequest request) {
    if(device != null){
      if(!isReady){
        broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR,0, null, "RetrievePrintJobStatus: The Clover Device is not ready"));
      }
      else{
        device.doRetrievePrintJobStatus(request.getPrintRequestId());
      }
    }
    else{
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR,0, null, "RetrievePrintJobStatus: The Clover Device is null"));
    }
  }

  @Override
  public void openCashDrawer(OpenCashDrawerRequest request) {
    if(device != null){
      if(!isReady){
        broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR,0, null, "OpenCashDrawer: The Clover Device is not ready"));
      }
      else{
        device.doOpenCashDrawer(request.getReason(), request.getDeviceId());
      }
    }
    else{
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR,0, null, "OpenCashDrawer: The Clover Device is null"));
    }
  }

    @Override
  public void showMessage(String message) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In showMessage: The Clover device is not connected."));
    } else if (message == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In showMessage: Invalid argument.  Null is not allowed."));
    } else {
      device.doTerminalMessage(message);
    }
  }

  @Override
  public void sendDebugLog(String message) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In sendCloverDeviceLog: The Clover device is not connected."));
    } else if (message == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In sendCloverDeviceLog: Invalid argument.  Null is not allowed."));
    } else {
      device.doSendDebugLog(message);
    }
  }

  @Override
  public void showWelcomeScreen() {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In showWelcomeScreen: The Clover device is not connected."));
    } else {
      device.doShowWelcomeScreen();
    }
  }

  @Override
  public void showThankYouScreen() {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In showThankYouScreen: The Clover device is not connected."));
    } else {
      device.doShowThankYouScreen();
    }
  }

  /**
   * Display the payment receipt screen for the orderId/paymentId combination
   * in the DisplayReceiptOptionsRequest object.
   *
   * @param request The request details
   */
  @Deprecated
  @Override
  public void displayPaymentReceiptOptions(DisplayReceiptOptionsRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In displayPaymentReceiptOptions: The Clover device is not connected."));
    } else if (request == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In displayPaymentReceiptOptions: Invalid argument.  The request object cannot be null."));
    } else if (request.getOrderId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In displayPaymentReceiptOptions: Invalid argument.  The orderId cannot be null."));
    } else if (request.getPaymentId() == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In displayPaymentReceiptOptions: Invalid argument.  The paymentId cannot be null."));
    } else {
      device.doShowPaymentReceiptScreen(request.getOrderId(), request.getPaymentId(), request.getDisablePrinting());
    }
  }

  @Override
  public void displayReceiptOptions(DisplayReceiptOptionsRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In displayReceiptOptions: The Clover device is not connected."));
    } else {
      device.doShowReceiptScreen(request.getOrderId(), request.getPaymentId(), request.getRefundId(), request.getCreditId(), request.getDisablePrinting());
    }
  }

  @Override
  public void showDisplayOrder(DisplayOrder order) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null,  "In showDisplayOrder: The Clover device is not connected."));
    } else if (order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In showDisplayOrder: Invalid argument.  The order cannot be null."));
    } else {
      device.doOrderUpdate(order, null);
    }
  }

  @Override
  public void removeDisplayOrder(DisplayOrder order) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In removeDisplayOrder: The Clover device is not connected."));
    } else if (order == null) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.VALIDATION_ERROR, 0, null, "In removeDisplayOrder: Invalid argument.  The order cannot be null."));
    } else {
      OrderDeletedOperation dao = new OrderDeletedOperation();
      dao.setId(order.getId());
      device.doOrderUpdate(order, dao);
    }
  }

  @Override
  public void dispose() {
    broadcaster.clear();
    if (device != null) {
      device.dispose();
      device = null;
    }
  }

  @Override
  public void invokeInputOption(InputOption io) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In invokeInputOption: The Clover device is not connected."));
    } else {
      device.doKeyPress(io.keyPress);
    }
  }

  @Override
  public void resetDevice() {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In resetDevice: The Clover device is not connected."));
    } else {
      device.doResetDevice();
    }
  }

  @Override
  public void retrieveDeviceStatus(RetrieveDeviceStatusRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In retrieveDeviceStatus: The Clover device is not connected."));
    } else {
      device.doRetrieveDeviceStatus(request.isSendLastMessage());
    }
  }

  @Override
  public void retrievePayment(RetrievePaymentRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In retrievePayment: The Clover device is not connected."));
    } else {
      device.doRetrievePayment(request.getExternalPaymentId());
    }
  }

  @Override
  public void registerForCustomerProvidedData(RegisterForCustomerProvidedDataRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In registerForCustomerProvidedData: The Clover device is not connected."));
    } else {
      List<DataProviderConfig> dataProviderConfigs = request.getConfigurations();
      ArrayList<LoyaltyDataConfig> configurations = convertConfiguration(dataProviderConfigs);
      device.doRegisterForCustomerProvidedData(configurations);
    }
  }

  private ArrayList<LoyaltyDataConfig> convertConfiguration(List<DataProviderConfig> dataProviderConfigs) {
    // this is here so we have a buffer between the public api and the private one.
    ArrayList<LoyaltyDataConfig> configs = null;
    if (dataProviderConfigs!= null) {
      configs = new ArrayList<>(dataProviderConfigs.size());
      for (DataProviderConfig dataProviderConfig : dataProviderConfigs) {
        configs.add(convertConfiguration(dataProviderConfig));
      }
    }
    return configs;
  }

  private LoyaltyDataConfig convertConfiguration(DataProviderConfig dataProviderConfig) {
    LoyaltyDataConfig loyaltyDataConfig = new LoyaltyDataConfig();
    loyaltyDataConfig.setType(dataProviderConfig.getType());
    loyaltyDataConfig.setConfiguration(dataProviderConfig.getConfiguration());
    return loyaltyDataConfig;
  }

  @Override
  public void setCustomerInfo(SetCustomerInfoRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In registerForCustomerProvidedData: The Clover device is not connected."));
    } else {
      device.doSetCustomerInfo(request.getCustomerInfo());
    }
  }

  private int getCardEntryMethods() {
    return cardEntryMethods;
  }

  @Override
  public void startCustomActivity(CustomActivityRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In startCustomActivity: The Clover device is not connected."));
    } else {
      device.doStartActivity(request.getAction(), request.getPayload(), request.isNonBlocking());
    }
  }

  @Override
  public void checkBalance(CheckBalanceRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In checkBalance: The Clover device is not connected."));
    } else {
      device.doCheckBalance(request.cardEntryMethods != null ? request.cardEntryMethods : cardEntryMethods);
    }
  }

  @Override
  public void requestSignature(SignatureRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In collectSignature: The Clover device is not connected."));
    } else {
      device.doCollectSignature(request.acknowledgementMessage);
    }
  }

  @Override
  public void requestTip(TipRequest request) {
    if (device == null || !isReady) {
      broadcaster.notifyOnDeviceError(new CloverDeviceErrorEvent(CloverDeviceErrorEvent.CloverDeviceErrorType.COMMUNICATION_ERROR, 0, null, "In collectSignature: The Clover device is not connected."));
    } else {
      device.doRequestTip(request.getTippableAmount(), request.getTipSuggestions());
    }
  }

  private static class InnerDeviceObserver implements CloverDeviceObserver {

    private RefundPaymentResponse lastPRR;
    private final CloverConnector cloverConnector;

    private InnerDeviceObserver(CloverConnector cc) {
      this.cloverConnector = cc;
    }

    @Override
    public void onTxState(TxState txState) {
    }

    @Override
    public void onTxStartResponse(TxStartResponseResult result, String externalId, String messageInfo, String message, String reason) {
      boolean success = result.equals(TxStartResponseResult.SUCCESS);
      if (success) {
        return;
      }
      boolean duplicate = result.equals(TxStartResponseResult.DUPLICATE);
      try {
        // Use the messageInfo if it exists, to determine the request type
        if (messageInfo != null) {
          if (messageInfo.equals(TxStartRequestMessage.PREAUTH_REQUEST)) {
            PreAuthResponse response = new PreAuthResponse(false, ResultCode.FAIL);
            if (duplicate) {
              response.setResult(ResultCode.CANCEL);
              response.setReason(reason);
              response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
            } else {
              response.setResult(ResultCode.FAIL);
              response.setReason(reason);
              response.setMessage(message);
            }
            cloverConnector.broadcaster.notifyOnPreAuthResponse(response);
          } else if (messageInfo.equals(TxStartRequestMessage.AUTH_REQUEST)) {
            AuthResponse response = new AuthResponse(false, ResultCode.FAIL);
            if (duplicate) {
              response.setResult(ResultCode.CANCEL);
              response.setReason(reason);
              response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
            } else {
              response.setResult(ResultCode.FAIL);
              response.setReason(reason);
              response.setMessage(message);
            }
            cloverConnector.broadcaster.notifyOnAuthResponse(response);
          } else if (messageInfo.equals(TxStartRequestMessage.SALE_REQUEST)) {
            SaleResponse response = new SaleResponse(false, ResultCode.FAIL);
            if (duplicate) {
              response.setResult(ResultCode.CANCEL);
              response.setReason(reason);
              response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
            } else {
              response.setResult(ResultCode.FAIL);
              response.setReason(reason);
              response.setMessage(message);
            }
            cloverConnector.broadcaster.notifyOnSaleResponse(response);
          } else if (messageInfo.equals(TxStartRequestMessage.CREDIT_REQUEST)) {
            ManualRefundResponse response = new ManualRefundResponse(false, ResultCode.FAIL);
            if (duplicate) {
              response.setResult(ResultCode.CANCEL);
              response.setReason(reason);
              response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
            } else {
              response.setResult(ResultCode.FAIL);
              response.setReason(reason);
              response.setMessage(message);
            }
            cloverConnector.broadcaster.notifyOnManualRefundResponse(response);
          }
        } else {
          // This is deprecated logic and should be removed at some point in the future
          // when we are comfortable that there are no longer any backward compatibility issues
          if (cloverConnector.lastRequest instanceof PreAuthRequest) {
            PreAuthResponse response = new PreAuthResponse(false, ResultCode.FAIL);
            if (duplicate) {
              response.setResult(ResultCode.CANCEL);
              response.setReason(reason);
              response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
            } else {
              response.setResult(ResultCode.FAIL);
              response.setReason(reason);
              response.setMessage(message);
            }
            cloverConnector.broadcaster.notifyOnPreAuthResponse(response);
          } else if (cloverConnector.lastRequest instanceof AuthRequest) {
            AuthResponse response = new AuthResponse(false, ResultCode.FAIL);
            if (duplicate) {
              response.setResult(ResultCode.CANCEL);
              response.setReason(reason);
              response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
            } else {
              response.setResult(ResultCode.FAIL);
              response.setReason(reason);
              response.setMessage(message);
            }
            cloverConnector.broadcaster.notifyOnAuthResponse(response);
          } else if (cloverConnector.lastRequest instanceof SaleRequest) {
            SaleResponse response = new SaleResponse(false, ResultCode.FAIL);
            if (duplicate) {
              response.setResult(ResultCode.CANCEL);
              response.setReason(reason);
              response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
            } else {
              response.setResult(ResultCode.FAIL);
              response.setReason(reason);
              response.setMessage(message);
            }
            cloverConnector.broadcaster.notifyOnSaleResponse(response);
          } else if (cloverConnector.lastRequest instanceof ManualRefundRequest) {
            ManualRefundResponse response = new ManualRefundResponse(false, ResultCode.FAIL);
            if (duplicate) {
              response.setResult(ResultCode.CANCEL);
              response.setReason(reason);
              response.setMessage("The provided transaction id of " + externalId + " has already been processed and cannot be resubmitted.");
            } else {
              response.setResult(ResultCode.FAIL);
              response.setReason(reason);
              response.setMessage(message);
            }
            cloverConnector.broadcaster.notifyOnManualRefundResponse(response);
          }
        }
      } finally {
        cloverConnector.lastRequest = null;
      }
    }

    private void onPendingPaymentsResponse(ResultCode code, String category, String message) {
      RetrievePendingPaymentsResponse rppr = new RetrievePendingPaymentsResponse(code, category, Collections.<PendingPaymentEntry>emptyList());
      rppr.setMessage(message);
      cloverConnector.broadcaster.notifyOnRetrievePendingPaymentResponse(rppr);
    }

    @Override
    public void onPendingPaymentsResponse(boolean success, List<PendingPaymentEntry> payments) {
      RetrievePendingPaymentsResponse rppr = new RetrievePendingPaymentsResponse(success ? ResultCode.SUCCESS : ResultCode.FAIL, "", payments);
      cloverConnector.broadcaster.notifyOnRetrievePendingPaymentResponse(rppr);
    }

    @Override
    public void onReadCardResponse(ResultStatus status, String reason, CardData cardData) {
      boolean success = status == ResultStatus.SUCCESS;
      ResultCode code = success ? ResultCode.SUCCESS : (status == ResultStatus.CANCEL ? ResultCode.CANCEL : ResultCode.FAIL);
      ReadCardDataResponse rcdr = new ReadCardDataResponse(success, code);
      rcdr.setReason(reason);
      if (success) {
        rcdr.setCardData(cardData);
      }
      cloverConnector.showWelcomeScreen();
      cloverConnector.broadcaster.notifyOnReadCardDataResponse(rcdr);
    }

    @Override
    public void onMessageFromActivity(String actionId, String payload) {
      MessageFromActivity messageFromActivity = new MessageFromActivity(actionId, payload);
      cloverConnector.broadcaster.notifyOnActivityMessage(messageFromActivity);
    }

    @Override
    public void onActivityResponse(ResultStatus status, String payload, String failReason, String actionId) {
      boolean success = status == ResultStatus.SUCCESS;
      CustomActivityResponse car = new CustomActivityResponse(success, success ? ResultCode.SUCCESS : ResultCode.CANCEL, payload, failReason, actionId);
      cloverConnector.broadcaster.notifyOnActivityResponse(car);
    }

    @Override
    public void onDeviceStatusResponse(ResultCode result, String reason, ExternalDeviceState state, ExternalDeviceStateData data) {
      boolean success = result == ResultCode.SUCCESS;
      RetrieveDeviceStatusResponse rdsr = new RetrieveDeviceStatusResponse(success, success ? ResultCode.SUCCESS : ResultCode.CANCEL, state, data);
      cloverConnector.broadcaster.notifyOnRetrieveDeviceStatusResponse(rdsr);
    }

    @Override
    public void onInvalidStateTransitionResponse(ResultCode result, String reason, String requestedTransition, ExternalDeviceState state, ExternalDeviceStateData data) {
      boolean success = result == ResultCode.SUCCESS;
      InvalidStateTransitionResponse response = new InvalidStateTransitionResponse(success, success ? ResultCode.SUCCESS : ResultCode.CANCEL, reason, requestedTransition, state, data);
      cloverConnector.broadcaster.notifyOnInvalidStateTransitionResponse(response);
    }

    @Override
    public void onResetDeviceResponse(ResultCode result, String reason, ExternalDeviceState state) {
      boolean success = result == ResultCode.SUCCESS;
      ResetDeviceResponse rdr = new ResetDeviceResponse(success, success ? ResultCode.SUCCESS : ResultCode.CANCEL, state);
      cloverConnector.broadcaster.notifyOnResetDeviceResponse(rdr);
    }

    @Override
    public void onRetrievePaymentResponse(ResultCode result, String reason, String externalPaymentId, QueryStatus queryStatus, Payment payment, String message) {
      RetrievePaymentResponse gpr = new RetrievePaymentResponse(result, reason, message, externalPaymentId, queryStatus, payment);
      cloverConnector.broadcaster.notifyOnRetrievePaymentResponse(gpr);
    }

    @Override
    public void onRetrievePrinterResponse(List<Printer> printers) {
      RetrievePrintersResponse response = new RetrievePrintersResponse(printers);
      response.setResult(printers != null ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setSuccess(response.getResult() == ResultCode.SUCCESS);
      cloverConnector.broadcaster.notifyOnRetrievePrinters(response);
    }

    @Override
    public void onPartialAuth(long partialAmount) {
      //TODO: For future use
    }

    @Override
    public void onTipAdded(long tip) {
      cloverConnector.broadcaster.notifyOnTipAdded(tip);
    }

    @Override
    public void onAuthTipAdjusted(String paymentId, long amount, boolean success, String message, String reason) {
      TipAdjustAuthResponse response = new TipAdjustAuthResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setPaymentId(paymentId);
      response.setTipAmount(amount);
      if (!success) {
        response.setReason(reason != null && reason.length() > 0 ? reason : "Failure");
        response.setMessage(message);
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

    @Override
    public void onCashbackSelected(long cashbackAmount) {
      //TODO: For future use
    }

    @Override
    public void onKeyPressed(KeyPress keyPress) {
      //TODO: For future use
    }

    @Override
    public void onPaymentRefundResponse(String orderId, String paymentId, Refund refund, TxState code, ErrorCode reason, String message) {
      // hold the response for finishOk for the refund. See comments in onFinishOk(Refund)
      boolean success = code == TxState.SUCCESS;
      RefundPaymentResponse prr = new RefundPaymentResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      prr.setOrderId(orderId);
      prr.setPaymentId(paymentId);
      prr.setRefund(refund);
      if(reason != null) {
        prr.setReason(reason.toString());
      }
      prr.setMessage(message);
      lastPRR = prr; // set this so we have the appropriate information for when onFinish(Refund) is called
    }

    @Override
    public void onPaymentRefundVoidResponse(String refundId, ResultStatus status, String reason, String message) {
      boolean success = status == ResultStatus.SUCCESS;
      VoidPaymentRefundResponse vprr = new VoidPaymentRefundResponse(success, success ? ResultCode.SUCCESS : ResultCode.CANCEL);
      vprr.setRefundId(refundId);
      vprr.setReason(reason);
      vprr.setMessage(message);
      cloverConnector.broadcaster.notifyOnVoidPaymentRefundResponse(vprr);
    }

    @Override
    public void onCloseoutResponse(ResultStatus status, String reason, Batch batch) {
      CloseoutResponse cr = new CloseoutResponse(status == ResultStatus.SUCCESS, status == ResultStatus.SUCCESS ? ResultCode.SUCCESS : ResultCode.FAIL);
      cr.setReason(reason);
      cr.setBatch(batch);
      cloverConnector.broadcaster.notifyCloseout(cr);
    }

    @Override
    public void onUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption[] inputOptions) {
      CloverDeviceEvent.DeviceEventState eventState;
      try {
        eventState = CloverDeviceEvent.DeviceEventState.valueOf(uiState.toString());
      } catch (IllegalArgumentException | NullPointerException ex) {
        Log.w(getClass().getSimpleName(), "Unsupported UI event type: " + uiState);
        return;
      }

      CloverDeviceEvent deviceEvent = new CloverDeviceEvent();
      deviceEvent.setInputOptions(inputOptions);
      deviceEvent.setEventState(eventState);
      deviceEvent.setMessage(uiText);
      if (uiDirection == UiState.UiDirection.ENTER) {
        cloverConnector.broadcaster.notifyOnDeviceActivityStart(deviceEvent);
      } else if (uiDirection == UiState.UiDirection.EXIT) {
        cloverConnector.broadcaster.notifyOnDeviceActivityEnd(deviceEvent);
        if (eventState == CloverDeviceEvent.DeviceEventState.RECEIPT_OPTIONS) {
          cloverConnector.showWelcomeScreen();
        }
      }
    }

    @Override
    public void onFinishOk(Payment payment, Signature2 signature2, String messageInfo) {
      cloverConnector.showThankYouScreen(); //need to do this first, so Listener implementation can replace the screen as desired
      Object lastReq = cloverConnector.lastRequest;
      cloverConnector.lastRequest = null;
      // Use messageInfo if it exists, to determine the original requested type
      if (messageInfo != null) {
        if (messageInfo.equals(TxStartRequestMessage.PREAUTH_REQUEST)) {
          PreAuthResponse response = new PreAuthResponse(true, ResultCode.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnPreAuthResponse(response);
        } else if (messageInfo.equals(TxStartRequestMessage.AUTH_REQUEST)) {
          AuthResponse response = new AuthResponse(true, ResultCode.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnAuthResponse(response);
        } else if (messageInfo.equals(TxStartRequestMessage.SALE_REQUEST)) {
          SaleResponse response = new SaleResponse(true, ResultCode.SUCCESS);
          response.setPayment(payment);
          response.setSignature(signature2);
          cloverConnector.broadcaster.notifyOnSaleResponse(response);
        } else {
          Log.e(getClass().getSimpleName(), String.format("Failed to pair this response: %s", payment) + ".  Request Type: " + messageInfo);
        }
      } else {  // This is deprecated logic and should be removed at some point in the future
        // when we are comfortable that there are no longer any backward compatibility issues
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
      }
    }

    @Override
    public void onFinishOk(Credit credit) {
      cloverConnector.showWelcomeScreen();
      cloverConnector.lastRequest = null;
      ManualRefundResponse response = new ManualRefundResponse(true, ResultCode.SUCCESS);
      response.setCredit(credit);
      cloverConnector.broadcaster.notifyOnManualRefundResponse(response);
    }

    @Override
    public void onFinishOk(Refund refund) {
      cloverConnector.showWelcomeScreen();
      cloverConnector.lastRequest = null;
      RefundPaymentResponse lastRefundResponse = lastPRR;
      lastPRR = null;
      // Since finishOk is the more appropriate/consistent location in the "flow" to
      // publish the RefundResponse (like SaleResponse, AuthResponse, etc., rather
      // than after the server call, which calls onPaymetRefund),
      // we will hold on to the response from
      // onRefundResponse (Which has more information than just the refund) and publish it here
      if(refund.getOrderRef() != null) {
        RefundPaymentResponse response = new RefundPaymentResponse(true, ResultCode.SUCCESS);
        response.setOrderId(refund.getOrderRef().getId());
        response.setPaymentId(refund.getPayment().getId());
        response.setRefund(refund);
        cloverConnector.broadcaster.notifyOnRefundPaymentResponse(response);
      } else {
        if (lastRefundResponse != null && lastRefundResponse.getRefund().getId().equals(refund.getId())) {
          cloverConnector.broadcaster.notifyOnRefundPaymentResponse(lastRefundResponse);
        } else {
          RefundPaymentResponse response = new RefundPaymentResponse(true, ResultCode.SUCCESS);
          response.setPaymentId(refund.getPayment().getId());
          response.setRefund(refund);
          cloverConnector.broadcaster.notifyOnRefundPaymentResponse(response);
        }
      }
    }

    private void onFinishCancel(ResultCode result, String reason, String message, String messageInfo) {
      cloverConnector.showWelcomeScreen();
      Object lastReq = cloverConnector.lastRequest;
      cloverConnector.lastRequest = null;
      // Use messageInfo if it exists, to determine the original requested type
      if (messageInfo != null) {
        if (messageInfo.equals(TxStartRequestMessage.PREAUTH_REQUEST)) {
          onFinishCancelPreAuth(result, reason, message);
        } else if (messageInfo.equals(TxStartRequestMessage.AUTH_REQUEST)) {
          onFinishCancelAuth(result, reason, message);
        } else if (messageInfo.equals(TxStartRequestMessage.SALE_REQUEST)) {
          onFinishCancelSale(result, reason, message);
        } else if (messageInfo.equals(TxStartRequestMessage.CREDIT_REQUEST)) {
          onFinishCancelManualRefund(result, reason, message);
        } else if (messageInfo.equals(TxStartRequestMessage.REFUND_REQUEST)) {
          cloverConnector.broadcaster.notifyOnRefundPaymentResponse(lastPRR);
          lastPRR = null;
        }
      } else {
        // This is deprecated logic and should be removed at some point in the future
        // when we are comfortable that there are no longer any backward compatibility issues
        if (lastReq instanceof PreAuthRequest) {
          onFinishCancelPreAuth(result, reason, message);
        } else if (lastReq instanceof SaleRequest) {
          onFinishCancelSale(result, reason, message);
        } else if (lastReq instanceof AuthRequest) {
          onFinishCancelAuth(result, reason, message);
        } else if (lastReq instanceof ManualRefundRequest) {
          onFinishCancelManualRefund(result, reason, message);
        } else if (lastPRR != null) {
          cloverConnector.broadcaster.notifyOnRefundPaymentResponse(lastPRR);
          lastPRR = null;
        }
      }
    }

    @Override
    public void onFinishCancel(String messageInfo) {
      onFinishCancel(ResultCode.CANCEL, null, null, messageInfo);
    }

    private void onFinishCancelSale(ResultCode result, String reason, String message) {
      SaleResponse saleResponse = new SaleResponse(false, result != null ? result : ResultCode.CANCEL);
      saleResponse.setReason(reason != null ? reason : "Request Canceled");
      saleResponse.setMessage(message != null ? message : "The Sale Request was canceled.");
      saleResponse.setPayment(null);
      cloverConnector.broadcaster.notifyOnSaleResponse(saleResponse);
    }

    private void onFinishCancelPreAuth(ResultCode result, String reason, String message) {
      PreAuthResponse preAuthResponse = new PreAuthResponse(false, result != null ? result : ResultCode.CANCEL);
      preAuthResponse.setReason(reason != null ? reason : "Request Canceled");
      preAuthResponse.setMessage(message != null ? message : "The PreAuth Request was canceled.");
      preAuthResponse.setPayment(null);
      cloverConnector.broadcaster.notifyOnPreAuthResponse(preAuthResponse);
    }

    private void onFinishCancelAuth(ResultCode result, String reason, String message) {
      AuthResponse authResponse = new AuthResponse(false, result != null ? result : ResultCode.CANCEL);
      authResponse.setReason(reason != null ? reason : "Request Canceled");
      authResponse.setMessage(message != null ? message : "The Auth Request was canceled.");
      authResponse.setPayment(null);
      cloverConnector.broadcaster.notifyOnAuthResponse(authResponse);
    }

    private void onFinishCancelManualRefund(ResultCode result, String reason, String message) {
      ManualRefundResponse manualRefundResponse = new ManualRefundResponse(false, result != null ? result : ResultCode.CANCEL);
      manualRefundResponse.setReason(reason != null ? reason : "Request Canceled");
      manualRefundResponse.setMessage(message != null ? message : "The Manual Refund Request was canceled.");
      manualRefundResponse.setCredit(null);
      cloverConnector.broadcaster.notifyOnManualRefundResponse(manualRefundResponse);
    }

    private void onFinishCancelReadCardData(ResultCode result, String reason, String message) {
      cloverConnector.showWelcomeScreen();
      ReadCardDataResponse readCardDataResponse = new ReadCardDataResponse(false, result != null ? result : ResultCode.CANCEL);
      readCardDataResponse.setReason(reason != null ? reason : "Request Canceled");
      readCardDataResponse.setMessage(message != null ? message : "The Read Card Data Request was canceled.");
      readCardDataResponse.setCardData(null);
      cloverConnector.broadcaster.notifyOnReadCardDataResponse(readCardDataResponse);
    }

    @Override
    public void onVerifySignature(Payment payment, Signature2 signature) {
      VerifySignatureRequest request = new VerifySignatureRequest();
      request.setSignature(signature);
      request.setPayment(payment);
      cloverConnector.broadcaster.notifyOnVerifySignatureRequest(request);
    }

    @Override
    public void onConfirmPayment(Payment payment, Challenge[] challenges) {
      ConfirmPaymentRequest cpr = new ConfirmPaymentRequest();
      cpr.setPayment(payment);
      cpr.setChallenges(challenges);
      cloverConnector.broadcaster.notifyOnConfirmPaymentRequest(cpr);
    }

    @Override
    public void onPaymentVoided(Payment payment, VoidReason voidReason, ResultStatus result, String reason, String message) {
      cloverConnector.showWelcomeScreen();
      boolean success = result == ResultStatus.SUCCESS;

      VoidPaymentResponse response = new VoidPaymentResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setReason(reason != null ? reason : result.toString());
      response.setMessage(message != null ? message : "No extended information provided.");
      response.setPaymentId(payment != null ? payment.getId() : null);
      response.setPayment(payment);
      cloverConnector.broadcaster.notifyOnVoidPaymentResponse(response);
    }

    private void onPaymentVoided(ResultCode code, String reason, String message) {
      VoidPaymentResponse response = new VoidPaymentResponse(code == ResultCode.SUCCESS, code);
      response.setReason(reason != null ? reason : code.toString());
      response.setMessage(message != null ? message : "No extended information provided.");
      response.setPaymentId(null);
      cloverConnector.broadcaster.notifyOnVoidPaymentResponse(response);
    }

    private void onPaymentRefundVoided(Refund refund, ResultCode code, String reason, String message) {
      VoidPaymentRefundResponse response = new VoidPaymentRefundResponse(code == ResultCode.SUCCESS, code);
      response.setReason(reason != null ? reason : code.toString());
      response.setMessage(message != null ? message : "No extended information provided.");
      response.setRefund(refund);
      cloverConnector.broadcaster.notifyOnVoidPaymentRefundResponse(response);
    }

    @Override
    public void onCapturePreAuth(ResultStatus status, String reason, String paymentId, long amount, long tipAmount, String message) {
      boolean success = ResultStatus.SUCCESS == status;
      CapturePreAuthResponse response = new CapturePreAuthResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setReason(reason);
      response.setPaymentId(paymentId);
      response.setAmount(amount);
      response.setTipAmount(tipAmount);
      response.setMessage(message);

      cloverConnector.broadcaster.notifyOnCapturePreAuth(response);
    }

    @Override
    public void onIncrementPreAuthResponse(ResultStatus status, String reason, String message, Authorization authorization) {
      boolean success = ResultStatus.SUCCESS == status;
      IncrementPreauthResponse response = new IncrementPreauthResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setAuthorization(authorization);
      cloverConnector.broadcaster.notifyOnIncrementPreAuth(response);
    }

    private void onCapturePreAuth(ResultCode code, String reason, String paymentId) {
      boolean success = ResultCode.SUCCESS == code;
      CapturePreAuthResponse response = new CapturePreAuthResponse(success, code);
      response.setReason(reason);
      response.setPaymentId(paymentId);

      cloverConnector.broadcaster.notifyOnCapturePreAuth(response);
    }

    private void onVaultCardResponse(boolean success, ResultCode code, String reason, String message, VaultedCard vaultedCard) {
      cloverConnector.showWelcomeScreen();
      VaultCardResponse ccr = new VaultCardResponse(success, code, vaultedCard);
      ccr.setReason(reason);
      ccr.setMessage(message);
      cloverConnector.broadcaster.notifyOnVaultCardRespose(ccr);
    }

    @Override
    public void onVaultCardResponse(VaultedCard vaultedCard, String code, String reason) {
      boolean success = "SUCCESS".equals(code);
      onVaultCardResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL, null, null, vaultedCard);
    }

    @Override
    public void onDeviceConnected(CloverDevice device) {
      Log.d(getClass().getSimpleName(), "Connected");
      cloverConnector.isReady = false;
      cloverConnector.broadcaster.notifyOnConnect();
    }

    @Override
    public void onDeviceReady(CloverDevice device, DiscoveryResponseMessage drm) {
      Log.d(getClass().getSimpleName(), "Ready");
      cloverConnector.isReady = drm.ready;

      MerchantInfo merchantInfo = new MerchantInfo(drm);
      cloverConnector.merchantInfo = merchantInfo;
      device.setSupportsAcks(merchantInfo.deviceInfo.supportsAcks);
      device.setSupportsVoidPaymentResponse(drm.supportsVoidPaymentResponse);

      if (drm.ready) {
        cloverConnector.broadcaster.notifyOnReady(merchantInfo);
      } else {
        cloverConnector.broadcaster.notifyOnConnect();
      }
    }

    @Override
    public void onDeviceError(CloverDeviceErrorEvent errorEvent) {
      cloverConnector.broadcaster.notifyOnDeviceError(errorEvent);
    }

    @Override
    public void onPrintRefundPayment(Payment payment, Order order, Refund refund) {
      cloverConnector.broadcaster.notifyOnPrintRefundPaymentReceipt(new PrintRefundPaymentReceiptMessage(payment, order, refund));
    }

    @Override
    public void onPrintMerchantReceipt(Payment payment) {
      cloverConnector.broadcaster.notifyOnPrintPaymentMerchantCopyReceipt(new PrintPaymentMerchantCopyReceiptMessage(payment));
    }

    @Override
    public void onPrintPaymentDecline(Payment payment, String reason) {
      cloverConnector.broadcaster.notifyOnPrintPaymentDeclineReceipt(new PrintPaymentDeclineReceiptMessage(payment, reason));
    }

    @Override
    public void onPrintPayment(Payment payment, Order order) {
      cloverConnector.broadcaster.notifyOnPrintPaymentReceipt(new PrintPaymentReceiptMessage(payment, order));
    }

    @Override
    public void onPrintCredit(Credit credit) {
      cloverConnector.broadcaster.notifyOnPrintCreditReceipt(new PrintManualRefundReceiptMessage(credit));
    }

    @Override
    public void onPrintCreditDecline(Credit credit, String reason) {
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

    @Override
    public void onRetrievePrintJobStatus(String printRequestId, PrintJobStatus status){
      PrintJobStatusResponse response = new PrintJobStatusResponse(printRequestId, status);
      response.setResult(status != null ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setSuccess(response.getResult() == ResultCode.SUCCESS);
      cloverConnector.broadcaster.notifyOnPrintJobStatusResponse(response);
    }



    @Override
    public void onCustomerProvidedDataMessage(ResultCode result, String eventId, LoyaltyDataConfig config, String data) {
      CustomerProvidedDataEvent event = new CustomerProvidedDataEvent();
      boolean success = result == ResultCode.SUCCESS;
      event.setResult(result);
      event.setSuccess(success);
      event.setEventId(eventId);
      DataProviderConfig dataProviderConfig = new DataProviderConfig();
      dataProviderConfig.setType(config.getType());
      dataProviderConfig.setConfiguration(config.getConfiguration());
      event.setConfig(dataProviderConfig);
      event.setData(data);
      cloverConnector.broadcaster.notifyOnCustomerProvidedDataEvent(event);
    }

    @Override
    public void onDisplayReceiptOptionsResponse(ResultStatus resultStatus, String reason) {
      DisplayReceiptOptionsResponse response = new DisplayReceiptOptionsResponse(resultStatus, reason);
      response.setResult(resultStatus == ResultStatus.SUCCESS ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setSuccess(response.getResult() == ResultCode.SUCCESS);
      cloverConnector.broadcaster.notifyOnDisplayReceiptOptionsResponse(response);
    }

    @Override
    public void onSignatureCollected(ResultStatus resultStatus, String reason, Signature2 signature) {
      boolean success = ResultStatus.SUCCESS == resultStatus;
      SignatureResponse response = new SignatureResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL, signature);
      response.setResult(resultStatus == ResultStatus.SUCCESS ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setSuccess(response.getResult() == ResultCode.SUCCESS);
      cloverConnector.broadcaster.notifyOnSignatureCollected(response);
    }

    @Override
    public void onBalanceInquiryResponse(ResultStatus resultStatus, String reason, Long amount) {
      boolean success = ResultStatus.SUCCESS == resultStatus;
      CheckBalanceResponse response = new CheckBalanceResponse(success, success ? ResultCode.SUCCESS : ResultCode.FAIL, amount);
      response.setResult(resultStatus == ResultStatus.SUCCESS ? ResultCode.SUCCESS : ResultCode.FAIL);
      response.setSuccess(response.getResult() == ResultCode.SUCCESS);
      cloverConnector.broadcaster.notifyOnCheckBalanceResponse(response);
    }
    @Override
    public void onRequestTipResponse(ResultStatus resultStatus, String reason, Long amount) {
      boolean success = ResultStatus.SUCCESS == resultStatus;
      TipResponse response = new TipResponse(success, resultStatus == ResultStatus.SUCCESS ? ResultCode.SUCCESS : ResultCode.FAIL, amount);
      cloverConnector.broadcaster.notifyOnTipResponse(response);
    }
  }
}