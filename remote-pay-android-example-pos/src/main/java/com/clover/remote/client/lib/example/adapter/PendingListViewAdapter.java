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
import com.clover.remote.PendingPaymentEntry;
import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;

import java.util.List;
import java.util.Locale;

public class PendingListViewAdapter extends ArrayAdapter<PendingPaymentEntry> {
  public PendingListViewAdapter(Context context, int resource) {
    super(context, resource);
  }

  public PendingListViewAdapter(Context context, int resource, List<PendingPaymentEntry> items) {
    super(context, resource, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.pendings_row, null);
    }

    PendingPaymentEntry pendingPaymentEntry = getItem(position);

    if (pendingPaymentEntry != null) {
      TextView idColumn = (TextView) v.findViewById(R.id.PendingPaymentIDColumn);
      TextView amountColumn = (TextView) v.findViewById(R.id.PendingAmountColumn);

      idColumn.setText(pendingPaymentEntry.paymentId == null ? "" : pendingPaymentEntry.paymentId);
      amountColumn.setText(CurrencyUtils.format(pendingPaymentEntry.amount, Locale.getDefault()));
    }

    return v;
  }
}
