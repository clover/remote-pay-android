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
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.lib.example.model.OrderObserver;
import com.clover.remote.client.lib.example.model.POSCard;
import com.clover.remote.client.lib.example.model.POSDiscount;
import com.clover.remote.client.lib.example.model.POSLineItem;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSRefund;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.lib.example.model.POSTransaction;
import com.clover.remote.client.lib.example.utils.CurrencyUtils;
import com.clover.remote.client.lib.example.utils.IdUtils;
import com.clover.remote.client.messages.SaleRequest;
import com.clover.sdk.v3.payments.VaultedCard;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CurrentOrderFragment extends Fragment implements OrderObserver, ChooseSaleTypeFragment.ChooseSaleTypeListener, EnterTipFragment.EnterTipDialogFragmentListener{

  private POSStore store;
  private View v;
  private boolean preAuth = false;
  private boolean vaulted = false;
  private POSCard vaultedCard;
  POSOrder order = new POSOrder();
  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  List<CurrentOrderFragmentListener> listeners = new ArrayList<CurrentOrderFragmentListener>(5);
  private OnFragmentInteractionListener mListener;
  private String TAG = CurrentOrderFragment.class.getSimpleName();

  public static CurrentOrderFragment newInstance(boolean preAuth) {
    CurrentOrderFragment fragment = new CurrentOrderFragment();
    fragment.setPreAuth(preAuth);
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public CurrentOrderFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    v = inflater.inflate(R.layout.fragment_current_order, container, false);
    updateListView();
    updateTotals();
    Button newOrderButton = ((Button) v.findViewById(R.id.NewOrderButton));
    newOrderButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (store.getCurrentOrder().getItems().size() > 0) {
          onNewOrderClicked();
        }
        else{
          ((ExamplePOSActivity)getActivity()).showPopupMessage(null, new String[]{"You cannot save an order with no items"}, false);
        }
      }
    });
    final ListView currentOrderItemsListView = (ListView) v.findViewById(R.id.CurrentOrderItems);

    currentOrderItemsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
      @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        // prompt for delete...
        final POSLineItem lineItem = (POSLineItem)currentOrderItemsListView.getItemAtPosition(position);
        String thisTheseLabel = lineItem.getQuantity() == 1 ? "this" : "these";

        new AlertDialog.Builder(getActivity())
            .setTitle("Delete?")
            .setMessage(String.format("Do you want to remove %s items from the order?", thisTheseLabel))
            .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
              @Override public void onClick(DialogInterface dialog, int which) {
                order.remoteAllItems(lineItem);
              }
            })
            .setNegativeButton("No", null)
            .show();
        return true; // consume the event
      }
    });

    Button payButton = ((Button) v.findViewById(R.id.PayButton));
    payButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (store.getCurrentOrder().getItems().size() > 0) {
          if(preAuth){
            showTransactionSettings(new Runnable() {
              @Override
              public void run() {
                showEnterTipDialog();
              }
            }, TransactionSettingsFragment.transactionTypes.PREAUTH );
          }
          else if(vaulted){
            showTransactionSettings(new Runnable() {
              @Override
              public void run() {
                makeVaultedSale();
              }
            }, TransactionSettingsFragment.transactionTypes.SALE);
          }
          else {
            showChooseSaleType();
          }
        }
        else{
          ((ExamplePOSActivity)getActivity()).showPopupMessage(null, new String[]{"You cannot make a sale with no items"}, false);
        }
      }
    });

    if(vaulted && vaultedCard != null){
      LinearLayout vaulted = (LinearLayout) v.findViewById(R.id.VaultedCardInfo);
      vaulted.setVisibility(View.VISIBLE);
      TextView vaultedName = (TextView) v.findViewById(R.id.VaultedName);
      TextView vaultedCardNum = (TextView) v.findViewById(R.id.VaultedCardNumber);
      vaultedName.setText(vaultedCard.getVaultedName());
      vaultedCardNum.setText(getString(R.string.vault_card_num, vaultedCard.getFirst6(), vaultedCard.getLast4()));
    }

    return v;
  }

  public void setCloverConnector(ICloverConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<>(cloverConnector);
  }

  public void setPreAuth(boolean preAuth) {
    this.preAuth = preAuth;
    if(!preAuth){
      LinearLayout preauthInfo = (LinearLayout) v.findViewById(R.id.PreAuthInfo);
      preauthInfo.setVisibility(View.GONE);
    }
  }

  public void setStore(POSStore store) {
    this.store = store;
  }

  private void showTransactionSettings(Runnable runnable, TransactionSettingsFragment.transactionTypes type) {
    FragmentManager fm = getFragmentManager();
    TransactionSettingsFragment editNameDialog = TransactionSettingsFragment.newInstance(store, type);
    editNameDialog.continueAction = runnable;
    editNameDialog.setWeakCloverConnector(cloverConnectorWeakReference);
    editNameDialog.show(fm, "fragment_transaction_settings");
  }

  private void showEnterTipDialog(){
    FragmentManager fm = getFragmentManager();
    EnterTipFragment enterTipFragment = EnterTipFragment.newInstance();
    enterTipFragment.addListener(this);
    enterTipFragment.show(fm, "fragment_enter_tip");
  }

  private void showChooseSaleType() {
    FragmentManager fm = getFragmentManager();
    ChooseSaleTypeFragment chooseSaleTypeFragment = ChooseSaleTypeFragment.newInstance();
    chooseSaleTypeFragment.addListener(this);
    chooseSaleTypeFragment.show(fm, "fragment_choose_sale_type");
  }

  private void onNewOrderClicked() {
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.onNewOrderClicked();
    }
  }

  private void onSaleClicked() {
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.onSaleClicked();
    }
  }

  private void onAuthClicked() {
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.onAuthClicked();
    }
  }

  private void updateListView() {

    if (getView() != null) {
      getView().post(new Runnable(){
        @Override public void run() {
          ListView listView = (ListView) getView().findViewById(R.id.CurrentOrderItems);
          POSLineItem[] itemArray = new POSLineItem[order.getItems().size()];
          AvailableItemListViewAdapter items = new AvailableItemListViewAdapter(listView.getContext(), R.layout.listitem_order_item, order.getItems().toArray(itemArray));
          listView.setAdapter(items);
        }
      });
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mListener = (OnFragmentInteractionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public void updateCurrentOrder() {
    updateListView();
    updateTotals();
  }

  public void addListener(CurrentOrderFragmentListener listener) {
    listeners.add(listener);
  }

  private void updateTotals() {
    if (getView() != null) {
      getView().post(new Runnable(){
        @Override public void run() {
          String subtotalFormatted = CurrencyUtils.format(order.getPreTaxSubTotal(), Locale.getDefault());
          ((TextView) getView().findViewById(R.id.SubtotalLabel)).setText(subtotalFormatted);
          String taxFormatted = CurrencyUtils.format(order.getTaxAmount(), Locale.getDefault());
          ((TextView) getView().findViewById(R.id.TaxLabel)).setText(taxFormatted);
          String totalFormatted = CurrencyUtils.format(order.getTotal(), Locale.getDefault());
          ((TextView) getView().findViewById(R.id.TotalLabel)).setText(totalFormatted);
          if(order.getDiscount() != null) {
            String discount = order.getDiscount().getName();
            ((TextView) getView().findViewById(R.id.DiscountsLabel)).setText(discount);
          }
          else{
            ((TextView) getView().findViewById(R.id.DiscountsLabel)).setText("");
          }
        }
      });
    }
  }

  private void makeVaultedSale(){
    VaultedCard vaulted = new VaultedCard();
    vaulted.setCardholderName(vaultedCard.getName());
    vaulted.setFirst6(vaultedCard.getFirst6());
    vaulted.setLast4(vaultedCard.getLast4());
    vaulted.setExpirationDate(vaultedCard.getMonth() + vaultedCard.getYear());
    vaulted.setToken(vaultedCard.getToken());

    String externalId = IdUtils.getNextId();
    store.getCurrentOrder().setPendingPaymentId(externalId);
    SaleRequest request = new SaleRequest(store.getCurrentOrder().getTotal(), externalId);
    request.setCardEntryMethods(store.getCardEntryMethods());
    request.setAllowOfflinePayment(store.getAllowOfflinePayment());
    request.setForceOfflinePayment(store.getForceOfflinePayment());
    request.setApproveOfflinePaymentWithoutPrompt(store.getApproveOfflinePaymentWithoutPrompt());
    request.setTippableAmount(store.getCurrentOrder().getTippableAmount());
    request.setTaxAmount(store.getCurrentOrder().getTaxAmount());
    request.setDisablePrinting(store.getDisablePrinting());
    SaleRequest.TipMode tipMode = store.getTipMode() != null ? store.getTipMode() : null;
    request.setTipMode(tipMode != null ? tipMode : null);
    request.setSignatureEntryLocation(store.getSignatureEntryLocation());
    request.setSignatureThreshold(store.getSignatureThreshold());
    request.setDisableReceiptSelection(store.getDisableReceiptOptions());
    request.setDisableDuplicateChecking(store.getDisableDuplicateChecking());
    request.setTipAmount(store.getTipAmount());
    request.setAutoAcceptPaymentConfirmations(store.getAutomaticPaymentConfirmation());
    request.setAutoAcceptSignature(store.getAutomaticSignatureConfirmation());
    request.setVaultedCard(vaulted);
    cloverConnectorWeakReference.get().sale(request);
  }


  @Override
  public void onSaleTypeChoice(final String choice) {
    showTransactionSettings(new Runnable() {
      @Override
      public void run() {
        if(choice == "Sale") {
          onSaleClicked();
        }
        else if (choice == "Auth"){
          onAuthClicked();
        }
      }
    }, choice == "Sale" ? TransactionSettingsFragment.transactionTypes.SALE : TransactionSettingsFragment.transactionTypes.AUTH);
  }

  @Override
  public void onContinue(long amount) {
    payWithPreAuth(amount);
  }

  private void payWithPreAuth(long amount){
    for (CurrentOrderFragmentListener listener : listeners) {
      listener.payWithPreAuth(amount);
    }
  }
  public interface OnFragmentInteractionListener {
    public void onFragmentInteraction(Uri uri);
  }

  public void setOrder(POSOrder order) {
    this.order.removeObserver(this);
    this.order = order;
    this.order.addOrderObserver(this);
    updateCurrentOrder();
    updateTotals();
  }
  public void setVaulted(boolean vaulted) {
    this.vaulted = vaulted;
  }

  public void setVaultedCard(POSCard vaultedCard) {
    this.vaultedCard = vaultedCard;
    if (v != null){
      if(vaultedCard != null) {
        LinearLayout vaulted = (LinearLayout) v.findViewById(R.id.VaultedCardInfo);
        vaulted.setVisibility(View.VISIBLE);
        TextView vaultedName = (TextView) v.findViewById(R.id.VaultedName);
        TextView vaultedCardNum = (TextView) v.findViewById(R.id.VaultedCardNumber);
        vaultedName.setText(vaultedCard.getVaultedName());
        vaultedCardNum.setText(getString(R.string.vault_card_num, vaultedCard.getFirst6(), vaultedCard.getLast4()));
      } else {
        LinearLayout vaulted = (LinearLayout) v.findViewById(R.id.VaultedCardInfo);
        vaulted.setVisibility(View.GONE);
        TextView vaultedName = (TextView) v.findViewById(R.id.VaultedName);
        TextView vaultedCardNum = (TextView) v.findViewById(R.id.VaultedCardNumber);
        vaultedName.setText("");
        vaultedCardNum.setText("");
      }
    }
  }

  @Override public void lineItemAdded(POSOrder posOrder, POSLineItem lineItem) {
    updateCurrentOrder();
  }

  @Override public void lineItemRemoved(POSOrder posOrder, POSLineItem lineItem) {
    updateCurrentOrder();

  }

  @Override public void lineItemChanged(POSOrder posOrder, POSLineItem lineItem) {
    updateCurrentOrder();

  }

  @Override public void paymentAdded(POSOrder posOrder, POSPayment payment) {
    updateCurrentOrder();

  }

  @Override public void refundAdded(POSOrder posOrder, POSRefund refund) {
    updateCurrentOrder();

  }

  @Override public void paymentChanged(POSOrder posOrder, POSTransaction pay) {
    updateCurrentOrder();

  }

  @Override public void discountAdded(POSOrder posOrder, POSDiscount discount) {
    updateCurrentOrder();

  }

  @Override public void discountChanged(POSOrder posOrder, POSDiscount discount) {
    Log.d(TAG, "discountChangedClicked");
    updateCurrentOrder();

  }

}
