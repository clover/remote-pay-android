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
1. Make sure your Clover Mini Dev Kit and Android POS device are on the same network submask and have ports unblocked.
2. Download the USB Pay Display app from the Clover App Market on your Clover Mini Dev Kit.
3. Open the USB Pay Display app. 
4. Run the Clover Connector Android Example POS app on your Android POS device (emulator, device etc.)
5. You should see the example POS screen and connection state listed. If everything worked you'll get a connected status. If it remains disconnected, you'll want to check that 1) You are connecting the correct cable to the correct connection point on the Clover Mini “hub” - port USB(port with Clover logo). You will need to use the USB cable that the device came with. 2)  That your Android devices support “host” or OTG mode, which is required to communicate with the mini, which is in “accessory” mode.



