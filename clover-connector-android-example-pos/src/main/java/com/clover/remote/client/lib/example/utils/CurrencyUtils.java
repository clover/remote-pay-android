package com.clover.remote.client.lib.example.utils;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by blakewilliams on 1/27/16.
 */
public class CurrencyUtils {

  public static String format(long value, Locale locale) {
    double amount = value / 100f;
    if(locale == null) {
      locale = Locale.getDefault();
    }
    Currency currentCurrency = Currency.getInstance(locale);
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);

    return currencyFormatter.format(amount);
  }
}
