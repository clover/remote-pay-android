
package com.clover.remote.client.messages;

@SuppressWarnings(value="unused")
public class DataProviderConfig {

  private String type = null;
  private java.util.Map<String,String> configuration = null;

  
  /**
  * Set the field value
  * The string type of the loyalty data of interest.
  *
  */
  public void setType(String type) {
    this.type = type;
  }

  /**
  * Get the field value
  * The string type of the loyalty data of interest.
  */
  public String getType() {
    return this.type;
  }  
  /**
  * Set the field value
  * Configuration for the data type.
  *
  */
  public void setConfiguration(java.util.Map<String,String> configuration) {
    this.configuration = configuration;
  }

  /**
  * Get the field value
  * Configuration for the data type.
  */
  public java.util.Map<String,String> getConfiguration() {
    return this.configuration;
  }
}
