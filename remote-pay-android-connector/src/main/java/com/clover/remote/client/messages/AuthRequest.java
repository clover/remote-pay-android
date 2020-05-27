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

import com.clover.sdk.v3.merchant.TipSuggestion;

/**
 * Request object for requesting an auth transaction.
 */
@SuppressWarnings(value="unused")
public class AuthRequest extends TransactionRequest {

  /**
   * Constructor
   *
   * @param amount     The amount of the transaction. This includes amount, tax, service charges, etc. except the tip
   * @param externalId An id assigned by the POS that can be used to track a payment through the Clover system
   */
  public AuthRequest(long amount, String externalId) {
    super(amount, externalId);
  }

  /**
   * Set the field value
   *
   * @param tipSuggestion1 - 4 set the tip suggestions
   */
  public void setTipSuggestions(TipSuggestion tipSuggestion1, TipSuggestion tipSuggestion2, TipSuggestion tipSuggestion3, TipSuggestion tipSuggestion4){
    super.setTipSuggestions(new TipSuggestion[] { tipSuggestion1, tipSuggestion2, tipSuggestion3, tipSuggestion4 });
  }

}
