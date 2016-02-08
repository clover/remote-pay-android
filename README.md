# Clover SDK for Android PoS Integration

Current version: 0.1

## Overview

This SDK provides an API with which to allow your Android-based Point-of-Sale (POS) system to interface with a [Clover® Mini device] (https://www.clover.com/pos-hardware/mini). From the Mini, merchants can accept payments using: credit, debit, EMV contact and contactless (including Apple Pay), gift cards, EBT (electronic benefit transfer), and more. Learn more about integrations at [clover.com/integrations](https://www.clover.com/integrations).

The Android project includes both a connector and example. To effectively work with the project you'll need:
- [Gradle](https://gradle.org) (suggested version 2.10).
- An [Android SDK](http://developer.android.com/sdk/index.html) (17+).
- An [IDE](http://developer.android.com/tools/studio/index.html), Android Studio works well .

To complete a transaction end to end, we recommend getting a [Clover Mini Dev Kit](http://cloverdevkit.com/collections/devkits/products/clover-mini-dev-kit).

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
    cloverConnector = new CloverConnector(new WebSocketCloverDeviceConfiguration(uri));
    cloverConnector.addCloverConnectorListener(new ICloverConnectorListener() {
    ...

} catch (URISyntaxException e) {
    e.printStackTrace();
}
```
