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

import java.util.Map;

/**
 * Request object for requesting to void a payment
 */
@SuppressWarnings(value="unused")
public class VoidPaymentRequest extends BaseRequest {
  private String paymentId;
  private String voidReason;

  private String employeeId;//optional TODO: Revisit
  private String orderId; //optional TODO: Revisit
  private boolean disablePrinting; //optional
  private boolean disableReceiptSelection; //optional
  private Map<String, String> extras = null;

  /**
   * Get the field value
   *
   * @return The unique identifier of the associated payment
   */
  public String getPaymentId() {
    return paymentId;
  }

  /**
   * Set the field value
   *
   * @param paymentId The unique identifier of the associated payment
   */
  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }

  /**
   * Get the field value
   *
   * @return Reason for void
   */
  public String getVoidReason() {
    return voidReason;
  }

  /**
   * Set the field value
   *
   * @param voidReason Reason for void
   */
  public void setVoidReason(String voidReason) {
    this.voidReason = voidReason;
  }

  /**
   * Get the field value
   *
   * @return The id of the employee requesting the void
   */
  public String getEmployeeId() {
    return employeeId;
  }

  /**
   * Set the field value
   *
   * @param employeeId The id of the employee requesting the void
   */
  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  /**
   * Get the field value
   *
   * @return The unique identifier of the associated order
   */
  public String getOrderId() {
    return orderId;
  }

  /**
   * Set the field value
   *
   * @param orderId The unique identifier of the associated order
   */
  public void setOrderId(String orderId) {
    this.orderId = orderId;
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

  /**
   * Get the field value
   * @return Extra pass-through data used by external systems.
   */
  public Map<String, String> getExtras() {
    return extras;
  }

  /**
   * Set the field value
   * @param extras Extra pass-through data used by external systems.
   */
  public void setExtras(Map<String, String> extras) {
    this.extras = extras;
  }
}


