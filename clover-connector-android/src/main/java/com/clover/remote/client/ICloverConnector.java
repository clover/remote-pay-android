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

import com.clover.remote.InputOption;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.CaptureAuthRequest;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.client.messages.SignatureVerifyRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.remote.order.DisplayDiscount;
import com.clover.remote.order.DisplayLineItem;
import com.clover.remote.order.DisplayOrder;

import android.graphics.Bitmap;

import java.util.List;

public interface ICloverConnector {

  /**
   * Sale method, aka "purchase"
   *
   * @param request - A SaleRequest object containing basic information needed for the transaction
   */
  int sale(SaleRequest request);

  /**
   * If signature is captured during a Sale, this method accepts the signature as entered
   *
   * @param request -
   **/
  void acceptSignature(SignatureVerifyRequest request);

  /**
   * If signature is captured during a Sale, this method rejects the signature as entered
   *
   * @param request -
   **/
  void rejectSignature(SignatureVerifyRequest request);

  /**
   * Auth method to obtain an Auth payment that can be used as the payment
   * to call tipAdjust
   *
   * @param request -
   **/
  int auth(AuthRequest request);

  /**
   * PreAuth method to obtain a Pre-Auth for a card
   *
   * @param request -
   **/
  int preAuth(PreAuthRequest request);

  /**
   * Capture a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
   *
   * @param request -
   **/
  void captureAuth(CaptureAuthRequest request);

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

  /*
   * called when requesting a payment be voided when only the request UUID is available
   * @param request -
   */
  //void voidTransaction(VoidTransactionRequest request);

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
   * @param allowOpenTabs
   * @param batchId
   */
  void closeout(boolean allowOpenTabs, String batchId);

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
   * display the receipt screen for the orderId/paymentId combination. The parameters can be null
   * to show the receipt screen for the last orderId/paymentId
   *
   * @param paymentId
   * @param orderId
   */
  void displayReceiptOptions(String orderId, String paymentId);

  /**
   * Will trigger cash drawer to open that is connected to Clover Mini
   **/
  void openCashDrawer(String reason);

  /**
   * Show the DisplayOrder on the device. Replaces the existing DisplayOrder on the device.
   *
   * @param order -
   **/
  void displayOrder(DisplayOrder order);

  /**
   * Notify the device of a DisplayLineItem being added to a DisplayOrder
   *
   * @param order    -
   * @param lineItem -
   **/
  void displayOrderLineItemAdded(DisplayOrder order, DisplayLineItem lineItem);

  /**
   * Notify the device of a DisplayLineItem being removed from a DisplayOrder
   *
   * @param order    -
   * @param lineItem -
   **/

  void displayOrderLineItemRemoved(DisplayOrder order, DisplayLineItem lineItem);

  /**
   * Notify device of a discount being added to the order.
   * Note: This is independent of a discount being added to a display line item.
   *
   * @param order    -
   * @param discount -
   **/
  void displayOrderDiscountAdded(DisplayOrder order, DisplayDiscount discount);

  /**
   * Notify the device that a discount was removed from the order.
   * Note: This is independent of a discount being removed from a display line item.
   *
   * @param order    -
   * @param discount -
   **/
  void displayOrderDiscountRemoved(DisplayOrder order, DisplayDiscount discount);

  /**
   * Remove the DisplayOrder from the device.
   *
   * @param order -
   **/
  void displayOrderDelete(DisplayOrder order);

  /**
   *  return the Merchant object for the Merchant configured for the Clover Mini
   **/
  //void getMerchantInfo();

  /**
   *
   */
  void dispose();

  /**
   * Used to invoke user options on the mini such as "OK", "CANCEL", "DONE", etc.
   *
   * @param io
   */
  void invokeInputOption(InputOption io);

}
