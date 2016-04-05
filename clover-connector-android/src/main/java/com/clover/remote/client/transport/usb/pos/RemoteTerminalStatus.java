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

package com.clover.remote.client.transport.usb.pos;


public enum RemoteTerminalStatus {
  /**
   * Remote terminal not connected
   */
  TERMINAL_DISCONNECTED,
  /**
   * Remote terminal is connected, but not ready to perform transactions at the moment
   */
  TERMINAL_CONNECTED_NOT_READY,
  /**
   * Remote terminal is connected and ready to perform transactions
   */
  TERMINAL_CONNECTED_READY,
  /**
   * Remote terminal is connected, but merchant mismatch, cannot perform transactions
   */
  TERMINAL_CONNECTED_MERCHANT_MISMATCH,
}
