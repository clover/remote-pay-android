package com.clover.remote.client.messages;

/**
 * Request object to open cash drawer
 */
public class OpenCashDrawerRequest extends BaseRequest {

  private String reason = null;
  private String deviceId = null;

  /**
   * Constructor
   *
   * @param reason String describing the reason to open the drawer
   */
  public OpenCashDrawerRequest(String reason){
    this.reason = reason;
  }

  /**
   * Get the field value
   *
   * @return String describing the reason to open the drawer
   */
  public String getReason() {
    return reason;
  }

  /**
   * Get the field value
   *
   * @return deviceId to use
   */
  public String getDeviceId() {
    return deviceId;
  }

  /**
   * Set the field value
   *
   * @param reason string describing reason to open the cash drawer
   */
  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * Set the field value
   *
   * @param deviceId printer to use
   */
  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }
}
