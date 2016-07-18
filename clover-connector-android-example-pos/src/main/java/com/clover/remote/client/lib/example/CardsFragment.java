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
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.lib.example.adapter.CardsListViewAdapter;
import com.clover.remote.client.lib.example.model.POSCard;
import com.clover.remote.client.lib.example.model.POSNakedRefund;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.lib.example.model.StoreObserver;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.sdk.v3.payments.VaultedCard;

import java.lang.ref.WeakReference;

public class CardsFragment extends Fragment {
    private static final String ARG_STORE = "store";

    private POSStore store;

    private OnFragmentInteractionListener mListener;

    private WeakReference<ICloverConnector> cloverConnectorWeakReference;
    private ListView cardsListView;

    public static CardsFragment newInstance(POSStore store, ICloverConnector cloverConnector) {
        CardsFragment fragment = new CardsFragment();
        fragment.setStore(store);
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public CardsFragment() {
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
        final View view = inflater.inflate(R.layout.fragment_cards, container, false);


        store.addStoreObserver(new StoreObserver() {
            @Override public void newOrderCreated(POSOrder order) {

            }

            @Override public void cardAdded(POSCard card) {
                final CardsListViewAdapter cardsListViewAdapter = new CardsListViewAdapter(view.getContext(), R.id.CardsListView, store.getCards());
                new AsyncTask(){
                    @Override protected Object doInBackground(Object[] params) {
                        return null;
                    }

                    @Override protected void onPostExecute(Object o) {
                        cardsListView.setAdapter(cardsListViewAdapter);
                    }
                }.execute();
            }

            @Override public void refundAdded(POSNakedRefund refund) {

            }

            @Override public void preAuthAdded(POSPayment payment) {

            }

            @Override public void preAuthRemoved(POSPayment payment) {

            }
        });

        cardsListView = (ListView)view.findViewById(R.id.CardsListView);
        final CardsListViewAdapter cardsListViewAdapter = new CardsListViewAdapter(view.getContext(), R.id.CardsListView, store.getCards());
        cardsListView.setAdapter(cardsListViewAdapter);


        cardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final POSCard posCard = (POSCard) cardsListView.getItemAtPosition(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String[] paymentOptions = null;

                String[] payOptions = new String[]{"Sale for current order", "Auth for current order"/*, "Pre-Auth"*/};

                builder.setTitle("Pay With Card").
                    setItems(payOptions, new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialog, int index) {
                            final ICloverConnector cloverConnector = cloverConnectorWeakReference.get();
                            if(cloverConnector != null) {

                                VaultedCard vaultedCard = new VaultedCard();
                                vaultedCard.setCardholderName(posCard.getName());
                                vaultedCard.setFirst6(posCard.getFirst6());
                                vaultedCard.setLast4(posCard.getLast4());
                                vaultedCard.setExpirationDate(posCard.getMonth() + posCard.getYear());
                                vaultedCard.setToken(posCard.getToken());

                                switch(index) {
                                    case 0: {
                                        SaleRequest saleRequest = new SaleRequest(store.getCurrentOrder().getTotal(), ExamplePOSActivity.getNextId());
                                        saleRequest.setTippableAmount(store.getCurrentOrder().getTippableAmount());
                                        saleRequest.setVaultedCard(vaultedCard);
                                        cloverConnector.sale(saleRequest);
                                        dialog.dismiss();
                                        break;
                                    }
                                    case 1: {
                                        AuthRequest authRequest = new AuthRequest(store.getCurrentOrder().getTotal(), ExamplePOSActivity.getNextId());
                                        authRequest.setVaultedCard(vaultedCard);
                                        cloverConnector.auth(authRequest);
                                        dialog.dismiss();
                                        break;
                                    }
                                }
                            } else {
                                Toast.makeText(getActivity().getBaseContext(), "Clover Connector is null", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                final Dialog dlg = builder.create();
                dlg.show();
            }
        });

        return view;
    }

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
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setStore(POSStore store) {
        this.store = store;
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    public void setCloverConnector(ICloverConnector cloverConnector) {
        cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
    }

}
