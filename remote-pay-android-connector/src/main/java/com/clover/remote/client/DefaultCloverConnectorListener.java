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
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.DisplayReceiptOptionsResponse;
import com.clover.remote.client.messages.MessageFromActivity;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.ManualRefundResponse;
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
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRefundResponse;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;

public abstract class DefaultCloverConnectorListener implements ICloverConnectorListener {
  private boolean ready = false;
  protected MerchantInfo merchantInfo;
  protected ICloverConnector cloverConnector;

  public DefaultCloverConnectorListener(ICloverConnector cloverConnector) {
    this.cloverConnector = cloverConnector;
  }

  public boolean isReady() {
    return ready;
  }

  @Override public void onDeviceDisconnected() {
    ready = false;
  }

  @Override public void onDeviceConnected() {
    ready = false;
  }

  @Override public void onDeviceReady(MerchantInfo merchantInfo) {
    ready = true;
    this.merchantInfo = merchantInfo;
  }

  @Override public void onDeviceActivityStart(CloverDeviceEvent deviceEvent) {

  }

  @Override public void onDeviceActivityEnd(CloverDeviceEvent deviceEvent) {

  }

  @Override public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent) {

  }

  @Override public void onAuthResponse(AuthResponse response) {

  }

  @Override public void onTipAdjustAuthResponse(TipAdjustAuthResponse response) {

  }

  @Override public void onCapturePreAuthResponse(CapturePreAuthResponse response) {

  }

  @Override public void onVerifySignatureRequest(VerifySignatureRequest request) {
    if(cloverConnector != null) {
      cloverConnector.acceptSignature(request);
    }
  }

  @Override public void onCloseoutResponse(CloseoutResponse response) {

  }

  @Override public void onSaleResponse(SaleResponse response) {

  }

  @Override public void onPreAuthResponse(PreAuthResponse response) {

  }

  @Override public void onManualRefundResponse(ManualRefundResponse response) {

  }

  @Override public void onRefundPaymentResponse(RefundPaymentResponse response) {

  }

  @Override public void onTipAdded(TipAddedMessage message) {

  }

  @Override public void onVoidPaymentResponse(VoidPaymentResponse response) {

  }

  @Override public void onVoidPaymentRefundResponse(VoidPaymentRefundResponse response) {

  }

  @Override public void onVaultCardResponse(VaultCardResponse response) {

  }

  @Override public void onPrintManualRefundReceipt(PrintManualRefundReceiptMessage pcm) {

  }

  @Override public void onPrintManualRefundDeclineReceipt(PrintManualRefundDeclineReceiptMessage pcdrm) {

  }

  @Override public void onPrintPaymentReceipt(PrintPaymentReceiptMessage pprm) {

  }

  @Override public void onPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage ppdrm) {

  }

  @Override public void onPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage ppmcrm) {

  }

  @Override public void onPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage pprrm) {

  }

  @Override public void onRetrievePendingPaymentsResponse(RetrievePendingPaymentsResponse retrievePendingPaymentResponse) {

  }

  @Override public void onReadCardDataResponse(ReadCardDataResponse response) {

  }

  @Override public void onMessageFromActivity(MessageFromActivity message) {

  }

  @Override public void onCustomActivityResponse(CustomActivityResponse response) {

  }

  @Override public void onRetrieveDeviceStatusResponse(RetrieveDeviceStatusResponse response){

  }

  @Override public void onResetDeviceResponse(ResetDeviceResponse response){

  }

  @Override public void onRetrievePaymentResponse(RetrievePaymentResponse response){

  }

   @Override public void onRetrievePrintersResponse(RetrievePrintersResponse response){

  }

  @Override public void onPrintJobStatusResponse(PrintJobStatusResponse response){

  }

  @Override public void onDisplayReceiptOptionsResponse(DisplayReceiptOptionsResponse response) {

  }
}
