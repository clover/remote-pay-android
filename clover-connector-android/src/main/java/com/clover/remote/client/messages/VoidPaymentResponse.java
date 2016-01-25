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

public class VoidPaymentResponse extends TransactionResponse {
  private String PaymentId;
  private String TransactionNumber; //optional?
  private String ResponseCode; //optional?
  private String ResponseText; //optional?

  public String getPaymentId() {
    return PaymentId;
  }

  public void setPaymentId(String paymentId) {
    PaymentId = paymentId;
  }

  public String getTransactionNumber() {
    return TransactionNumber;
  }

  public void setTransactionNumber(String transactionNumber) {
    TransactionNumber = transactionNumber;
  }

  public String getResponseCode() {
    return ResponseCode;
  }

  public void setResponseCode(String responseCode) {
    ResponseCode = responseCode;
  }

  public String getResponseText() {
    return ResponseText;
  }

  public void setResponseText(String responseText) {
    ResponseText = responseText;
  }
}