package com.clover.remote.client.messages;

/**
 * Response object for a custom activity request
 */
@SuppressWarnings(value="unused")
public class CustomActivityResponse extends BaseResponse {
  private final String payload;
  private final String action;

  /**
   * Constructor
   *
   * @param success If true then the requested operation succeeded
   * @param code The result of the requested operation
   * @param payload Data for this activity
   * @param failReason Optional information about result
   * @param action The name of the action for this activity
   */
  public CustomActivityResponse(boolean success, ResultCode code, String payload, String failReason, String action) {
    super(success, code);
    this.setReason(failReason);
    this.payload = payload;
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
}
