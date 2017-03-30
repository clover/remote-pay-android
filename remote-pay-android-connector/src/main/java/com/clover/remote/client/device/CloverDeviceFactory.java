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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CloverDeviceFactory {
  public static CloverDevice get(final CloverDeviceConfiguration configuration) {
    String cloverDevicetypeName = configuration.getCloverDeviceTypeName();

    CloverDevice cd = null;
    try {
      Class cls = Class.forName(cloverDevicetypeName);
      Constructor<CloverDevice> ctor = cls.getConstructor(CloverDeviceConfiguration.class);
      cd = ctor.newInstance(configuration);
    } catch (ClassNotFoundException ex) {
      ex.printStackTrace();
    } catch (ClassCastException cce) {
      cce.printStackTrace();
    } catch (InstantiationException ie) {
      ie.printStackTrace();
    } catch (IllegalAccessException iae) {
      iae.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException ite) {
      ite.printStackTrace();
    }
    return cd;
  }
}