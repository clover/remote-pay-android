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
import com.clover.remote.terminal.InputOption;

import java.util.List;

public interface ICloverConnector {

  /**
   * Sale method, aka "purchase"
   *
   * @param request - A SaleRequest object containing basic information needed for the transaction
   */
  void sale(SaleRequest request);

  /// <summary>
  /// If signature is captured during a Sale, this method accepts the signature as entered
  /// </summary>
  /// <param name="request"></param>
  /// <returns></returns>
  void acceptSignature(SignatureVerifyRequest request);

  /// <summary>
  /// If signature is captured during a Sale, this method rejects the signature as entered
  /// </summary>
  /// <param name="request"></param>
  /// <returns></returns>
  void rejectSignature(SignatureVerifyRequest request);

  /// <summary>
  /// Auth method to obtain an Auth or Pre-Auth, based on the AuthRequest IsPreAuth flag
  /// </summary>
  /// <param name="request"></param>
  /// <returns></returns>
  void auth(AuthRequest request);

  /// <summary>
  /// PreAuth method to obtain a Pre-Auth for a card
  /// </summary>
  /// <param name="request"></param>
  /// <returns></returns>
  void preAuth(PreAuthRequest request);

  /// <summary>
  /// Capture a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
  /// </summary>
  /// <param name="request"></param>
  /// <returns></returns>
  void captureAuth(CaptureAuthRequest request);

  /// <summary>
  /// Adjust the tip for a previous Auth. Note: Should only be called if request's PaymentID is from an AuthResponse
  /// </summary>
  /// <param name="request"></param>
  /// <returns></returns>
  void tipAdjustAuth(TipAdjustAuthRequest request);

  /// <summary>
  /// Void a transaction, given a previously used order ID and/or payment ID
  /// TBD - defining a payment or order ID to be used with a void without requiring a response from Sale()
  /// </summary>
  /// <param name="request">A VoidRequest object containing basic information needed to void the transaction</param>
  /// <returns>Status code, 0 for success, -1 for failure (need to use pre-defined constants)</returns>
  void voidPayment(VoidPaymentRequest request);


  /// <summary>
  /// called when requesting a payment be voided when only the request UUID is available
  /// </summary>
  /// <param name="request"></param>
  /// <returns></returns>
  //void voidTransaction(VoidTransactionRequest request);


  /// <summary>
  /// Refund a specific payment
  /// </summary>
  /// <param name="request"></param>
  /// <returns></returns>
  void refundPayment(RefundPaymentRequest request);


  /// <summary>
  /// Manual refund method, aka "naked credit"
  /// </summary>
  /// <param name="request">A ManualRefundRequest object</param>
  /// <returns>Status code, 0 for success, -1 for failure (need to use pre-defined constants)</returns>
  void manualRefund(ManualRefundRequest request); // NakedRefund is a Transaction, with just negative amount

  /// <summary>
  /// Vault card information. Requests the mini capture card information and request a payment token from the payment gateway.
  /// The value returned in the response is a card, with all the information necessary to use for payment in a SaleRequest or AuthRequest
  /// </summary>
  /// <param name="cardEntryMethods">The card entry methods allowed to capture the payment token. null will provide default values</param>
  void vaultCard(Integer cardEntryMethods);

  /// <summary>
  /// Cancels the device from waiting for a payment card.
  /// </summary>
  /// <returns></returns>
  void cancel();

  /// <summary>
  /// Send a request to the server to closeout all orders.
  /// </summary>
  /// <param name="request"></param>
  /// <returns></returns>
  //void closeout();

  /// <summary>
  /// Print simple lines of text to the Clover Mini printer
  /// </summary>
  /// <param name="messages"></param>
  /// <returns></returns>
  void printText(List<String> messages);

  /// <summary>
  /// Print an image on the Clover Mini printer
  /// </summary>
  /// <param name="bitmap"></param>
  /// <returns></returns>
  void printImage(Bitmap image);

  /// <summary>
  /// Show a message on the Clover Mini screen
  /// </summary>
  /// <param name="message"></param>
  /// <returns></returns>
  void showMessage(String message);

  /// <summary>
  /// Return the device to the Welcome Screen
  /// </summary>
  void showWelcomeScreen();

  /// <summary>
  /// Show the thank you screen on the device
  /// </summary>
  void showThankYouScreen();

  /**
   * display the receipt screen for the orderId/paymentId combination. The parameters can be null
   * to show the receipt screen for the last orderId/paymentId
   * @param paymentId
   * @param orderId
   */
  void displayReceiptOptions(String orderId, String paymentId);

  /// <summary>
  /// Will trigger cash drawer to open that is connected to Clover Mini
  /// </summary>
  void openCashDrawer(String reason);

  /// <summary>
  /// Show the DisplayOrder on the device. Replaces the existing DisplayOrder on the device.
  /// </summary>
  /// <param name="order"></param>
  void displayOrder(DisplayOrder order);

  /// <summary>
  /// Notify the device of a DisplayLineItem being added to a DisplayOrder
  /// </summary>
  /// <param name="order"></param>
  /// <param name="lineItem"></param>
  void displayOrderLineItemAdded(DisplayOrder order, DisplayLineItem lineItem);

  /// <summary>
  /// Notify the device of a DisplayLineItem being removed from a DisplayOrder
  /// </summary>
  /// <param name="order"></param>
  /// <param name="lineItem"></param>
  void displayOrderLineItemRemoved(DisplayOrder order, DisplayLineItem lineItem);

  /// <summary>
  /// Notify device of a discount being added to the order.
  /// Note: This is independent of a discount being added to a display line item.
  /// </summary>
  /// <param name="order"></param>
  /// <param name="discount"></param>
  void displayOrderDiscountAdded(DisplayOrder order, DisplayDiscount discount);

  /// <summary>
  /// Notify the device that a discount was removed from the order.
  /// Note: This is independent of a discount being removed from a display line item.
  /// </summary>
  /// <param name="order"></param>
  /// <param name="discount"></param>
  void displayOrderDiscountRemoved(DisplayOrder order, DisplayDiscount discount);

  /// <summary>
  /// Remove the DisplayOrder from the device.
  /// </summary>
  /// <param name="order"></param>
  void displayOrderDelete(DisplayOrder order);


  /// <summary>
  /// return the Merchant object for the Merchant configured for the Clover Mini
  /// </summary>
  /// <returns></returns>
  //void getMerchantInfo();

  // TODO: should we call through, repurpose or remove?
  void dispose();


  void invokeInputOption(InputOption io);
}
