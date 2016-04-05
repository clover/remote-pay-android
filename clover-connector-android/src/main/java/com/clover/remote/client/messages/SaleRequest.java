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
import com.clover.sdk.v3.payments.VaultedCard;

public class SaleRequest extends TxRequest{
  private Long tippableAmount;
  private Long taxAmount;
  private boolean cardNotPresent;

    /*
        public bool DisableCashback { get; set; } //
        public bool DisableTip { get; set; } // if the merchant account is
        public bool DisablePrinting { get; set; }
        public bool DisableRestartTransactionOnFail { get; set; }
     */

  public PayIntent.TransactionType getType() {
    return PayIntent.TransactionType.PAYMENT;
  }

  public Long getTippableAmount() {
    return tippableAmount;
  }

  public void setTippableAmount(Long tippableAmount) {
    this.tippableAmount = tippableAmount;
  }

  public Long getTaxAmount() {
    return taxAmount;
  }

  public void setTaxAmount(Long taxAmount) {
    this.taxAmount = taxAmount;
  }

}
