package com.clover.remote.client.messages;

/**
 * Request for status of print job
 */
public class PrintJobStatusRequest extends BaseRequest {

  private String printRequestId = null;

  /**
   * Constructor
   *
   * @param printRequestId id of the print job to be retrieved
   */
  public PrintJobStatusRequest(String printRequestId){
    this.printRequestId = printRequestId;
  }

  /**
   * Get the field value
   *
   * @return id of the print job to be retrieved
   */
  public String getPrintRequestId() {
    return printRequestId;
  }
}
