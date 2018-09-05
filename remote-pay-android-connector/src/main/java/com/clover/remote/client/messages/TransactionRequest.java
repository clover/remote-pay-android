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
public abstract class TransactionRequest extends BaseTransactionRequest {

  private Long signatureThreshold = null;
  private DataEntryLocation signatureEntryLocation = null;
  private Boolean autoAcceptSignature = null;

  /**
   * Constructor
   *
   * @param amount The amount of the transaction. This includes amount, tax, service charges, etc. except the tip
   * @param externalId An id assigned by the POS that can be used to track a payment through the Clover system
   */
  public TransactionRequest(long amount, String externalId) {
    super(amount, externalId);
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
}
