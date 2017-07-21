package com.clover.remote.client.transport;

/**
 * Defines the callback interface for the pairing process
 */
public interface PairingDeviceConfiguration {
  /**
   * Called when a pairing code needs to be entered on the device
   *
   * @param pairingCode code to enter
   */
  void onPairingCode(String pairingCode);

  /**
   * Called when the pairing to the device has completed
   *
   * @param authToken a pairing token which may be provided when reconnecting to the device to bypass the manual pairing step
   */
  void onPairingSuccess(String authToken);
}
