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

package com.clover.remote.client.lib.example;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import com.clover.remote.PendingPaymentEntry;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.lib.example.adapter.AvailableItemsAdapter;
import com.clover.remote.client.lib.example.model.OrderObserver;
import com.clover.remote.client.lib.example.model.POSCard;
import com.clover.remote.client.lib.example.model.POSDiscount;
import com.clover.remote.client.lib.example.model.POSExchange;
import com.clover.remote.client.lib.example.model.POSItem;
import com.clover.remote.client.lib.example.model.POSLineItem;
import com.clover.remote.client.lib.example.model.POSNakedRefund;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSRefund;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.lib.example.model.StoreObserver;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.order.DisplayDiscount;
import com.clover.remote.order.DisplayLineItem;
import com.clover.remote.order.DisplayOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class RegisterFragment extends Fragment implements CurrentOrderFragmentListener, AvailableItemListener {
  private OnFragmentInteractionListener mListener;

  POSStore store;
  ICloverConnector cloverConnector;
  Map<POSItem, AvailableItem> itemToAvailableItem = new HashMap<POSItem, AvailableItem>();

  public static RegisterFragment newInstance(POSStore store, ICloverConnector cloverConnector) {

    RegisterFragment fragment = new RegisterFragment();
    fragment.setStore(store);
    fragment.setCloverConnector(cloverConnector);

    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }


  public RegisterFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_register, container, false);

    GridView gv = (GridView)view.findViewById(R.id.AvailableItems);

    gv.setId(R.id.AvailableItems);

    final AvailableItemsAdapter availableItemsAdapter = new AvailableItemsAdapter(view.getContext(), R.id.AvailableItems, new ArrayList<POSItem>(store.getAvailableItems()), store);
    gv.setAdapter(availableItemsAdapter);

    gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        POSItem item = availableItemsAdapter.getItem(position);
        onItemSelected(item);
      }
    });

    CurrentOrderFragment currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder));
    currentOrderFragment.setOrder(store.getCurrentOrder());
    currentOrderFragment.addListener(this);
    return view;
  }


  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {

      throw new ClassCastException(activity.toString()
          + " must implement OnFragmentInteractionListener: " + activity.getClass().getName());
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }

  public POSStore getStore() {
    return store;
  }

  public void setStore(POSStore store) {
    this.store = store;

    RegisterObserver observer = new RegisterObserver();
    store.addStoreObserver(observer);
    store.addCurrentOrderObserver(observer);
  }


  public ICloverConnector getCloverConnector() {
    return cloverConnector;
  }

  public void setCloverConnector(ICloverConnector cloverConnector) {
    this.cloverConnector = cloverConnector;
  }


  @Override
  public void onSaleClicked() {
    SaleRequest request = new SaleRequest(store.getCurrentOrder().getTotal(), ExamplePOSActivity.getNextId());
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setAllowOfflinePayment(store.getAllowOfflinePayment());
    request.setApproveOfflinePaymentWithoutPrompt(store.getApproveOfflinePaymentWithoutPrompt());
    request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
    request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
    request.setDisablePrinting(store.getDisablePrinting());
    cloverConnector.sale(request);
  }

  @Override
  public void onNewOrderClicked() {
    store.createOrder();
    CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
    currentOrderFragment.setOrder(store.getCurrentOrder());
    cloverConnector.showWelcomeScreen();
  }

  @Override
  public void onAuthClicked() {
    AuthRequest request = new AuthRequest(store.getCurrentOrder().getTotal(), ExamplePOSActivity.getNextId());
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setAllowOfflinePayment(store.getAllowOfflinePayment());
    request.setApproveOfflinePaymentWithoutPrompt(store.getApproveOfflinePaymentWithoutPrompt());
    request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
    request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
    request.setDisablePrinting(store.getDisablePrinting());
    cloverConnector.auth(request);
  }

  @Override
  public void onSelectLineItem() {
    //
  }

  @Override
  public void onItemSelected(POSItem item) {
    store.getCurrentOrder().addItem(item, 1);
  }

  class RegisterObserver implements StoreObserver, OrderObserver {
    DisplayOrder displayOrder = new DisplayOrder();
    Map<POSLineItem, DisplayLineItem> liToDli = new HashMap<POSLineItem, DisplayLineItem>();

    public RegisterObserver() {
      displayOrder.setLineItems(Collections.EMPTY_LIST);
    }

    @Override
    public void newOrderCreated(POSOrder order) {
      if (cloverConnector != null) {
        cloverConnector.showWelcomeScreen();
      }
      liToDli.clear();
      displayOrder = new DisplayOrder();
      displayOrder.setLineItems(Collections.EMPTY_LIST);
      updateTotals(order, displayOrder);

    }

    @Override 
    public void cardAdded(POSCard card) {

    }

    @Override public void refundAdded(POSNakedRefund refund) {

    }


    @Override public void preAuthAdded(POSPayment payment) {

    }

    @Override public void preAuthRemoved(POSPayment payment) {

    }

    @Override public void pendingPaymentsRetrieved(List<PendingPaymentEntry> pendingPayments) {

    }

    @Override
    public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = new DisplayLineItem();
      dli.setId(lineItem.getId());
      dli.setName(lineItem.getItem().getName());
      dli.setPrice(CurrencyUtils.format(lineItem.getPrice(), Locale.getDefault()));
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getPrice()), Locale.getDefault()));
      }
      liToDli.put(lineItem, dli);
      List<DisplayLineItem> items = new ArrayList<DisplayLineItem>();
      items.addAll(displayOrder.getLineItems());
      items.add(dli);
      displayOrder.setLineItems(items);
      updateTotals(posOrder, displayOrder);
      cloverConnector.showDisplayOrder(displayOrder);

    }

    @Override
    public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = liToDli.get(lineItem);
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getPrice()), Locale.getDefault()));
      }

      liToDli.remove(lineItem);
      List<DisplayLineItem> items = new ArrayList<DisplayLineItem>();
      for (DisplayLineItem dlItem : displayOrder.getLineItems()) {
        if (!dlItem.getId().equals(dli.getId())) {
          items.add(dlItem);
        }
      }

      displayOrder.setLineItems(items);
      updateTotals(posOrder, displayOrder);
      cloverConnector.showDisplayOrder(displayOrder);
    }

    @Override
    public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = liToDli.get(lineItem);
      dli.setName(lineItem.getItem().getName());
      dli.setQuantity("" + lineItem.getQuantity());
      dli.setPrice(CurrencyUtils.format(lineItem.getPrice(), Locale.getDefault()));
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      //dli.getDiscounts().clear();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getPrice()), Locale.getDefault()));
      }
      dli.setDiscounts(dDiscounts);
      updateTotals(posOrder, displayOrder);
      cloverConnector.showDisplayOrder(displayOrder);

    }

    private void updateTotals(POSOrder order, DisplayOrder displayOrder) {
      displayOrder.setTax(CurrencyUtils.format(order.getTaxAmount(), Locale.getDefault()));
      displayOrder.setSubtotal(CurrencyUtils.format(order.getPreTaxSubTotal(), Locale.getDefault()));
      displayOrder.setTotal(CurrencyUtils.format(order.getTotal(), Locale.getDefault()));


      POSDiscount discount = order.getDiscount();
      List<DisplayDiscount> displayDiscounts = null;
      if (discount != null && discount.getValue(1000) != 0) {
        displayDiscounts = new ArrayList<DisplayDiscount>();
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(discount.getName());
        dd.setAmount("" + discount.getValue(order.getPreDiscountSubTotal()));
        displayDiscounts.add(dd);
      }
      displayOrder.setDiscounts(displayDiscounts);
    }

    @Override
    public void paymentAdded(POSOrder posOrder, POSPayment payment) {

    }

    @Override
    public void refundAdded(POSOrder posOrder, POSRefund refund) {

    }

    @Override
    public void paymentChanged(POSOrder posOrder, POSExchange pay) {

    }

    @Override
    public void discountAdded(POSOrder posOrder, POSDiscount discount) {

    }

    @Override
    public void discountChanged(POSOrder posOrder, POSDiscount discount) {

    }
  }

}
