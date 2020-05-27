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

import com.clover.remote.QueryStatus;
import com.clover.sdk.v3.payments.Payment;

/**
 * Response object for a retrieve payment request
 */
@SuppressWarnings(value="unused")
public class RetrievePaymentResponse extends BaseResponse {
  private final String externalPaymentId;
  private final Payment payment;
  private final QueryStatus queryStatus;

  /**
   * Constructor
   *
   * @param code The result of the requested operation
   * @param message Detailed information about result
   * @param externalPaymentId the request external payment id
   * @param queryStatus the status of the payment
   * @param payment the payment object, if found
   */
  public RetrievePaymentResponse(ResultCode code, String reason, String message, String externalPaymentId, QueryStatus queryStatus, Payment payment) {
    super(code == ResultCode.SUCCESS, code);
    this.setMessage(message);
    this.setReason(reason);
    this.externalPaymentId = externalPaymentId;
    this.payment = payment;
    this.queryStatus = queryStatus;
  }

  /**
   * Get the field value
   *
   * @return the request external payment id
   */
  public String getExternalPaymentId(){
    return externalPaymentId;
  }

  /**
   * Get the field value
   *
   * @return the payment object, if found
   */
  public Payment getPayment() {
    return payment;
  }

  /**
   * Get the field value
   *
   * @return the status of the payment
   */
  public QueryStatus getQueryStatus() {
    return queryStatus;
  }
}
