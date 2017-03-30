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

/** A authorization request */
@SuppressWarnings(value="unused")
public class AuthRequest extends TransactionRequest {

  private Boolean disableCashback = null;
  private Long taxAmount = null;
  private Long tippableAmount = null;
  private Boolean allowOfflinePayment = null;
  private Boolean approveOfflinePaymentWithoutPrompt = null;

  public AuthRequest(long amount, String externalId) {
    super(amount, externalId);
  }
  /**
  * Set the field value
  * Do not allow cash back
  *
  */
  public void setDisableCashback(Boolean disableCashback) {
    this.disableCashback = disableCashback;
  }

  /**
  * Get the field value
  * Do not allow cash back
  */
  public Boolean getDisableCashback() {
    return this.disableCashback;
  }  
  /**
  * Set the field value
  * Amount paid in tips
  *
  */
  public void setTaxAmount(Long taxAmount) {
    this.taxAmount = taxAmount;
  }

  /**
  * Get the field value
  * Amount paid in tips
  */
  public Long getTaxAmount() {
    return this.taxAmount;
  }  
  /**
  * Set the field value
  * If true then offline payments can be accepted
  *
  */
  public void setAllowOfflinePayment(Boolean allowOfflinePayment) {
    this.allowOfflinePayment = allowOfflinePayment;
  }

  /**
  * Get the field value
  * If true then offline payments can be accepted
  */
  public Boolean getAllowOfflinePayment() {
    return this.allowOfflinePayment;
  }  
  /**
  * Set the field value
  * If true then offline payments will be approved without a prompt.  Currently must be true.
  *
  */
  public void setApproveOfflinePaymentWithoutPrompt(Boolean approveOfflinePaymentWithoutPrompt) {
    this.approveOfflinePaymentWithoutPrompt = approveOfflinePaymentWithoutPrompt;
  }

  /**
  * Get the field value
  * If true then offline payments will be approved without a prompt.  Currently must be true.
  */
  public Boolean getApproveOfflinePaymentWithoutPrompt() {
    return this.approveOfflinePaymentWithoutPrompt;
  }

  @Override public PayIntent.TransactionType getType() {
    return PayIntent.TransactionType.PAYMENT;
  }

  public Long getTippableAmount() {
    return tippableAmount;
  }

  public void setTippableAmount(Long tippableAmount) {
    this.tippableAmount = tippableAmount;
  }
}
