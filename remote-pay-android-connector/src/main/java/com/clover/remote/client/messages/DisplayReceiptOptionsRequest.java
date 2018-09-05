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

import com.clover.remote.client.messages.BaseRequest;

/**
 * Request object to display receipt options
 */
@SuppressWarnings(value="unused")
public class DisplayReceiptOptionsRequest extends BaseRequest {
  private String orderId;
  private String paymentId;
  private String refundId;
  private String creditId;
  private boolean disablePrinting; // optional

  public String getCreditId() {
    return creditId;
  }

  public void setCreditId(String creditId) {
    this.creditId = creditId;
  }

  public String getRefundId() {
    return refundId;
  }

  public void setRefundId(String refundId) {
    this.refundId = refundId;
  }

  /**
   * Get the field value
   *
   * @return unique identifier of the order to be printed
   */
  public String getOrderId() {
    return orderId;
  }

  /**
   * Set the field value
   *
   * @param orderId unique identifier of the order to be printed
   */
  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  /**
   * Get the field value
   *
   * @return unique identifier of the payment to be printed
   */
  public String getPaymentId() {
    return paymentId;
  }

  /**
   * Set the field value
   *
   * @param paymentId unique identifier of the payment to be printed
   */
  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  /**
   * Get the field value
   *
   * @return The disablePrinting transaction override value
   */
  public boolean getDisablePrinting() {
    return disablePrinting;
  }

  /**
   * Set the field value
   *
   * @param disablePrinting The transaction override to turn off Clover device printing
   */
  public void setDisablePrinting(boolean disablePrinting) {
    this.disablePrinting = disablePrinting;
  }
}

