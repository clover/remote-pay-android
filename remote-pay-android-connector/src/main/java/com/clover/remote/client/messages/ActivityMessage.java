package com.clover.remote.client.messages;

/**
 * Base class for custom activity message communication
 */
@SuppressWarnings(value="unused")
public class ActivityMessage {
  private final String action;
  private final String payload;

  /**
   * Constructor
   *
   * @param action the custom activity action
   * @param payload the custom activity payload
   */
  public ActivityMessage(String action, String payload) {
    this.action = action;
    this.payload = payload;
  }

  /**
   * Get the field value
   *
   * @return the custom activity action
   */
  public String getAction() {
    return this.action;
  }

  /**
   * Get the field value
   *
   * @return the custom activity payload
   */
  public String getPayload() {
    return this.payload;
  }
}
