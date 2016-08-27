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


public class ReadCardDataRequest {

  Integer cardEntryMethods;
  boolean isForceSwipePinEntry;

  /**
   *
   * @param cardEntryMethods - if null, will get the default configured methods. Initially SWIPE, CHIP and CONTACTLESS. The Integer can be
   *                         an OR's value of CloverConnector.CARD_ENTRY_METHOD_MAG_STRIPE | CloverConnector.CARD_ENTRY_METHOD_MAG_MANUAL, etc.
   */
  public ReadCardDataRequest(Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }

  public boolean isForceSwipePinEntry() {
    return isForceSwipePinEntry;
  }

  public void setForceSwipePinEntry(boolean forceSwipePinEntry) {
    isForceSwipePinEntry = forceSwipePinEntry;
  }

  public Integer getCardEntryMethods() {
    return cardEntryMethods;
  }

  public void setCardEntryMethods(Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }
}
