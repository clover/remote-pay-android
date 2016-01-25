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

import java.util.UUID;

public class BaseResponse {
  public static final String SUCCESS = "SUCCESS";
  public static final String CANCEL = "CANCEL";
  public static final String FAIL = "FAIL";
  public static final String ERROR = "ERROR";

  private UUID requestMessageUUID;
  /*
  the status of the transaction activity.
  */
  private String code;//SUCCESS, CANCEL, ERROR, FAIL - TODO: enum

  protected BaseResponse() {

  }

  protected BaseResponse(UUID requestUUID) {
    requestMessageUUID = requestUUID;
  }

  protected void setRequestMessageUUID(UUID requestID) {
    if (requestMessageUUID != null) {
      throw new IllegalArgumentException("Request Message UUID is already set");
    }
    requestMessageUUID = requestID;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }
}