package com.clover.remote.client.messages;

import java.io.Serializable;

public class IncrementPreauthRequest extends BaseRequest implements Serializable {
  private long amount;
  private String paymentId;

  public long getAmount() {
    return amount;
  }

  public void setAmount(long amount) {
    this.amount = amount;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(String paymentId) {
    this.paymentId = paymentId;
  }
}
