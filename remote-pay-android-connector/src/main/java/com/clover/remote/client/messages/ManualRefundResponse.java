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

import com.clover.sdk.v3.payments.Credit;

public class ManualRefundResponse extends BaseResponse {
  private Credit Credit;

  public ManualRefundResponse(boolean success, ResultCode result) {
    super(success, result);
  }

  public com.clover.sdk.v3.payments.Credit getCredit() {
    return Credit;
  }

  public void setCredit(com.clover.sdk.v3.payments.Credit credit) {
    Credit = credit;
  }

}