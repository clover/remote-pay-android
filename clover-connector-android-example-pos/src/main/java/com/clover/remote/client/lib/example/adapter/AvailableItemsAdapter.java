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

package com.clover.remote.client.lib.example.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.clover.remote.client.lib.example.R;
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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by blakewilliams on 2/8/16.
 */
public class AvailableItemsAdapter extends ArrayAdapter<POSItem>
{
  POSStore store;
  Map<POSItem, Integer> itemToCount = new HashMap<POSItem, Integer>();

  public AvailableItemsAdapter(Context context, int resource) {
    super(context, resource);
  }

  public AvailableItemsAdapter(Context context, int resource, List<POSItem> items, POSStore store) {
    super(context, resource, items);
    this.store = store;

    this.store.addCurrentOrderObserver(new OrderObserver() {
      @Override public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {
        POSItem item = lineItem.getItem();
        itemToCount.put(item, lineItem.getQuantity());
        notifyDataSetChanged();
      }

      @Override public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {
        itemToCount.remove(lineItem.getItem());
        notifyDataSetChanged();
      }

      @Override public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {
        POSItem item = lineItem.getItem();
        itemToCount.put(item, lineItem.getQuantity());
        notifyDataSetChanged();
      }

      @Override public void paymentAdded(POSOrder posOrder, POSPayment payment) {

      }

      @Override public void refundAdded(POSOrder posOrder, POSRefund refund) {

      }

      @Override public void paymentChanged(POSOrder posOrder, POSExchange pay) {

      }

      @Override public void discountAdded(POSOrder posOrder, POSDiscount discount) {

      }

      @Override public void discountChanged(POSOrder posOrder, POSDiscount discount) {

      }
    });
    this.store.addStoreObserver(new StoreObserver() {
      @Override public void newOrderCreated(POSOrder order) {
        itemToCount.clear();
      }

      @Override public void cardAdded(POSCard card) {

      }

      @Override public void refundAdded(POSNakedRefund refund) {

      }

      @Override public void preAuthAdded(POSPayment payment) {

      }

      @Override public void preAuthRemoved(POSPayment payment) {

      }
    });
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.fragment_available_item, null);
    }

    POSItem posItem = getItem(position);


    if (posItem != null) {
      TextView nameField = (TextView) v.findViewById(R.id.ItemNameLabel);
      TextView priceField = (TextView) v.findViewById(R.id.ItemNamePrice);


      nameField.setText(posItem.getName());
      priceField.setText(CurrencyUtils.format(posItem.getPrice(), Locale.getDefault()));

      Integer count = itemToCount.get(posItem);
      TextView tv = (TextView)v.findViewById(R.id.ItemBadge);
      if(count != null && count > 0) {
        tv.setVisibility(View.VISIBLE);
        tv.setText(count.toString());
      } else {
        tv.setVisibility(View.GONE);
        notifyDataSetChanged();
      }
    }

    return v;
  }
}
