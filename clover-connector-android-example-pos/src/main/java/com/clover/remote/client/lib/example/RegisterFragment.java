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
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import com.clover.common.util.CurrencyUtils;
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
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.order.DisplayDiscount;
import com.clover.remote.order.DisplayLineItem;
import com.clover.remote.order.DisplayOrder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegisterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment implements CurrentOrderFragmentListener, AvailableItemListener {
  private OnFragmentInteractionListener mListener;

  POSStore store;
  ICloverConnector cloverConnector;
  Map<POSItem, AvailableItem> itemToAvailableItem = new HashMap<POSItem, AvailableItem>();

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment RegisterFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static RegisterFragment newInstance(POSStore store, ICloverConnector cloverConnector) {
    //this.store = store;
    //this.cloverConnector = cloverConnector;

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


  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
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

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
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
    SaleRequest request = new SaleRequest();
    request.setAmount(store.getCurrentOrder().getTotal());
    request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
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
    AuthRequest request = new AuthRequest();
    request.setAmount(store.getCurrentOrder().getTotal());
    request.setTipAmount(null);
    //request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
    cloverConnector.auth(request);
  }

  @Override
  public void onSelectLineItem() {
    //
  }

  @Override
  public void onItemSelected(POSItem item) {
    store.getCurrentOrder().addItem(item, 1);
    CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
    currentOrderFragment.updateCurrentOrder();
  }

  class RegisterObserver implements StoreObserver, OrderObserver {
    DisplayOrder displayOrder = new DisplayOrder();
    Map<POSLineItem, DisplayLineItem> liToDli = new HashMap<POSLineItem, DisplayLineItem>();

    public RegisterObserver() {
      displayOrder.setLineItems(Collections.EMPTY_LIST);
    }

    @Override
    public void newOrderCreated(POSOrder order) {
      //TODO: I think this should show the welcome screen
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

    @Override
    public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = new DisplayLineItem();
      dli.setName(lineItem.getItem().getName());
      dli.setPrice(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), lineItem.getPrice()));
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), lineItem.getDiscount().getValue(lineItem.getPrice())));
      }
      liToDli.put(lineItem, dli);
      List<DisplayLineItem> items = new ArrayList<DisplayLineItem>();
      items.addAll(displayOrder.getLineItems());
      items.add(dli);
      displayOrder.setLineItems(items);
      updateTotals(posOrder, displayOrder);
      cloverConnector.displayOrderLineItemAdded(displayOrder, dli);

    }

    @Override
    public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = liToDli.get(lineItem);
      //dli.setName(lineItem.getItem().getName());
      //dli.setPrice(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), lineItem.getPrice()));
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), lineItem.getDiscount().getValue(lineItem.getPrice())));
      }

      liToDli.remove(lineItem);
      List<DisplayLineItem> items = new ArrayList<DisplayLineItem>();
      for (DisplayLineItem dlItem : displayOrder.getLineItems()) {
        if (dlItem != dli) {
          items.add(dlItem);
        }
      }

      displayOrder.setLineItems(items);
      updateTotals(posOrder, displayOrder);
      cloverConnector.displayOrderLineItemRemoved(displayOrder, dli);
    }

    @Override
    public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = liToDli.get(lineItem);
      dli.setName(lineItem.getItem().getName());
      dli.setQuantity("" + lineItem.getQuantity());
      dli.setPrice(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), lineItem.getPrice()));
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      //dli.getDiscounts().clear();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), lineItem.getDiscount().getValue(lineItem.getPrice())));
      }
      dli.setDiscounts(dDiscounts);
      updateTotals(posOrder, displayOrder);
      cloverConnector.displayOrder(displayOrder);

    }

    private void updateTotals(POSOrder order, DisplayOrder displayOrder) {
      displayOrder.setTax(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), order.getTaxAmount()));
      displayOrder.setSubtotal(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), order.getPreTaxSubTotal()));
      displayOrder.setTotal(CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), order.getTotal()));


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
