package com.clover.remote.client.messages;

public class TipResponse extends BaseResponse {
  private Long tipAmount;

  public TipResponse(boolean success, ResultCode result, Long tipAmount) {
    super(success, result);
    this.tipAmount = tipAmount;
  }

  public Long getTipAmount() {
    return tipAmount;
  }
}
