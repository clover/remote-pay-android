package com.clover.remote.client.messages;

public class CustomActivityResponse extends BaseResponse {
  public final String action;
  public final String payload;

  public CustomActivityResponse(boolean success, ResultCode code, String action, String payload, String failReason) {
    super(success, code);
    super.setReason(failReason);
    this.action = action;
    this.payload = payload;
  }
}
