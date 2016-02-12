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

import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CaptureAuthResponse;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.SignatureVerifyRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.protocol.message.TipAddedMessage;
import com.clover.remote.terminal.TxState;

import java.util.ArrayList;

public class CloverConnectorBroadcaster extends ArrayList<ICloverConnectorListener> {

  public void notifyOnTipAdded(long tip) {
    for (ICloverConnectorListener listener : this) {
      listener.onTipAdded(new TipAddedMessage(tip));
    }
  }

  public void notifyOnRefundPaymentResponse(RefundPaymentResponse refundPaymentResponse) {
    for (ICloverConnectorListener listener : this) {
      listener.onRefundPaymentResponse(refundPaymentResponse);
    }
  }

  public void notifyCloseout(CloseoutResponse closeoutResponse) {
    for (ICloverConnectorListener listener : this) {
      listener.onCloseoutResponse(closeoutResponse);
    }
  }

  public void notifyOnDeviceActivityStart(CloverDeviceEvent deviceEvent) {
    for (ICloverConnectorListener listener : this) {
      listener.onDeviceActivityStart(deviceEvent);
    }
  }

  public void notifyOnDeviceActivityEnd(CloverDeviceEvent deviceEvent) {
    for (ICloverConnectorListener listener : this) {
      listener.onDeviceActivityEnd(deviceEvent);
    }

  }

  public void notifyOnSaleResponse(SaleResponse response) {
    for (ICloverConnectorListener listener : this) {
      listener.onSaleResponse(response);
    }
  }

  public void notifyOnAuthResponse(AuthResponse response) {
    for (ICloverConnectorListener listener : this) {
      listener.onAuthResponse(response);
    }
  }

  public void notifyOnManualRefundResponse(ManualRefundResponse response) {
    for (ICloverConnectorListener listener : this) {
      listener.onManualRefundResponse(response);
    }
  }

  public void notifyOnSignatureVerifyRequest(SignatureVerifyRequest request) {
    for (ICloverConnectorListener listener : this) {
      listener.onSignatureVerifyRequest(request);
    }
  }

  public void notifyOnVoidPaymentResponse(VoidPaymentResponse response) {
    for (ICloverConnectorListener listener : this) {
      listener.onVoidPaymentResponse(response);
    }
  }

  public void notifyOnConnect() {
    for (ICloverConnectorListener listener : this) {
      listener.onConnected();
    }
  }

  public void notifyOnDisconnect() {
    for (ICloverConnectorListener listener : this) {
      listener.onDisconnected();
    }
  }

  public void notifyOnReady() {
    for (ICloverConnectorListener listener : this) {
      listener.onReady();
    }
  }

  public void notifyOnTipAdjustAuthResponse(TipAdjustAuthResponse response) {
    for (ICloverConnectorListener listener : this) {
      listener.onAuthTipAdjustResponse(response);
    }
  }

  public void notifyOnTxState(TxState txState) {
    for (ICloverConnectorListener listener : this) {
      listener.onTransactionState(txState);
    }
  }

  public void notifyOnCaptureCardRespose(VaultCardResponse ccr) {
    for (ICloverConnectorListener listener : this) {
      listener.onVaultCardResponse(ccr);
    }
  }

  public void notifyOnPreAuthResponse(PreAuthResponse response) {
    for(ICloverConnectorListener listener : this) {
      listener.onPreAuthResponse(response);
    }
  }

  public void notifyOnCapturePreAuth(CaptureAuthResponse response) {
    for(ICloverConnectorListener listener : this) {
      listener.onPreAuthCaptureResponse(response);
    }
  }
}
