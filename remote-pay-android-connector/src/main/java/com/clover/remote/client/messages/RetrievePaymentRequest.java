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
 * Request object for requesting information on a specific payment
 */
@SuppressWarnings(value="unused")
public class RetrievePaymentRequest extends BaseRequest {
  private String externalPaymentId;

  public RetrievePaymentRequest(String externalPaymentId) {
    this.externalPaymentId = externalPaymentId;
  }

  /**
   * Get the field value
   *
   * @return The externalPaymentId used when a payment was created
   */
  public String getExternalPaymentId() {
    return externalPaymentId;
  }

  /**
   * Set the field value
   *
   * @param externalPaymentId The externalPaymentId used when a payment was created
   */
  public void setExternalPaymentId(String externalPaymentId) {
    this.externalPaymentId = externalPaymentId;
  }
}
