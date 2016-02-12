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
import com.clover.remote.client.transport.CloverTransportObserver;
import com.clover.remote.terminal.InputOption;
import com.clover.remote.terminal.KeyPress;
import com.clover.remote.terminal.ResultStatus;
import com.clover.remote.terminal.TxState;
import com.clover.remote.terminal.UiState;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;
import com.clover.sdk.v3.payments.VaultedCard;

public interface CloverDeviceObserver extends CloverTransportObserver {

  void onTxState(TxState txState);

  void onUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption[] inputOptions);

  void onTipAdded(long tipAmount);

  void onAuthTipAdjusted(String paymentId, long amount, boolean success);

  void onCashbackSelected(long cashbackAmount);

  void onPartialAuth(long partialAuthAmount);

  void onFinishOk(Payment payment, Signature2 signature2);

  void onFinishOk(Credit credit);

  void onFinishOk(Refund refund);

  void onFinishCancel();

  void onVerifySignature(Payment payment, Signature2 signature);

  void onPaymentVoided(Payment payment, VoidReason voidReason);

  void onKeyPressed(KeyPress keyPress);

  void onPaymentRefundResponse(String orderId, String paymentId, Refund refund, TxState code);

  void onVaultCardResponse(VaultedCard vaultedCard, String code, String reason);

  void onCapturePreAuth(ResultStatus status, String reason, String paymentId, long amount, long tipAmount);

  void onCloseoutResponse();

  //void onPrint(Payment payment, Order order);
  //void onPrint(Credit credit);
  //void onPrintDecline(Payment payment, String reason);
  //void onPrintDecline(Credit credit, String reason);
  //void onPrintMerchantCopy(Payment payment);
  //void onModifyOrder(AddDiscountAction addDiscountAction);
  //void onModifyOrder(RemoveDiscountAction removeDiscountAction);
  //void onModifyOrder(AddLineItemAction addLineItemAction);
  //void onModifyOrder(RemoveLineItemAction removeLineItemAction);
  void onTxStartResponse(boolean success);
}