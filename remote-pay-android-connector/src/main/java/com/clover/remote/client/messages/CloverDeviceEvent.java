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

import com.clover.remote.InputOption;

/**
 * Encapsulates the start or stop of a Clover Device activity (e.g. a UI event)
 */
@SuppressWarnings(value="unused")
public class CloverDeviceEvent {

  /**
   * Enum corresponding to valid UI State events from the device
   */
  public enum DeviceEventState {
    // payment flow
    START,
    FAILED,
    FATAL,
    TRY_AGAIN,
    INPUT_ERROR,
    PIN_BYPASS_CONFIRM,
    CANCELED,
    TIMED_OUT,
    DECLINED,
    VOIDED,
    CONFIGURING,
    PROCESSING,
    REMOVE_CARD,
    PROCESSING_GO_ONLINE,
    PROCESSING_CREDIT,
    PROCESSING_SWIPE,
    SELECT_APPLICATION,
    PIN_PAD,
    MANUAL_CARD_NUMBER,
    MANUAL_CARD_CVV,
    MANUAL_CARD_CVV_UNREADABLE,
    MANUAL_CARD_EXPIRATION,
    SELECT_ACCOUNT,
    CASHBACK_CONFIRM,
    CASHBACK_SELECT,
    CONTACTLESS_TAP_REQUIRED,
    VOICE_REFERRAL_RESULT,
    CONFIRM_PARTIAL_AUTH,
    PACKET_EXCEPTION,
    CONFIRM_DUPLICATE_CHECK,

    // verify CVM flow
    VERIFY_SIGNATURE_ON_PAPER,
    VERIFY_SIGNATURE_ON_PAPER_CONFIRM_VOID,
    VERIFY_SIGNATURE_ON_SCREEN,
    VERIFY_SIGNATURE_ON_SCREEN_CONFIRM_VOID,
    ADD_SIGNATURE,
    SIGNATURE_ON_SCREEN_FALLBACK,
    RETURN_TO_MERCHANT,
    SIGNATURE_REJECT,
    ADD_SIGNATURE_CANCEL_CONFIRM,

    // add tip flow
    ADD_TIP,

    // receipt options flow
    RECEIPT_OPTIONS,

    // tender handling flow
    HANDLE_TENDER,

    // starting custom activity, called from RTKA
    STARTING_CUSTOM_ACTIVITY,
    CUSTOM_ACTIVITY,

    //Canada-specific
    SELECT_WITHDRAW_FROM_ACCOUNT,
    VERIFY_SURCHARGES,
    VOID_CONFIRM
  }

  private DeviceEventState eventState;
  private int code;
  private String message;
  private InputOption[] inputOptions;

  /**
   * Get the field value
   *
   * @return the event
   */
  public DeviceEventState getEventState() {
    return eventState;
  }

  /**
   * Set the field value
   *
   * @param eventState the event
   */
  public void setEventState(DeviceEventState eventState) {
    this.eventState = eventState;
  }

  /**
   * Get the field value
   *
   * @return code
   */
  public int getCode() {
    return code;
  }

  /**
   * Set the field value
   *
   * @param code code
   */
  public void setCode(int code) {
    this.code = code;
  }

  /**
   * Get the field value
   *
   * @return the display message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Set the field value
   *
   * @param message the display message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get the field value
   *
   * @return available input options
   */
  public InputOption[] getInputOptions() {
    return inputOptions;
  }

  /**
   * Set the field value
   *
   * @param inputOptions available input options
   */
  public void setInputOptions(InputOption[] inputOptions) {
    this.inputOptions = inputOptions;
  }
}

