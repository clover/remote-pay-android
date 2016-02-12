package com.clover.remote.client.messages;

import com.clover.common2.payments.PayIntent;

/**
 * Created by blakewilliams on 2/2/16.
 */
public class PreAuthRequest extends SaleRequest{

  @Override public PayIntent.TransactionType getType() {
    return PayIntent.TransactionType.AUTH;
  }
}
