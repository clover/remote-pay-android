package com.clover.remote.client.messages;

import com.clover.sdk.v3.printer.Printer;

import java.util.ArrayList;
import java.util.List;

/**
 * Response object for retrieving the printers
 */
public class RetrievePrintersResponse extends BaseResponse {

  private List<Printer> printers = new ArrayList<>();

  /**
   * Constructor
   *
   * @param printers a list of printers being passed back
   */
  public RetrievePrintersResponse(List<Printer> printers){
    this.printers = printers;
  }

  /**
   * Get the field value
   *
   * @return a list of printers
   */
  public List<Printer> getPrinters() {
    return printers;
  }
}
