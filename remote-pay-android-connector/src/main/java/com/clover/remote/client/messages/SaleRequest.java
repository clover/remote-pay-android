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
 * Request object for requesting a sale transaction.
 */
@SuppressWarnings(value="unused")
public class SaleRequest extends TransactionRequest {

  private Long tippableAmount = null;
  private Long tipAmount = null;
  private Boolean disableCashback = null;
  private Boolean disableTipOnScreen = null;
  private Long taxAmount = null;
  private Boolean allowOfflinePayment = null;
  private Boolean forceOfflinePayment = null;
  private Boolean approveOfflinePaymentWithoutPrompt = null;
  private TipMode tipMode = null;

  /**
   * Constructor
   *
   * @param amount The amount of the transaction. This includes amount, tax, service charges, etc. except the tip
   * @param externalId An id assigned by the POS that can be used to track a payment through the Clover system
   */
  public SaleRequest(long amount, String externalId) {
    super(amount, externalId);
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
   * Set the field value
   *
   * @param tipAmount Included tip
   */
  public void setTipAmount(Long tipAmount) {
    this.tipAmount = tipAmount;
  }

  /**
   * Get the field value
   *
   * @return Included tip
   */
  public Long getTipAmount() {
    return this.tipAmount;
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
   * @param disableTipOnScreen If true, the tip screen will not be displayed on the device, even if the merchant is configured for tips on screen
   */
  public void setDisableTipOnScreen(Boolean disableTipOnScreen) {
    this.disableTipOnScreen = disableTipOnScreen;
  }

  /**
   * Get the field value
   *
   * @return If true, the tip screen will not be displayed on the device, even if the merchant is configured for tips on screen
   */
  public Boolean getDisableTipOnScreen() {
    return this.disableTipOnScreen;
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

  @Override
  public PayIntent.TransactionType getType() {
    return PayIntent.TransactionType.PAYMENT;
  }

  /**
   * Set the field value
   *
   * @param tipMode The tip mode settings overrides
   */
  public void setTipMode(TipMode tipMode) {
    this.tipMode = tipMode;
  }

  /**
   * Get the field value
   *
   * @return The tip mode settings overrides
   */
  public TipMode getTipMode() {
    return this.tipMode;
  }

  /**
   * Enumeration for indicating the mode of acquiring a tip
   */
  public enum TipMode {TIP_PROVIDED, ON_SCREEN_BEFORE_PAYMENT, NO_TIP}
}
