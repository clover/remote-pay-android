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

import com.clover.common2.Signature2;
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
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.message.DiscoveryResponseMessage;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Batch;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;
import com.clover.sdk.v3.payments.VaultedCard;
import com.clover.sdk.v3.printer.PrintJobStatus;
import com.clover.sdk.v3.printer.Printer;

import java.util.List;

public interface CloverDeviceObserver {

  void onTxState(TxState txState);

  void onTxStartResponse(TxStartResponseResult result, String externalId, String messageInfo);

  void onUiState(UiState uiState, String uiText, UiState.UiDirection uiDirection, InputOption[] inputOptions);

  void onTipAdded(long tipAmount);

  void onAuthTipAdjusted(String paymentId, long amount, boolean success);

  void onCashbackSelected(long cashbackAmount);

  void onPartialAuth(long partialAuthAmount);

  void onFinishOk(Payment payment, Signature2 signature2, String messageInfo);

  void onFinishOk(Credit credit);

  void onFinishOk(Refund refund);

  void onFinishCancel(String messageInfo);

  void onVerifySignature(Payment payment, Signature2 signature);

  void onConfirmPayment(Payment payment, Challenge[] challenges);

  void onPaymentVoided(Payment payment, VoidReason voidReason, ResultStatus result, String reason, String message);

  void onKeyPressed(KeyPress keyPress);

  void onPaymentRefundResponse(String orderId, String paymentId, Refund refund, TxState code, ErrorCode reason, String message);

  void onVaultCardResponse(VaultedCard vaultedCard, String code, String reason);

  void onCapturePreAuth(ResultStatus status, String reason, String paymentId, long amount, long tipAmount);

  void onCloseoutResponse(ResultStatus status, String reason, Batch batch);

  void onDeviceDisconnected(CloverDevice device);

  void onDeviceConnected(CloverDevice device);

  void onDeviceReady(CloverDevice device, DiscoveryResponseMessage drm);

  void onDeviceError(CloverDeviceErrorEvent errorEvent);

  void onPrintRefundPayment(Payment payment, Order order, Refund refund);

  void onPrintMerchantReceipt(Payment payment);

  void onPrintPaymentDecline(Payment payment, String reason);

  void onPrintPayment(Payment payment, Order order);

  void onPrintCredit(Credit credit);

  void onPrintCreditDecline(Credit credit, String reason);

  void onMessageAck(String sourceMessageId);

  void onPendingPaymentsResponse(boolean success, List<PendingPaymentEntry> payments);

  void onReadCardResponse(ResultStatus status, String reason, CardData cardData);

  void onMessageFromActivity(String actionId, String payload);

  void onActivityResponse(ResultStatus status, String payload, String failReason, String actionId);

  void onDeviceStatusResponse(ResultCode result, String reason, ExternalDeviceState state, ExternalDeviceStateData data);

  void onResetDeviceResponse(ResultCode result, String reason, ExternalDeviceState state);

  void onRetrievePaymentResponse(ResultCode result, String reason, String externalPaymentId, QueryStatus queryStatus, Payment payment);

  void onRetrievePrinterResponse(List<Printer> printers);

  void onRetrievePrintJobStatus(String printRequestId, PrintJobStatus status);

}