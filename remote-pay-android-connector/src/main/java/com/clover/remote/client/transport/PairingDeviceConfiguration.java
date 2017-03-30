package com.clover.remote.client.transport;


public interface PairingDeviceConfiguration {
  public void onPairingCode(String pairingCode);
  public void onPairingSuccess(String authToken);
}
