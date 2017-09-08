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

import com.clover.common2.payments.PayIntent;
import com.clover.remote.Challenge;
import com.clover.remote.KeyPress;
import com.clover.remote.ResultStatus;
import com.clover.remote.client.CloverDeviceConfiguration;
import com.clover.remote.client.messages.ResultCode;
import com.clover.remote.client.transport.ICloverTransport;
import com.clover.remote.client.transport.ICloverTransportObserver;
import com.clover.remote.message.FinishCancelMessage;
import com.clover.remote.message.ResetDeviceResponseMessage;
import com.clover.remote.message.RetrieveDeviceStatusRequestMessage;
import com.clover.remote.message.RetrieveDeviceStatusResponseMessage;
import com.clover.remote.message.AcknowledgementMessage;
import com.clover.remote.message.ActivityMessageFromActivity;
import com.clover.remote.message.ActivityMessageToActivity;
import com.clover.remote.message.ActivityRequest;
import com.clover.remote.message.ActivityResponseMessage;
import com.clover.remote.message.BreakMessage;
import com.clover.remote.message.CapturePreAuthMessage;
import com.clover.remote.message.CapturePreAuthResponseMessage;
import com.clover.remote.message.CardDataRequestMessage;
import com.clover.remote.message.CardDataResponseMessage;
import com.clover.remote.message.CashbackSelectedMessage;
import com.clover.remote.message.CloseoutRequestMessage;
import com.clover.remote.message.CloseoutResponseMessage;
import com.clover.remote.message.ConfirmPaymentMessage;
import com.clover.remote.message.CreditPrintMessage;
import com.clover.remote.message.DeclineCreditPrintMessage;
import com.clover.remote.message.DeclinePaymentPrintMessage;
import com.clover.remote.message.DiscoveryRequestMessage;
import com.clover.remote.message.DiscoveryResponseMessage;
import com.clover.remote.message.FinishOkMessage;
import com.clover.remote.message.ImagePrintMessage;
import com.clover.remote.message.KeyPressMessage;
import com.clover.remote.message.Message;
import com.clover.remote.message.Method;
import com.clover.remote.message.OpenCashDrawerMessage;
import com.clover.remote.message.OrderUpdateMessage;
import com.clover.remote.message.PartialAuthMessage;
import com.clover.remote.message.PaymentConfirmedMessage;
import com.clover.remote.message.PaymentPrintMerchantCopyMessage;
import com.clover.remote.message.PaymentPrintMessage;
import com.clover.remote.message.PaymentRejectedMessage;
import com.clover.remote.message.RefundPaymentPrintMessage;
import com.clover.remote.message.RefundRequestMessage;
import com.clover.remote.message.RefundResponseMessage;
import com.clover.remote.message.RemoteMessage;
import com.clover.remote.message.RetrievePaymentRequestMessage;
import com.clover.remote.message.RetrievePaymentResponseMessage;
import com.clover.remote.message.RetrievePendingPaymentsMessage;
import com.clover.remote.message.RetrievePendingPaymentsResponseMessage;
import com.clover.remote.message.ShowPaymentReceiptOptionsMessage;
import com.clover.remote.message.SignatureVerifiedMessage;
import com.clover.remote.message.TerminalMessage;
import com.clover.remote.message.TextPrintMessage;
import com.clover.remote.message.ThankYouMessage;
import com.clover.remote.message.TipAddedMessage;
import com.clover.remote.message.TipAdjustMessage;
import com.clover.remote.message.TipAdjustResponseMessage;
import com.clover.remote.message.TxStartRequestMessage;
import com.clover.remote.message.TxStartResponseMessage;
import com.clover.remote.message.TxStateMessage;
import com.clover.remote.message.UiStateMessage;
import com.clover.remote.message.VaultCardMessage;
import com.clover.remote.message.VaultCardResponseMessage;
import com.clover.remote.message.VerifySignatureMessage;
import com.clover.remote.message.VoidPaymentMessage;
import com.clover.remote.message.WelcomeMessage;
import com.clover.remote.order.DisplayOrder;
import com.clover.remote.order.operation.DiscountsAddedOperation;
import com.clover.remote.order.operation.DiscountsDeletedOperation;
import com.clover.remote.order.operation.LineItemsAddedOperation;
import com.clover.remote.order.operation.LineItemsDeletedOperation;
import com.clover.remote.order.operation.OrderDeletedOperation;
import com.clover.sdk.v3.order.Order;
import com.clover.sdk.v3.order.VoidReason;
import com.clover.sdk.v3.payments.Payment;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCloverDevice extends CloverDevice implements ICloverTransportObserver {
  private static final String TAG = DefaultCloverDevice.class.getName();
  private static final String REMOTE_SDK = "com.clover.cloverconnector.android:1.3.2";

  private Gson gson = new Gson();
  private static int id = 0;
  private RefundResponseMessage refRespMsg;

  private final Map<String, AsyncTask<Object, Object, Object>> msgIdToTask = new HashMap<>();

  private final Object ackLock = new Object();

  public DefaultCloverDevice(CloverDeviceConfiguration configuration) {
    this(configuration.getMessagePackageName(), configuration.getCloverTransport(), configuration.getApplicationId());
  }

  public DefaultCloverDevice(String packageName, ICloverTransport transport, String applicationId) {
    super(packageName, transport, applicationId);
    transport.addObserver(this);
  }

  @Override
  public void onDeviceConnected(ICloverTransport transport) {
    notifyObserversConnected();
  }


  @Override
  public void onDeviceDisconnected(ICloverTransport transport) {
    notifyObserversDisconnected();
  }


  @Override
  public void onDeviceReady(ICloverTransport transport) {
    // now that the device is ready, let's send it a discovery request. the discovery response should trigger
    // the callback for the device observer that it is connected and able to communicate
    Log.d(getClass().getSimpleName(), "Sending Discovery Request");
    doDiscoveryRequest();
  }

  @Override
  public void onMessage(String message) {
    Log.d(getClass().getSimpleName(), "onMessage: " + message);
    RemoteMessage rMessage;
    try {
      rMessage = gson.fromJson(message, RemoteMessage.class);
    } catch (Exception e) {
      Log.e(TAG, "Error parsing message", e);
      return;
    }

    try {
      RemoteMessage.Type msgType = rMessage.type;
      if (msgType == RemoteMessage.Type.PING) {
        sendPong();
      } else if (msgType == RemoteMessage.Type.COMMAND) {
        onCommand(rMessage);
      } else {
        Log.e(TAG, "Don't support messages of type: " + rMessage.type.toString());
      }
    } catch (Exception e) {
      Log.e(TAG, "Error processing message: " + rMessage.payload, e);
    }
  }

  private void sendPong() {
    RemoteMessage remoteMessage = new RemoteMessage(null, RemoteMessage.Type.PONG, this.packageName, null, null, REMOTE_SDK, getApplicationId());
    Log.v(TAG, "Sending PONG...");
    sendRemoteMessage(remoteMessage);
  }

  private void onCommand(RemoteMessage rMessage) {
    Method m;
    try {
      m = Method.valueOf(rMessage.method);
    } catch (IllegalArgumentException iae) {
      Log.e(TAG, "Unsupported method type: " + rMessage.method);
      return;
    }

    try {
      switch (m) {
        case BREAK:
          break;
        case CASHBACK_SELECTED:
          CashbackSelectedMessage cbsMessage = (CashbackSelectedMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversCashbackSelected(cbsMessage, rMessage.payload);
          break;
        case ACK:
          AcknowledgementMessage ackMessage = (AcknowledgementMessage) Message.fromJsonString(rMessage.payload);
          notifyObserverAck(ackMessage, rMessage.payload);
          break;
        case DISCOVERY_RESPONSE:
          Log.d(getClass().getSimpleName(), "Got a Discovery Response");
          DiscoveryResponseMessage drm = (DiscoveryResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversReady(drm, rMessage.payload);
          break;
        case CONFIRM_PAYMENT_MESSAGE:
          ConfirmPaymentMessage cpym = (ConfirmPaymentMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversConfirmPayment(cpym, rMessage.payload);
          break;
        case FINISH_CANCEL:
          FinishCancelMessage msg = (FinishCancelMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversFinishCancel(msg.requestInfo, rMessage.payload);
          break;
        case FINISH_OK:
          FinishOkMessage fokmsg = (FinishOkMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversFinishOk(fokmsg, rMessage.payload);
          break;
        case KEY_PRESS:
          KeyPressMessage kpm = (KeyPressMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversKeyPressed(kpm, rMessage.payload);
          break;
        case ORDER_ACTION_RESPONSE:
          break;
        case PARTIAL_AUTH:
          PartialAuthMessage partialAuth = (PartialAuthMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversPartialAuth(partialAuth, rMessage.payload);
          break;
        case PAYMENT_VOIDED:
          // currently this only gets called during a TX, so falls outside our current process flow
          //PaymentVoidedMessage vpMessage = (PaymentVoidedMessage) Message.fromJsonString(rMessage.payload);
          //notifyObserversPaymentVoided(vpMessage.payment, vpMessage.voidReason, ResultStatus.SUCCESS, null, null);
          break;
        case TIP_ADDED:
          TipAddedMessage tipMessage = (TipAddedMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversTipAdded(tipMessage, rMessage.payload);
          break;
        case TX_START_RESPONSE:
          TxStartResponseMessage txStartResponse = (TxStartResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserverTxStart(txStartResponse, rMessage.payload);
          break;
        case TX_STATE:
          TxStateMessage txStateMsg = (TxStateMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversTxState(txStateMsg, rMessage.payload);
          break;
        case UI_STATE:
          UiStateMessage uiStateMsg = (UiStateMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversUiState(uiStateMsg, rMessage.payload);
          break;
        case VERIFY_SIGNATURE:
          VerifySignatureMessage vsigMsg = (VerifySignatureMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversVerifySignature(vsigMsg, rMessage.payload);
          break;
        case REFUND_RESPONSE:
          // for now, deprecating and refund is handled in finish_ok
          // finish_ok also get this message after a receipt, but it doesn't have all the information
          refRespMsg = (RefundResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversPaymentRefundResponse(refRespMsg, rMessage.payload);
          break;
        case REFUND_REQUEST:
          //Outbound no-op
          break;
        case TIP_ADJUST_RESPONSE:
          TipAdjustResponseMessage tipAdjustMsg = (TipAdjustResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversTipAdjusted(tipAdjustMsg, rMessage.payload);
          break;
        case VAULT_CARD_RESPONSE:
          VaultCardResponseMessage vcrm = (VaultCardResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserverVaultCardResponse(vcrm, rMessage.payload);
          break;
        case CAPTURE_PREAUTH_RESPONSE:
          CapturePreAuthResponseMessage cparm = (CapturePreAuthResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversCapturePreAuth(cparm, rMessage.payload);
          break;
        case CLOSEOUT_RESPONSE:
          CloseoutResponseMessage crm = (CloseoutResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversCloseout(crm, rMessage.payload);
          break;
        case RETRIEVE_PENDING_PAYMENTS_RESPONSE:
          RetrievePendingPaymentsResponseMessage rpprm = (RetrievePendingPaymentsResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversPendingPaymentsResponse(rpprm, rMessage.payload);
          break;
        case CARD_DATA_RESPONSE:
          CardDataResponseMessage rcdrm = (CardDataResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversReadCardData(rcdrm, rMessage.payload);
          break;
        case ACTIVITY_MESSAGE_FROM_ACTIVITY:
          ActivityMessageFromActivity amfa = (ActivityMessageFromActivity) Message.fromJsonString(rMessage.payload);
          notifyObserverActivityMessage(amfa, rMessage.payload);
          break;
        case ACTIVITY_RESPONSE:
          ActivityResponseMessage arm = (ActivityResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversActivityResponse(arm, rMessage.payload);
          break;
        case DISCOVERY_REQUEST:
          //Outbound no-op
          break;
        case ORDER_ACTION_ADD_DISCOUNT:
          //Outbound no-op
          break;
        case ORDER_ACTION_ADD_LINE_ITEM:
          //Outbound no-op
          break;
        case ORDER_ACTION_REMOVE_LINE_ITEM:
          //Outbound no-op
          break;
        case ORDER_ACTION_REMOVE_DISCOUNT:
          //Outbound no-op
          break;
        case PRINT_IMAGE:
          //Outbound no-op
          break;
        case PRINT_TEXT:
          //Outbound no-op
          break;
        case PRINT_CREDIT:
          CreditPrintMessage cpm = (CreditPrintMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversPrintCredit(cpm, rMessage.payload);
          break;
        case PRINT_CREDIT_DECLINE:
          DeclineCreditPrintMessage dcpm = (DeclineCreditPrintMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversPrintCreditDecline(dcpm, rMessage.payload);
          break;
        case PRINT_PAYMENT:
          PaymentPrintMessage ppm = (PaymentPrintMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversPrintPayment(ppm, rMessage.payload);
          break;
        case PRINT_PAYMENT_DECLINE:
          DeclinePaymentPrintMessage dppm = (DeclinePaymentPrintMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversPrintPaymentDecline(dppm, rMessage.payload);
          break;
        case PRINT_PAYMENT_MERCHANT_COPY:
          PaymentPrintMerchantCopyMessage ppmcm = (PaymentPrintMerchantCopyMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversPrintMerchantCopy(ppmcm, rMessage.payload);
          break;
        case REFUND_PRINT_PAYMENT:
          RefundPaymentPrintMessage rppm = (RefundPaymentPrintMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversPrintMessage(rppm, rMessage.payload);
          break;
        case SHOW_ORDER_SCREEN:
          //Outbound no-op
          break;
        case SHOW_THANK_YOU_SCREEN:
          //Outbound no-op
          break;
        case SHOW_WELCOME_SCREEN:
          //Outbound no-op
          break;
        case SIGNATURE_VERIFIED:
          //Outbound no-op
          break;
        case TERMINAL_MESSAGE:
          //Outbound no-op
          break;
        case TX_START:
          //Outbound no-op
          break;
        case VOID_PAYMENT:
          //Outbound no-op
          break;
        case CAPTURE_PREAUTH:
          //Outbound no-op
          break;
        case LAST_MSG_REQUEST:
          //Outbound no-op
          break;
        case LAST_MSG_RESPONSE:
          //Outbound no-op
          break;
        case TIP_ADJUST:
          //Outbound no-op
          break;
        case OPEN_CASH_DRAWER:
          //Outbound no-op
          break;
        case SHOW_PAYMENT_RECEIPT_OPTIONS:
          //Outbound no-op
          break;
//        case SHOW_REFUND_RECEIPT_OPTIONS:
//          //Outbound no-op
//          break;
//        case SHOW_MANUAL_REFUND_RECEIPT_OPTIONS:
//          //Outbound no-op
//          break;
        case VAULT_CARD:
          //Outbound no-op
          break;
        case CLOSEOUT_REQUEST:
          //Outbound no-op
          break;
        case RETRIEVE_DEVICE_STATUS_REQUEST:
          break;
        case RETRIEVE_DEVICE_STATUS_RESPONSE:
          RetrieveDeviceStatusResponseMessage rdsr = (RetrieveDeviceStatusResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversRetrieveDeviceStatusResponse(rdsr, rMessage.payload);
          break;
        case RETRIEVE_PAYMENT_RESPONSE:
          RetrievePaymentResponseMessage rprm = (RetrievePaymentResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversRetrievePaymentResponse(rprm, rMessage.payload);
          break;
        case RESET_DEVICE_RESPONSE:
          ResetDeviceResponseMessage rdr = (ResetDeviceResponseMessage) Message.fromJsonString(rMessage.payload);
          notifyObserversResetDeviceResponse(rdr, rMessage.payload);
          break;
        default:
          Log.e(TAG, "Don't support COMMAND messages of method: " + rMessage.method);
          break;
      }
    } catch (Exception e) {
      Log.e(TAG, "Error parsing command message: " + rMessage.payload);
    }
  }

  private void notifyObserverAck(final AcknowledgementMessage ackMessage, final String message) {
    synchronized (ackLock) {
      AsyncTask<Object, Object, Object> ackTask = msgIdToTask.remove(ackMessage.sourceMessageId);
      if (ackTask != null) {
        ackTask.execute();
      }
      // go ahead and notify listeners of the ACK
      new AsyncTask<Object, Object, Object>() {
        @Override
        protected Object doInBackground(Object[] params) {
          for (CloverDeviceObserver observer : deviceObservers) {
            try {
            observer.onMessageAck(ackMessage.sourceMessageId);
            } catch (Exception ex) {
              Log.w(getClass().getSimpleName(), "Error processing AcknowledgementMessage for observer: " + message, ex);
            }
          }
          return null;
        }
      }.execute();
    }
  }

  private void notifyObserversReadCardData(final CardDataResponseMessage rcdrm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onReadCardResponse(rcdrm.status, rcdrm.reason, rcdrm.cardData);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing CardDataResponseMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserverActivityMessage(final ActivityMessageFromActivity amfa, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onMessageFromActivity(amfa.action, amfa.payload);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing ActivityMessageFromActivity for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversActivityResponse(final ActivityResponseMessage arm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          ResultStatus status = arm.resultCode == -1 ? ResultStatus.SUCCESS : ResultStatus.CANCEL;
          observer.onActivityResponse(status, arm.payload, arm.failReason, arm.action);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing ActivityResponseMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintMessage(final RefundPaymentPrintMessage rppm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPrintRefundPayment(rppm.payment, rppm.order, rppm.refund);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing RefundPaymentPrintMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintMerchantCopy(final PaymentPrintMerchantCopyMessage ppmcm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPrintMerchantReceipt(ppmcm.payment);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing PaymentPrintMerchantCopyMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintPaymentDecline(final DeclinePaymentPrintMessage dppm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPrintPaymentDecline(dppm.payment, dppm.reason);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing DeclinePaymentPrintMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintPayment(final PaymentPrintMessage ppm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPrintPayment(ppm.payment, ppm.order);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing PaymentPrintMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversRetrieveDeviceStatusResponse(final RetrieveDeviceStatusResponseMessage rdsr, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onDeviceStatusResponse(ResultCode.SUCCESS, rdsr.reason, rdsr.state, rdsr.data);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing RetrieveDeviceStatusResponseMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversRetrievePaymentResponse(final RetrievePaymentResponseMessage gprm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onRetrievePaymentResponse(ResultCode.SUCCESS, gprm.reason, gprm.externalPaymentId, gprm.queryStatus, gprm.payment);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing RetrievePaymentResponseMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversResetDeviceResponse(final ResetDeviceResponseMessage rdr, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onResetDeviceResponse(ResultCode.SUCCESS, rdr.reason, rdr.state);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing ResetDeviceResponseMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }


  private void notifyObserversPrintCredit(final CreditPrintMessage cpm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPrintCredit(cpm.credit);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing CreditPrintMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintCreditDecline(final DeclineCreditPrintMessage dcpm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPrintCreditDecline(dcpm.credit, dcpm.reason);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing DeclineCreditPrintMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }


  private void notifyObserversConnected() {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
            observer.onDeviceConnected(DefaultCloverDevice.this);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing observer connected", ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversDisconnected() {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onDeviceDisconnected(DefaultCloverDevice.this);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing observer disconnected", ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversReady(final DiscoveryResponseMessage drm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onDeviceReady(DefaultCloverDevice.this, drm);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPaymentRefundResponse(final RefundResponseMessage rrm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPaymentRefundResponse(rrm.orderId, rrm.paymentId, rrm.refund, rrm.code);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing RefundResponseMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversKeyPressed(final KeyPressMessage keyPress, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onKeyPressed(keyPress.keyPress);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing KeyPressMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversCashbackSelected(final CashbackSelectedMessage cbSelected, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onCashbackSelected(cbSelected.cashbackAmount);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing CashbackSelectedMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversTipAdded(final TipAddedMessage tipAdded, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onTipAdded(tipAdded.tipAmount);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing TipAddedMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();

  }

  private void notifyObserverTxStart(final TxStartResponseMessage txsrm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onTxStartResponse(txsrm.result, txsrm.externalPaymentId, txsrm.requestInfo);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing TxStartResponseMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversTipAdjusted(final TipAdjustResponseMessage tarm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onAuthTipAdjusted(tarm.paymentId, tarm.amount, tarm.success);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();

  }

  private void notifyObserversPartialAuth(final PartialAuthMessage partialAuth, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPartialAuth(partialAuth.partialAuthAmount);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();

  }

  private void notifyObserversPaymentVoided(final Payment payment, final VoidReason voidReason, final ResultStatus result, final String reason, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPaymentVoided(payment, voidReason, result, reason, message);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing Payment Void for observer", ex);
          }
        }
        return null;
      }
    }.execute();

  }

  private void notifyObserversVerifySignature(final VerifySignatureMessage verifySigMsg, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onVerifySignature(verifySigMsg.payment, verifySigMsg.signature);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();

  }

  private void notifyObserversConfirmPayment(final ConfirmPaymentMessage confirmPaymentMessage, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        Object[] challenges = confirmPaymentMessage.challenges.toArray(new Challenge[0]);
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onConfirmPayment(confirmPaymentMessage.payment, (Challenge[]) challenges);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();

  }

  private void notifyObserverVaultCardResponse(final VaultCardResponseMessage vaultCardResponseMessage, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onVaultCardResponse(vaultCardResponseMessage.card, vaultCardResponseMessage.status.toString(), vaultCardResponseMessage.reason);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversUiState(final UiStateMessage uiStateMsg, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
            observer.onUiState(uiStateMsg.uiState, uiStateMsg.uiText, uiStateMsg.uiDirection, uiStateMsg.inputOptions);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversCapturePreAuth(final CapturePreAuthResponseMessage cparm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onCapturePreAuth(cparm.status, cparm.reason, cparm.paymentId, cparm.amount, cparm.tipAmount);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversCloseout(final CloseoutResponseMessage crm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onCloseoutResponse(crm.status, crm.reason, crm.batch);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPendingPaymentsResponse(final RetrievePendingPaymentsResponseMessage rpprm, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onPendingPaymentsResponse(rpprm.status == ResultStatus.SUCCESS, rpprm.pendingPaymentEntries);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversTxState(final TxStateMessage txStateMsg, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onTxState(txStateMsg.txState);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();

  }

  private void notifyObserversFinishCancel(final String messageInfo, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          observer.onFinishCancel(messageInfo);
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();

  }

  private void notifyObserversFinishOk(final FinishOkMessage msg, final String message) {
    new AsyncTask<Object, Object, Object>() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          try {
          if (msg.payment != null) {
            observer.onFinishOk(msg.payment, msg.signature, msg.requestInfo);
          } else if (msg.credit != null) {
            observer.onFinishOk(msg.credit);
          } else if (msg.refund != null) {
            observer.onFinishOk(msg.refund);
          }
          } catch (Exception ex) {
            Log.w(getClass().getSimpleName(), "Error processing UiStateMessage for observer: " + message, ex);
          }
        }
        return null;
      }
    }.execute();
  }

  @Override
  public void doShowPaymentReceiptScreen(String orderId, String paymentId) {
    sendObjectMessage(new ShowPaymentReceiptOptionsMessage(orderId, paymentId, 2));
  }

//  public void doShowRefundReceiptScreen(String orderId, String refundId) {
//    sendObjectMessage(new ShowRefundReceiptOptionsMessage(orderId, refundId));
//  }

//  public void doShowManualRefundReceiptScreen(String orderId, String creditId) {
//    sendObjectMessage(new ShowManualRefundReceiptOptionsMessage(orderId, creditId));
//  }

  @Override
  public void doKeyPress(KeyPress keyPress) {
    sendObjectMessage(new KeyPressMessage(keyPress));
  }

  @Override
  public void doShowThankYouScreen() {
    sendObjectMessage(new ThankYouMessage());
  }

  @Override
  public void doShowWelcomeScreen() {
    sendObjectMessage(new WelcomeMessage());
  }

  @Override
  public void doSignatureVerified(Payment payment, boolean verified) {
    sendObjectMessage(new SignatureVerifiedMessage(payment, verified));
  }

  @Override
  public void doRetrievePendingPayments() {
    sendObjectMessage(new RetrievePendingPaymentsMessage());
  }

  @Override
  public void doTerminalMessage(String text) {
    sendObjectMessage(new TerminalMessage(text));
  }

  @Override
  public void doOpenCashDrawer(String reason) {
    sendObjectMessage(new OpenCashDrawerMessage(reason) {
    }); // TODO: fix OpenCashDrawerMessage ctor
  }

  @Override
  public void doCloseout(boolean allowOpenTabs, String batchId) {
    sendObjectMessage(new CloseoutRequestMessage(allowOpenTabs, batchId));
  }

  @Override
  public void doTxStart(PayIntent payIntent, Order order, String messageInfo) {
    sendObjectMessage(new TxStartRequestMessage(payIntent, order, messageInfo));
  }

  @Override
  public void doTipAdjustAuth(String orderId, String paymentId, long amount) {
    sendObjectMessage(new TipAdjustMessage(orderId, paymentId, amount));
  }

  @Override
  public void doPrintText(List<String> textLines) {
    TextPrintMessage tpm = new TextPrintMessage(textLines);
    sendObjectMessage(tpm);
  }

  @Override
  public void doReadCardData(PayIntent payIntent) {
    CardDataRequestMessage rcdr = new CardDataRequestMessage(payIntent);
    sendObjectMessage(rcdr);
  }

  @Override
  public void doPrintImage(Bitmap bitmap) {
    ImagePrintMessage ipm = new ImagePrintMessage(bitmap);
    sendObjectMessage(ipm);
  }

  @Override
  public void doPrintImage(String url) {
    ImagePrintMessage ipm = new ImagePrintMessage(url);
    sendObjectMessage(ipm);
  }

  @Override
  public void doSendMessageToActivity(String actionId, String payload) {
    ActivityMessageToActivity msg = new ActivityMessageToActivity(actionId, payload);
    sendObjectMessage(msg);
  }

  @Override
  public void doStartActivity(String action, String payload, boolean nonBlocking) {
    ActivityRequest ar = new ActivityRequest(action, payload, nonBlocking, false);
    sendObjectMessage(ar);
  }

  @Override
  public void doVoidPayment(final Payment payment, final VoidReason reason) {
    synchronized (ackLock) {
      final String msgId = sendObjectMessage(new VoidPaymentMessage(payment, reason));

      AsyncTask<Object, Object, Object> aTask = new AsyncTask<Object, Object, Object>() {
        @Override
        protected Object doInBackground(Object[] params) {
          notifyObserversPaymentVoided(payment, reason, ResultStatus.SUCCESS, null, null);
          return null;
        }
      };

      if (!supportsAcks()) {
        aTask.execute();
      } else {
        // we will send back response after we get an ack
        msgIdToTask.put(msgId, aTask);
      }
    }
  }

  @Override
  public void doPaymentRefund(String orderId, String paymentId, long amount, boolean fullAmount) {
    /*
     * Need this to get a V2 of refund request
     */
    RefundRequestMessage refundRequestMessage = new RefundRequestMessage(orderId, paymentId, amount, fullAmount);
    sendObjectMessage(refundRequestMessage, 2);
  }

  @Override
  public void doVaultCard(int cardEntryMethods) {
    sendObjectMessage(new VaultCardMessage(cardEntryMethods));
  }

  @Override
  public void doCaptureAuth(String paymentId, long amount, long tipAmount) {
    sendObjectMessage(new CapturePreAuthMessage(paymentId, amount, tipAmount));
  }

  @Override
  public void doAcceptPayment(Payment payment) {
    PaymentConfirmedMessage pcm = new PaymentConfirmedMessage(payment);
    sendObjectMessage(pcm);
  }

  @Override
  public void doRejectPayment(Payment payment, Challenge challenge) {
    PaymentRejectedMessage prm = new PaymentRejectedMessage(payment, challenge.reason);
    sendObjectMessage(prm);
  }

  @Override
  public void doDiscoveryRequest() {
    sendObjectMessage(new DiscoveryRequestMessage(false));
  }

  @Override
  public void doOrderUpdate(DisplayOrder order, Object operation) {
    OrderUpdateMessage updateMessage;

    if (operation instanceof DiscountsAddedOperation) {
      updateMessage = new OrderUpdateMessage(order, (DiscountsAddedOperation) operation);
    } else if (operation instanceof DiscountsDeletedOperation) {
      updateMessage = new OrderUpdateMessage(order, (DiscountsDeletedOperation) operation);
    } else if (operation instanceof LineItemsAddedOperation) {
      updateMessage = new OrderUpdateMessage(order, (LineItemsAddedOperation) operation);
    } else if (operation instanceof LineItemsDeletedOperation) {
      updateMessage = new OrderUpdateMessage(order, (LineItemsDeletedOperation) operation);
    } else if (operation instanceof OrderDeletedOperation) {
      updateMessage = new OrderUpdateMessage(order, (OrderDeletedOperation) operation);
    } else {
      updateMessage = new OrderUpdateMessage(order);
    }

    sendObjectMessage(updateMessage);
  }

  @Override
  public void doResetDevice() {
    sendObjectMessage(new BreakMessage());
  }

  @Override
  public void doRetrieveDeviceStatus(boolean sendLastResponse) {
    sendObjectMessage(new RetrieveDeviceStatusRequestMessage(sendLastResponse));
  }

  @Override
  public void doRetrievePayment(String externalPaymentId) {
    sendObjectMessage(new RetrievePaymentRequestMessage(externalPaymentId));
  }

  @Override
  public void dispose() {
    super.dispose();
    refRespMsg = null;
  }

  private String sendObjectMessage(Message message) {
    return sendObjectMessage(message, 1);
  }

  private String sendObjectMessage(Message message, int version) {
    if (message == null) {
      Log.d(getClass().getName(), "Message is null");
      return null;
    }
    Log.d(getClass().getName(), message.toString());
    if (message.method == null) {
      Log.e(getClass().getName(), "Invalid message", new IllegalArgumentException("Invalid message: " + message.toString()));
      return null;
    }

    String applicationId = getApplicationId();
    if (applicationId == null) {
      Log.e(getClass().getName(), "ApplicationId is null");
      throw new IllegalArgumentException("Invalid applicationId");
    }

    String messageId = (++id) + "";
    RemoteMessage remoteMessage = new RemoteMessage(messageId, RemoteMessage.Type.COMMAND, this.packageName, message.method.toString(), message.toJsonString(), REMOTE_SDK, applicationId);
    sendRemoteMessage(remoteMessage);
    return messageId;
  }

  private void sendRemoteMessage(RemoteMessage remoteMessage) {
    sendRemoteMessage(gson.toJson(remoteMessage));
  }
}
