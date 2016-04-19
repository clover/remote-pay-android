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
import com.clover.remote.client.CloverConnector;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.lib.example.adapter.CardsListViewAdapter;
import com.clover.remote.client.lib.example.adapter.PreAuthListViewAdapter;
import com.clover.remote.client.lib.example.model.POSCard;
import com.clover.remote.client.lib.example.model.POSNakedRefund;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.lib.example.model.StoreObserver;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.CaptureAuthRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.sdk.v3.payments.VaultedCard;

import java.lang.ref.WeakReference;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PreAuthFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PreAuthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PreAuthFragment extends Fragment {
  private static final String ARG_STORE = "store";

  private POSStore store;

  private OnFragmentInteractionListener mListener;

  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  private ListView preAuthsListView;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param store           Parameter 1.
   * @param cloverConnector Parameter 2.
   * @return A new instance of fragment OrdersFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static PreAuthFragment newInstance(POSStore store, CloverConnector cloverConnector) {
    PreAuthFragment fragment = new PreAuthFragment();
    fragment.setStore(store);
    Bundle args = new Bundle();
    fragment.setArguments(args);

    fragment.setCloverConnector(cloverConnector);

    return fragment;
  }

  public PreAuthFragment() {
    // Required empty public constructor
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    // Inflate the layout for this fragment
    final View view = inflater.inflate(R.layout.fragment_preauth, container, false);

    store.addStoreObserver(new StoreObserver() {
      @Override public void newOrderCreated(POSOrder order) {

      }

      @Override public void cardAdded(POSCard card) {

      }

      @Override public void refundAdded(POSNakedRefund refund) {

      }

      @Override public void preAuthAdded(POSPayment payment) {
        //preAuthsListView = (ListView)view.findViewById(R.id.PreAuthListView);
        new AsyncTask() {
          @Override protected Object doInBackground(Object[] params) {
            return null;
          }

          @Override protected void onPostExecute(Object o) {
            final PreAuthListViewAdapter cardsListViewAdapter = new PreAuthListViewAdapter(view.getContext(), R.id.PreAuthListView, store.getPreAuths());
            preAuthsListView.setAdapter(cardsListViewAdapter);
          }
        }.execute();
      }

      @Override public void preAuthRemoved(POSPayment payment) {
        new AsyncTask() {
          @Override protected Object doInBackground(Object[] params) {
            return null;
          }

          @Override protected void onPostExecute(Object o) {
            final PreAuthListViewAdapter cardsListViewAdapter = new PreAuthListViewAdapter(view.getContext(), R.id.PreAuthListView, store.getPreAuths());
            preAuthsListView.setAdapter(cardsListViewAdapter);
          }
        }.execute();
      }
    });

    preAuthsListView = (ListView) view.findViewById(R.id.PreAuthListView);
    final CardsListViewAdapter cardsListViewAdapter = new CardsListViewAdapter(view.getContext(), R.id.PreAuthListView, store.getCards());
    preAuthsListView.setAdapter(cardsListViewAdapter);

    preAuthsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final POSPayment posPayment = (POSPayment) preAuthsListView.getItemAtPosition(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] paymentOptions = null;

        String[] payOptions = new String[] { "Pay for current order" };

        builder.setTitle("Pay With PreAuth").
            setItems(payOptions, new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialog, int index) {
                final ICloverConnector cloverConnector = cloverConnectorWeakReference.get();
                if (cloverConnector != null) {

                  switch (index) {
                    case 0: {
                    CaptureAuthRequest car = new CaptureAuthRequest();
                    car.paymentID = posPayment.getPaymentID();
                    car.amount = store.getCurrentOrder().getTotal();
                    car.tipAmount = store.getCurrentOrder().getTips();
                    cloverConnector.captureAuth(car);
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

  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override public void onDetach() {
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
