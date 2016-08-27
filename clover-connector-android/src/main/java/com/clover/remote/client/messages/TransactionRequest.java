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
 * base class used by all transaction requests
 * SaleRequest, AuthRequest, PreAuthRequest and ManualRefundRequest
 */
@SuppressWarnings(value="unused")
public abstract class TransactionRequest extends BaseRequest {

  private java.lang.Boolean disablePrinting = null;
  private java.lang.Boolean cardNotPresent = null;
  private java.lang.Boolean disableRestartTransactionOnFail = null;
  private long amount;
  private java.lang.Integer cardEntryMethods = null;
  private com.clover.sdk.v3.payments.VaultedCard vaultedCard = null;
  private java.lang.String externalId = null;
  private PayIntent.TransactionType type = null;

  public TransactionRequest(long amount, String externalId) {
    if(externalId == null || externalId.length() > 32) {
      throw new IllegalArgumentException("The externalId must be provided.  The maximum length is 32 characters.");
    }
    this.amount = amount;
    this.externalId = externalId;
  }
    
  /**
  * Set the field value
  * Do not print
  *
  */
  public void setDisablePrinting(java.lang.Boolean disablePrinting) {
    this.disablePrinting = disablePrinting;
  }

  /**
  * Get the field value
  * Do not print
  */
  public java.lang.Boolean getDisablePrinting() {
    return this.disablePrinting;
  }  
  /**
  * Set the field value
  * If true then card not present is accepted
  *
  */
  public void setCardNotPresent(java.lang.Boolean cardNotPresent) {
    this.cardNotPresent = cardNotPresent;
  }

  /**
  * Get the field value
  * If true then card not present is accepted
  */
  public java.lang.Boolean getCardNotPresent() {
    return this.cardNotPresent;
  }  
  /**
  * Set the field value
  * If the transaction times out or fails because of decline, do not restart it
  *
  */
  public void setDisableRestartTransactionOnFail(java.lang.Boolean disableRestartTransactionOnFail) {
    this.disableRestartTransactionOnFail = disableRestartTransactionOnFail;
  }

  /**
  * Get the field value
  * If the transaction times out or fails because of decline, do not restart it
  */
  public java.lang.Boolean getDisableRestartTransactionOnFail() {
    return this.disableRestartTransactionOnFail;
  }  
  /**
  * Set the field value
  * Total amount paid
  *
  */
  public void setAmount(long amount) {
    this.amount = amount;
  }

  /**
  * Get the field value
  * Total amount paid
  */
  public long getAmount() {
    return this.amount;
  }  
  /**
  * Set the field value
  * Allowed entry methods
  *
  */
  public void setCardEntryMethods(java.lang.Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }

  /**
  * Get the field value
  * Allowed entry methods
  */
  public java.lang.Integer getCardEntryMethods() {
    return this.cardEntryMethods;
  }  
  /**
  * Set the field value
  * A saved card
  *
  */
  public void setVaultedCard(com.clover.sdk.v3.payments.VaultedCard vaultedCard) {
    this.vaultedCard = vaultedCard;
  }

  /**
  * Get the field value
  * A saved card
  */
  public com.clover.sdk.v3.payments.VaultedCard getVaultedCard() {
    return this.vaultedCard;
  }  
  /**
  * Set the field value
  * An id that will be persisted with transactions.
  *
  */
  public void setExternalId(java.lang.String externalId) {
    this.externalId = externalId;
  }

  /**
  * Get the field value
  * An id that will be persisted with transactions.
  */
  public java.lang.String getExternalId() {
    return this.externalId;
  }  


  /**
  * Get the field value
  * The type of the transaction.
  */
  abstract public PayIntent.TransactionType getType();
}
