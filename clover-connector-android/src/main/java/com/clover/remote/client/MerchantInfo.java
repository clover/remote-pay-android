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

import com.clover.remote.message.DiscoveryResponseMessage;

public class MerchantInfo {
  public MerchantInfo(){}
  public MerchantInfo(DiscoveryResponseMessage drm) {
    merchantID = drm.merchantId;
    merchantMID = drm.merchantMId;
    merchantName = drm.merchantName;
    deviceInfo.name = drm.name;
    deviceInfo.model = drm.model;
    deviceInfo.serial = drm.serial;
    supportsPreAuths = drm.supportsTipAdjust;
    supportsManualRefunds = drm.supportsManualRefund;
    supportsTipAdjust = drm.supportsTipAdjust;
    supportsVaultCards = drm.supportsManualRefund;
  }
  DeviceInfo deviceInfo = new DeviceInfo();

  String merchantID;
  String merchantMID;
  String merchantName;

  boolean supportsAuths;
  boolean supportsPreAuths;
  boolean supportsSales;
  boolean supportsVaultCards;
  boolean supportsManualRefunds;
  boolean supportsVoids;
  boolean supportsTipAdjust;

  public static class DeviceInfo {
    String name;
    String serial;
    String model;

    public String getName() {
      return name;
    }

    public String getSerial() {
      return serial;
    }

    public String getModel() {
      return model;
    }
  }


  public DeviceInfo getDeviceInfo() {
    return deviceInfo;
  }

  public String getMerchantID() {
    return merchantID;
  }

  public String getMerchantMID() {
    return merchantMID;
  }

  public String getMerchantName() {
    return merchantName;
  }

  public boolean isSupportsAuths() {
    return supportsAuths;
  }

  public boolean isSupportsPreAuths() {
    return supportsPreAuths;
  }

  public boolean isSupportsSales() {
    return supportsSales;
  }

  public boolean isSupportsVaultCards() {
    return supportsVaultCards;
  }

  public boolean isSupportsManualRefunds() {
    return supportsManualRefunds;
  }

  public boolean isSupportsVoids() {
    return supportsVoids;
  }

  public boolean isSupportsTipAdjust() {
    return supportsTipAdjust;
  }

}
