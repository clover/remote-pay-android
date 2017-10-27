package com.clover.remote.client.messages;


import com.clover.sdk.v3.printer.PrintCategory;

/**
 * Request to retrieve all available printers
 */
public class RetrievePrintersRequest extends BaseRequest {
  private PrintCategory category = null;

  /**
   * Constructor
   *
   *
   */
  public RetrievePrintersRequest(){
  }

  /**
   * Constructor
   *
   * @param printCategory category of printers to retrieve
   */
  public RetrievePrintersRequest(PrintCategory printCategory){
    this.category = printCategory;
  }

  /**
   * Get the field value
   *
   * @return category of printers to retrieve
   */
  public PrintCategory getCategory() {
    return category;
  }
}
