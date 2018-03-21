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
 * Request object to refund a payment, either partially or fully
 */
@SuppressWarnings(value="unused")
public class RefundPaymentRequest extends BaseRequest {
  private String orderId;
  private String paymentId;
  private long amount; // optional
  private boolean fullRefund; // optional
  private boolean disablePrinting; // optional
  private boolean disableReceiptSelection; // optional

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
   * @return Amount to be refunded
   */
  public long getAmount() {
    return amount;
  }

  /**
   * Set the field value
   *
   * @param amount Amount to be refunded
   */
  public void setAmount(long amount) {
    this.amount = amount;
  }

  /**
   * Get the field value
   *
   * @return If true, then it is a full refund.  The amount will be ignored.
   */
  public boolean isFullRefund() {
    return fullRefund;
  }

  /**
   * Set the field value
   *
   * @param fullRefund If true, then it is a full refund.  The amount will be ignored.
   */
  public void setFullRefund(boolean fullRefund) {
    this.fullRefund = fullRefund;
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

  /**
   * Get the field value
   *
   * @return The disableReceiptSelection transaction override value
   */
  public boolean getDisableReceiptSelection() {
    return disableReceiptSelection;
  }

  /**
   * Set the field value
   *
   * @param disableReceiptSelection The transaction override to turn off the Clover customer print screen
   */
  public void setDisableReceiptSelection(boolean disableReceiptSelection) {
    this.disableReceiptSelection = disableReceiptSelection;
  }
}

