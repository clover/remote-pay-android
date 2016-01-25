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

package com.clover.remote.client.lib.example.model;

public class POSOrderDiscount extends POSDiscount {
  public POSOrderDiscount(long fixedDiscountAmount, String name) {
    setAmountOff(fixedDiscountAmount);
    this.name = name;
  }

  public POSOrderDiscount(float percentageOff, String name) {
    setPercentageOff(percentageOff);
    this.name = name;
  }

  public long Value(POSOrder order) {
    if (getAmountOff() > 0) {
      return getAmountOff();
    } else {
      return (int) (order.getPreTaxSubTotal() * getPercentageOff());
    }
  }

}
