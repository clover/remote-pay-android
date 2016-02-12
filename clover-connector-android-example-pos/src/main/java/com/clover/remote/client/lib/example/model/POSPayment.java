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

package com.clover.remote.client.lib.example.model;

public class POSPayment extends POSExchange {

  private long tipAmount;
  private long cashBackAmount;
  private String orderId;
  private transient POSOrder order;

  public enum Status {
    PAID, VOIDED, REFUNDED, AUTHORIZED
  }

  public POSPayment(String paymentID, String orderID, String employeeID, long amount) {
    this(paymentID, orderID, employeeID, amount, 0, 0);
  }

  public POSPayment(String paymentID, String orderID, String employeeID, long amount, long tip, long cashBack) {

    super(paymentID, orderID, employeeID, amount);

    tipAmount = tip;
    cashBackAmount = cashBack;

  }

  private Status _status;

  public Status getPaymentStatus() {
    return _status;
  }

  public void setPaymentStatus(Status status) {
    _status = status;
  }

  public boolean isVoided() {
    return _status == Status.VOIDED;
  }

  public boolean isRefunded() {
    return _status == Status.REFUNDED;
  }

  public long getTipAmount() {
    return tipAmount;
  }

  public long getCashBackAmount() {
    return cashBackAmount;
  }

  public POSOrder getOrder() {
    return order;
  }

  public void setOrder(POSOrder order) {
    this.order = order;
  }

  public void setTipAmount(long tipAmount) {
    this.tipAmount = tipAmount;
  }

  public long getAmount() {
    return super.getAmount() + getTipAmount();
  }
}
