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
import com.clover.sdk.v3.payments.DataEntryLocation;
import com.clover.sdk.v3.payments.VaultedCard;

/**
 * Base class used by all transaction request objects like {@link SaleRequest}, {@link AuthRequest}, etc.
 */
@SuppressWarnings(value="unused")
public abstract class TransactionRequest extends BaseRequest {

  private Boolean disablePrinting = null;
  private Boolean cardNotPresent = null;
  private Boolean disableRestartTransactionOnFail = null;
  private long amount;
  private Integer cardEntryMethods = null;
  private VaultedCard vaultedCard = null;
  private String externalId = null;
  private PayIntent.TransactionType type = null;
  private Long signatureThreshold = null;
  private DataEntryLocation signatureEntryLocation = null;
  private Boolean disableReceiptSelection = null;
  private Boolean disableDuplicateChecking = null;
  private Boolean autoAcceptPaymentConfirmations = null;
  private Boolean autoAcceptSignature = null;

  /**
   * Constructor
   *
   * @param amount The amount of the transaction. This includes amount, tax, service charges, etc. except the tip
   * @param externalId An id assigned by the POS that can be used to track a payment through the Clover system
   */
  public TransactionRequest(long amount, String externalId) {
    if(externalId == null || externalId.length() > 32) {
      throw new IllegalArgumentException("The externalId must be provided.  The maximum length is 32 characters.");
    }
    this.amount = amount;
    this.externalId = externalId;
  }

  /**
   * Set the field value
   *
   * @param disablePrinting Do not print
   */
  public void setDisablePrinting(Boolean disablePrinting) {
    this.disablePrinting = disablePrinting;
  }

  /**
   * Get the field value
   *
   * @return Do not print
   */
  public Boolean getDisablePrinting() {
    return this.disablePrinting;
  }

  /**
   * Set the field value
   *
   * @param cardNotPresent If true then card not present is accepted
   */
  public void setCardNotPresent(Boolean cardNotPresent) {
    this.cardNotPresent = cardNotPresent;
  }

  /**
   * Get the field value
   *
   * @return If true then card not present is accepted
   */
  public Boolean getCardNotPresent() {
    return this.cardNotPresent;
  }

  /**
   * Set the field value
   *
   * @param disableRestartTransactionOnFail If the transaction times out or fails because of decline, do not restart it
   */
  public void setDisableRestartTransactionOnFail(Boolean disableRestartTransactionOnFail) {
    this.disableRestartTransactionOnFail = disableRestartTransactionOnFail;
  }

  /**
   * Get the field value
   *
   * @return If the transaction times out or fails because of decline, do not restart it
   */
  public Boolean getDisableRestartTransactionOnFail() {
    return this.disableRestartTransactionOnFail;
  }

  /**
   * Set the field value
   *
   * @param amount Total amount paid
   */
  public void setAmount(long amount) {
    this.amount = amount;
  }

  /**
   * Get the field value
   *
   * @return Total amount paid
   */
  public long getAmount() {
    return this.amount;
  }

  /**
   * Set the field value
   *
   * @param cardEntryMethods Allowed entry methods
   */
  public void setCardEntryMethods(Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }

  /**
   * Get the field value
   *
   * @return Allowed entry methods
   */
  public Integer getCardEntryMethods() {
    return this.cardEntryMethods;
  }

  /**
   * Set the field value
   *
   * @param vaultedCard A saved card
   */
  public void setVaultedCard(VaultedCard vaultedCard) {
    this.vaultedCard = vaultedCard;
  }

  /**
   * Get the field value
   *
   * @return A saved card
   */
  public VaultedCard getVaultedCard() {
    return this.vaultedCard;
  }

  /**
   * Set the field value
   *
   * @param externalId An id that will be persisted with transactions.
   */
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  /**
   * Get the field value
   *
   * @return An id that will be persisted with transactions.
   */
  public String getExternalId() {
    return this.externalId;
  }

  /**
   * Get the field value
   *
   * @return The type of the transaction.
   */
  abstract public PayIntent.TransactionType getType();

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
   * @param disableReceiptSelection The disable receipt options screen settings overrides
   */
  public void setDisableReceiptSelection(Boolean disableReceiptSelection) {
    this.disableReceiptSelection = disableReceiptSelection;
  }

  /**
   * Get the field value
   *
   * @return The disable receipt options screen settings overrides
   */
  public Boolean getDisableReceiptSelection() {
    return this.disableReceiptSelection;
  }

  /**
   * Set the field value
   *
   * @param disableDuplicateChecking The disable duplicate transaction validation settings overrides
   */
  public void setDisableDuplicateChecking(Boolean disableDuplicateChecking) {
    this.disableDuplicateChecking = disableDuplicateChecking;
  }

  /**
   * Get the field value
   *
   * @return The disable duplicate transaction validation settings overrides
   */
  public Boolean getDisableDuplicateChecking() {
    return this.disableDuplicateChecking;
  }

  /**
   * Set the field value
   *
   * @param autoAcceptPaymentConfirmations The automatically accept payment confirmations settings override
   */
  public void setAutoAcceptPaymentConfirmations(Boolean autoAcceptPaymentConfirmations) {
    this.autoAcceptPaymentConfirmations = autoAcceptPaymentConfirmations;
  }

  /**
   * Get the field value
   *
   * @return The automatically accept payment confirmations settings override
   */
  public Boolean getAutoAcceptPaymentConfirmations() {
    return this.autoAcceptPaymentConfirmations;
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
}
