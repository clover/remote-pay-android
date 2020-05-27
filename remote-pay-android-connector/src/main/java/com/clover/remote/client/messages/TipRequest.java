package com.clover.remote.client.messages;

import com.clover.sdk.v3.merchant.TipSuggestion;

public class TipRequest extends BaseRequest {
  private Long tippableAmount;
  private TipSuggestion[] tipSuggestions;

  public TipRequest(Long tippableAmount, TipSuggestion[] suggestions) {
    this.tippableAmount = tippableAmount;
    this.tipSuggestions = suggestions;
  }

  public Long getTippableAmount() {
    return tippableAmount;
  }

  public void setTippableAmount(Long tippableAmount) {
    this.tippableAmount = tippableAmount;
  }

  public TipSuggestion[] getTipSuggestions() {
    return tipSuggestions;
  }

  public void setTipSuggestions(TipSuggestion[] suggestions) {
    this.tipSuggestions = suggestions;
  }
}
