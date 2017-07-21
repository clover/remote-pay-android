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

import com.clover.common2.payments.PayIntent;

/**
 * Request object used when requesting a Manual Refund (Naked Credit)
 */
@SuppressWarnings(value="unused")
public class ManualRefundRequest extends TransactionRequest {

  /**
   * Constructor
   *
   * @param amount The amount of the transaction. This includes amount, tax, service charges, etc. except the tip
   * @param externalId An id assigned by the POS that can be used to track a payment through the Clover system
   */
  public ManualRefundRequest(long amount, String externalId){
    super(amount, externalId);
  }

  @Override
  public PayIntent.TransactionType getType() {
    return PayIntent.TransactionType.CREDIT;
  }
}

