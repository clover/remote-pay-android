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

import android.graphics.Bitmap;
import com.clover.remote.Challenge;
import com.clover.remote.InputOption;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.CloseoutRequest;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.ReadCardDataRequest;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.VerifySignatureRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.order.DisplayOrder;
import com.clover.sdk.v3.payments.Payment;

import java.io.Serializable;
import java.util.List;

public interface ICloverConnector extends Serializable {

  /**
   * Initialize the CloverConnector's connection. Must be called before calling any other method other than to add or remove listeners
   */
  void initializeConnection();

  /**
   * add an ICloverConnectorListener to receive callbacks
   * @param listener
   */
  public void addCloverConnectorListener(ICloverConnectorListener listener);

  /**
   * remove an ICloverConnectorListener from receiving callbacks
   * @param listener
   */
  public void removeCloverConnectorListener(ICloverConnectorListener listener);

  /**
   * Sale method, aka "purchase"
   *
   * @param request - A SaleRequest object containing basic information needed for the transaction
   */
  void sale(SaleRequest request);

  /**
   * If signature is captured during a Sale, this method accepts the signature as entered
   *
   * @param request -
   **/
  void acceptSignature(VerifySignatureRequest request);

  /**
   * If signature is captured during a Sale, this method rejects the signature as entered
   *
   * @param request -
   **/
  void rejectSignature(VerifySignatureRequest request);

  /**
   * If payment confirmation is required during a Sale, this method accepts the payment
   *
   * @param payment -
   **/
  void acceptPayment(Payment payment);

  /**
   * If payment confirmation is required during a Sale, this method rejects the payment
   *
   * @param payment -
   * @param challenge -
   **/
  void rejectPayment(Payment payment, Challenge challenge);

  /**
   * Auth method to obtain an Auth payment that can be used as the payment
   * to call tipAdjust
   *
   * @param request -
   **/
  void auth(AuthRequest request);

  /**
   * PreAuth method to obtain a Pre-Auth for a card
   *
   * @param request -
   **/
  void preAuth(PreAuthRequest request);

  /**
   * Capture a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request -
   **/
  void capturePreAuth(CapturePreAuthRequest request);

  /**
   * Adjust the tip for a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request -
   **/
  void tipAdjustAuth(TipAdjustAuthRequest request);

  /**
   * Void a transaction, given a previously used order ID and/or payment ID
   *
   * @param request - A VoidRequest object containing basic information needed to void the transaction
   **/
  void voidPayment(VoidPaymentRequest request);

  /**
   * Refund a specific payment
   *
   * @param request -
   **/
  void refundPayment(RefundPaymentRequest request);

  /**
   * Manual refund method, aka "naked credit"
   *
   * @param request - A ManualRefundRequest object
   **/
  void manualRefund(ManualRefundRequest request); // NakedRefund is a Transaction, with just negative amount

  /**
   * Vault card information. Requests the mini capture card information and request a payment token from the payment gateway.
   * The value returned in the response is a card, with all the information necessary to use for payment in a SaleRequest or AuthRequest
   *
   * @param cardEntryMethods - The card entry methods allowed to capture the payment token. null will provide default values
   **/
  void vaultCard(Integer cardEntryMethods);

  /**
   * used to cancel the current user action on the device.
   */
  void cancel();

  /**
   * Request a closeout of all orders.
   *
   * @param request -
   */
  void closeout(CloseoutRequest request);

  /**
   * Print simple lines of text to the Clover Mini printer
   *
   * @param messages -
   **/
  void printText(List<String> messages);

  /**
   * Print an image on the Clover Mini printer
   *
   * @param image -
   **/
  void printImage(Bitmap image);

  /**
   * Print an image on the Clover Mini printer
   * @param url
   */
  void printImageFromURL(String url);

  /**
   * Show a message on the Clover Mini screen
   *
   * @param message -
   **/
  void showMessage(String message);

  /**
   * Return the device to the Welcome Screen
   **/
  void showWelcomeScreen();

  /**
   * Show the thank you screen on the device
   **/
  void showThankYouScreen();

  /**
   * display the payment receipt screen for the orderId/paymentId combination.
   *
   * @param paymentId
   * @param orderId
   */
  void displayPaymentReceiptOptions(String orderId, String paymentId);

  /**
   * Will trigger cash drawer to open that is connected to Clover Mini
   **/
  void openCashDrawer(String reason);

  /**
   * Show the DisplayOrder on the device. Replaces the existing DisplayOrder on the device.
   *
   * @param order -
   **/
  void showDisplayOrder(DisplayOrder order);

  /**
   * Remove the DisplayOrder from the device.
   *
   * @param order -
   **/
  void removeDisplayOrder(DisplayOrder order);

  /**
   *  return the Merchant object for the Merchant configured for the Clover Mini
   **/

  /**
   * will dispose of the underlying connection to the device
   */
  void dispose();

  /**
   * Used to invoke user options on the mini such as "OK", "CANCEL", "DONE", etc.
   *
   * @param io
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
   * haven't processed yet. will trigger an onRetrievePendingPaymentsResponse callback
   */
  void retrievePendingPayments();

  /**
   * Used to request card information. Specifically track1 and track2 information
   *
   * @param request - The card entry methods allowed to request track information. null will provide default values
   */
  void readCardData(ReadCardDataRequest request);
}
