package com.clover.remote.client.lib.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.clover.remote.client.lib.example.R;
import com.clover.remote.client.lib.example.model.POSCard;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;

import java.util.List;
import java.util.Locale;

/**
 * Created by blakewilliams on 1/17/16.
 */
public class PreAuthListViewAdapter extends ArrayAdapter<POSPayment>{

    public PreAuthListViewAdapter(Context context, int resource) {
        super(context, resource);
    }

    public PreAuthListViewAdapter(Context context, int resource, List<POSPayment> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.preauth_row, null);
        }

        POSPayment posPayment = getItem(position);

        if (posPayment != null) {
            TextView nameColumn = (TextView) v.findViewById(R.id.PreAuthNameColumn);
            TextView amountColumn = (TextView) v.findViewById(R.id.PreAuthAmountColumn);

            nameColumn.setText("Pre-Authorized");
            amountColumn.setText(CurrencyUtils.format(posPayment.getAmount(), Locale.getDefault()));
        }

        return v;
    }
}
