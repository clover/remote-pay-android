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
import com.clover.sdk.v3.payments.VaultedCard;

public abstract class TxRequest {
  private long amount;
  private Long tipAmount;
  private Integer cardEntryMethods;
  private boolean cardNotPresent;
  private VaultedCard vaultedCard;
  private String externalPaymentId;


  public abstract PayIntent.TransactionType getType();

  public long getAmount() {
    return amount;
  }

  public void setAmount(long amount) {
    this.amount = amount;
  }

  public Long getTipAmount() {
    return tipAmount;
  }

  public void setTipAmount(Long tipAmount) {
    this.tipAmount = tipAmount;
  }

  public Integer getCardEntryMethods() {
    return cardEntryMethods;
  }

  public void setCardEntryMethods(Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }

  public boolean isCardNotPresent() {
    return cardNotPresent;
  }

  public void setCardNotPresent(boolean cardNotPresent) {
    this.cardNotPresent = cardNotPresent;
  }

  public com.clover.sdk.v3.payments.VaultedCard getVaultedCard() {
    return vaultedCard;
  }

  public void setVaultedCard(com.clover.sdk.v3.payments.VaultedCard vaultedCard) {
    this.vaultedCard = vaultedCard;
  }

  public String getExternalPaymentId() {
    return externalPaymentId;
  }

  public void setExternalPaymentId(String externalPaymentId) {
    this.externalPaymentId = externalPaymentId;
  }

}
