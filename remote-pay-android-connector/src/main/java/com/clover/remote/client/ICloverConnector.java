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
   * Initialize the CloverConnector's connection and start communication with the device.
   * Must be called before calling any other method other than to add or remove listeners
   */
  void initializeConnection();

  /**
   * Adds a clover connection listener.
   *
   * @param listener The connection listener.
   */
  void addCloverConnectorListener(ICloverConnectorListener listener);

  /**
   * Removes a clover connector listener.
   *
   * @param listener The connection listener.
   */
  void removeCloverConnectorListener(ICloverConnectorListener listener);

  /**
   * Sale method, aka "purchase"
   *
   * @param request A SaleRequest object containing basic information needed for the transaction
   */
  void sale(SaleRequest request);

  /**
   * If signature is captured during a Sale, this method accepts the signature as entered
   *
   * @param request Accepted request
   */
  void acceptSignature(VerifySignatureRequest request);

  /**
   * If signature is captured during a Sale, this method rejects the signature as entered
   *
   * @param request Rejected request
   */
  void rejectSignature(VerifySignatureRequest request);

  /**
   * If payment confirmation is required during a Sale, this method accepts the payment
   *
   * @param payment Payment to accept
   */
  void acceptPayment(Payment payment);

  /**
   * If payment confirmation is required during a Sale, this method rejects the payment
   *
   * @param payment Payment to reject
   * @param challenge The challenge which resulted in payment rejection
   */
  void rejectPayment(Payment payment, Challenge challenge);

  /**
   * Auth method to obtain an Auth payment that can be used as the payment
   * to call tipAdjust
   *
   * @param request The request details
   */
  void auth(AuthRequest request);

  /**
   * PreAuth method to obtain a Pre-Auth for a card
   *
   * @param request The request details
   */
  void preAuth(PreAuthRequest request);

  /**
   * Capture a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request The request details
   */
  void capturePreAuth(CapturePreAuthRequest request);

  /**
   * Adjust the tip for a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request The request details
   */
  void tipAdjustAuth(TipAdjustAuthRequest request);

  /**
   * Void a transaction, given a previously used order ID and/or payment ID
   *
   * @param request The request details
   */
  void voidPayment(VoidPaymentRequest request);

  /**
   * Refund a specific payment
   *
   * @param request The request details
   */
  void refundPayment(RefundPaymentRequest request);

  /**
   * Manual refund method, aka "naked credit"
   *
   * @param request The request details
   */
  void manualRefund(ManualRefundRequest request);

  /**
   * Vault card information. Requests the mini capture card information and request a payment token from the payment gateway.
   * The value returned in the response is a card, with all the information necessary to use for payment in a SaleRequest or AuthRequest
   *
   * @param cardEntryMethods The card entry methods allowed to capture the payment token. null will provide default values
   */
  void vaultCard(Integer cardEntryMethods);

  /**
   * Cancels the current user action on the device.
   */
  void cancel();

  /**
   * Request a closeout of all orders.
   *
   * @param request The request details
   */
  void closeout(CloseoutRequest request);

  /**
   * Request to print.
   *
   * @param request The request details: info and content needed to print
   */
  void print(PrintRequest request);

  /**
   * Request to retrieve available printers.
   *
   * @param request object that contains additional information to be applied during the request
   */
  void retrievePrinters(RetrievePrintersRequest request);

  /**
   * Request the status of a given print job
   *
   * @param request object defining the print job to be queried
   */
  void retrievePrintJobStatus(PrintJobStatusRequest request);

  /**
   * Request that the cash drawer connected to the device be opened.
   *
   * @param request object defining the reason the cash drawer is being opened, and an optional device identifier
   */
  void openCashDrawer(OpenCashDrawerRequest request);

  /**
   * Print simple lines of text to the Clover Mini printer
   *
   * @param messages A list of text to print
   */
  void printText(List<String> messages);

  /**
   * Print an image on the Clover Mini printer
   *
   * @param image An image to print
   *
   * NOTE:  This method is not implemented for the Java SDK.  Use {@link #printImageFromURL(String)} instead.
   */
  @Deprecated
  void printImage(Bitmap image);

  /**
   * Print an image on the Clover Mini printer
   *
   * @param url The url of an image to print
   */
  void printImageFromURL(String url);

  /**
   * Show a message on the Clover Mini screen
   *
   * @param message The message to display
   */
  void showMessage(String message);

  /**
   * Return the device to the Welcome Screen
   */
  void showWelcomeScreen();

  /**
   * Show the thank you screen on the device
   */
  void showThankYouScreen();

  /**
   * Display the payment receipt screen for the orderId/paymentId combination.
   *
   * @param paymentId The ID of the payment to print a receipt for
   * @param orderId The ID of the order to print a receipt for
   */
  void displayPaymentReceiptOptions(String orderId, String paymentId);

  /**
   * Will trigger cash drawer to open that is connected to Clover Mini
   *
   * @param reason Reason for opening the cash drawer
   */
  void openCashDrawer(String reason);

  /**
   * Show the DisplayOrder on the device. Replaces the existing DisplayOrder on the device.
   *
   * @param order The order to display
   */
  void showDisplayOrder(DisplayOrder order);

  /**
   * Remove the DisplayOrder from the device.
   *
   * @param order The order to remove
   */
  void removeDisplayOrder(DisplayOrder order);

  /**
   * Will dispose of the underlying connection to the device
   */
  void dispose();

  /**
   * Used to invoke user options on the mini such as "OK", "CANCEL", "DONE", etc.
   *
   * @param io The option to invoke
   */
  void invokeInputOption(InputOption io);

  /**
   * Used to reset the device if it gets in an invalid state from POS perspective.
   * This could cause a missed transaction or other missed information, so it
   * needs to be used cautiously as a last resort
   */
  void resetDevice();

  /**
   * Used to request a list of pending payments that have been taken offline, but
   * haven't processed yet.  Will trigger an onRetrievePendingPaymentsResponse callback.
   */
  void retrievePendingPayments();

  /**
   * Used to request card information. Specifically track1 and track2 information
   *
   * @param request The request details
   */
  void readCardData(ReadCardDataRequest request);

  /**
   * Send a message to a running custom activity on the Clover device
   *
   * @param request The request details
   */
  void sendMessageToActivity(MessageToActivity request);

  /**
   * Request to start a Custom Activity on the Clover device
   *
   * @param request The request details
   */
  void startCustomActivity(CustomActivityRequest request);

  /**
   * Send a message requesting the current status of the device.
   *
   * @param request The request details
   */
  void retrieveDeviceStatus(RetrieveDeviceStatusRequest request);

  /**
   * Sends a request to get a payment.
   * Only valid for payments made in the past 24 hours on the device queried.
   *
   * @param request The request details
   */
  void retrievePayment(RetrievePaymentRequest request);

}
