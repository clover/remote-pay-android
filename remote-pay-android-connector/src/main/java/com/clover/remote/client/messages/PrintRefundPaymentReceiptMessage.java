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
package com.clover.remote.client.messages;

import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Refund;

/**
 * Callback to the POS to request a payment refund receipt
 * be printed
 */
public class PrintRefundPaymentReceiptMessage {
  private Payment payment;
  private Refund refund;
  private Order order;

  public PrintRefundPaymentReceiptMessage(Payment payment, Order order, Refund refund) {
    this.payment = payment;
    this.order = order;
    this.refund = refund;
  }

  public Payment getPayment() {
    return payment;
  }

  public Refund getRefund() {
    return refund;
  }

  public Order getOrder() {
    return order;
  }
}
