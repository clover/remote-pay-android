package com.clover.remote.client.messages;

import com.clover.common2.Signature2;

public class SignatureResponse extends BaseResponse {
  private Signature2 signature;

  public SignatureResponse(boolean success, ResultCode result, Signature2 signature) {
    super(success, result);
    this.signature = signature;
  }

  public Signature2 getSignature() {
    return signature;
  }
}
