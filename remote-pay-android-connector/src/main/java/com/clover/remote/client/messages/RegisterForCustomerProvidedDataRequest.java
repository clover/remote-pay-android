
package com.clover.remote.client.messages;

import java.util.List;

@SuppressWarnings(value = "unused")
public class RegisterForCustomerProvidedDataRequest extends BaseRequest {

  private List<DataProviderConfig> configurations = null;


  /**
   * Set the field value
   * Configurations for the data we want to receive.
   *
   */
  public void setConfigurations(java.util.List<DataProviderConfig> configurations) {
    this.configurations = configurations;
  }

  /**
   * Get the field value
   * Configurations for the data we want to receive.
   */
  public java.util.List<DataProviderConfig> getConfigurations() {
    return this.configurations;
  }
}
