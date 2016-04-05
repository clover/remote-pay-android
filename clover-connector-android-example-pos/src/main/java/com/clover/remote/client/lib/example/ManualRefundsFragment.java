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
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.clover.remote.client.CloverConnector;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.lib.example.adapter.ItemsListViewAdapter;
import com.clover.remote.client.lib.example.adapter.OrdersListViewAdapter;
import com.clover.remote.client.lib.example.adapter.PaymentsListViewAdapter;
import com.clover.remote.client.lib.example.adapter.RefundsListViewAdapter;
import com.clover.remote.client.lib.example.model.POSCard;
import com.clover.remote.client.lib.example.model.POSNakedRefund;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.lib.example.model.StoreObserver;
import com.clover.remote.client.messages.RefundPaymentRequest;
import com.clover.remote.client.messages.TipAdjustAuthRequest;
import com.clover.remote.client.messages.VoidPaymentRequest;
import com.clover.sdk.v3.order.VoidReason;

import java.lang.ref.WeakReference;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ManualRefundsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ManualRefundsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManualRefundsFragment extends Fragment {
    private static final String ARG_STORE = "store";

    private POSStore store;

    private OnFragmentInteractionListener mListener;

    private WeakReference<ICloverConnector> cloverConnectorWeakReference;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param store Parameter 1.
     * @param cloverConnector Parameter 2.
     * @return A new instance of fragment OrdersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ManualRefundsFragment newInstance(POSStore store, CloverConnector cloverConnector) {
        ManualRefundsFragment fragment = new ManualRefundsFragment();
        fragment.setStore(store);
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public ManualRefundsFragment() {
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
        final View view = inflater.inflate(R.layout.fragment_refunds, container, false);



        final ListView refundsListView = (ListView)view.findViewById(R.id.RefundsListView);
        final RefundsListViewAdapter itemsListViewAdapter = new RefundsListViewAdapter(view.getContext(), R.id.RefundsListView, store.getRefunds());
        refundsListView.setAdapter(itemsListViewAdapter);

        store.addStoreObserver(new StoreObserver() {
            @Override public void newOrderCreated(POSOrder order) {

            }

            @Override public void cardAdded(POSCard card) {

            }

            @Override public void refundAdded(POSNakedRefund refund) {
                final RefundsListViewAdapter itemsListViewAdapter = new RefundsListViewAdapter(view.getContext(), R.id.RefundsListView, store.getRefunds());
                refundsListView.setAdapter(itemsListViewAdapter);
            }


            @Override public void preAuthAdded(POSPayment payment) {

            }

            @Override public void preAuthRemoved(POSPayment payment) {

            }
        });


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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    public void setCloverConnector(ICloverConnector cloverConnector) {
        cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
    }

}
