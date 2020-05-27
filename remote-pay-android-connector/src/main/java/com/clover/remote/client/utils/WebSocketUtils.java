package com.clover.remote.client.utils;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Locale;

public class WebSocketUtils {
  private static final String LINE_SEPERATOR = System.getProperty("line.separator");
  protected static final String DEV_CERT_URL_PATTERN = "%s/v2/device_ca_certificate";
  protected static final String ENV_CERT_URL_PATTERN = "%s/v2/env_device_ca_certificate";
  public static final String CLOVER_PROD_SERVER = "https://www.clover.com";
  private static final String SECURE_URL = "wss://%s:%s/remote_pay";
  private static final String NONSECURE_URL = "ws://%s:%s/remote_pay";
  private WebSocketUtils() {
    // Private constructor for utility class
  }

  /**
   * Creates a default KeyStore from '/resources/certs' of Clover Device CA Certificates
   * @param afterLoaded listener for when certs are loaded
   */
  public static void createTrustStoreFromResource(TrustStoreLoadListener afterLoaded) {
    try {
      KeyStore trustStore = initTrustStore();
      int numLoaded=0;
      // This can happen on the main thread.
      // Load the old "dev" cert.  This should be valid for all target environments (dev, stg, sandbox, prod).
      Certificate cert = loadCertificateFromResource("/certs/device_ca_certificate.crt");
      if (null != cert) {
        trustStore.setCertificateEntry("dev", cert);
        numLoaded++;
      }
      // Now load the environment specific cert (e.g. prod).  Always retrieving this cert from prod as it is really
      // only valid in prod at this point, and we also don't have a mechanism within the SDK of specifying the target
      // environment.
      cert = loadCertificateFromResource("/certs/env_device_ca_certificate.crt");
      if (null != cert) {
        trustStore.setCertificateEntry("prod", cert);
        numLoaded++;
      }
      afterLoaded.onTrustStoreLoaded(trustStore, numLoaded);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates a default KeyStore from 'https://www.clover.com' of Clover Device CA Certificates
   * @param afterLoaded listener for when certs are loaded
   */
  public static void createTrustStoreFromNetwork(TrustStoreLoadListener afterLoaded) {
    try {
      KeyStore trustStore = initTrustStore();
      RetrieveCertificatesTask retrieveCertificatesTask = new RetrieveCertificatesTask(trustStore, afterLoaded);
      retrieveCertificatesTask.execute(CLOVER_PROD_SERVER);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static KeyStore initTrustStore() throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {

    String storeType = KeyStore.getDefaultType();
    KeyStore trustStore = KeyStore.getInstance(storeType);
    char[] TRUST_STORE_PASSWORD = "clover".toCharArray();
    trustStore.load(null, TRUST_STORE_PASSWORD);
    return trustStore;
  }

  private static Certificate loadCertificateFromResource(String name) {
    System.out.println("Loading cert:  " + name);

    InputStream is = null;
    try {
      is = WebSocketUtils.class.getResourceAsStream(name);

      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      return cf.generateCertificate(is);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Exception ex) {
          // NO-OP
        }
      }
    }
  }

  static Certificate loadCertificate(URL url) {
    BufferedReader in = null;
    ByteArrayInputStream bais = null;
    try {
      in = new BufferedReader(new InputStreamReader(url.openStream()));
      StringBuilder response = new StringBuilder();
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine).append(LINE_SEPERATOR);
      }
      bais = new ByteArrayInputStream(response.toString().getBytes("US-ASCII"));

      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      return cf.generateCertificate(bais);
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    } finally {
      if (bais != null) {
        try {
          bais.close();
        } catch (Exception ex) {
          // NO-OP
        }
      }

      if (in != null) {
        try {
          in.close();
        } catch (Exception ex) {
          // NO-OP
        }
      }
    }
  }

  /**
   * Creates a URI for a secure websocket connection to a Clover Device
   * @param ipAddress IP Address of Clover Device
   * @param port Port Number of Clover Device
   * @return a URI for secure connection to Clover Device
   */
  public static URI createSecureURI(String ipAddress, String port) {
    try {
      return new URI(String.format(Locale.US, SECURE_URL, ipAddress, port));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Creates a URI for a non-secure websocket connection to a Clover Device
   * @param ipAddress IP Address of Clover Device
   * @param port Port Number of Clover Device
   * @return a URI for non-secure connection to Clover Device
   */
  public static URI createNonsecureURI(String ipAddress, String port) {
    try {
      return new URI(String.format(Locale.US, NONSECURE_URL, ipAddress, port));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}

class RetrieveCertificatesTask extends AsyncTask<String, Void, Integer> {

  private static final String TAG = RetrieveCertificatesTask.class.getSimpleName();

  private KeyStore trustStore;
  private TrustStoreLoadListener trustStoreLoadListener;

  RetrieveCertificatesTask(KeyStore keyStore, TrustStoreLoadListener listener) {
    this.trustStore = keyStore;
    this.trustStoreLoadListener = listener;
  }

  @Override
  protected Integer doInBackground(String... cloverServerBaseUrls) {
    int numLoaded = 0;
    try {
      if (null != cloverServerBaseUrls && cloverServerBaseUrls.length > 0) {
        String cloverServerBaseUrl = cloverServerBaseUrls[0];
        if (load(WebSocketUtils.DEV_CERT_URL_PATTERN, "dev", cloverServerBaseUrl)) {
          numLoaded++;
        }
        if (load(WebSocketUtils.ENV_CERT_URL_PATTERN, "prod", cloverServerBaseUrl)) {
          numLoaded++;
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "Error loading certs %s", e);
    }
    return numLoaded;
  }

  @Override
  protected void onPostExecute(Integer numLoaded) {
    trustStoreLoadListener.onTrustStoreLoaded(trustStore, numLoaded != null ? numLoaded : 0);
  }

  private boolean load(String pattern, String alias, String cloverServerBaseUrl) {
    try {
      URL url = new URL(String.format(pattern, cloverServerBaseUrl));
      Log.d(TAG, String.format("Loading cert from %s", url));
      Certificate cert = WebSocketUtils.loadCertificate(url);
      if (null != cert) {
        trustStore.setCertificateEntry(alias, cert);
        return true;
      }
    } catch (Exception e) {
      Log.e(TAG, "Could not load cert %s", e);
    }
    return false;
  }
}

