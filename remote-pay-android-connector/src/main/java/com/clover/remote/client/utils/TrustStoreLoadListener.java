package com.clover.remote.client.utils;

import java.security.KeyStore;

public interface TrustStoreLoadListener {

  void onTrustStoreLoaded(KeyStore trustStore, int numLoaded);
}
