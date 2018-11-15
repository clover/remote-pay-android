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

/**
 * Request object for requesting an auth transaction.
 */
@SuppressWarnings(value="unused")
public class AuthRequest extends TransactionRequest {

  private TipSuggestion tipSuggestion1 = null;
  private TipSuggestion tipSuggestion2 = null;
  private TipSuggestion tipSuggestion3 = null;
  private TipSuggestion tipSuggestion4 = null;

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
   * set custom tip suggestions
   */
  public void setTipSuggestions(TipSuggestion tipSuggestion1, TipSuggestion tipSuggestion2, TipSuggestion tipSuggestion3, TipSuggestion tipSuggestion4){
    this.tipSuggestion1 = tipSuggestion1;
    this.tipSuggestion2 = tipSuggestion2;
    this.tipSuggestion3 = tipSuggestion3;
    this.tipSuggestion4 = tipSuggestion4;
  }
  /**
   * Get the field value
   *
   * @return The array of tip suggestions
   */
  public TipSuggestion [] getTipSuggestions(){
    TipSuggestion [] tipSuggestions = new TipSuggestion[4];
    if(tipSuggestion1 == null && tipSuggestion2 == null && tipSuggestion3 == null && tipSuggestion4 == null) {
      return null;
    }
    tipSuggestions[0] = tipSuggestion1;
    tipSuggestions[1] = tipSuggestion2;
    tipSuggestions[2] = tipSuggestion3;
    tipSuggestions[3] = tipSuggestion4;
    return tipSuggestions;
  }

}
