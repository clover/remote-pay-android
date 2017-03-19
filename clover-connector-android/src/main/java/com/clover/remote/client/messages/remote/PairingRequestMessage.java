package com.clover.remote.client.messages.remote;

import com.google.gson.Gson;

public class PairingRequestMessage {
  public final String id;
  public final String method = "PAIRING_REQUEST";
  public final String payload;
  public final String type = "COMMAND";

  private static int reqNumber = 0;

  public PairingRequestMessage(PairingRequest request) {
    id = "PR-"+(reqNumber++);
    this.payload = new Gson().toJson(request);
  }
}
