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

package com.clover.remote.client.device;

import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.usb.USBCloverTransport;

import android.content.Context;

/**
 * Created by blakewilliams on 3/30/16.
 */
public class USBCloverDeviceConfiguration implements CloverDeviceConfiguration {
  Context context;

  public USBCloverDeviceConfiguration(Context ctx) {
    context = ctx;
  }

  @Override
  public String getCloverDeviceTypeName() {
    return DefaultCloverDevice.class.getCanonicalName();
  }

  @Override
  public String getMessagePackageName() {
    return "com.clover.remote.protocol.usb";
  }

  @Override
  public String getName() {
    return "Clover USB Connector";
  }

  @Override
  public CloverTransport getCloverTransport() {
    return new USBCloverTransport(context);
  }
}
