/*
 * Copyright (C) 2018 Clover Network, Inc.
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

import com.clover.remote.client.ICloverConnector;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;

public class HomeFragment extends Fragment{

  private OnFragmentInteractionListener mListener;
  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  private View view;

  public static HomeFragment newInstance( ICloverConnector cloverConnector) {
    HomeFragment fragment = new HomeFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public HomeFragment(){

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_home, container, false);
    initButtons();
    return view;
  }

  private void initButtons(){
    int size = (getResources().getDisplayMetrics().heightPixels/4) - 15;
    RelativeLayout[] options = {
        (RelativeLayout) view.findViewById(R.id.registerButton),
        (RelativeLayout) view.findViewById(R.id.PreAuthButton),
        (RelativeLayout) view.findViewById(R.id.OrdersButton),
        (RelativeLayout) view.findViewById(R.id.CardsButton),
        (RelativeLayout) view.findViewById(R.id.RefundButton),
        (RelativeLayout) view.findViewById(R.id.TransactionButton),
        (RelativeLayout) view.findViewById(R.id.CustomActivityButton),
        (RelativeLayout) view.findViewById(R.id.DeviceButton),
        (RelativeLayout) view.findViewById(R.id.RecoveryButton)
    };

    for (RelativeLayout layout : options){
      layout.getLayoutParams().width = size;
      layout.getLayoutParams().height = size;
    }

  }


  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onFragmentInteraction(Uri uri);
  }

  public void setCloverConnector(ICloverConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
  }
}
