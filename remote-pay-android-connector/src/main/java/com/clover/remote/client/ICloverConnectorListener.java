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
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.PairingCodeMessage;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.PrintManualRefundDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintManualRefundReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentMerchantCopyReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentReceiptMessage;
import com.clover.remote.client.messages.PrintRefundPaymentReceiptMessage;
import com.clover.remote.client.messages.ReadCardDataResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;

public interface ICloverConnectorListener {

  /**
   * Called when a Clover device activity starts.
   * @param deviceEvent
   */
  public void onDeviceActivityStart(CloverDeviceEvent deviceEvent);

  /**
   * Called when a Clover device activity ends.
   * @param deviceEvent
   */
  public void onDeviceActivityEnd(CloverDeviceEvent deviceEvent);

  /**
   * Called when a Clover device error event is encountered
   * @param deviceErrorEvent
   */
  public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent);

  /**
   * Called in response to a pre auth request
   * @param response
   */
  public void onPreAuthResponse(PreAuthResponse response);

  /**
   * Called in response to an auth request
   * @param response
   */
  public void onAuthResponse(AuthResponse response);

  /**
   * Called in response to a tip adjust of an auth payment
   * @param response
   */
  public void onTipAdjustAuthResponse(TipAdjustAuthResponse response);

  /**
   * Called in response to a capture of a pre auth payment
   * @param response
   */
  public void onCapturePreAuthResponse(CapturePreAuthResponse response);

  /**
   * Called when the Clover device requires a signature to be verified
   * @param request
   */
  public void onVerifySignatureRequest(VerifySignatureRequest request);

  /**
   * Called when the Clover device requires confirmation for a payment
   * e.g. Duplicates or Offline
   * @param request
   */
  public void onConfirmPaymentRequest(ConfirmPaymentRequest request);

  /**
   * Called in response to a closeout being processed
   * @param response
   */
  public void onCloseoutResponse(CloseoutResponse response);

  /**
   * Called in response to a sale request
   * @param response
   */
  public void onSaleResponse(SaleResponse response);

  /**
   * Called in response to a manual refund request
   * @param response
   */
  public void onManualRefundResponse(ManualRefundResponse response);

  /**
   * Called in response to a refund payment request
   * @param response
   */
  public void onRefundPaymentResponse(RefundPaymentResponse response);

  /**
   * Called when a customer selects a tip amount on the Clover device screen
   * @param message
   */
  public void onTipAdded(TipAddedMessage message);

  /**
   * Called in response to a void payment request
   * @param response
   */
  public void onVoidPaymentResponse(VoidPaymentResponse response);

  /**
   * Called when the Clover device is disconnected
   */
  public void onDeviceDisconnected();

  /**
   * Called when the Clover device is connected, but not ready to communicate
   */
  public void onDeviceConnected();

  /**
   * Called when the Clover device is ready to communicate
   * @param merchantInfo
   */
  public void onDeviceReady(MerchantInfo merchantInfo);

  /**
   * Called in response to a vault card request
   * @param response
   */
  public void onVaultCardResponse(VaultCardResponse response);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a ManualRefund
   * @param printManualRefundReceiptMessage
   */
  public void onPrintManualRefundReceipt(PrintManualRefundReceiptMessage printManualRefundReceiptMessage);
  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a declined ManualRefund
   * @param printManualRefundDeclineReceiptMessage
   */
  public void onPrintManualRefundDeclineReceipt(PrintManualRefundDeclineReceiptMessage printManualRefundDeclineReceiptMessage);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a payment
   * @param printPaymentReceiptMessage
   */
  public void onPrintPaymentReceipt(PrintPaymentReceiptMessage printPaymentReceiptMessage);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a declined payment
   * @param printPaymentDeclineReceiptMessage
   */
  public void onPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage printPaymentDeclineReceiptMessage);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a merchant copy of a payment receipt
   * @param printPaymentMerchantCopyReceiptMessage
   */
  public void onPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage printPaymentMerchantCopyReceiptMessage);

  /**
   * Will only be called if disablePrinting = true on the Sale, Auth, PreAuth or ManualRefund Request
   * Called when a user requests to print a receipt for a payment refund
   * @param printRefundPaymentReceiptMessage
   */
  public void onPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage printRefundPaymentReceiptMessage);

  /**
   * Called in response to a retrievePendingPayment(...) request.
   * @param retrievePendingPaymentResponse
   */
  public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse retrievePendingPaymentResponse);

  /**
   * Called in response to a readCardData(...) request.
   * @param response
   */
  public void onReadCardDataResponse(ReadCardDataResponse response);
}
