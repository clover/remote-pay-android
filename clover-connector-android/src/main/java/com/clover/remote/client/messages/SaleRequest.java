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

import com.clover.sdk.internal.PayIntent;

public class SaleRequest extends TxRequest {
  private Long tippableAmount;
  private Long taxAmount;
  private Boolean allowOfflinePayment;
  private Boolean approveOfflinePaymentWithoutPrompt;
  private Long tipAmount;

  private boolean disableCashback;
  private boolean disableTip;
  private boolean disablePrinting;
  private boolean disableRestartTransactionOnFail;

  public Boolean getAllowOfflinePayment() {
    return allowOfflinePayment;
  }

  public void setAllowOfflinePayment(Boolean allowOfflinePayment) {
    this.allowOfflinePayment = allowOfflinePayment;
  }

  public Boolean getApproveOfflinePaymentWithoutPrompt() {
    return approveOfflinePaymentWithoutPrompt;
  }

  public void setApproveOfflinePaymentWithoutPrompt(Boolean approveOfflinePaymentWithoutPrompt) {
    this.approveOfflinePaymentWithoutPrompt = approveOfflinePaymentWithoutPrompt;
  }
  public PayIntent.TransactionType getType() {
    return PayIntent.TransactionType.PAYMENT;
  }

  public Long getTippableAmount() {
    return tippableAmount;
  }

  public void setTippableAmount(Long tippableAmount) {
    this.tippableAmount = tippableAmount;
  }

  public Long getTaxAmount() {
    return taxAmount;
  }

  public void setTaxAmount(Long taxAmount) {
    this.taxAmount = taxAmount;
  }

  protected boolean getDisableCashback() { return disableCashback; }

  protected void setDisableCashback(boolean disableCashback) { this.disableCashback = disableCashback; }

  protected boolean getDisableTip() { return disableTip; }

  protected void setDisableTip(boolean disableTip) { this.disableTip = disableTip; }

  protected boolean getDisablePrinting() { return disablePrinting; }

  protected void setDisablePrinting(boolean disablePrinting) { this.disablePrinting = disablePrinting; }

  protected boolean getDisableRestartTransactionOnFail() { return disableRestartTransactionOnFail; }

  protected void setDisableRestartTransactionOnFail(boolean disableRestartTransactionOnFail) { this.disableRestartTransactionOnFail = disableRestartTransactionOnFail; }

  public Long getTipAmount() {
    return tipAmount;
  }

  public void setTipAmount(Long tipAmount) {
    this.tipAmount = tipAmount;
  }

}
