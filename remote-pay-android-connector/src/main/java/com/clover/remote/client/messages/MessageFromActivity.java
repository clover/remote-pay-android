package com.clover.remote.client.messages;

/**
 * Contains the message information sent from a custom Activity
 */
@SuppressWarnings(value="unused")
public class MessageFromActivity extends ActivityMessage {
  /**
   * Constructor
   *
   * @param action the custom activity action
   * @param payload the custom activity payload
   */
  public MessageFromActivity(String action, String payload) {
    super(action, payload);
  }
}
