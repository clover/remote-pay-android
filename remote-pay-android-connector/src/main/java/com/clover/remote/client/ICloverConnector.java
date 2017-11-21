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

import com.clover.remote.Challenge;
import com.clover.remote.InputOption;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.CloseoutRequest;
import com.clover.remote.client.messages.CustomActivityRequest;
import com.clover.remote.client.messages.OpenCashDrawerRequest;
import com.clover.remote.client.messages.PrintJobStatusRequest;
import com.clover.remote.client.messages.PrintRequest;
import com.clover.remote.client.messages.RetrievePaymentRequest;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.MessageToActivity;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.ReadCardDataRequest;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.RetrieveDeviceStatusRequest;
import com.clover.remote.client.messages.RetrievePrintersRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.order.DisplayOrder;
import com.clover.sdk.v3.payments.Payment;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.List;

/**
 * Interface to define the available methods to send requests to a connected Clover device.
 */
@SuppressWarnings("unused")
public interface ICloverConnector extends Serializable {

 /**
   * Initializes the connection and starts communication with the Clover device.  
   * This method is called after the connector has been created and listeners have been 
   * added to it. It must be called before any other method (other than those that add or 
   * remove listeners).
   */
  void initializeConnection();

  /**
   * Adds a Clover Connector listener.
   *
   * @param listener The connection listener.
   */
  void addCloverConnectorListener(ICloverConnectorListener listener);

  /**
   * Removes a Clover Connector listener.
   *
   * @param listener The connection listener.
   */
  void removeCloverConnectorListener(ICloverConnectorListener listener);

  /**
   * Requests a Sale transaction (i.e. purchase).
   *
   * @param request A SaleRequest object containing basic information for the transaction.
   */
  void sale(SaleRequest request);

  /**
   * If a signature is captured during a transaction, this method accepts the signature 
   * as entered.
   *
   * @param request The accepted VerifySignatureRequest the device passed 
   * to onVerifySignatureRequest().
   */
  void acceptSignature(VerifySignatureRequest request);

  /**
   * If a signature is captured during a transaction, this method rejects the signature as 
   * entered.
   *
   * @param request The rejected VerifySignatureRequest() the device passed to 
   * onVerifySignatureRequest().
   */
  void rejectSignature(VerifySignatureRequest request);

  /**
   * If Payment confirmation is required during a transaction due to a Challenge, 
   * this method accepts the Payment. A Challenge may be triggered by a potential 
   * duplicate Payment or an offline Payment.
   *
   * @param payment The Payment to accept.
   */
  void acceptPayment(Payment payment);

  /**
   * If Payment confirmation is required during a transaction due to a Challenge, 
   * this method rejects the Payment. A Challenge may be triggered by a potential 
   * duplicate Payment or an offline Payment.
   *
   * @param payment The Payment to reject.
   * @param challenge The Challenge that resulted in Payment rejection.
   */
  void rejectPayment(Payment payment, Challenge challenge);

  /**
   * Requests an Auth transaction. The tip for an Auth can be adjusted through the 
   * TipAdjustAuth() call until the batch Closeout is processed.
   *
   * <p>
   * <b>Note:</b> The MerchantInfo.SupportsAuths boolean must be set to true.
   * 
   * @param request The AuthRequest details.
   */
  void auth(AuthRequest request);

  /**
   * Initiates a PreAuth transaction (a pre-authorization for a certain amount). This
   * transaction lets the merchant know whether the account associated with a card has 
   * sufficient funds, without actually charging the card. When the merchant is ready to 
   * charge a final amount, the POS will call CapturePreAuth() to complete the Payment.
   *
   * <p>
   * <b>Note:</b> The MerchantInfo.SupportsPreAuths boolean must be set to true.
   *
   * @param request The PreAuthRequest details.
   */
  void preAuth(PreAuthRequest request);

  /**
   * Marks a PreAuth Payment for capture by a Closeout process. After a PreAuth is 
   * captured, it is effectively the same as an Auth Payment. 
   *
   * <p>
   * <b>Note:</b> Should only be called if the request's PaymentID is from a 
   * PreAuthResponse.
   *
   * @param request The CapturePreAuthRequest details.
   */
  void capturePreAuth(CapturePreAuthRequest request);

  /**
   * Adjusts the tip for a previous Auth transaction. This call can be made until 
   * the Auth Payment has been finalized by a Closeout. 
   *
   * <p>
   * <b>Note:</b> Should only be called if the request's PaymentID is from an 
   * AuthResponse.
   *
   * @param request The TipAdjustAuthRequest details.
   */
  void tipAdjustAuth(TipAdjustAuthRequest request);

  /**
   * Voids a transaction.
   *
   * @param request A VoidRequest object containing basic information needed to 
   * void the transaction.
   */
  void voidPayment(VoidPaymentRequest request);

  /**
   * Refunds the full or partial amount of a Payment.
   *
   * @param request The RefundPaymentRequest details.
   */
  void refundPayment(RefundPaymentRequest request);

  /**
   * Initiates a Manual Refund transaction (a “Refund” or credit that is not associated 
   * with a previous Payment).
   *
   * @param request A ManualRefundRequest object with the request details.
   */
  void manualRefund(ManualRefundRequest request);

  /**
   * Asks the Clover device to capture card information and request a payment token 
   * from the payment gateway. The payment token can be used for future 
   * Sale and Auth requests in place of the card details. The merchant account 
   * must be configured to allow payment tokens.
   *
   * <p>
   * <b>Note:</b> The MerchantInfo.SupportsVaultCards boolean must be set to true.
   *
   * @param cardEntryMethods The card entry methods allowed to capture the payment token. 
   * If this parameter is null, the default values (CARD_ENTRY_METHOD_MAG_STRIPE,
   * CARD_ENTRY_METHOD_ICC_CONTACT, and CARD_ENTRY_METHOD_NFC_CONTACTLESS) 
   * will be used.
   */
  void vaultCard(Integer cardEntryMethods);

/**
   * Sends a "cancel" button press to the Clover device.
   * 
   * @deprecated Use {@link #invokeInputOption(InputOption)} instead.
   */
  @Deprecated
  void cancel();

  /**
   * Sends a request to the Clover server to close out all transactions.
   *
   * @param request The CloseoutRequest details.
   */
  void closeout(CloseoutRequest request);

  /**
   * Sends a print request using the PrintRequest object. Used to print text, images, 
   * and images from a URL using the specified printer.
   * 
   * @param request The PrintRequest details.
   */
  void print(PrintRequest request);

  /**
   * Queries available printers attached to the Clover device using the 
   * RetrievePrintersRequest object.
   * @param request The RetrievePrintersRequest details.
   */
  void retrievePrinters(RetrievePrintersRequest request);

  /**
   * Queries the status of a print job using the PrintJobStatusRequest object.
   * @param request The PrintJobStatusRequest details.
   */
  void retrievePrintJobStatus(PrintJobStatusRequest request);

  /**
   * Opens the first cash drawer found connected to the Clover device. The reason for 
   * opening the cash drawer must be set on OpenCashDrawerRequest.
   *
   * @param request The OpenCashDrawerRequest object defining the reason 
   * the cash drawer is being opened, and an optional device identifier.
   */
  void openCashDrawer(OpenCashDrawerRequest request);
  
  /**
   * Opens the first cash drawer found connected to the Clover device. The reason for 
   * opening the cash drawer must be set on OpenCashDrawerRequest.
   *
   * @param reason
   */
   @Deprecated
  void openCashDrawer(String reason);

  /**
   * Prints custom messages in plain text through the Clover Mini's built-in printer.
   *
   * @param messages An array of text messages to print.
   *
   * @deprecated Use {@link #print(PrintRequest request)} instead.
   */
  @Deprecated
  void printText(List<String> messages);

  /**
   * Prints an image on paper receipts through the Clover Mini's built-in printer.
   *
   * @param image The image to print.
   *
   * @deprecated Use {@link #print(PrintRequest request)} instead.
   */
  @Deprecated
  void printImage(Bitmap image);

  /**
   * Prints an image from the web on paper receipts through the Clover device's built-in 
   * printer.
   *
   * @param url The URL for the image to print.
   * 
   * @deprecated Use {@link #print(PrintRequest request)} instead.
   */
  @Deprecated
  void printImageFromURL(String url);

  /**
   * Displays a string-based message on the Clover device's screen.
   *
   * @param message The string message to display.
   */
  void showMessage(String message);

  /**
   * Displays the welcome screen on the Clover device.
   */
  void showWelcomeScreen();

  /**
   * Displays the thank you screen on the Clover device.
   */
  void showThankYouScreen();

  /**
   * Displays the customer-facing receipt options (print, email, etc.) for a Payment on 
   * the Clover device.
   *
   * @param paymentId The ID of the Payment associated with the receipt.
   * @param orderId The ID of the Order associated with the receipt.
   */ 
  void displayPaymentReceiptOptions(String orderId, String paymentId);

  /**
   * Displays an Order and associated lineItems on the Clover device. Will replace an 
   * Order that is already displayed on the device screen.
   *
   * @param order The Order to display.
   */
  void showDisplayOrder(DisplayOrder order);

  /**
   * Removes the DisplayOrder object from the Clover device's screen.
   *
   * @param order The Order to remove.
   */
  void removeDisplayOrder(DisplayOrder order);

  /**
   * Disposes the connection to the Clover device. After this is called, the connection 
   * to the device is severed, and the CloverConnector object is no longer usable. 
   * Instantiate a new CloverConnector object in order to call initializeConnection().
   */
  void dispose();

  /**
   * Sends a keystroke to the Clover device that invokes an input option (e.g. OK, 
   * CANCEL, DONE, etc.) on the customer's behalf. InputOptions are on the 
   * CloverDeviceEvent passed to onDeviceActivityStart().
   *
   * @param InputOption The input option to invoke.
   */
  void invokeInputOption(InputOption io);

  /**
   * Sends a request to reset the Clover device back to the welcome screen. Can be used 
   * when the device is in an unknown or invalid state from the perspective of the POS.
   *  
   * NOTE: This request could cause the POS to miss a transaction or other information. 
   * Use cautiously as a last resort.
   */
  void resetDevice();

  /**
   * Retrieves a list of unprocessed Payments that were taken offline and 
   * are pending submission to the server.
   */
  void retrievePendingPayments();

  /**
   * Requests card information (specifically Track 1 and Track 2 card data).
   *
   * @param request The ReadCardDataRequest details.
   */
  void readCardData(ReadCardDataRequest request);

  /**
   * Sends a message to a Custom Activity running on a Clover device.
   *
   * @param request The MessageToActivity with the message to send to the Custom Activity.
   */
  void sendMessageToActivity(MessageToActivity request);

  /**
   * Starts a Custom Activity on the Clover device.
   *
   * @param request The CustomActivityRequest details.
   */
  void startCustomActivity(CustomActivityRequest request);

  /**
   * Sends a message requesting the current status of the Clover device.
   *
   * @param request The RetrieveDeviceStatusRequest details.
   */
  void retrieveDeviceStatus(RetrieveDeviceStatusRequest request);

  /**
   * Requests the Payment information associated with the externalPaymentId passed in.
   * Only valid for Payments made in the past 24 hours on the Clover device queried.
   *
   * @param request The request details.
   */
  void retrievePayment(RetrievePaymentRequest request);
  
 }