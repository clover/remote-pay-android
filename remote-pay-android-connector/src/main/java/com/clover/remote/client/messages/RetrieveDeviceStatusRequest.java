package com.clover.remote.client.messages;


/**
 * Request object for requesting the current device status. If {@link #sendLastMessage} is true, the device will
 * send the last request it is waiting for a response to such as a signature or payment confirmation request
 */
@SuppressWarnings(value="unused")
public class RetrieveDeviceStatusRequest extends BaseRequest {

  private boolean sendLastMessage;

  public RetrieveDeviceStatusRequest(boolean sendLastMessage) {
    this.sendLastMessage = sendLastMessage;
  }

  /**
   * Get the field value
   *
   * @return Send last message, if true
   */
  public boolean isSendLastMessage() {
    return sendLastMessage;
  }

  /**
   * Set the field value
   *
   * @param sendLastMessage Send last message, if true
   */
  public void setSendLastMessage(boolean sendLastMessage) {
    this.sendLastMessage = sendLastMessage;
  }
}
