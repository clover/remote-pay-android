
package com.clover.remote.client.messages;

import com.clover.sdk.v3.customers.CustomerInfo;

@SuppressWarnings(value="unused")
public class SetCustomerInfoRequest extends BaseRequest {

  private CustomerInfo customerInfo = null;

    
  /**
  * Set the field value
  * Customer information  for a 'current' customer.  This indicates a customer of interest.
  *
  */
  public void setCustomerInfo(CustomerInfo customerInfo) {
    this.customerInfo = customerInfo;
  }

  /**
  * Get the field value
  * Customer information  for a 'current' customer.  This indicates a customer of interest.
  */
  public CustomerInfo getCustomerInfo() {
    return this.customerInfo;
  }
}
