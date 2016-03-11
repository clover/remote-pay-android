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
import com.clover.remote.client.lib.example.model.POSExchange;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSRefund;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;

import java.util.List;
import java.util.Locale;

public class PaymentsListViewAdapter extends ArrayAdapter<POSExchange> {

  public PaymentsListViewAdapter(Context context, int resource) {
    super(context, resource);
  }

  public PaymentsListViewAdapter(Context context, int resource, List<POSExchange> items) {
    super(context, resource, items);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    View v = convertView;

    if (v == null) {
      LayoutInflater vi;
      vi = LayoutInflater.from(getContext());
      v = vi.inflate(R.layout.payments_row, null);
    }

    POSExchange posPayment = getItem(position);

    if (posPayment != null) {
      TextView statusColumn = (TextView) v.findViewById(R.id.PaymentsRowStatusColumn);
      //TextView itemAmountColumn = (TextView) v.findViewById(R.id.PaymentsRowAmountColumn);
      TextView tipColumn = (TextView) v.findViewById(R.id.PaymentsRowTipColumn);
      TextView totalColumn = (TextView) v.findViewById(R.id.PaymentsRowTotalColumn);
      TextView tipColumnLabel = (TextView) v.findViewById(R.id.PaymentsRowTipColumnLabel);
      TextView externalIdColumn = (TextView) v.findViewById(R.id.PaymentRowExternalPaymentId);
      //TextView itemAmountColumnLabel = (TextView) v.findViewById(R.id.PaymentsRowItemAmountColumnLabel);


      if (posPayment instanceof POSPayment) {

        statusColumn.setText(((POSPayment) posPayment).getPaymentStatus() == null ? "" : "" + ((POSPayment) posPayment).getPaymentStatus());
        //itemAmountColumn.setText(CurrencyUtils.format(((POSPayment) posPayment).getOrder().getTotal(), Locale.getDefault()));
        tipColumn.setVisibility(View.VISIBLE);
        totalColumn.setVisibility(View.VISIBLE);
        tipColumn.setText(CurrencyUtils.format(((POSPayment) posPayment).getTipAmount(), Locale.getDefault()));
        totalColumn.setText(CurrencyUtils.format(((POSPayment) posPayment).getAmount(), Locale.getDefault()));
        String externalPaymentId = ((POSPayment) posPayment).getExternalPaymentId();
        externalIdColumn.setText(externalPaymentId != null ? externalPaymentId : "<unset>");
      } else if (posPayment instanceof POSRefund) {
        statusColumn.setText("REFUND");
        //itemAmountColumn.setText(CurrencyUtils.format(((POSRefund) posPayment).getAmount(), Locale.getDefault()));
        tipColumn.setVisibility(View.GONE);
        //itemAmountColumn.setVisibility(View.INVISIBLE);
        tipColumnLabel.setVisibility(View.GONE);
        //itemAmountColumnLabel.setVisibility(View.INVISIBLE);
      }
    }

    return v;
  }
}
