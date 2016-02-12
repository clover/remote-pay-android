package com.clover.remote.client.lib.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.model.POSNakedRefund;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSRefund;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;

import java.util.List;
import java.util.Locale;

/**
 * Created by blakewilliams on 1/27/16.
 */
public class RefundsListViewAdapter extends ArrayAdapter<POSNakedRefund> {
    public RefundsListViewAdapter(Context context, int resource) {
      super(context, resource);
    }

    public RefundsListViewAdapter(Context context, int resource, List<POSNakedRefund> items) {
      super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

      View v = convertView;

      if (v == null) {
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());
        v = vi.inflate(R.layout.refunds_row, null);
      }

      POSNakedRefund posRefund = getItem(position);

      if (posRefund != null) {
        TextView amountColumn = (TextView) v.findViewById(R.id.RefundsRowAmount);
        TextView dateColumn = (TextView) v.findViewById(R.id.RefundsRowDate);

        amountColumn.setText(CurrencyUtils.format(posRefund.Amount, Locale.getDefault()));
        dateColumn.setText(""+posRefund.date);
      }

      return v;
    }
}
