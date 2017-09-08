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
 * Request object for requesting to read card data
 */
@SuppressWarnings(value="unused")
public class ReadCardDataRequest extends BaseRequest {

  private Integer cardEntryMethods;
  private boolean isForceSwipePinEntry;

  /**
   * Constructor
   *
   * @param cardEntryMethods - if null, will get the default configured methods. Initially SWIPE, CHIP and CONTACTLESS. The Integer can be
   *                         an OR's value of CloverConnector.CARD_ENTRY_METHOD_MAG_STRIPE | CloverConnector.CARD_ENTRY_METHOD_MAG_MANUAL, etc.
   */
  public ReadCardDataRequest(Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }

  /**
   * Get the field value
   *
   * @return If true, then if the card is swiped, a pin entry must be done
   */
  public boolean isForceSwipePinEntry() {
    return isForceSwipePinEntry;
  }

  /**
   * Set the field value
   *
   * @param forceSwipePinEntry If true, then if the card is swiped, a pin entry must be done
   */
  public void setForceSwipePinEntry(boolean forceSwipePinEntry) {
    isForceSwipePinEntry = forceSwipePinEntry;
  }

  /**
   * Get the field value
   *
   * @return configured card entry methods
   */
  public Integer getCardEntryMethods() {
    return cardEntryMethods;
  }

  /**
   * Set the field value
   *
   * @param cardEntryMethods configured card entry methods
   */
  public void setCardEntryMethods(Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }
}
