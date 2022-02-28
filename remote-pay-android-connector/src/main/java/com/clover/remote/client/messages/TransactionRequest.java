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
import com.clover.sdk.v3.merchant.TipSuggestion;
import com.clover.sdk.v3.payments.DataEntryLocation;
import com.clover.sdk.v3.payments.VaultedCard;


/**
 * Base class used by all transaction request objects like {@link SaleRequest}, {@link AuthRequest}, etc.
 */
@SuppressWarnings(value="unused")
public abstract class TransactionRequest extends BaseTransactionRequest {

  private Long signatureThreshold = null;
  private DataEntryLocation signatureEntryLocation = null;
  private Boolean autoAcceptSignature = null;
  private Boolean allowOfflinePayment = null;
  private Boolean forceOfflinePayment = null;
  private Boolean approveOfflinePaymentWithoutPrompt = null;
  private Boolean disableCashback = null;
  private Long taxAmount = null;
  private Long tippableAmount = null;
  private TipSuggestion[] tipSuggestions = null;

  /**
   * Constructor
   *
   * @param amount The amount of the transaction. This includes amount, tax, service charges, etc. except the tip
   * @param externalId An id assigned by the POS that can be used to track a payment through the Clover system
   */
  public TransactionRequest(long amount, String externalId) {
    super(amount, externalId);
  }


  @Override
  public PayIntent.TransactionType getType() {
    return PayIntent.TransactionType.PAYMENT;
  }

  /**
   * Set the field value
   *
   * @param signatureThreshold The signature threshold settings overrides
   */
  public void setSignatureThreshold(Long signatureThreshold) {
    this.signatureThreshold = signatureThreshold;
  }

  /**
   * Get the field value
   *
   * @return The transaction level settings overrides
   */
  public Long getSignatureThreshold() {
    return this.signatureThreshold;
  }

  /**
   * Set the field value
   *
   * @param signatureEntryLocation The signature entry location settings overrides
   */
  public void setSignatureEntryLocation(DataEntryLocation signatureEntryLocation) {
    this.signatureEntryLocation = signatureEntryLocation;
  }

  /**
   * Get the field value
   *
   * @return The transaction level settings overrides
   */
  public DataEntryLocation getSignatureEntryLocation() {
    return this.signatureEntryLocation;
  }

  /**
   * Set the field value
   *
   * @param autoAcceptSignature The automatically accept signature override
   */
  public void setAutoAcceptSignature(Boolean autoAcceptSignature) {
    this.autoAcceptSignature = autoAcceptSignature;
  }

  /**
   * Get the field value
   *
   * @return The automatically accept signature override
   */
  public Boolean getAutoAcceptSignature() {
    return this.autoAcceptSignature;
  }

  /**
   * Set the field value
   *
   * @param disableCashback If true, do not allow cash back
   */
  public void setDisableCashback(Boolean disableCashback) {
    this.disableCashback = disableCashback;
  }

  /**
   * Get the field value
   *
   * @return If true, do not allow cash back
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
   * @return Amount paid in tips
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
   * Get the field value
   *
   * @return If true then offline payments can be accepted
   */
  public Boolean getAllowOfflinePayment() {
    return this.allowOfflinePayment;
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
   * @return If true then the payment will be taken offline
   */
  public Boolean getForceOfflinePayment() {
    return this.forceOfflinePayment;
  }

  /**
   * Set the field value
   *
   * @param approveOfflinePaymentWithoutPrompt If true then offline payments will be approved without a prompt
   */
  public void setApproveOfflinePaymentWithoutPrompt(Boolean approveOfflinePaymentWithoutPrompt) {
    this.approveOfflinePaymentWithoutPrompt = approveOfflinePaymentWithoutPrompt;
  }

  /**
   * Get the field value
   *
   * @return If true then offline payments will be approved without a prompt
   */
  public Boolean getApproveOfflinePaymentWithoutPrompt() {
    return this.approveOfflinePaymentWithoutPrompt;
  }

  /**
   * Set the field value
   *
   * @param tippableAmount The total amount used when calculating tips
   */
  public void setTippableAmount(Long tippableAmount) {
    this.tippableAmount = tippableAmount;
  }

  /**
   * Get the field value
   *
   * @return The total amount used when calculating tips
   */
  public Long getTippableAmount() {
    return this.tippableAmount;
  }


  /**
   * Get the field value
   *
   * @return The list of tip suggestions
   */
  public TipSuggestion[] getTipSuggestions() {
    return tipSuggestions;
  }

  /**
   * Set the field value
   *
   * @param tipSuggestions The list of tip suggestions
   */
  public void setTipSuggestions(TipSuggestion[] tipSuggestions) {
    this.tipSuggestions = tipSuggestions;
  }

}
