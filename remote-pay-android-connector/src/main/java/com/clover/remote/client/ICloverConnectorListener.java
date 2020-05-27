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

import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CheckBalanceResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.CustomerProvidedDataEvent;
import com.clover.remote.client.messages.DisplayReceiptOptionsResponse;
import com.clover.remote.client.messages.IncrementPreauthResponse;
import com.clover.remote.client.messages.InvalidStateTransitionResponse;
import com.clover.remote.client.messages.PrintJobStatusResponse;
import com.clover.remote.client.messages.RetrievePaymentResponse;
import com.clover.remote.client.messages.MessageFromActivity;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.MessageFromActivity;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.PrintJobStatusResponse;
import com.clover.remote.client.messages.PrintManualRefundDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintManualRefundReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentMerchantCopyReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentReceiptMessage;
import com.clover.remote.client.messages.PrintRefundPaymentReceiptMessage;
import com.clover.remote.client.messages.ReadCardDataResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.ResetDeviceResponse;
import com.clover.remote.client.messages.RetrieveDeviceStatusResponse;
import com.clover.remote.client.messages.RetrievePaymentResponse;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.RetrievePrintersResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.SignatureResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.TipResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRefundResponse;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;

/**
 * These are the methods to implement for intercepting messages that are sent from a Clover device.
 */
@SuppressWarnings("unused")
public interface ICloverConnectorListener {

  /**
   * Called when a Clover device activity starts.
   *
   * @param deviceEvent The device event.
   */
  void onDeviceActivityStart(CloverDeviceEvent deviceEvent);

  /**
   * Called when a Clover device activity ends.
   *
   * @param deviceEvent The device event.
   */
  void onDeviceActivityEnd(CloverDeviceEvent deviceEvent);

  /**
   * Called when a Clover device error event is encountered
   *
   * @param deviceErrorEvent The device error event.
   */
  void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent);

  /**
   * Called in response to a pre auth request
   *
   * @param response The response
   */
  void onPreAuthResponse(PreAuthResponse response);

  /**
   * Called in response to an auth request
   *
   * @param response The response
   */
  void onAuthResponse(AuthResponse response);

  /**
   * Called in response to a tip adjust of an auth payment
   *
   * @param response The response
   */
  void onTipAdjustAuthResponse(TipAdjustAuthResponse response);

  /**
   * Called in response to a capture of a pre auth payment
   *
   * @param response The response
   */
  void onCapturePreAuthResponse(CapturePreAuthResponse response);

  /**
   * Called in response to an `incrementPreAuth()` call on the CloverConnector. Returns the incremented pre-auth, if successful.
   * @param response
   */
  void onIncrementPreAuthResponse(IncrementPreauthResponse response);

  /**
   * Called when the Clover device requires a signature to be verified
   *
   * @param request The request
   */
  void onVerifySignatureRequest(VerifySignatureRequest request);

  /**
   * Called when the Clover device requires confirmation for a payment
   * e.g. Duplicates or Offline
   *
   * @param request The request
   */
  void onConfirmPaymentRequest(ConfirmPaymentRequest request);

  /**
   * Called in response to a closeout being processed
   *
   * @param response The response
   */
  void onCloseoutResponse(CloseoutResponse response);

  /**
   * Called in response to a sale request
   *
   * @param response The response
   */
  void onSaleResponse(SaleResponse response);

  /**
   * Called in response to a manual refund request
   *
   * @param response The response
   */
  void onManualRefundResponse(ManualRefundResponse response);

  /**
   * Called in response to a refund payment request
   *
   * @param response The response
   */
  void onRefundPaymentResponse(RefundPaymentResponse response);

  /**
   * Called when a customer selects a tip amount on the Clover device screen
   *
   * @param message The message
   */
  void onTipAdded(TipAddedMessage message);

  /**
   * Called in response to a void payment request
   *
   * @param response The response
   */
  void onVoidPaymentResponse(VoidPaymentResponse response);

  /**
   * Called in response to a void payment refund request
   *
   * @param response The response
   */
  void onVoidPaymentRefundResponse(VoidPaymentRefundResponse response);

  /**
   * Called when the Clover device is disconnected
   */
  void onDeviceDisconnected();

  /**
   * Called when the Clover device is connected, but not ready to communicate
   */
  void onDeviceConnected();

  /**
   * Called when the Clover device is ready to communicate
   *
   * @param merchantInfo The merchant info for the device
   */
  void onDeviceReady(MerchantInfo merchantInfo);

  /**
   * Called in response to a vault card request
   *
   * @param response The response
   */
  void onVaultCardResponse(VaultCardResponse response);

  /**
   * Called to update the status of a print job
   *
   * @param response The response contains the print job identifier and that job's status
   */
  void onPrintJobStatusResponse(PrintJobStatusResponse response);

  /**
   * Called in response to a retrievePrinters() request
   *
   * @param response Response object containing an array of the printers being passed back
   */
  void onRetrievePrintersResponse(RetrievePrintersResponse response);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a ManualRefund
   *
   * @param message The message
   */
  void onPrintManualRefundReceipt(PrintManualRefundReceiptMessage message);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a declined ManualRefund
   *
   * @param message The message
   */
  void onPrintManualRefundDeclineReceipt(PrintManualRefundDeclineReceiptMessage message);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a payment
   *
   * @param message The message
   */
  void onPrintPaymentReceipt(PrintPaymentReceiptMessage message);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a declined payment
   *
   * @param message The message
   */
  void onPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage message);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a merchant copy of a payment receipt
   *
   * @param message The message
   */
  void onPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage message);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a payment refund
   *
   * @param message The message
   */
  void onPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage message);

  /**
   * Called in response to a retrievePendingPayment(...) request.
   *
   * @param response The response
   */
  void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse response);

  /**
   * Called in response to a readCardData(...) request.
   *
   * @param response The response
   */
  void onReadCardDataResponse(ReadCardDataResponse response);

  /**
   * Called when a message is sent from a custom activity
   * @param message The message
   */
  void onMessageFromActivity(MessageFromActivity message);

  /**
   * Called when a custom activity finishes
   *
   * @param response The response
   */
  void onCustomActivityResponse(CustomActivityResponse response);

  /**
   * Called in response to a RetrieveDeviceState request
   *
   * @param response The response
   */
  void onRetrieveDeviceStatusResponse(RetrieveDeviceStatusResponse response);

  /**
   * Called in response to a request that results in an invalid kiosk flow transition
   *
   * @param response The response
   */
  void onInvalidStateTransitionResponse(InvalidStateTransitionResponse response);

  /**
   * Called in response to a ResetDevice request
   *
   * @param response The response
   */
  void onResetDeviceResponse(ResetDeviceResponse response);

  /**
   * Called in response to a RetrievePaymentRequest
   *
   * @param response The response
   */
  void onRetrievePaymentResponse(RetrievePaymentResponse response);

  /**
   * Called when customer information is provided from a loyalty service.
   *
   * @param event The event
   */
  void onCustomerProvidedData(CustomerProvidedDataEvent event);
  /**
   * Called in response to a DisplayReceiptOptionsRequest
   * @param response
   */
  void onDisplayReceiptOptionsResponse(DisplayReceiptOptionsResponse response);

  /**
   * Called in response to a RequestSignatureRequest
   * @param response
   */
  void onRequestSignatureResponse(SignatureResponse response);

  /**
   * Called in response to a CheckBalanceRequest
   * @param response
   */
  void onCheckBalanceResponse(CheckBalanceResponse response);

  /**
   * Called in response to a TipRequest
   * @param response
   */
  void onRequestTipResponse(TipResponse response);
}
