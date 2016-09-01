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

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import com.clover.common2.payments.PayIntent;
import com.clover.remote.Challenge;
import com.clover.remote.KeyPress;
import com.clover.remote.ResultStatus;
import com.clover.remote.client.CloverDeviceObserver;
import com.clover.remote.client.messages.ReadCardDataResponse;
import com.clover.remote.client.transport.CloverTransport;
import com.clover.remote.client.transport.CloverTransportObserver;
import com.clover.remote.message.AcknowledgementMessage;
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
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultCloverDevice extends CloverDevice implements CloverTransportObserver {
  private static final String TAG = DefaultCloverDevice.class.getName();
  Gson gson = new Gson();
  private static int id = 0;
  private RefundResponseMessage refRespMsg;
  private static final String REMOTE_SDK = "com.clover.cloverconnector.android.public:1.1-RC1";

  private String applicationId;
  Map<String, AsyncTask> msgIdToTask = new HashMap<String, AsyncTask>();

  Object ackLock = new Object();

  public DefaultCloverDevice(CloverDeviceConfiguration configuration) {
    this(configuration.getMessagePackageName(), configuration.getCloverTransport(), configuration.getApplicationId());
  }

  public DefaultCloverDevice(String packageName, CloverTransport transport, String applicationId) {
    super(packageName, transport, applicationId);
    this.applicationId = applicationId;
    transport.Subscribe(this);
  }

  public void onDeviceConnected(CloverTransport transport) {
    notifyObserversConnected(transport);
  }


  public void onDeviceDisconnected(CloverTransport transport) {
    notifyObserversDisconnected(transport);
  }


  public void onDeviceReady(CloverTransport transport) {
    // now that the device is ready, let's send it a discovery request. the discovery response should trigger
    // the callback for the device observer that it is connected and able to communicate
    Log.d(getClass().getSimpleName(), "Sending Discovery Request");
    doDiscoveryRequest();
  }

  public String getApplicationId() {
    return applicationId;
  }

  public void onMessage(String message) {
    try {
      RemoteMessage rMessage = gson.fromJson(message, RemoteMessage.class);

      Method m = null;

      try {
          RemoteMessage.Type msgType = rMessage.type;
          if(msgType == RemoteMessage.Type.PING) {
             sendPong(rMessage);
          } else if(msgType == RemoteMessage.Type.COMMAND) {
            try {
              m = Method.valueOf(rMessage.method);
            } catch(IllegalArgumentException iae) {
              Log.e(TAG, "Unsupported method type: " + rMessage.method);
            }
            if(m != null) {
              switch (m) {
              case BREAK:
                break;
              case CASHBACK_SELECTED:
                CashbackSelectedMessage cbsMessage = (CashbackSelectedMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversCashbackSelected(cbsMessage);
                break;
              case ACK:
                AcknowledgementMessage ackMessage = (AcknowledgementMessage) Message.fromJsonString(rMessage.payload);
                notifyObserverAck(ackMessage);
                break;
              case DISCOVERY_RESPONSE:
                Log.d(getClass().getSimpleName(), "Got a Discovery Response");
                DiscoveryResponseMessage drm = (DiscoveryResponseMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversReady(transport, drm);
                break;
              case CONFIRM_PAYMENT_MESSAGE:
                ConfirmPaymentMessage cpym = (ConfirmPaymentMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversConfirmPayment(cpym);
                break;
              case FINISH_CANCEL:
                notifyObserversFinishCancel();
                break;
              case FINISH_OK:
                FinishOkMessage fokmsg = (FinishOkMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversFinishOk(fokmsg);
                break;
              case KEY_PRESS:
                KeyPressMessage kpm = (KeyPressMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversKeyPressed(kpm);
                break;
              case ORDER_ACTION_RESPONSE:
                break;
              case PARTIAL_AUTH:
                PartialAuthMessage partialAuth = (PartialAuthMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversPartialAuth(partialAuth);
                break;
              case PAYMENT_VOIDED:
                VoidPaymentMessage vpMessage = (VoidPaymentMessage) Message.fromJsonString(rMessage.payload);
                //Payment payment = gson.fromJson(vpMessage.payment, Payment.class);
                notifyObserversPaymentVoided(vpMessage.payment, vpMessage.voidReason, ResultStatus.SUCCESS, null, null);
                break;
              case TIP_ADDED:
                TipAddedMessage tipMessage = (TipAddedMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversTipAdded(tipMessage);
                break;
              case TX_START_RESPONSE:
                TxStartResponseMessage txStartResponse = (TxStartResponseMessage) Message.fromJsonString(rMessage.payload);
                notifyObserverTxStart(txStartResponse);
                break;
              case TX_STATE:
                TxStateMessage txStateMsg = (TxStateMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversTxState(txStateMsg);
                break;
              case UI_STATE:
                UiStateMessage uiStateMsg = (UiStateMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversUiState(uiStateMsg);
                break;
              case VERIFY_SIGNATURE:
                VerifySignatureMessage vsigMsg = (VerifySignatureMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversVerifySignature(vsigMsg);
                break;
              case REFUND_RESPONSE:
                // for now, deprecating and refund is handled in finish_ok
                // finish_ok also get this message after a receipt, but it doesn't have all the information
                refRespMsg = (RefundResponseMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversPaymentRefundResponse(refRespMsg);
                break;
              case REFUND_REQUEST:
                //Outbound no-op
                break;
              case TIP_ADJUST_RESPONSE:
                TipAdjustResponseMessage tipAdjustMsg = (TipAdjustResponseMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversTipAdjusted(tipAdjustMsg);
                break;
              case VAULT_CARD_RESPONSE:
                VaultCardResponseMessage vcrm = (VaultCardResponseMessage) Message.fromJsonString(rMessage.payload);
                notifyObserverVaultCardResponse(vcrm);
                break;
              case CAPTURE_PREAUTH_RESPONSE:
                CapturePreAuthResponseMessage cparm = (CapturePreAuthResponseMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversCapturePreAuth(cparm);
                break;
              case CLOSEOUT_RESPONSE:
                CloseoutResponseMessage crm = (CloseoutResponseMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversCloseout(crm);
                break;
              case RETRIEVE_PENDING_PAYMENTS_RESPONSE:
                RetrievePendingPaymentsResponseMessage rpprm = (RetrievePendingPaymentsResponseMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversPendingPaymentsResponse(rpprm);
                break;
              case CARD_DATA_RESPONSE:
                CardDataResponseMessage rcdrm = (CardDataResponseMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversReadCardData(rcdrm);
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
                CreditPrintMessage cpm = (CreditPrintMessage)Message.fromJsonString(rMessage.payload);
                notifyObserversPrintCredit(cpm);
                break;
              case PRINT_CREDIT_DECLINE:
                DeclineCreditPrintMessage dcpm = (DeclineCreditPrintMessage)Message.fromJsonString(rMessage.payload);
                notifyObserversPrintCreditDecline(dcpm);
                break;
              case PRINT_PAYMENT:
                PaymentPrintMessage ppm = (PaymentPrintMessage)Message.fromJsonString(rMessage.payload);
                notifyObserversPrintPayment(ppm);
                break;
              case PRINT_PAYMENT_DECLINE:
                DeclinePaymentPrintMessage dppm = (DeclinePaymentPrintMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversPrintPaymentDecline(dppm);
                break;
              case PRINT_PAYMENT_MERCHANT_COPY:
                PaymentPrintMerchantCopyMessage ppmcm = (PaymentPrintMerchantCopyMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversPrintMerchantCopy(ppmcm);
                break;
              case REFUND_PRINT_PAYMENT:
                RefundPaymentPrintMessage rppm = (RefundPaymentPrintMessage) Message.fromJsonString(rMessage.payload);
                notifyObserversPrintMessage(rppm);
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
              //            case SHOW_REFUND_RECEIPT_OPTIONS:
              //              //Outbound no-op
              //              break;
              //            case SHOW_MANUAL_REFUND_RECEIPT_OPTIONS:
              //              //Outbound no-op
              //              break;
              case VAULT_CARD:
                //Outbound no-op
                break;
              case CLOSEOUT_REQUEST:
                //Outbound no-op
                break;
              default:
                Log.e(TAG, "Don't support COMMAND messages of method: " + rMessage.method);
                break;
              }
            } else {
              Log.e(TAG, "Method is null");
            }
          } else {
            Log.e(TAG, "Don't support messages of type: " + rMessage.type.toString());
          }

      } catch (Exception e) {
        Log.e(TAG, "Error processing message: " + rMessage.payload);
        e.printStackTrace();
      }

    } catch (Exception e) {
      e.printStackTrace();
      //onError(e);
    }
  }

  private void sendPong(RemoteMessage pingMessage) {
    RemoteMessage remoteMessage = new RemoteMessage(null, RemoteMessage.Type.PONG, this.packageName, null, null, REMOTE_SDK, applicationId);
    Log.d(TAG, "Sending PONG...");
    sendRemoteMessage(remoteMessage);
  }

  private void notifyObserverAck(final AcknowledgementMessage ackMessage) {
    synchronized (ackLock) {
      AsyncTask ackTask = msgIdToTask.remove(ackMessage.sourceMessageId);
      if(ackTask != null) {
        ackTask.execute();
      }
      // go ahead and notify listeners of the ACK
      new AsyncTask() {
        @Override protected Object doInBackground(Object[] params) {
          for (final CloverDeviceObserver observer : deviceObservers) {
            observer.onMessageAck(ackMessage.sourceMessageId);
          }
          return null;
        }
      }.execute();
    }
  }

  private void notifyObserversReadCardData(final CardDataResponseMessage rcdrm) {
    new AsyncTask() {
      @Override protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onReadCardResponse(rcdrm.status, rcdrm.reason, rcdrm.cardData);
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintMessage(final RefundPaymentPrintMessage rppm) {
    new AsyncTask() {
      @Override protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onPrintRefundPayment(rppm.payment, rppm.order, rppm.refund);
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintMerchantCopy(final PaymentPrintMerchantCopyMessage ppmcm) {
    new AsyncTask() {
      @Override protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onPrintMerchantReceipt(ppmcm.payment);
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintPaymentDecline(final DeclinePaymentPrintMessage dppm) {
    new AsyncTask() {
      @Override protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onPrintPaymentDecline(dppm.payment, dppm.reason);
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintPayment(final PaymentPrintMessage ppm) {
    new AsyncTask() {
      @Override protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onPrintPayment(ppm.payment, ppm.order);
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintCredit(final CreditPrintMessage cpm) {
    new AsyncTask() {
      @Override protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onPrintCredit(cpm.credit);
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversPrintCreditDecline(final DeclineCreditPrintMessage dcpm) {
    new AsyncTask() {
      @Override protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onPrintCreditDecline(dcpm.credit, dcpm.reason);
        }
        return null;
      }
    }.execute();
  }


  private void notifyObserversConnected(final CloverTransport transport) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onDeviceConnected(DefaultCloverDevice.this);
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversDisconnected(final CloverTransport transport) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onDeviceDisconnected(DefaultCloverDevice.this);
        }
        return null;
      }
    }.execute();
  }

  private void notifyObserversReady(final CloverTransport transport, final DiscoveryResponseMessage drm) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onDeviceReady(DefaultCloverDevice.this, drm);
        }
        return null;
      }
    }.execute();
  }

  //---------------------------------------------------
  /// <summary>
  /// this is for a payment refund
  /// </summary>
  /// <param name="rrm"></param>
  public void notifyObserversPaymentRefundResponse(final RefundResponseMessage rrm) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onPaymentRefundResponse(rrm.orderId, rrm.paymentId, rrm.refund, rrm.code);
        }
        return null;
      }
    }.execute();
  }

  public void notifyObserversKeyPressed(final KeyPressMessage keyPress) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onKeyPressed(keyPress.keyPress);
        }
        return null;
      }
    }.execute();
  }

  public void notifyObserversCashbackSelected(final CashbackSelectedMessage cbSelected) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (final CloverDeviceObserver observer : deviceObservers) {
          observer.onCashbackSelected(cbSelected.cashbackAmount);
        }
        return null;
      }
    }.execute();
  }

  public void notifyObserversTipAdded(final TipAddedMessage tipAdded) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onTipAdded(tipAdded.tipAmount);
        }
        return null;
      }
    }.execute();

  }

  public void notifyObserverTxStart(final TxStartResponseMessage txsrm) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onTxStartResponse(txsrm.result, txsrm.externalPaymentId);
        }
        return null;
      }
    }.execute();
  }

  public void notifyObserversTipAdjusted(final TipAdjustResponseMessage tarm) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onAuthTipAdjusted(tarm.paymentId, tarm.amount, tarm.success);
        }
        return null;
      }
    }.execute();

  }

  public void notifyObserversPartialAuth(final PartialAuthMessage partialAuth) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onPartialAuth(partialAuth.partialAuthAmount);
        }
        return null;
      }
    }.execute();

  }

  public void notifyObserversPaymentVoided(final Payment payment, final VoidReason voidReason, final ResultStatus result, final String reason, final String message) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onPaymentVoided(payment, voidReason, result, reason, message);
        }
        return null;
      }
    }.execute();

  }

  public void notifyObserversVerifySignature(final VerifySignatureMessage verifySigMsg) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onVerifySignature(verifySigMsg.payment, verifySigMsg.signature);
        }
        return null;
      }
    }.execute();

  }

  public void notifyObserversConfirmPayment(final ConfirmPaymentMessage confirmPaymentMessage) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        Object[] challenges = confirmPaymentMessage.challenges.toArray(new Challenge[0]);
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onConfirmPayment(confirmPaymentMessage.payment, (Challenge[])challenges);
        }
        return null;
      }
    }.execute();

  }

  public void notifyObserverVaultCardResponse(final VaultCardResponseMessage vaultCardResponseMessage) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onVaultCardResponse(vaultCardResponseMessage.card, vaultCardResponseMessage.status.toString(), vaultCardResponseMessage.reason);
        }
        return null;
      }
    }.execute();
  }

  public void notifyObserversUiState(final UiStateMessage uiStateMsg) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onUiState(uiStateMsg.uiState, uiStateMsg.uiText, uiStateMsg.uiDirection, uiStateMsg.inputOptions);
        }
        return null;
      }
    }.execute();
  }

  public void notifyObserversCapturePreAuth(final CapturePreAuthResponseMessage cparm) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onCapturePreAuth(cparm.status, cparm.reason, cparm.paymentId, cparm.amount, cparm.tipAmount);
        }
        return null;
      }
    }.execute();
  }

  public void notifyObserversCloseout(final CloseoutResponseMessage crm) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {

          observer.onCloseoutResponse(crm.status, crm.reason, crm.batch);
        }
        return null;
      }
    }.execute();
  }

  public void notifyObserversPendingPaymentsResponse(final RetrievePendingPaymentsResponseMessage rpprm) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onPendingPaymentsResponse(rpprm.status == ResultStatus.SUCCESS, rpprm.pendingPaymentEntries);
        }
        return null;
      }
    }.execute();
  }

  public void notifyObserversTxState(final TxStateMessage txStateMsg) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onTxState(txStateMsg.txState);
        }
        return null;
      }
    }.execute();

  }

  public void notifyObserversFinishCancel() {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          observer.onFinishCancel();
        }
        return null;
      }
    }.execute();

  }

  public void notifyObserversFinishOk(final FinishOkMessage msg) {
    new AsyncTask() {
      @Override
      protected Object doInBackground(Object[] params) {
        for (CloverDeviceObserver observer : deviceObservers) {
          if (msg.payment != null) {
            observer.onFinishOk(msg.payment, msg.signature);
          } else if (msg.credit != null) {
            observer.onFinishOk(msg.credit);
          } else if (msg.refund != null) {
            observer.onFinishOk(msg.refund);
          }
        }
        return null;
      }
    }.execute();

  }

  public void doShowPaymentReceiptScreen(String orderId, String paymentId) {
    sendObjectMessage(new ShowPaymentReceiptOptionsMessage(orderId, paymentId, 2));
  }

//  public void doShowRefundReceiptScreen(String orderId, String refundId) {
//    sendObjectMessage(new ShowRefundReceiptOptionsMessage(orderId, refundId));
//  }

//  public void doShowManualRefundReceiptScreen(String orderId, String creditId) {
//    sendObjectMessage(new ShowManualRefundReceiptOptionsMessage(orderId, creditId));
//  }

  public void doKeyPress(KeyPress keyPress) {
    sendObjectMessage(new KeyPressMessage(keyPress));
  }

  public void doShowThankYouScreen() {
    sendObjectMessage(new ThankYouMessage());
  }

  public void doShowWelcomeScreen() {
    sendObjectMessage(new WelcomeMessage());
  }

  public void doSignatureVerified(Payment payment, boolean verified) {
    sendObjectMessage(new SignatureVerifiedMessage(payment, verified));
  }

  public void doRetrievePendingPayments() {
    sendObjectMessage(new RetrievePendingPaymentsMessage());
  }

  public void doTerminalMessage(String text) {
    sendObjectMessage(new TerminalMessage(text));
  }

  public void doOpenCashDrawer(String reason) {
    sendObjectMessage(new OpenCashDrawerMessage(reason) {
    }); // TODO: fix OpenCashDrawerMessage ctor
  }

  public void doCloseout(boolean allowOpenTabs, String batchId) {
    sendObjectMessage(new CloseoutRequestMessage(allowOpenTabs, batchId));
  }

  public void doTxStart(PayIntent payIntent, Order order, boolean suppressTipScreen) {
    sendObjectMessage(new TxStartRequestMessage(payIntent, order, suppressTipScreen));
  }

  public void doTipAdjustAuth(String orderId, String paymentId, long amount) {
    sendObjectMessage(new TipAdjustMessage(orderId, paymentId, amount));
  }

  public void doPrintText(List<String> textLines) {
    TextPrintMessage tpm = new TextPrintMessage(textLines);
    sendObjectMessage(tpm);
  }

  public void doReadCardData(PayIntent payIntent) {
    CardDataRequestMessage rcdr = new CardDataRequestMessage(payIntent);
    sendObjectMessage(rcdr);
  }

  public void doPrintImage(Bitmap bitmap) {
    ImagePrintMessage ipm = new ImagePrintMessage(bitmap);
    sendObjectMessage(ipm);
  }

  public void doPrintImage(String url) {
    ImagePrintMessage ipm = new ImagePrintMessage(url);
    sendObjectMessage(ipm);
  }

  public void doVoidPayment(final Payment payment, final VoidReason reason) {
    synchronized (ackLock) {
      final String msgId = sendObjectMessage(new VoidPaymentMessage(payment, reason));

      AsyncTask aTask = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] params) {
          notifyObserversPaymentVoided(payment, reason, ResultStatus.SUCCESS, null, null);
          return null;
        }
      };

      if(!supportsAcks()) {
        aTask.execute();
      }
      else {
        // we will send back response after we get an ack
        msgIdToTask.put(msgId, aTask);
      }
    }
  }

  public void doPaymentRefund(String orderId, String paymentId, long amount, boolean fullAmount) {
    /*
     * Need this to get a V2 of refund request
     */
    RefundRequestMessage refundRequestMessage = new RefundRequestMessage(orderId, paymentId, amount, fullAmount);
    sendObjectMessage(refundRequestMessage, 2);
  }

  public void doVaultCard(int cardEntryMethods) {
    sendObjectMessage(new VaultCardMessage(cardEntryMethods));
  }

  public void doCaptureAuth(String paymentId, long amount, long tipAmount) {
    sendObjectMessage(new CapturePreAuthMessage(paymentId, amount, tipAmount));
  }

  public void doAcceptPayment(Payment payment) {
    PaymentConfirmedMessage pcm = new PaymentConfirmedMessage(payment);
    sendObjectMessage(pcm);
  }

  public void doRejectPayment(Payment payment, Challenge challenge) {
    PaymentRejectedMessage prm = new PaymentRejectedMessage(payment, challenge.reason);
    sendObjectMessage(prm);
  }

  public void doDiscoveryRequest() {
    sendObjectMessage(new DiscoveryRequestMessage(false));
  }

  public void doOrderUpdate(DisplayOrder order, Object operation) {
    OrderUpdateMessage updateMessage = null;

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

  public void dispose() {
    deviceObservers.clear();
    refRespMsg = null;
    if (transport != null) {
      transport.dispose();
      transport = null;
    }
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
    if (applicationId == null) {
      Log.e(getClass().getName(), "Invalid applicationId: " + applicationId);
      throw new IllegalArgumentException("Invalid applicationId");
    }

    String messageId = (++id) + "";
    RemoteMessage remoteMessage = new RemoteMessage(messageId, RemoteMessage.Type.COMMAND, this.packageName, message.method.toString(), message.toJsonString(), REMOTE_SDK, applicationId);
    sendRemoteMessage(remoteMessage);
    return messageId;
  }
  private void sendRemoteMessage(RemoteMessage remoteMessage) {
    String msg = gson.toJson(remoteMessage);
    transport.sendMessage(msg);
  }
}
