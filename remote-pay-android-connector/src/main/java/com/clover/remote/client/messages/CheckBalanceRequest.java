package com.clover.remote.client.messages;

public class CheckBalanceRequest extends BaseRequest {
  public Integer cardEntryMethods = null;

  public CheckBalanceRequest(Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }
}
