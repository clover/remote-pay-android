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

import com.clover.sdk.v3.payments.Refund;

/**
 * Response object for a request to refund a payment, either partially or fully
 */
@SuppressWarnings(value="unused")
public class RefundPaymentResponse extends BaseResponse {
  private String orderId;
  private String paymentId;
  private Refund refund;

  /**
   * Constructor
   *
   * @param success If true then the requested operation succeeded
   * @param result The result of the requested operation
   */
  public RefundPaymentResponse(boolean success, ResultCode result) {
    super(success, result);
  }

  /**
   * Get the field value
   *
   * @return unique identifier of the order to be refunded
   */
  public String getOrderId() {
    return orderId;
  }

  /**
   * Set the field value
   *
   * @param orderId unique identifier of the order to be refunded
   */
  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  /**
   * Get the field value
   *
   * @return unique identifier of the payment to be refunded
   */
  public String getPaymentId() {
    return paymentId;
  }

  /**
   * Set the field value
   *
   * @param paymentId unique identifier of the payment to be refunded
   */
  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  /**
   * Get the field value
   *
   * @return The actual refund made
   */
  public Refund getRefund() {
    return refund;
  }

  /**
   * Set the field value
   *
   * @param refund The actual refund made
   */
  public void setRefund(Refund refund) {
    this.refund = refund;
  }

}