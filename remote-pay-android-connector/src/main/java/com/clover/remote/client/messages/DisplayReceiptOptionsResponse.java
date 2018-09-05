package com.clover.remote.client.messages;

import com.clover.remote.ResultStatus;

public class DisplayReceiptOptionsResponse extends BaseResponse {
  private ResultStatus status;

  public DisplayReceiptOptionsResponse(ResultStatus status, String reason) {
    this.status = status;
    this.setReason(reason);
  }

  public ResultStatus getStatus() {
    return status;
  }
}
