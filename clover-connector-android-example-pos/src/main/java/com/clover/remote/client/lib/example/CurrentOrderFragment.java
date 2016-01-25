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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.clover.common.util.CurrencyUtils;
import com.clover.remote.client.lib.example.model.POSLineItem;
import com.clover.remote.client.lib.example.model.POSOrder;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CurrentOrderFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CurrentOrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CurrentOrderFragment extends Fragment {

  POSOrder order = new POSOrder();
  List<CurrentOrderFragmentListener> listeners = new ArrayList<CurrentOrderFragmentListener>(5);
  private OnFragmentInteractionListener mListener;

  public static CurrentOrderFragment newInstance() {
    CurrentOrderFragment fragment = new CurrentOrderFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public CurrentOrderFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_current_order, container, false);
    updateListView();
    Button newOrderButton = ((Button) v.findViewById(R.id.NewOrderButton));
    newOrderButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onNewOrderClicked();
      }
    });
    Button saleButton = ((Button) v.findViewById(R.id.SaleButton));
    saleButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onSaleClicked();
      }
    });
    Button authButton = ((Button) v.findViewById(R.id.AuthButton));
    authButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onAuthClicked();
      }
    });
    return v;
  }

  private void onNewOrderClicked() {
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.onNewOrderClicked();
    }
  }

  private void onSaleClicked() {
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.onSaleClicked();
    }
  }

  private void onAuthClicked() {
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.onAuthClicked();
    }
  }

  private void updateListView() {

    if (getView() != null) {
      ListView listView = (ListView) getView().findViewById(R.id.CurrentOrderItems);
      POSLineItem[] itemArray = new POSLineItem[order.getItems().size()];
      AvailableItemListViewAdapter items = new AvailableItemListViewAdapter(listView.getContext(), R.layout.listitem_order_item, order.getItems().toArray(itemArray));
      listView.setAdapter(items);
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public void updateCurrentOrder() {
    updateListView();
    updateTotals();
  }

  public void addListener(CurrentOrderFragmentListener listener) {
    listeners.add(listener);
  }

  private void updateTotals() {
    if (getView() != null) {
      String subtotalFormatted = CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), order.getPreTaxSubTotal());
      ((TextView) getView().findViewById(R.id.SubtotalLabel)).setText(subtotalFormatted);
      String taxFormatted = CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), order.getTaxAmount());
      ((TextView) getView().findViewById(R.id.TaxLabel)).setText(taxFormatted);
      String totalFormatted = CurrencyUtils.longToAmountString(Currency.getInstance(Locale.getDefault()), order.getTotal());
      ((TextView) getView().findViewById(R.id.TotalLabel)).setText(totalFormatted);
    }
  }

  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }

  public void setOrder(POSOrder order) {
    this.order = order;
    updateCurrentOrder();
    updateTotals();
  }

}
