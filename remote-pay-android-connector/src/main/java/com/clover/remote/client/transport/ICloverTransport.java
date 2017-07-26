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

package com.clover.remote.client.transport;

import java.nio.channels.NotYetConnectedException;

/**
 * Interface to operate on an underlying transport
 */
public interface ICloverTransport {

  /**
   * Initializes the connection using the underlying transport
   */
  void initializeConnection();

  /**
   * Closes the connection to the underlying transport
   */
  void dispose();

  /**
   * Registers a listener to receive connect events and messages from the transport
   *
   * @param observer listener to add
   */
  void addObserver(ICloverTransportObserver observer);

  /**
   * Remove a previously added listener.  If the provided listener is not registered, this call has no effect.
   *
   * @param observer listener to remove
   */
  void removeObserver(ICloverTransportObserver observer);

  /**
   * Sends the specified encoded message
   *
   * @param message encoded message to send
   * @return 0 if successful, -1 if failure
   * @throws NotYetConnectedException if the message is sent when the underlying transport is not connected
   */
  int sendMessage(String message) throws NotYetConnectedException;
}

