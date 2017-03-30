package com.clover.remote.client.messages.remote;

/**
 * Created by blakewilliams on 11/18/16.
 */
public class PairingRequest {

  public final String method = "PAIRING_REQUEST";
  public final String serialNumber;
  public final String name;
  public final String authenticationToken;

  public PairingRequest(String name, String serialNumber, String token) {
    this.name = name;
    this.serialNumber = serialNumber;
    this.authenticationToken = token;
  }
}
