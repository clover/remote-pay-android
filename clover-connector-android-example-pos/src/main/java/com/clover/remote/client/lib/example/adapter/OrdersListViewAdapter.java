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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;

import java.util.List;
import java.util.Locale;

public class OrdersListViewAdapter extends ArrayAdapter<POSOrder> {

  public OrdersListViewAdapter(Context context, int resource) {
    super(context, resource);
  }

  public OrdersListViewAdapter(Context context, int resource, List<POSOrder> items) {
    super(context, resource, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.orders_row, null);
    }

    POSOrder posOrder = getItem(position);

    if (posOrder != null) {
      TextView idColumn = (TextView) v.findViewById(R.id.OrdersRowIdColumn);
      TextView dateColumn = (TextView) v.findViewById(R.id.OrdersRowDateColumn);
      TextView statusColumn = (TextView) v.findViewById(R.id.OrdersRowStatusColumn);
      TextView subtotalColumn = (TextView) v.findViewById(R.id.OrdersRowSubtotalColumn);
      TextView taxColumn = (TextView) v.findViewById(R.id.OrdersRowTaxColumn);
      TextView totalColumn = (TextView) v.findViewById(R.id.OrdersRowTotalColumn);

      idColumn.setText(posOrder.id);
      dateColumn.setText(posOrder.date.toString());
      statusColumn.setText(posOrder.status.toString());
      subtotalColumn.setText(CurrencyUtils.format(posOrder.getPreTaxSubTotal(), Locale.getDefault()));
      taxColumn.setText(CurrencyUtils.format(posOrder.getTaxAmount(), Locale.getDefault()));
      totalColumn.setText(CurrencyUtils.format(posOrder.getTotal(), Locale.getDefault()));
    }

    return v;
  }
}
