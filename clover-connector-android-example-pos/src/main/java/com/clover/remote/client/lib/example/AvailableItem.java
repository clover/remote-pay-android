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
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.clover.common.util.CurrencyUtils;
import com.clover.remote.client.lib.example.model.POSItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AvailableItem.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AvailableItem#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AvailableItem extends Fragment {
  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

  POSItem item;
  List<AvailableItemListener> listeners = new ArrayList<AvailableItemListener>(5);

  private OnFragmentInteractionListener mListener;

  private TextView badgeView;


  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment AvailableItem.
   */
  // TODO: Rename and change types and number of parameters
  public static AvailableItem newInstance() {
    AvailableItem fragment = new AvailableItem();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public AvailableItem() {
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
    View v = inflater.inflate(R.layout.fragment_available_item, container, false);
    if (v != null) {
      TextView tv = (TextView) v.findViewById(R.id.ItemNameLabel);
      if (tv != null) {
        tv.setText(item.getName());
      }
      tv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          addItemToOrder();
        }
      });
      tv = (TextView) v.findViewById(R.id.ItemNamePrice);
      tv.setText(CurrencyUtils.longToAmountString(java.util.Currency.getInstance(Locale.getDefault()), item.getPrice()));
      tv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          addItemToOrder();
        }
      });

      badgeView = (TextView) v.findViewById(R.id.ItemBadge);
    }
    return v;
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

  public void addListener(AvailableItemListener listener) {
    listeners.add(listener);
  }

  public void setQuantity(int quantity) {

    badgeView.setText("" + quantity);
    if (quantity <= 0) {
      badgeView.setVisibility(View.INVISIBLE);
    } else {
      badgeView.setVisibility(View.VISIBLE);
    }
  }


  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onFragmentInteraction(Uri uri);
  }

  public void addItemToOrder() {
    for (AvailableItemListener listener : listeners) {
      listener.onItemSelected(item);
    }
  }


  public void setItem(POSItem item) {
    this.item = item;

  }
}
