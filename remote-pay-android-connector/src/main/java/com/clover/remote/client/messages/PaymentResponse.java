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

import com.clover.common2.Signature2;
import com.clover.sdk.v3.payments.CardTransactionType;
import com.clover.sdk.v3.payments.Payment;
import com.clover.sdk.v3.payments.Result;

/**
 * Base reponse use for callbacks that contain
 * payments. Sale, Auth & PreAuth
 */
@SuppressWarnings(value="unused")
public class PaymentResponse extends BaseResponse {

  private Payment payment = null;
  private Boolean isSale = false;
  private Boolean isPreAuth = false;
  private Boolean isAuth = false;
  private Signature2 signature = null;

  /**
   * Constructor
   *
   * @param success If true then the requested operation succeeded
   * @param result The result of the requested operation
   */
  public PaymentResponse(boolean success, ResultCode result) {
    super(success, result);
  }

  /**
   * Set the field value
   *
   * @param payment The payment from the sale
   */
  public void setPayment(Payment payment) {
    this.payment = payment;
  }

  /**
   * Get the field value
   *
   * @return The payment from the sale
   */
  public Payment getPayment() {
    return this.payment;
  }

  /**
   * Check to see if the payment was processed as a sale
   *
   * @return true if processed as a sale
   */
  public boolean isSale() {
    if (payment != null && payment.getCardTransaction() != null) {
      return CardTransactionType.AUTH.equals(payment.getCardTransaction().getType()) &&
             Result.SUCCESS.equals(payment.getResult());
    } else {
      return false;
    }
  }  

  /**
   * Check to see if the payment was processed as an auth
   *
   * @return true if processed as an auth
   */
  public boolean isAuth() {
    if (payment != null && payment.getCardTransaction() != null) {
      return CardTransactionType.PREAUTH.equals(payment.getCardTransaction().getType()) &&
             Result.SUCCESS.equals(payment.getResult());
    } else {
      return false;
    }
  }

  /**
   * Check to see if the payment was processed as a pre-auth
   *
   * @return true if processed as a pre-auth
   */
  public boolean isPreAuth() {
    if (payment != null && payment.getCardTransaction() != null) {
      return CardTransactionType.PREAUTH.equals(payment.getCardTransaction().getType()) &&
             Result.AUTH.equals(payment.getResult());
    } else {
      return false;
    }
  }

  /**
   * Set the field value
   *
   * @param signature signature collected for the payment
   */
  public void setSignature(Signature2 signature) {
    this.signature = signature;
  }

  /**
   * Get the field value
   *
   * @return signature collected for the payment
   */
  public Signature2 getSignature() {
    return this.signature;
  }
}
