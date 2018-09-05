![alt text](https://www.clover.com/assets/images/public-site/press/clover_primary_gray_rgb.png)

# Clover SDK for Android POS integration

## Version 

Current version: 2.0.0

## Overview

This SDK allows your Android-based Point-of-Sale (POS) system to communicate with a [Clover® payment device](https://www.clover.com/pos-hardware/) and process payments. Learn more about [Clover Integrations](https://www.clover.com/integrations).

The Android project includes a connector and an example POS. To work with the project effectively, you will need:
- [Gradle](https://gradle.org) (suggested version 3.4).
- An [Android SDK](http://developer.android.com/sdk/index.html) (17+).
- An IDE, such as [Android Studio](http://developer.android.com/tools/studio/index.html).
- To experience transactions end-to-end from the merchant and customer perspectives, we also recommend ordering a [Clover DevKit](http://cloverdevkit.com/collections/devkits/products/clover-mini-dev-kit).

## Getting connected
1. Download the USB Pay Display app from the Clover App Market onto your Clover DevKit. You can also use the [Secure Network Pay Display app](https://docs.clover.com/build/secure-network-pay-display/) for a local network connection with the Clover DevKit. The remaining steps assume that you are using the USB Pay Display app.
2. Open the USB Pay Display app.
3. Run the Clover Connector Android Example POS app on your Android POS device or emulator.
4. The Example POS screen and device connection status should appear. If the connection was successful, the device status should be "connected." If the device remains disconnected, verify that:
	1) You are connecting the correct cable to the correct connection point on the Clover device. (On the Clover Mini, this is the USB port with the Clover logo.) You will need to use the USB cable that came with the device. 
	2) Your Android device supports “host” mode, which is also referred to as OTG mode. This functionality is required to communicate with the Clover Mini, which will operate in “accessory” mode.
  
## Additional resources

* [Release Notes](https://github.com/clover/remote-pay-android/releases)
* [Secure Network Pay Display](https://docs.clover.com/build/secure-network-pay-display/)
* [Tutorial for the Android SDK](https://docs.clover.com/build/getting-started-with-cloverconnector/?sdk=android)
* [API Documentation](https://clover.github.io/remote-pay-android/2.0.0/docs/)
* [Clover Developer Community](https://community.clover.com/index.html)
