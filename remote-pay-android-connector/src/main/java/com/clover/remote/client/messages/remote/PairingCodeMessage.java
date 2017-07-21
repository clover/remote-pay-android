package com.clover.remote.client.messages.remote;

public class PairingCodeMessage {
  public final String pairingCode;

  public static final String PAIRING_CODE = "PAIRING_CODE";
  public static final String PAIRING_REQUEST = "PAIRING_REQUEST";
  public static final String PAIRING_RESPONSE = "PAIRING_RESPONSE";
  public static final String PAIRED = "PAIRED";
  public static final String INITIAL = "INITIAL";
  public static final String FAILED = "FAILED";


  public PairingCodeMessage(String pairingCode) {
    this.pairingCode = pairingCode;
  }
}