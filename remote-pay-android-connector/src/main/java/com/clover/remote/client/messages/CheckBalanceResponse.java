package com.clover.remote.client.messages;

public class CheckBalanceResponse extends BaseResponse {
  private Long amount;

  public CheckBalanceResponse(boolean success, ResultCode result, Long amount) {
    super(success, result);
    this.amount = amount;
  }

  public Long getAmount() {
    return amount;
  }
}
