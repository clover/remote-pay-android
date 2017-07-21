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

import com.clover.sdk.v3.payments.Credit;

/**
 * Callback to request the POS print a ManualRefund declined receipt
 */
@SuppressWarnings(value="unused")
public class PrintManualRefundDeclineReceiptMessage {
  private final Credit credit;
  private final String reason;

  /**
   * Constructor
   *
   * @param credit credit object created when processing the manual refund
   * @param reason reason refund was declined
   */
  public PrintManualRefundDeclineReceiptMessage(Credit credit, String reason) {
    this.credit = credit;
    this.reason = reason;
  }

  /**
   * Get the field value
   *
   * @return credit object created when processing the manual refund
   */
  public Credit getCredit() {
    return credit;
  }

  /**
   * Get the field value
   *
   * @return reason refund was declined
   */
  public String getReason() {
    return reason;
  }
}
