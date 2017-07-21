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
 * Request to capture a payment that had been previously
 * obtained from a preAuth(PreAuthRequest request) call
 */
@SuppressWarnings("unused")
public class CapturePreAuthRequest extends BaseRequest {
  private String paymentID;
  private long amount;
  private long tipAmount;

  /**
   * Get the field value
   *
   * @return unique identifier for the payment
   */
  public String getPaymentID() {
    return paymentID;
  }

  /**
   * Set the field value
   *
   * @param paymentID unique identifier for the payment
   */
  public void setPaymentID(String paymentID) {
    this.paymentID = paymentID;
  }

  /**
   * Get the field value
   *
   * @return total amount paid
   */
  public long getAmount() {
    return amount;
  }

  /**
   * Set the field value
   *
   * @param amount total amount paid
   */
  public void setAmount(long amount) {
    this.amount = amount;
  }

  /**
   * Get the field value
   *
   * @return amount paid in tips
   */
  public long getTipAmount() {
    return tipAmount;
  }

  /**
   * Set the field value
   *
   * @param tipAmount amount paid in tips
   */
  public void setTipAmount(long tipAmount) {
    this.tipAmount = tipAmount;
  }
}
