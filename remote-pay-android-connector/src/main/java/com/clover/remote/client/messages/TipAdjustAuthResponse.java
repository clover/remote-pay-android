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

/**
 * Response object for a request to adjust the tip of a prior auth
 */
@SuppressWarnings(value="unused")
public class TipAdjustAuthResponse extends BaseResponse {
  private String paymentId;
  private long tipAmount;

  /**
   * Constructor
   *
   * @param success If true then the requested operation succeeded
   * @param result The result of the requested operation
   */
  public TipAdjustAuthResponse(boolean success, ResultCode result) {
    super(success, result);
  }

  /**
   * Get the field value
   *
   * @return The payment id from the authorization payment, or captured pre-auth payment
   */
  public String getPaymentId() {
    return paymentId;
  }

  /**
   * Set the field value
   *
   * @param paymentId The payment id from the authorization payment, or captured pre-auth payment
   */
  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  /**
   * Get the field value
   *
   * @return The amount paid in tips
   */
  public long getTipAmount() {
    return tipAmount;
  }

  /**
   * Set the field value
   *
   * @param tipAmount The amount paid in tips
   */
  public void setTipAmount(long tipAmount) {
    this.tipAmount = tipAmount;
  }
}