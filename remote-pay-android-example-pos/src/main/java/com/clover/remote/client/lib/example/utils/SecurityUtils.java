package com.clover.remote.client.lib.example.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class SecurityUtils {
  private static final String LINE_SEPERATOR = System.getProperty("line.separator");

  private SecurityUtils() {
    // Private constructor for utility class
  }

  public static KeyStore createTrustStore(boolean loadFromResource) {
    try {
      String storeType = KeyStore.getDefaultType();
      KeyStore trustStore = KeyStore.getInstance(storeType);
      char[] TRUST_STORE_PASSWORD = "clover".toCharArray();
      trustStore.load(null, TRUST_STORE_PASSWORD);

      // Load the old "dev" cert.  This should be valid for all target environments (dev, stg, sandbox, prod).
      Certificate cert = loadFromResource ? loadCertificateFromResource("/certs/device_ca_certificate.crt") :
          loadCertificate(new URL("https://www.clover.com/v2/device_ca_certificate"));
      trustStore.setCertificateEntry("dev", cert);

      // Now load the environment specific cert (e.g. prod).  Always retrieving this cert from prod as it is really
      // only valid in prod at this point, and we also don't have a mechanism within the SDK of specifying the target
      // environment.
      cert = loadFromResource ? loadCertificateFromResource("/certs/env_device_ca_certificate.crt") :
          loadCertificate(new URL("https://www.clover.com/v2/env_device_ca_certificate"));
      trustStore.setCertificateEntry("prod", cert);

      return trustStore;
    } catch(Throwable t) {
      t.printStackTrace();
    }
    return null;
  }

  private static Certificate loadCertificate(URL url) {
    System.out.println("Loading cert:  " + url.toString());
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

  private static Certificate loadCertificateFromResource(String name) {
    System.out.println("Loading cert:  " + name);

    InputStream is = null;
    try {
      is = SecurityUtils.class.getResourceAsStream(name);

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
}
