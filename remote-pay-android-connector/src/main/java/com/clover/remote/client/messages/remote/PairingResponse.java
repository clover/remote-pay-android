package com.clover.remote.client.messages.remote;

public class PairingResponse extends PairingRequest {
  public final String pairingState;
  public final String applicationName;
  public final long millis;

  public PairingResponse(String name, String serialNumber, String pairingState, String applicationName, String authenticationToken, long millis) {
    super(name, serialNumber, authenticationToken);
    this.pairingState = pairingState;
    this.applicationName = applicationName;
    this.millis = millis;
  }
}
