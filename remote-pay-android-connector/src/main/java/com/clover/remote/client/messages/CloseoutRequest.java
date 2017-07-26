/*
 * Copyright (C) 2016 Clover Network, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clover.remote.client.messages;

/**
 * Request object for closeout request
 */
@SuppressWarnings(value="unused")
public class CloseoutRequest extends BaseRequest {
  private boolean allowOpenTabs;
  private String batchId;

  /**
   * Get the field value
   *
   * @return Indicator to allow closeout if there are open tabs
   */
  public boolean isAllowOpenTabs() {
    return allowOpenTabs;
  }

  /**
   * Set the field value
   *
   * @param allowOpenTabs Indicator to allow closeout if there are open tabs
   */
  public void setAllowOpenTabs(boolean allowOpenTabs) {
    this.allowOpenTabs = allowOpenTabs;
  }

  /**
   * Get the field value
   *
   * @return the ID of the batch to close out
   */
  public String getBatchId() {
    return batchId;
  }

  /**
   * Set the field value
   *
   * @param batchId the ID of the batch to close out
   */
  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }
}
