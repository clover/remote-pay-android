/*
 * Copyright (C) 2017 Clover Network, Inc.
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

import com.clover.remote.ExternalDeviceState;
import com.clover.remote.ExternalDeviceStateData;
import com.clover.remote.client.messages.BaseResponse;
import com.clover.remote.client.messages.ResultCode;

/**
 * Response object for a retrieve device status request
 */
@SuppressWarnings(value="unused")
public class InvalidStateTransitionResponse extends BaseResponse {
  private final String requestedTransition;
  private final ExternalDeviceState state;
  private final ExternalDeviceStateData data;

  /**
   * Constructor
   *
   * @param result If true then the requested operation succeeded
   * @param code The result of the requested operation
   * @param state The state of the device
   * @param data Additional optional relevant information for the state
   */
  public InvalidStateTransitionResponse(boolean result, ResultCode code, String reason, String requestedTransition, ExternalDeviceState state, ExternalDeviceStateData data) {
    super(result, code);
    this.setReason(reason);
    this.requestedTransition = requestedTransition;
    this.state = state;
    this.data = data;
  }

  /**
   * Get the field value
   *
   * @return the state of the device
   */
  public String getRequestedTransition() {
    return requestedTransition;
  }

  /**
   * Get the field value
   *
   * @return the state of the device
   */
  public ExternalDeviceState getState() {
    return state;
  }

  /**
   * Get the field value
   *
   * @return optionally contains relevant information for the state
   */
  public ExternalDeviceStateData getData() {
    return data;
  }
}
