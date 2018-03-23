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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clover.remote.PendingPaymentEntry;
import com.clover.remote.client.lib.example.model.POSTransaction;
import com.clover.remote.client.lib.example.utils.AvailableDiscountListener;
import com.clover.remote.client.lib.example.utils.IdUtils;
import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.lib.example.adapter.AvailableItemsAdapter;
import com.clover.remote.client.lib.example.model.OrderObserver;
import com.clover.remote.client.lib.example.model.POSCard;
import com.clover.remote.client.lib.example.model.POSDiscount;
import com.clover.remote.client.lib.example.model.POSItem;
import com.clover.remote.client.lib.example.model.POSLineItem;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSRefund;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.lib.example.model.StoreObserver;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;
import com.clover.remote.client.messages.AuthRequest;
import com.clover.remote.client.messages.CapturePreAuthRequest;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.remote.order.DisplayDiscount;
import com.clover.remote.order.DisplayLineItem;
import com.clover.remote.order.DisplayOrder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class RegisterFragment extends Fragment implements CurrentOrderFragmentListener, AvailableItemListener, AvailableDiscountListener, PreAuthDialogFragment.PreAuthDialogFragmentListener {
  private OnFragmentInteractionListener mListener;
  private static final String TAG = RegisterFragment.class.getSimpleName();
  private View view;
  POSStore store;
  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  boolean preAuth = false;
  boolean vaulted = false;
  POSCard vaultedCard;
  Map<POSItem, AvailableItem> itemToAvailableItem = new HashMap<POSItem, AvailableItem>();
  GridView availableItems;
  DisplayOrder currentDisplayOrder;

  public static RegisterFragment newInstance(POSStore store, ICloverConnector cloverConnector) {
    RegisterFragment fragment = new RegisterFragment();
    fragment.setStore(store);
    fragment.setCloverConnector(cloverConnector);
    fragment.setPreAuth(false);
    fragment.setVaulted(false);
    fragment.setVaultedCard(null);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public static RegisterFragment newInstance(POSStore store, ICloverConnector cloverConnector, boolean preauth) {
    RegisterFragment fragment = new RegisterFragment();
    fragment.setStore(store);
    fragment.setCloverConnector(cloverConnector);
    fragment.setPreAuth(preauth);
    fragment.setVaulted(false);
    fragment.setVaultedCard(null);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public static RegisterFragment newInstance(POSStore store, ICloverConnector cloverConnector, boolean vaulted, POSCard vaultedCard) {
    RegisterFragment fragment = new RegisterFragment();
    fragment.setStore(store);
    fragment.setCloverConnector(cloverConnector);
    fragment.setVaulted(vaulted);
    fragment.setVaultedCard(vaultedCard);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public RegisterFragment() {
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
    view = inflater.inflate(R.layout.fragment_register, container, false);

    availableItems = (GridView)view.findViewById(R.id.AvailableItems);


    final AvailableItemsAdapter availableItemsAdapter = new AvailableItemsAdapter(view.getContext(), R.id.AvailableItems, new ArrayList<POSItem>(store.getAvailableItems()), store);
    availableItems.setAdapter(availableItemsAdapter);

    availableItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        POSItem item = availableItemsAdapter.getItem(position);
        onItemSelected(item);
      }
    });

    availableItems.setOnScrollListener(new AbsListView.OnScrollListener() {
      @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}

      int lastFirstVisibleItem = -1;
      @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem != lastFirstVisibleItem) {
          availableItemsAdapter.notifyDataSetChanged();
          lastFirstVisibleItem = firstVisibleItem;
        }
      }
    });

    final CurrentOrderFragment currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder));
    currentOrderFragment.setOrder(store.getCurrentOrder());
    currentOrderFragment.setStore(store);
    currentOrderFragment.addListener(this);
    currentOrderFragment.setCloverConnector(getCloverConnector());
    if(preAuth) {
      showPreAuthDialog();
      currentOrderFragment.setPreAuth(true);
    }
    if(vaulted){
      currentOrderFragment.setVaulted(true);
      currentOrderFragment.setVaultedCard(vaultedCard);
    }
    return view;
  }


  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {

      throw new ClassCastException(activity.toString()
                                   + " must implement OnFragmentInteractionListener: " + activity.getClass().getName());
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    CurrentOrderFragment f = (CurrentOrderFragment) getFragmentManager()
        .findFragmentById(R.id.PendingOrder);
    if (f != null)
      getFragmentManager().beginTransaction().remove(f).commit();
  }


  @Override
  public void onContinue(String name, String amount) {
    Log.d("RegisterFragment", name + amount);
    TextView preAuthName = (TextView) getActivity().findViewById(R.id.PreAuthName);
    TextView preAuthAmount = (TextView) getActivity().findViewById(R.id.PreAuthAmount);
    preAuthName.setText("Name: "+name);
    preAuthAmount.setText("Amount: "+amount);
    makePreAuth(amount);
  }

  public void makePreAuth(String amount){
    Long preauthAmount = CurrencyUtils.convertToLong(amount);
    String externalId = IdUtils.getNextId();
    store.getCurrentOrder().setPendingPaymentId(externalId);
    PreAuthRequest request = new PreAuthRequest(preauthAmount, externalId);
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setDisablePrinting(store.getDisablePrinting());
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    Log.d("setPaymentStatus: ", request.toString());
    getCloverConnector().preAuth(request);
  }

  public void clearPreAuth(){
    setPreAuth(false);
    LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.PreAuthInfo);
    layout.setVisibility(View.GONE);
    TextView preAuthName = (TextView) getActivity().findViewById(R.id.PreAuthName);
    TextView preAuthAmount = (TextView) getActivity().findViewById(R.id.PreAuthAmount);
    preAuthName.setText("Name: ");
    preAuthAmount.setText("Amount: ");
  }


  public void clearVaultedCard(){
    setVaulted(false);
    LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.VaultedCardInfo);
    layout.setVisibility(View.GONE);
    TextView vaultName = (TextView) getActivity().findViewById(R.id.VaultedName);
    TextView vaultCardNumber = (TextView) getActivity().findViewById(R.id.VaultedCardNumber );
    vaultName.setText("");
    vaultCardNumber.setText("");
  }



  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }

  public POSStore getStore() {
    return store;
  }

  public void setStore(POSStore store) {
    this.store = store;

    RegisterObserver observer = new RegisterObserver();
    store.addStoreObserver(observer);
    store.addCurrentOrderObserver(observer);
  }

  public ICloverConnector getCloverConnector(){
    return cloverConnectorWeakReference.get();
  }

  public void setCloverConnector(ICloverConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<>(cloverConnector);
  }

  public void setVaulted(boolean value){
    vaulted = value;
    if(!vaulted){
      setVaultedCard(null);
    }

  }

  public void setVaultedCard(POSCard vaultedCard) {
    this.vaultedCard = vaultedCard;
    if (view != null) {
      CurrentOrderFragment currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder));
      currentOrderFragment.setOrder(store.getCurrentOrder());
      currentOrderFragment.setStore(store);
      currentOrderFragment.setVaulted(vaulted);
      currentOrderFragment.setVaultedCard(vaultedCard);
      currentOrderFragment.setCloverConnector(getCloverConnector());
    }
  }

  public void setPreAuth (boolean value){
    preAuth = value;
    if (view != null) {
      if(preAuth) {
        showPreAuthDialog();
      }
      CurrentOrderFragment currentOrderFragment = ((CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder));
      currentOrderFragment.setOrder(store.getCurrentOrder());
      currentOrderFragment.setStore(store);
      currentOrderFragment.setCloverConnector(getCloverConnector());
      currentOrderFragment.setPreAuth(preAuth);
    }
  }

  private void showPreAuthDialog () {
    FragmentManager fm = getFragmentManager();
    PreAuthDialogFragment preAuthDialogFragment = PreAuthDialogFragment.newInstance();
    preAuthDialogFragment.addListener(this);
    preAuthDialogFragment.show(fm, "fragment_preauth_dialog");
  }

  @Override
  public void onSaleClicked() {
    String externalPaymentID = IdUtils.getNextId();
    Log.d(TAG, "Sale ExternalPaymentID:" + externalPaymentID);
    store.getCurrentOrder().setPendingPaymentId(externalPaymentID);
    SaleRequest request = new SaleRequest(store.getCurrentOrder().getTotal(), externalPaymentID);
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setAllowOfflinePayment(store.getAllowOfflinePayment());
    request.setForceOfflinePayment(store.getForceOfflinePayment());
    request.setApproveOfflinePaymentWithoutPrompt(store.getApproveOfflinePaymentWithoutPrompt());
    request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
    request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
    request.setDisablePrinting(store.getDisablePrinting());
    request.setTipMode(store.getTipMode());
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    request.setTipAmount(store.getTipAmount());
    request.setAutoAcceptPaymentConfirmations(store.getAutomaticPaymentConfirmation());
    request.setAutoAcceptSignature(store.getAutomaticSignatureConfirmation());
    getCloverConnector().sale(request);
  }

  @Override
  public void onNewOrderClicked() {
    clearPreAuth();
    clearVaultedCard();
    store.createOrder(true);
    CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
    currentOrderFragment.setOrder(store.getCurrentOrder());
  }

  @Override
  public void onAuthClicked() {
    String externalPaymentID = IdUtils.getNextId();
    Log.d(TAG, "Auth ExternalPaymentID:" + externalPaymentID);
    store.getCurrentOrder().setPendingPaymentId(externalPaymentID);
    AuthRequest request = new AuthRequest(store.getCurrentOrder().getTotal(), externalPaymentID);
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setAllowOfflinePayment(store.getAllowOfflinePayment());
    request.setForceOfflinePayment(store.getForceOfflinePayment());
    request.setApproveOfflinePaymentWithoutPrompt(store.getApproveOfflinePaymentWithoutPrompt());
    request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
    request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
    request.setDisablePrinting(store.getDisablePrinting());
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    request.setAutoAcceptPaymentConfirmations(store.getAutomaticPaymentConfirmation());
    request.setAutoAcceptSignature(store.getAutomaticSignatureConfirmation());
    getCloverConnector().auth(request);
  }

  @Override
  public void onSelectLineItem() {
    //
  }

  @Override
  public void payWithPreAuth(long tipAmount) {
    CapturePreAuthRequest car = new CapturePreAuthRequest();
    car.setPaymentID(store.getCurrentOrder().getPreAuth().getId());
    car.setAmount(store.getCurrentOrder().getTotal());
    car.setTipAmount(tipAmount);
    getCloverConnector().capturePreAuth(car);
  }

  @Override
  public void onItemSelected(POSItem item) {
    store.getCurrentOrder().addItem(item, 1);
  }

  @Override
  public void onDiscountSelected(POSDiscount discount) {
    store.getCurrentOrder().setDiscount(discount);
  }

  class RegisterObserver implements StoreObserver, OrderObserver {
    DisplayOrder displayOrder = new DisplayOrder();
    Map<POSLineItem, DisplayLineItem> liToDli = new HashMap<POSLineItem, DisplayLineItem>();

    public RegisterObserver() {
      displayOrder.setLineItems(Collections.EMPTY_LIST);
    }

    @Override
    public void onCurrentOrderChanged(POSOrder currentOrder) {

    }

    @Override
    public void newOrderCreated(POSOrder order, boolean userInitiated) {
      if (getCloverConnector() != null && userInitiated) {
        getCloverConnector().showWelcomeScreen();
      }
      liToDli.clear();
      displayOrder = new DisplayOrder();
      displayOrder.setLineItems(Collections.EMPTY_LIST);
      updateTotals(order, displayOrder);

    }

    @Override
    public void cardAdded(POSCard card) {

    }

    @Override public void refundAdded(POSTransaction refund) {

    }


    @Override public void preAuthAdded(POSPayment payment) {

    }

    @Override public void preAuthRemoved(POSPayment payment) {

    }

    @Override public void pendingPaymentsRetrieved(List<PendingPaymentEntry> pendingPayments) {

    }

    @Override
    public void transactionsChanged(List<POSTransaction> transactions) {

    }

    @Override
    public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = new DisplayLineItem();
      dli.setId(lineItem.getId());
      dli.setName(lineItem.getItem().getName());
      dli.setPrice(CurrencyUtils.format(lineItem.getPrice(), Locale.getDefault()));
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getPrice()), Locale.getDefault()));
      }
      liToDli.put(lineItem, dli);
      List<DisplayLineItem> items = new ArrayList<DisplayLineItem>();
      items.addAll(displayOrder.getLineItems());
      items.add(dli);
      displayOrder.setLineItems(items);
      updateTotals(posOrder, displayOrder);
      getCloverConnector().showDisplayOrder(displayOrder);

    }

    @Override
    public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = liToDli.get(lineItem);
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getPrice()), Locale.getDefault()));
      }

      liToDli.remove(lineItem);
      List<DisplayLineItem> items = new ArrayList<DisplayLineItem>();
      for (DisplayLineItem dlItem : displayOrder.getLineItems()) {
        if (!dlItem.getId().equals(dli.getId())) {
          items.add(dlItem);
        }
      }

      displayOrder.setLineItems(items);
      updateTotals(posOrder, displayOrder);
      getCloverConnector().showDisplayOrder(displayOrder);
    }

    @Override
    public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {
      DisplayLineItem dli = liToDli.get(lineItem);
      dli.setName(lineItem.getItem().getName());
      dli.setQuantity("" + lineItem.getQuantity());
      dli.setPrice(CurrencyUtils.format(lineItem.getPrice(), Locale.getDefault()));
      List<DisplayDiscount> dDiscounts = new ArrayList<DisplayDiscount>();
      if (lineItem.getDiscount() != null && lineItem.getDiscount().getValue(lineItem.getPrice()) != lineItem.getPrice()) {
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(lineItem.getDiscount().name);
        dd.setAmount(CurrencyUtils.format(lineItem.getDiscount().getValue(lineItem.getPrice()), Locale.getDefault()));
      }
      dli.setDiscounts(dDiscounts);
      updateTotals(posOrder, displayOrder);
      getCloverConnector() .showDisplayOrder(displayOrder);

    }

    private void updateTotals(POSOrder order, DisplayOrder displayOrder) {
      displayOrder.setTax(CurrencyUtils.format(order.getTaxAmount(), Locale.getDefault()));
      displayOrder.setSubtotal(CurrencyUtils.format(order.getPreTaxSubTotal(), Locale.getDefault()));
      displayOrder.setTotal(CurrencyUtils.format(order.getTotal(), Locale.getDefault()));


      POSDiscount discount = order.getDiscount();
      List<DisplayDiscount> displayDiscounts = null;
      if (discount != null && discount.getValue(1000) != 0) {
        displayDiscounts = new ArrayList<DisplayDiscount>();
        DisplayDiscount dd = new DisplayDiscount();
        dd.setName(discount.getName());
        dd.setAmount("" + discount.getValue(order.getPreDiscountSubTotal()));
        displayDiscounts.add(dd);
      }
      displayOrder.setDiscounts(displayDiscounts);
    }

    @Override
    public void paymentAdded(POSOrder posOrder, POSPayment payment) {

    }

    @Override
    public void refundAdded(POSOrder posOrder, POSRefund refund) {

    }

    @Override
    public void paymentChanged(POSOrder posOrder, POSTransaction pay) {

    }

    @Override
    public void discountAdded(POSOrder posOrder, POSDiscount discount) {

    }

    @Override
    public void discountChanged(POSOrder posOrder, POSDiscount discount) {

    }
  }

}
