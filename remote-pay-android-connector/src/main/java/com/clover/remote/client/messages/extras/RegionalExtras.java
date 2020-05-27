package com.clover.remote.client.messages.extras;
/*
  This class represents the key values that should be used when setting region specific
  transaction data in a request using the setRegionalExtras(Map<String, String>) in a
  request.  The values can be used in conjunction with keys to induce the desired effect during
  the processing of a transaction on the Clover payment device.
 */

public final class RegionalExtras {
  //  As of now, these are Argentina specific keys
  public static final String FISCAL_INVOICE_NUMBER_KEY = "com.clover.regionalextras.ar.FISCAL_INVOICE_NUMBER_KEY";
  public static final String INSTALLMENT_NUMBER_KEY = "com.clover.regionalextras.ar.INSTALLMENT_NUMBER_KEY";
  public static final String INSTALLMENT_PLAN_KEY = "com.clover.regionalextras.ar.INSTALLMENT_PLAN_KEY";


  //  Values - can be used in conjunction with keys to induce the desired effect during
  //  the processing of a transaction on the Clover payment device.

  //  Use with FISCAL_INVOICE_NUMBER_KEY
  public static final String SKIP_FISCAL_INVOICE_NUMBER_SCREEN_VALUE = "com.clover.regionalextras.ar.SKIP_FISCAL_INVOICE_NUMBER_SCREEN_VALUE";

  //  Use with INSTALLMENT_NUMBER_KEY
  public static final String INSTALLMENT_NUMBER_DEFAULT_VALUE = "1";
}