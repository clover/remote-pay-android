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
import com.clover.remote.client.messages.CaptureAuthResponse;
import com.clover.remote.client.messages.CaptureCardResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.SignatureVerifyRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.protocol.message.TipAddedMessage;
import com.clover.remote.terminal.TxState;

public interface ICloverConnectorListener {
  public void onDisconnected();

  void onDisconnecting();

  public void onConnected();

  public void onReady();

  public void onError(Exception e);

  public void onDebug(String s);

  public void onDeviceActivityStart(CloverDeviceEvent deviceEvent);

  public void onDeviceActivityEnd(CloverDeviceEvent deviceEvent);

  public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent);

  public void onAuthResponse(AuthResponse response);

  public void onAuthTipAdjustResponse(TipAdjustAuthResponse response);

  public void onAuthCaptureResponse(CaptureAuthResponse response);

  public void onSignatureVerifyRequest(SignatureVerifyRequest request);

  public void onCloseoutResponse(CloseoutResponse response);

  public void onSaleResponse(SaleResponse response);

  public void onManualRefundResponse(ManualRefundResponse response);

  public void onRefundPaymentResponse(RefundPaymentResponse response);

  public void onTipAdded(TipAddedMessage message);

  //public void OnVoidTransactionResponse(VoidTransactionResponse response);
  public void onVoidPaymentResponse(VoidPaymentResponse response);

  //public void OnDisplayReceiptOptionsResponse(DisplayReceiptOptionsResponse response);
  public void onCaptureCardResponse(CaptureCardResponse response);

  public void onTransactionState(TxState txState);
}
