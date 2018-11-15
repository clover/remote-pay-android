package com.clover.remote.client.messages;
/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


@SuppressWarnings(value="unused")
public class CustomerProvidedDataEvent extends BaseResponse {

  private java.lang.String eventId = null;
  private DataProviderConfig config = null;
  private java.lang.String data = null;


  /**
   * Set the field value
   * An id that represents this collection event
   *
   */
  public void setEventId(java.lang.String eventId) {
    this.eventId = eventId;
  }

  /**
   * Get the field value
   * An id that represents this collection event
   */
  public java.lang.String getEventId() {
    return this.eventId;
  }
  /**
   * Set the field value
   * The configuration that identifies what this event data represents.  This is used when interpreting the data.
   *
   */
  public void setConfig(DataProviderConfig config) {
    this.config = config;
  }

  /**
   * Get the field value
   * The configuration that identifies what this event data represents.  This is used when interpreting the data.
   */
  public DataProviderConfig getConfig() {
    return this.config;
  }
  /**
   * Set the field value
   * The data that was collected.  This is similar to a CLOB.
   *
   */
  public void setData(java.lang.String data) {
    this.data = data;
  }

  /**
   * Get the field value
   * The data that was collected.  This is similar to a CLOB.
   */
  public java.lang.String getData() {
    return this.data;
  }
}
