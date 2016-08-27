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
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.clover.remote.client.lib.example.model.OrderObserver;
import com.clover.remote.client.lib.example.model.POSDiscount;
import com.clover.remote.client.lib.example.model.POSExchange;
import com.clover.remote.client.lib.example.model.POSLineItem;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSRefund;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CurrentOrderFragment extends Fragment implements OrderObserver {

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
    final ListView currentOrderItemsListView = (ListView) v.findViewById(R.id.CurrentOrderItems);

    currentOrderItemsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
      @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // prompt for delete...
        final POSLineItem lineItem = (POSLineItem)currentOrderItemsListView.getItemAtPosition(position);
        String thisTheseLabel = lineItem.getQuantity() == 1 ? "this" : "these";

        new AlertDialog.Builder(getActivity())
            .setTitle("Delete?")
            .setMessage(String.format("Do you want to remove %s items from the order?", thisTheseLabel))
            .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
              @Override public void onClick(DialogInterface dialog, int which) {
                order.remoteAllItems(lineItem);
              }
            })
            .setNegativeButton("No", null)
            .show();
        return true; // consume the event
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
      getView().post(new Runnable(){
        @Override public void run() {
          ListView listView = (ListView) getView().findViewById(R.id.CurrentOrderItems);
          POSLineItem[] itemArray = new POSLineItem[order.getItems().size()];
          AvailableItemListViewAdapter items = new AvailableItemListViewAdapter(listView.getContext(), R.layout.listitem_order_item, order.getItems().toArray(itemArray));
          listView.setAdapter(items);
        }
      });
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
      getView().post(new Runnable(){
        @Override public void run() {
          String subtotalFormatted = CurrencyUtils.format(order.getPreTaxSubTotal(), Locale.getDefault());
          ((TextView) getView().findViewById(R.id.SubtotalLabel)).setText(subtotalFormatted);
          String taxFormatted = CurrencyUtils.format(order.getTaxAmount(), Locale.getDefault());
          ((TextView) getView().findViewById(R.id.TaxLabel)).setText(taxFormatted);
          String totalFormatted = CurrencyUtils.format(order.getTotal(), Locale.getDefault());
          ((TextView) getView().findViewById(R.id.TotalLabel)).setText(totalFormatted);
        }
      });
    }
  }

  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }

  public void setOrder(POSOrder order) {
    this.order.removeObserver(this);
    this.order = order;
    this.order.addOrderObserver(this);
    updateCurrentOrder();
    updateTotals();
  }

  @Override public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {
    updateCurrentOrder();
  }

  @Override public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {
    updateCurrentOrder();

  }

  @Override public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {
    updateCurrentOrder();

  }

  @Override public void paymentAdded(POSOrder posOrder, POSPayment payment) {
    updateCurrentOrder();

  }

  @Override public void refundAdded(POSOrder posOrder, POSRefund refund) {
    updateCurrentOrder();

  }

  @Override public void paymentChanged(POSOrder posOrder, POSExchange pay) {
    updateCurrentOrder();

  }

  @Override public void discountAdded(POSOrder posOrder, POSDiscount discount) {
    updateCurrentOrder();

  }

  @Override public void discountChanged(POSOrder posOrder, POSDiscount discount) {
    updateCurrentOrder();

  }

}
