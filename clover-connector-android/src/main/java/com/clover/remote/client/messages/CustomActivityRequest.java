package com.clover.remote.client.messages;

public class CustomActivityRequest extends BaseRequest {
  public final String action;
  private String payload;
  private boolean nonBlocking;

  public CustomActivityRequest(String action) {
    this.action = action;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public boolean isNonBlocking() {
    return nonBlocking;
  }

  public void setNonBlocking(boolean nonBlocking) {
    this.nonBlocking = nonBlocking;
  }
}
