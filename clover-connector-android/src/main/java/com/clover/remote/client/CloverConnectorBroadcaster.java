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

import com.clover.remote.client.messages.ActivityMessage;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CapturePreAuthResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.CustomActivityResponse;
import com.clover.remote.client.messages.PairingCodeMessage;
import com.clover.remote.client.messages.ConfirmPaymentRequest;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.PrintManualRefundDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintManualRefundReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentDeclineReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentMerchantCopyReceiptMessage;
import com.clover.remote.client.messages.PrintPaymentReceiptMessage;
import com.clover.remote.client.messages.PrintRefundPaymentReceiptMessage;
import com.clover.remote.client.messages.ReadCardDataResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.RetrievePendingPaymentsResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;

import java.util.concurrent.CopyOnWriteArrayList;

public class CloverConnectorBroadcaster extends CopyOnWriteArrayList<ICloverConnectorListener> {

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

  public void notifyOnVerifySignatureRequest(VerifySignatureRequest request) {
    for (ICloverConnectorListener listener : this) {
      listener.onVerifySignatureRequest(request);
    }
  }

  public void notifyOnVoidPaymentResponse(VoidPaymentResponse response) {
    for (ICloverConnectorListener listener : this) {
      listener.onVoidPaymentResponse(response);
    }
  }

  public void notifyOnConnect() {
    for (ICloverConnectorListener listener : this) {
      listener.onDeviceConnected();
    }
  }

  public void notifyOnDisconnect() {
    for (ICloverConnectorListener listener : this) {
      listener.onDeviceDisconnected();
    }
  }

  public void notifyOnReady(MerchantInfo merchantInfo) {
    for (ICloverConnectorListener listener : this) {
      listener.onDeviceReady(merchantInfo);
    }
  }

  public void notifyOnTipAdjustAuthResponse(TipAdjustAuthResponse response) {
    for (ICloverConnectorListener listener : this) {
      listener.onTipAdjustAuthResponse(response);
    }
  }

  public void notifyOnVaultCardRespose(VaultCardResponse ccr) {
    for (ICloverConnectorListener listener : this) {
      listener.onVaultCardResponse(ccr);
    }
  }

  public void notifyOnPreAuthResponse(PreAuthResponse response) {
    for (ICloverConnectorListener listener : this) {
      listener.onPreAuthResponse(response);
    }
  }

  public void notifyOnCapturePreAuth(CapturePreAuthResponse response) {
    for (ICloverConnectorListener listener : this) {
      listener.onCapturePreAuthResponse(response);
    }
  }

  public void notifyOnDeviceError(CloverDeviceErrorEvent errorEvent) {
    for (ICloverConnectorListener listener : this) {
      listener.onDeviceError(errorEvent);
    }
  }

  public void notifyOnPrintRefundPaymentReceipt(PrintRefundPaymentReceiptMessage printRefundPaymentReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      listener.onPrintRefundPaymentReceipt(printRefundPaymentReceiptMessage);
    }
  }

  public void notifyOnPrintPaymentMerchantCopyReceipt(PrintPaymentMerchantCopyReceiptMessage printPaymentMerchantCopyReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      listener.onPrintPaymentMerchantCopyReceipt(printPaymentMerchantCopyReceiptMessage);
    }
  }

  public void notifyOnPrintPaymentDeclineReceipt(PrintPaymentDeclineReceiptMessage printPaymentDeclineReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      listener.onPrintPaymentDeclineReceipt(printPaymentDeclineReceiptMessage);
    }
  }

  public void notifyOnPrintPaymentReceipt(PrintPaymentReceiptMessage printPaymentReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      listener.onPrintPaymentReceipt(printPaymentReceiptMessage);
    }
  }

  public void notifyOnPrintCreditReceipt(PrintManualRefundReceiptMessage printManualRefundReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      listener.onPrintManualRefundReceipt(printManualRefundReceiptMessage);
    }
  }

  public void notifyOnPrintCreditDeclineReceipt(PrintManualRefundDeclineReceiptMessage printManualRefundDeclineReceiptMessage) {
    for (ICloverConnectorListener listener : this) {
      listener.onPrintManualRefundDeclineReceipt(printManualRefundDeclineReceiptMessage);
    }
  }

  public void notifyOnConfirmPaymentRequest(ConfirmPaymentRequest confirmPaymentRequest) {
    for (ICloverConnectorListener listener : this) {
      listener.onConfirmPaymentRequest(confirmPaymentRequest);
    }
  }

  public void notifyOnRetrievePendingPaymentResponse(RetrievePendingPaymentsResponse rppr) {
    for (ICloverConnectorListener listener : this) {
      listener.onRetrievePendingPaymentsResponse(rppr);
    }
  }

  public void notifyOnReadCardDataResponse(ReadCardDataResponse rcdr) {
    for (ICloverConnectorListener listener : this) {
      listener.onReadCardDataResponse(rcdr);
    }
  }

  public void notifyOnActivityResponse(CustomActivityResponse car) {
    for (ICloverConnectorListener listener : this) {
      listener.onCustomActivityResponse(car);
    }
  }
}
