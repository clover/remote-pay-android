package com.clover.remote.client.messages;

@SuppressWarnings(value="unused")
public class CustomActivityRequest extends BaseRequest {
  private final String action;
  private String payload;
  private boolean nonBlocking;

  /**
   * Constructor
   *
   * @param action The name of the action for this activity
   */
  public CustomActivityRequest(String action) {
    this.action = action;
  }

  /**
   * Get the field value
   *
   * @return The name of the action for this activity
   */
  public String getAction() {
    return action;
  }

  /**
   * Get the field value
   *
   * @return Data for this activity
   */
  public String getPayload() {
    return payload;
  }

  /**
   * Set the field value
   *
   * @param payload Data for this activity
   */
  public void setPayload(String payload) {
    this.payload = payload;
  }

  /**
   * Get the field value
   *
   * @return If true, then the activity can be stopped in regular execution
   */
  public boolean isNonBlocking() {
    return nonBlocking;
  }

  /**
   * Set the field value
   *
   * @param nonBlocking If true, then the activity can be stopped in regular execution
   */
  public void setNonBlocking(boolean nonBlocking) {
    this.nonBlocking = nonBlocking;
  }
}
