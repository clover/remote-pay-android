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
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.clover.remote.client.CloverConnector;
import com.clover.remote.client.messages.SignatureVerifyRequest;
import com.clover.sdk.internal.Signature2;

/**
 * A fragment with a Google +1 button.
 * Activities that contain this fragment must implement the
 * {@link SignatureFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SignatureFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignatureFragment extends Fragment {


  private Signature2 signature;
  Button rejectButton;
  Button acceptButton;
  SignatureView sigCanvas;

  private OnFragmentInteractionListener mListener;
  private SignatureVerifyRequest signatureVerifyRequest;
  private CloverConnector cloverConnector;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param signatureVerifyRequest Parameter 1.
   * @param cloverConnector        Parameter 2.
   * @return A new instance of fragment SignatureFragment.
   */

  public static SignatureFragment newInstance(SignatureVerifyRequest signatureVerifyRequest, CloverConnector cloverConnector) {
    SignatureFragment fragment = new SignatureFragment();
    fragment.setSignatureVerifyRequest(signatureVerifyRequest);
    fragment.setCloverConnector(cloverConnector);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public SignatureFragment() {
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
    final View view = inflater.inflate(R.layout.fragment_signature, container, false);

    if (view != null) {
      sigCanvas = (SignatureView) view.findViewById(R.id.SignatureView);
      rejectButton = (Button) view.findViewById(R.id.RejectButton);
      acceptButton = (Button) view.findViewById(R.id.AcceptButton);

      sigCanvas.setSignature(signatureVerifyRequest.getSignature());

      acceptButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
          fragmentTransaction.hide(SignatureFragment.this);
          fragmentTransaction.commit();
          cloverConnector.acceptSignature(signatureVerifyRequest);
          //((FrameLayout)view.getParent()).removeView(view);
        }
      });
      rejectButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
          fragmentTransaction.hide(SignatureFragment.this);
          fragmentTransaction.commit();
          cloverConnector.rejectSignature(signatureVerifyRequest);
          //((FrameLayout)view.getParent()).removeView(view);
        }
      });
    }

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();

  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (SignatureFragment.OnFragmentInteractionListener) activity;
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

  public void setSignatureVerifyRequest(SignatureVerifyRequest signatureVerifyRequest) {
    this.signatureVerifyRequest = signatureVerifyRequest;
    if (sigCanvas != null) {
      sigCanvas.setSignature(signatureVerifyRequest.getSignature());
      sigCanvas.postInvalidate();
    }
  }

  public SignatureVerifyRequest getSignatureVerifyRequest() {
    return signatureVerifyRequest;
  }

  public void setCloverConnector(CloverConnector cloverConnector) {
    this.cloverConnector = cloverConnector;
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
    public void onFragmentInteraction(Uri uri);
  }


  public void setSignature(Signature2 signature) {
    this.signature = signature;

    int sigWidth = Math.abs(signature.getBounds().first.x - signature.getBounds().second.x);
    int sigHeight = Math.abs(signature.getBounds().first.y - signature.getBounds().second.y);

    float widthToHeight = sigHeight == 0 ? 2 : (1.0f * sigWidth / sigHeight);

    int parentWidth = ((View) getView().getParent()).getWidth();
    int parentHeight = ((View) getView().getParent()).getHeight();


    sigCanvas.setMinimumHeight(sigHeight);
    sigCanvas.setMinimumWidth(sigWidth);


  }


}
