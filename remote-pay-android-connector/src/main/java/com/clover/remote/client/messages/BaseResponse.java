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
 * The base for responses
 */
@SuppressWarnings(value="unused")
public class BaseResponse {

  private boolean success = false;
  private ResultCode result = null;
  private String reason = null;
  private String message = null;

  public BaseResponse() {
  }

  /**
   * Constructor
   *
   * @param success If true then the requested operation succeeded
   * @param result The result of the requested operation
   */
  public BaseResponse(boolean success, ResultCode result) {
    this.success = success;
    this.result = result;
  }

  /**
   * Set the field value
   *
   * @param success If true then the requested operation succeeded
   */
  public void setSuccess(boolean success) {
    this.success = success;
  }

  /**
   * Get the field value
   *
   * @return If true then the requested operation succeeded
   */
  public final boolean isSuccess() {
    return this.success;
  }

  /**
   * Set the field value
   *
   * @param result The result of the requested operation
   */
  public void setResult(ResultCode result) {
    this.result = result;
  }

  /**
   * Get the field value
   *
   * @return The result of the requested operation
   */
  public ResultCode getResult() {
    return this.result;
  }

  /**
   * Set the field value
   *
   * @param reason Optional information about result.
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * Get the field value
   *
   * @return Optional information about result.
   */
  public String getReason() {
    return this.reason;
  }

  /**
   * Set the field value
   *
   * @param message Detailed information about result.
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Get the field value
   *
   * @return Detailed information about result.
   */
  public String getMessage() {
    return this.message;
  }
}
