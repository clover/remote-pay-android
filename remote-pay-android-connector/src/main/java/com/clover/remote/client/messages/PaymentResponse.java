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
import com.clover.sdk.v3.payments.Result;

/**
 * Base reponse use for callbacks that contain
 * payments. Sale, Auth & PreAuth
 */
@SuppressWarnings(value="unused")
public class PaymentResponse extends BaseResponse {

  private com.clover.sdk.v3.payments.Payment payment = null;
  private java.lang.Boolean isSale = false;
  private java.lang.Boolean isPreAuth = false;
  private java.lang.Boolean isAuth = false;
  private Signature2 signature = null;


  public PaymentResponse(boolean success, ResultCode result) {
    super(success, result);
  }
  /**
  * Set the field value
  * The payment from the sale
  *
  */
  public void setPayment(com.clover.sdk.v3.payments.Payment payment) {
    this.payment = payment;
  }

  /**
  * Get the field value
  * The payment from the sale
  */
  public com.clover.sdk.v3.payments.Payment getPayment() {
    return this.payment;
  }  

  /**
  * Get the field value
  */
  public boolean isSale() {
    return CardTransactionType.AUTH.equals(payment.getCardTransaction().getType()) &&
        Result.SUCCESS.equals(payment.getResult());
  }  

  /**
  * Get the field value
  */
  public boolean isAuth() {
    return CardTransactionType.PREAUTH.equals(payment.getCardTransaction().getType()) &&
        Result.SUCCESS.equals(payment.getResult());
  }

  /**
  * Get the field value
  */
  public boolean isPreAuth() {
    return CardTransactionType.PREAUTH.equals(payment.getCardTransaction().getType()) &&
        Result.AUTH.equals(payment.getResult());
  }  
  /**
  * Set the field value
  */
  public void setSignature(Signature2 signature) {
    this.signature = signature;
  }

  /**
  * Get the field value
  */
  public Signature2 getSignature() {
    return this.signature;
  }
}
