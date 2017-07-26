package com.clover.remote.client.messages;

/**
 * Contains the message information sent to a custom Activity
 */
@SuppressWarnings(value="unused")
public class MessageToActivity extends ActivityMessage {
  /**
   * Constructor
   *
   * @param action the custom activity action
   * @param payload the custom activity payload
   */
  public MessageToActivity(String action, String payload) {
    super(action, payload);
  }
}
