# Clover SDK for Android PoS Integration

Current version: 0.5

## Overview

This SDK provides an API with which to allow your Android-based Point-of-Sale (POS) system to interface with a [Clover® Mini device] (https://www.clover.com/pos-hardware/mini). From the Mini, merchants can accept payments using: credit, debit, EMV contact and contactless (including Apple Pay), gift cards, EBT (electronic benefit transfer), and more. Learn more about integrations at [clover.com/integrations](https://www.clover.com/integrations).

The Android project includes both a connector and example. To effectively work with the project you'll need:
- [Gradle](https://gradle.org) (suggested version 2.10).
- An [Android SDK](http://developer.android.com/sdk/index.html) (17+).
- An [IDE](http://developer.android.com/tools/studio/index.html), Android Studio works well .

To complete a transaction end to end, we recommend getting a [Clover Mini Dev Kit](http://cloverdevkit.com/collections/devkits/products/clover-mini-dev-kit).

## Release Notes
# Version 1.0
* Renamed/Added/Removed a number of API operations and request/response objects to establish 
  better consistency across platforms
  
  * ICloverConnector (Operations)
    * Added 
      * printImageFromURL 
      * initializeConnection (REQUIRED) 
      * addCloverConnectorListener 
      * removeCloverConnectorListener
      * dispose
      * acceptPayment - (REQUIRED) Takes a payment object - possible response to a ConfirmPaymentRequest
      * rejectPayment - (REQUIRED) Takes a payment object and the challenge that was associated with
                        the rejection - possible response to a ConfirmPaymentRequest
    * Renamed
      * capturePreAuth - formerly captureAuth
      * showDisplayOrder - formerly displayOrder - this is now the only operation needed to display/change 
        order information that is displayed on the mini
      * removeDisplayOrder - formerly displayOrderDelete
    * Removed 
      * displayOrderLineItemAdded
      * displayOrderLineItemRemoved
      * displayOrderDiscountAdded
      * displayOrderDiscountRemoved
  * ICloverConnectorListener (Notifications)
    * Added
      * onPaymentConfirmation - (REQUIRED) consists of a Payment and a list of challenges/void reasons  
      * onDeviceError
      * onPrintRefundPayment
      * onPrintMerchantReceipt
      * onPrintPaymentDecline
      * onPrintPayment
      * onPrintCredit
      * onPrintCreditDecline
    * Renamed
      * onDeviceDisconnected - formerly onDisconnected
      * onDeviceConnected - formerly on onConnected
      * onDeviceReady - formerly onReady
      * onTipAdjustAuthResponse - formerly onAuthTipAdjustResponse
      * onCapturePreAuthResponse - formerly onPreAuthCaptureResponse
      * onVerifySignatureRequest - formerly onSignatureVerifyRequest
    * Removed
      * onTransactionState
      * onConfigErrorResponse - These are now processed as normal operation responses
      * onError - now handled by onDeviceError or through normal operation responses
      * onDebug
  * Request/Response Objects
    * Added
      * ConfirmPaymentRequest - Contains a Payment and a list of "challenges" from the 
        Clover device during payment operations, if there are questions for the merchant
        on their willingness to accept whatever risk is associated with that payment's 
        challenge. 
    * Renamed
      * VerifySignatureRequest - formerly SignatureVerifyRequest
      * CapturePreAuthRequest - formerly CaptureAuthRequest
      * VoidPaymentRequest - formerly VoidTransactionRequest
      * CloseoutRequest - formerly separate field-level parameters
      * TipAdjustAuthResponse - formerly AuthTipAdjustResponse
    * Removed
      * ConfigErrorResponse - These are now processed as normal operation responses
* voidPayment operation fix to verify connection status and check for void request
  acknowledgement from the Clover device prior to issuing a successful response
* Added DefaultCloverConnectorListener, which automatically accepts signature if a verify
  signature request is received
  
# Version 0.5
* Fix performance issue in USB connector
* Updated action of broadcast messages for USB connect/disconnect/ready
   * CloverTransport.DEVICE_DISCONNECTED, DEVICE_CONNECTED, DEVICE_READY
* added resetDevice() to CloverConnector, to reset the clover device state

# Version 0.4
* Add support for USB (USBCloverDeviceConfiguration)
* Add support for offline payments

# Version 0.3
* Updated support for externalPaymentId in SaleRequest, AuthRequest and PreAuthRequest
* Added closeout implementation
* Updated reconnect logic in WebSocketTransport
* Updated ExamplePOS App

# Version 0.2
* Update example POS app; should demonstrate all library functions now
* Update PreAuth, PreAuthCapture, and VaultCard methods in CloverConnector

# Version 0.1
* Initial capability

## Getting Connected

1. Make sure your Clover Mini Dev Kit and Android POS device are on the same network submask and have ports unblocked.
2. Download the Network Pay Display app from the Clover App Market on your Clover Mini Dev Kit.
3. Open the Network Pay Display app and you should see a web socket address.
4. Run the Clover Connector Android Example POS app on your Android POS device (emulator, device etc.)
5. Enter the web socket address from step 3. Tap 'OK' and go back.
6. You should see the example POS screen and connection state listed. If everything worked you'll get a connected status. If it remains disconnected, you'll want to do some network troubleshooting. Checking firewall ports and network submasks are good starting points.

## Working with the SDK

If the project libaries are successfully built and synced using Gradle you should see no errors from your IDE when importing or opening the project. Transactions between the device and a POS Android app will work through an instance of a CloverConnector object. Instantiating the object will require a configuration scheme which is usually a web socket device configuration. The next step is to setup a connection listener. Here is an example: 
```
URI uri = null;
try {
    if (cloverConnector != null) {
        cloverConnector.dispose();
    }
    uri = new URI(_checksURL);
    cloverConnector = new CloverConnector(new WebSocketCloverDeviceConfiguration(uri, 2000, 3000));
    cloverConnector.addCloverConnectorListener(new ICloverConnectorListener() {
    ...

} catch (URISyntaxException e) {
    e.printStackTrace();
}
```
