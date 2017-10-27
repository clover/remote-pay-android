package com.clover.remote.client.messages;

import com.clover.sdk.v3.printer.PrintJobStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object, called to update status of print job
 */
public class PrintJobStatusResponse extends BaseResponse {

  private List<String> printRequestId = new ArrayList<>();
  private PrintJobStatus status = null;

  /**
   * Constructor
   *
   * @param printRequestId printRequestId id of the print job to be retrieved
   * @param status of the print job
   */
  public PrintJobStatusResponse(String printRequestId, PrintJobStatus status){
    this.printRequestId.add(printRequestId);
    this.status = status;
  }

  /**
   * Constructor
   *
   * @param status of the print job
   */
  public PrintJobStatusResponse(PrintJobStatus status){
    this(null, status);
  }

  /**
   * Get the field value
   *
   * @return id of the print job to be retrieved
   */
  public String getPrintRequestId() {
    return this.printRequestId.get(0);
  }

  /**
   * Get the field value
   *
   * @return status of the print job
   */
  public PrintJobStatus getStatus() {
    return status;
  }
}
