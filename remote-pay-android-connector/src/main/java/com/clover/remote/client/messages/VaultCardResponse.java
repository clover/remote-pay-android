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
 * Response object for a vault card request
 */
@SuppressWarnings(value="unused")
public class VaultCardResponse extends BaseResponse {
  private final VaultedCard card;

  /**
   * Constructor
   *
   * @param success If true then the requested operation succeeded
   * @param result The result of the requested operation
   * @param card The vaulted card
   */
  public VaultCardResponse(boolean success, ResultCode result, VaultedCard card) {
    super(success, result);
    this.card = card;
  }

  /**
   * Get the field value
   *
   * @return The vaulted card
   */
  public VaultedCard getCard() {
    return card;
  }
}
