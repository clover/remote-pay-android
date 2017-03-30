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

import com.clover.remote.Challenge;
import com.clover.sdk.v3.payments.Payment;

import java.io.Serializable;

/**
 * request sent from the Mini for the POS to verify or reject a signature
 */
public class ConfirmPaymentRequest implements Serializable {
  private Payment payment;
  private Challenge[] challenges;

  public Payment getPayment() {
    return payment;
  }

  public void setPayment(Payment payment) {
    this.payment = payment;
  }

  public Challenge[] getChallenges() {
    return challenges;
  }

  public void setChallenges(Challenge[] challenges) {
    this.challenges = challenges;
  }
}
