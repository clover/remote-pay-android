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

import com.clover.remote.PendingPaymentEntry;
import com.clover.remote.client.CloverConnector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class POSStore {
  private static int orderNumber = 1000;

  private LinkedHashMap<String, POSItem> availableItems;
  private List<POSDiscount> availableDiscounts;
  private List<POSOrder> orders;
  private List<POSCard> cards;
  private List<POSNakedRefund> refunds;
  private List<POSPayment> preAuths;
  private POSOrder currentOrder;

  private transient Map<String, POSOrder> orderIdToOrder = new HashMap<String, POSOrder>();
  private transient Map<String, POSPayment> paymentIdToPOSPayment = new HashMap<String, POSPayment>();

  private transient List<OrderObserver> orderObservers = new ArrayList<OrderObserver>();
  private transient List<StoreObserver> storeObservers = new ArrayList<StoreObserver>();


  private int cardEntryMethods = CloverConnector.CARD_ENTRY_METHOD_MAG_STRIPE | CloverConnector.CARD_ENTRY_METHOD_NFC_CONTACTLESS | CloverConnector.CARD_ENTRY_METHOD_ICC_CONTACT;
  private Boolean approveOfflinePaymentWithoutPrompt;
  private Boolean allowOfflinePayment;
  private Boolean disablePrinting;
  private List<PendingPaymentEntry> pendingPayments;

  public POSStore() {
    availableItems = new LinkedHashMap<String, POSItem>();
    availableDiscounts = new ArrayList<POSDiscount>();
    orders = new ArrayList<POSOrder>();
    cards = new ArrayList<POSCard>();
    refunds = new ArrayList<POSNakedRefund>();
    preAuths = new ArrayList<POSPayment>();
  }

  public void createOrder() {
    if (currentOrder != null) {
      for (OrderObserver oo : orderObservers) {
        currentOrder.removeObserver(oo);
      }
    }
    POSOrder order = new POSOrder();
    for (OrderObserver oo : orderObservers) {
      order.addOrderObserver(oo);
    }
    order.id = "" + (++orderNumber);
    currentOrder = order;
    orders.add(order);
    orderIdToOrder.put(order.id, order);

    notifyNewOrderCreated(currentOrder);
  }

  private void notifyNewOrderCreated(POSOrder currentOrder) {
    for (StoreObserver so : storeObservers) {
      so.newOrderCreated(currentOrder);
    }
  }

  public void addPaymentToOrder(POSPayment payment, POSOrder order) {
    order.addPayment(payment);
    paymentIdToPOSPayment.put(payment.paymentID, payment);
  }

  public void addRefundToOrder(POSRefund refund, POSOrder order) {
    order.addRefund(refund);
  }

  public void addCurrentOrderObserver(OrderObserver observer) {
    this.orderObservers.add(observer);
    if(currentOrder != null) {
      currentOrder.addOrderObserver(observer);
    }
  }

  public void addStoreObserver(StoreObserver storeObserver) {
    this.storeObservers.add(storeObserver);
  }

  public POSItem addAvailableItem(POSItem item) {
    availableItems.put(item.getId(), item);
    return item;
  }

  public void addAvailableDiscount(POSDiscount discount) {
    availableDiscounts.add(discount);
  }

  public POSOrder getCurrentOrder() {
    return currentOrder;
  }

  public Collection<POSItem> getAvailableItems() {
    return Collections.unmodifiableCollection(availableItems.values());
  }

  public void addCard(POSCard card) {
    cards.add(card);
    for(StoreObserver so : storeObservers) {
      so.cardAdded(card);
    }
  }

  public List<POSCard> getCards() {
    return Collections.unmodifiableList(cards);
  }

  public List<POSOrder> getOrders() {
    return orders;
  }

  public void addRefund(POSNakedRefund nakedRefund) {
    refunds.add(nakedRefund);
    for(StoreObserver so : storeObservers) {
      so.refundAdded(nakedRefund);
    }
  }
  public List<POSNakedRefund> getRefunds() {
    return refunds;
  }

  public void addPreAuth(POSPayment payment) {
    preAuths.add(payment);
    for(StoreObserver so : storeObservers) {
      so.preAuthAdded(payment);
    }
  }

  public void removePreAuth(POSPayment payment) {
    if(preAuths.remove(payment)) {
      for(StoreObserver so : storeObservers) {
        so.preAuthRemoved(payment);
      }
    }
  }

  public List<POSPayment> getPreAuths() {
    return Collections.unmodifiableList(preAuths);
  }

  public void setAllowOfflinePayment(Boolean allowOfflinePayment) {
    this.allowOfflinePayment = allowOfflinePayment;
  }
  public Boolean getAllowOfflinePayment() {
    return this.allowOfflinePayment;
  }

  public void setApproveOfflinePaymentWithoutPrompt(Boolean approveOfflinePaymentWithoutPrompt) {
    this.approveOfflinePaymentWithoutPrompt = approveOfflinePaymentWithoutPrompt;
  }
  public Boolean getApproveOfflinePaymentWithoutPrompt() {
    return this.approveOfflinePaymentWithoutPrompt;
  }

  public void setCardEntryMethods(int cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }

  public int getCardEntryMethods() {
    return cardEntryMethods;
  }

  public Boolean getDisablePrinting() {
    return disablePrinting;
  }

  public void setDisablePrinting(Boolean disablePrinting) {
    this.disablePrinting = disablePrinting;
  }

  public void setPendingPayments(List<PendingPaymentEntry> pendingPayments) {
    this.pendingPayments = pendingPayments;
    for(StoreObserver so : storeObservers) {
      so.pendingPaymentsRetrieved(pendingPayments);
    }
  }
}