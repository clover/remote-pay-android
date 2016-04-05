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

package com.clover.remote.client;

public class MerchantInfo {
  DeviceInfo deviceInfo = new DeviceInfo();

  String merchantID;

  boolean supportsAuths;
  boolean supportsPreAuths;
  boolean supportsSales;
  boolean supportsVaultCards;
  boolean supportsManualRefunds;
  boolean supportsVoids;

  static class DeviceInfo {
    String name;
    String serial;
    String model;
  }
}