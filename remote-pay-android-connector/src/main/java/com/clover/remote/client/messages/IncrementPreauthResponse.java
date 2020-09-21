package com.clover.remote.client.messages;

import com.clover.sdk.v3.payments.Authorization;

public class IncrementPreauthResponse extends PaymentResponse {
  private Authorization authorization;

  public IncrementPreauthResponse(boolean success, ResultCode result) {
    super(success, result);
  }

  public Authorization getAuthorization() {
    return authorization;
  }

  public void setAuthorization(Authorization authorization) {
    this.authorization = authorization;
  }
}
