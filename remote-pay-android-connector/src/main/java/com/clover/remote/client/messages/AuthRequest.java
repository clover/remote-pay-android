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
 * Request object for requesting an auth transaction.
 */
@SuppressWarnings(value="unused")
public class AuthRequest extends TransactionRequest {

  private Boolean disableCashback = null;
  private Long taxAmount = null;
  private Long tippableAmount = null;
  private Boolean allowOfflinePayment = null;
  private Boolean approveOfflinePaymentWithoutPrompt = null;
  private Boolean forceOfflinePayment = null;

  /**
   * Constructor
   *
   * @param amount The amount of the transaction. This includes amount, tax, service charges, etc. except the tip
   * @param externalId An id assigned by the POS that can be used to track a payment through the Clover system
   */
  public AuthRequest(long amount, String externalId) {
    super(amount, externalId);
  }

  /**
   * Set the field value
   *
   * @param disableCashback If true then do not allow cash back
   */
  public void setDisableCashback(Boolean disableCashback) {
    this.disableCashback = disableCashback;
  }

  /**
   * Get the field value
   *
   * @return If true then do not allow cash back
   */
  public Boolean getDisableCashback() {
    return this.disableCashback;
  }

  /**
   * Set the field value
   *
   * @param taxAmount Amount paid in taxes
   */
  public void setTaxAmount(Long taxAmount) {
    this.taxAmount = taxAmount;
  }

  /**
   * Get the field value
   *
   * @return Amount paid in taxes
   */
  public Long getTaxAmount() {
    return this.taxAmount;
  }

  /**
   * Set the field value
   *
   * @param allowOfflinePayment If true then offline payments can be accepted
   */
  public void setAllowOfflinePayment(Boolean allowOfflinePayment) {
    this.allowOfflinePayment = allowOfflinePayment;
  }

  /**
   * Set the field value
   *
   * @param forceOfflinePayment If true then the payment will be taken offline
   */
  public void setForceOfflinePayment(Boolean forceOfflinePayment) {
    this.forceOfflinePayment = forceOfflinePayment;
  }

  /**
   * Get the field value
   *
   * @return If true then offline payments can be accepted
   */
  public Boolean getAllowOfflinePayment() {
    return this.allowOfflinePayment;
  }

  /**
   * Get the field value
   *
   * @return If true then the payment will be taken offline
   */
  public Boolean getForceOfflinePayment() {
    return this.forceOfflinePayment;
  }

  /**
   * Set the field value
   *
   * @param approveOfflinePaymentWithoutPrompt If true then offline payments will be approved without a prompt.
   */
  public void setApproveOfflinePaymentWithoutPrompt(Boolean approveOfflinePaymentWithoutPrompt) {
    this.approveOfflinePaymentWithoutPrompt = approveOfflinePaymentWithoutPrompt;
  }

  /**
   * Get the field value
   *
   * @return If true then offline payments will be approved without a prompt.
   */
  public Boolean getApproveOfflinePaymentWithoutPrompt() {
    return this.approveOfflinePaymentWithoutPrompt;
  }

  @Override
  public PayIntent.TransactionType getType() {
    return PayIntent.TransactionType.PAYMENT;
  }

  /**
   * Get the field value
   *
   * @return If true then offline payments will be approved without a prompt.  Currently must be true.
   */
  public Long getTippableAmount() {
    return tippableAmount;
  }

  /**
   * Set the field value
   *
   * @param tippableAmount If true then offline payments will be approved without a prompt.  Currently must be true.
   */
  public void setTippableAmount(Long tippableAmount) {
    this.tippableAmount = tippableAmount;
  }
}
