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
 * General error used for reporting error events
 * to the POS
 */
@SuppressWarnings(value="unused")
public class CloverDeviceErrorEvent {

  /**
   * Type of device error
   */
  public enum CloverDeviceErrorType {
    COMMUNICATION_ERROR,
    VALIDATION_ERROR,
    EXCEPTION
  }

  private final CloverDeviceErrorType errorType;
  private final int code;
  private final String message;

  /**
   * Constructor
   *
   * @param errorType error type
   * @param devCode error code
   * @param msg description of the error
   */
  public CloverDeviceErrorEvent(CloverDeviceErrorType errorType, int devCode, String msg) {
    this.errorType = errorType;
    code = devCode;
    message = msg;
  }

  /**
   * Get the field value
   *
   * @return error code
   */
  public int getCode() {
    return code;
  }

  /**
   * Get the field value
   *
   * @return description of the error
   */
  public String getMessage() {
    return message;
  }

  /**
   * Get the field value
   *
   * @return error type
   */
  public CloverDeviceErrorType getErrorType() {
    return errorType;
  }
}