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

import com.clover.sdk.v3.payments.Payment;

/**
 * Callback to the POS to request a merchant payment receipt
 * be printed
 */
@SuppressWarnings(value="unused")
public class PrintPaymentMerchantCopyReceiptMessage {
  private final Payment payment;

  /**
   * Constructor
   *
   * @param payment payment for requested receipt
   */
  public PrintPaymentMerchantCopyReceiptMessage(Payment payment) {
    this.payment = payment;
  }

  /**
   * Get the field value
   *
   * @return payment for requested receipt
   */
  public Payment getPayment() {
    return payment;
  }
}
