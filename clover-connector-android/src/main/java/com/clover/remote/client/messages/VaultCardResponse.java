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

import com.clover.sdk.v3.payments.VaultedCard;

/**
 * Created by blakewilliams on 1/17/16.
 */
public class VaultCardResponse extends BaseResponse {
  private VaultedCard card;
  private String code;
  private String reason;

  public VaultCardResponse(VaultedCard card, String code, String reason) {
    this.card = card;
    this.code = code;
    this.reason = code;
  }

  public VaultedCard getCard() {
    return card;
  }

  public void setCard(VaultedCard card) {
    this.card = card;
  }

  @Override public String getCode() {
    return code;
  }

  @Override public void setCode(String code) {
    this.code = code;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
