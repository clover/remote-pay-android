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
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.clover.remote.client.CloverConnector;
import com.clover.remote.client.ICloverConnectorListener;
import com.clover.remote.client.device.WebSocketCloverDeviceConfiguration;
import com.clover.remote.client.lib.example.model.POSCard;
import com.clover.remote.client.lib.example.model.POSDiscount;
import com.clover.remote.client.lib.example.model.POSExchange;
import com.clover.remote.client.lib.example.model.POSItem;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSRefund;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CaptureAuthResponse;
import com.clover.remote.client.messages.CaptureCardResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.SignatureVerifyRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.protocol.message.TipAddedMessage;
import com.clover.remote.terminal.InputOption;
import com.clover.remote.terminal.TxState;
import com.clover.sdk.v3.payments.CardTransactionType;

import java.net.URI;
import java.net.URISyntaxException;


public class ExamplePOSActivity extends Activity implements CurrentOrderFragment.OnFragmentInteractionListener, AvailableItem.OnFragmentInteractionListener, OrdersFragment.OnFragmentInteractionListener, RegisterFragment.OnFragmentInteractionListener, SignatureFragment.OnFragmentInteractionListener, ProcessingFragment.OnFragmentInteractionListener {

  private static final String TAG = "ExamplePOSActivity";
  public static final String EXAMPLE_POS_SERVER_KEY = "clover_device_endpoint";
  public static final int WS_ENDPOINT_ACTIVITY = 123;
  public static final int SVR_ACTIVITY = 456;


  String _checksURL = null;

  CloverConnector cloverConnector;

  POSStore store = new POSStore();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_example_pos);

    if (loadBaseURL()) {

      initialize();

    }

    FrameLayout frameLayout = (FrameLayout) findViewById(R.id.contentContainer);

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    RegisterFragment register = RegisterFragment.newInstance(store, cloverConnector);

    fragmentTransaction.add(R.id.contentContainer, register, "REGISTER");
    fragmentTransaction.commit();


    // initialize store...
    store.addAvailableItem(new POSItem("1", "Hamburger", 759, true, true));
    store.addAvailableItem(new POSItem("2", "Cheeseburger", 699, true, true));
    store.addAvailableItem(new POSItem("3", "Double Hamburger", 819, true, true));
    store.addAvailableItem(new POSItem("4", "Double Cheeseburger", 899, true, true));
    store.addAvailableItem(new POSItem("5", "Bacon Cheeseburger", 999, true, true));
    store.addAvailableItem(new POSItem("6", "Small French Fries", 239, true, true));
    store.addAvailableItem(new POSItem("7", "Medium French Fries", 259, true, true));
    store.addAvailableItem(new POSItem("8", "Large French Fries", 279, true, true));
    store.addAvailableItem(new POSItem("9", "Small Fountain Drink", 169, true, true));
    store.addAvailableItem(new POSItem("10", "Medium Fountain Drink", 189, true, true));
    store.addAvailableItem(new POSItem("11", "Large Fountain Drink", 229, true, true));
    store.addAvailableItem(new POSItem("12", "Chocolate Milkshake", 449, true, true));
    store.addAvailableItem(new POSItem("13", "Vanilla Milkshake", 419, true, true));
    store.addAvailableItem(new POSItem("14", "Strawberry Milkshake", 439, true, true));
    store.addAvailableItem(new POSItem("15", "$25 Gift Card", 2500, false, false));
    store.addAvailableItem(new POSItem("16", "$50 Gift Card", 5000, false, false));

    store.addAvailableDiscount(new POSDiscount("10% Off", 0.1f));
    store.addAvailableDiscount(new POSDiscount("$5 Off", 500));
    store.addAvailableDiscount(new POSDiscount("None", 0));

    store.createOrder();
  }

  private boolean loadBaseURL() {
    String _serverBaseURL = PreferenceManager.getDefaultSharedPreferences(this).getString(EXAMPLE_POS_SERVER_KEY, null);

    if (_serverBaseURL == null || "".equals(_serverBaseURL.trim())) {
      Intent intent = new Intent(this, ExamplePOSSettingsActivity.class);
      startActivityForResult(intent, WS_ENDPOINT_ACTIVITY);
      return false;
    }

    _checksURL = _serverBaseURL;

    Log.d(TAG, _serverBaseURL);
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == WS_ENDPOINT_ACTIVITY) {

      loadBaseURL();
      initialize();

    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_parent, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  public void onClickWelcome(View view) {
    cloverConnector.showWelcomeScreen();
  }

  public void onClickThankYou(View view) {
    cloverConnector.showThankYouScreen();
  }
    /*public void onSale(View view) {
        SaleRequest saleRequest = new SaleRequest();
        saleRequest.setAmount(2250L);

        cloverConnector.sale(saleRequest);
    }

    public void onAuth(View view) {
        AuthRequest authRequest = new AuthRequest(false);
        authRequest.setAmount(5000L);

        cloverConnector.auth(authRequest);
    }

    public void onPreAuth(View view) {
        AuthRequest authRequest = new AuthRequest(true);
        authRequest.setAmount(5000L);

        cloverConnector.sale(authRequest);
    }*/

  public void onClickStatus(View view) {

    //Toast.makeText(ExamplePOSActivity.this, "Status: " + cloverConnector.getStatus() + " [Error:" + cloverConnector.getLastException() + "]", Toast.LENGTH_SHORT).show();
  }

  public void onClickReconnect(View view) {
    //initialize();
  }

  public void onClickDisplayMessage(View view) {
    //EditText editText = (EditText)findViewById(R.id.DeviceM);
    //cloverConnector.showMessage(editText.getText().toString());
  }

  public void onPrintTextMessage(View view) {
//        EditText editText = (EditText)findViewById(R.id.PrintTextMessageContent);
//        List<String> messages = new ArrayList<String>();
//        messages.add(editText.getText().toString());
//        cloverConnector.printText(messages);
  }

  public void onClickCancel(View view) {
    cloverConnector.cancel();
  }


  public void initialize() {
    URI uri = null;
    try {
      if (cloverConnector != null) {
        cloverConnector.dispose();
      }
      uri = new URI(_checksURL);
      cloverConnector = new CloverConnector(new WebSocketCloverDeviceConfiguration(uri));
      cloverConnector.addCloverConnectorListener(new ICloverConnectorListener() {
        public void onDisconnected() {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Disconnected");
            }
          });

        }

        @Override
        public void onDisconnecting() {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Disconnecting");
            }
          });
        }

        public void onConnected() {

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              //Toast.makeText(ExamplePOSActivity.this, "Connecting...", Toast.LENGTH_SHORT).show();
              ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Connecting");
            }
          });
        }

        public void onReady() {
          runOnUiThread(new Runnable() {
            public void run() {
              Toast.makeText(ExamplePOSActivity.this, "Ready!", Toast.LENGTH_SHORT).show();
              ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Connected");
            }
          });
        }

        public void onError(final Exception e) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(ExamplePOSActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
          });
        }

        public void onDebug(final String s) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(ExamplePOSActivity.this, "Debug: " + s, Toast.LENGTH_SHORT).show();
            }
          });
        }

        @Override
        public void onDeviceActivityStart(final CloverDeviceEvent deviceEvent) {

          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ((TextView) findViewById(R.id.DeviceStatus)).setText(deviceEvent.getMessage());
              //Toast.makeText(ExamplePOSActivity.this, deviceEvent.getMessage(), Toast.LENGTH_SHORT).show();
              LinearLayout ll = (LinearLayout) findViewById(R.id.DeviceOptionsPanel);
              for (final InputOption io : deviceEvent.getInputOptions()) {
                Button btn = new Button(ExamplePOSActivity.this);
                btn.setText(io.description);
                btn.setOnClickListener(new View.OnClickListener() {
                  @Override
                  public void onClick(View v) {
                    cloverConnector.invokeInputOption(io);
                  }
                });
                ll.addView(btn);
              }
            }
          });
        }

        @Override
        public void onDeviceActivityEnd(final CloverDeviceEvent deviceEvent) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ((TextView) findViewById(R.id.DeviceStatus)).setText(deviceEvent.getMessage());
              //Toast.makeText(ExamplePOSActivity.this, deviceEvent.getMessage(), Toast.LENGTH_SHORT).show();
              LinearLayout ll = (LinearLayout) findViewById(R.id.DeviceOptionsPanel);
              ll.removeAllViews();
            }
          });
        }

        @Override
        public void onDeviceError(CloverDeviceErrorEvent deviceErrorEvent) {

        }

        @Override
        public void onAuthResponse(final AuthResponse response) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              POSPayment payment = new POSPayment(response.getPayment().getId(), response.getPayment().getOrder().getId(), "DFLTEMPLYEE", response.getPayment().getAmount());
              payment.setPaymentStatus(CardTransactionType.PREAUTH.equals(response.getPayment().getCardTransaction().getType()) ? POSPayment.Status.AUTHORIZED : POSPayment.Status.PAID);
              store.addPaymentToOrder(payment, store.getCurrentOrder());

              store.createOrder();
              CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
              currentOrderFragment.setOrder(store.getCurrentOrder());
              cloverConnector.showWelcomeScreen();

              showRegister(null);
            }
          });
        }

        @Override
        public void onAuthTipAdjustResponse(TipAdjustAuthResponse response) {
          if (response.isSuccess()) {

            boolean updatedTip = false;
            for (POSOrder order : store.getOrders()) {
              for (POSExchange exchange : order.getPayments()) {
                if (exchange instanceof POSPayment) {
                  POSPayment posPayment = (POSPayment) exchange;
                  if (exchange.getPaymentID().equals(response.getPaymentId())) {
                    posPayment.setTipAmount(response.getAmount());
                    // TODO: should the stats be updated?
                    updatedTip = true;
                    break;
                  }
                }
              }
              if (updatedTip) {
                break;
              }
            }
          } else {
            Toast.makeText(getBaseContext(), "Tip adjust failed", Toast.LENGTH_LONG);
          }
        }

        @Override
        public void onAuthCaptureResponse(CaptureAuthResponse response) {

        }

        @Override
        public void onSignatureVerifyRequest(SignatureVerifyRequest request) {

                    /*
                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    SignatureFragment signature = SignatureFragment.newInstance(request, cloverConnector);

                    fragmentTransaction.replace(R.id.contentContainer, signature, "SIGNATURE");
                    fragmentTransaction.commit();
                    */


          FragmentManager fragmentManager = getFragmentManager();
          FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

          hideFragments(fragmentManager, fragmentTransaction);

          Fragment fragment = fragmentManager.findFragmentByTag("SIGNATURE");
          if (fragment == null) {
            fragment = SignatureFragment.newInstance(request, cloverConnector);
            fragmentTransaction.add(R.id.contentContainer, fragment, "SIGNATURE");
          } else {
            ((SignatureFragment) fragment).setSignatureVerifyRequest(request);
            fragmentTransaction.show(fragment);
          }

          fragmentTransaction.commit();
        }

        @Override
        public void onCloseoutResponse(CloseoutResponse response) {

        }

        @Override
        public void onSaleResponse(final SaleResponse response) {
          if (response != null && response.getPayment() != null) {
            POSPayment payment = new POSPayment(response.getPayment().getId(), response.getPayment().getOrder().getId(), "DFLTEMPLYEE", response.getPayment().getAmount());
            payment.setPaymentStatus(CardTransactionType.PREAUTH.equals(response.getPayment().getCardTransaction().getType()) ? POSPayment.Status.AUTHORIZED : POSPayment.Status.PAID);
            store.addPaymentToOrder(payment, store.getCurrentOrder());
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                store.createOrder();
                CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
                currentOrderFragment.setOrder(store.getCurrentOrder());
                cloverConnector.showWelcomeScreen();

                showRegister(null);
              }
            });
          } else {
            // TODO: handle null payment response when payment is cancelled
          }
        }

        @Override
        public void onManualRefundResponse(final ManualRefundResponse response) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(ExamplePOSActivity.this, "Refund: " + response.getCredit().getAmount(), Toast.LENGTH_LONG);
            }
          });
          //store.refunds.add(new POSRefund(response.getCredit().getId(), response.getCredit().getOrderRef().getId(), "DFLTEMPLYEE", response.getCredit().getAmount()));
        }

        @Override
        public void onRefundPaymentResponse(RefundPaymentResponse response) {
          if (response.getCode() == "SUCCESS") {
            POSRefund refund = new POSRefund(response.getPaymentId(), response.getOrderId(), "DEFAULT", response.getRefundObj().getAmount());
            boolean done = false;
            for (POSOrder order : store.getOrders()) {
              for (POSExchange payment : order.getPayments()) {
                if (payment instanceof POSPayment) {
                  if (payment.getPaymentID().equals(response.getRefundObj().getPayment().getId())) {
                    ((POSPayment) payment).setPaymentStatus(POSPayment.Status.REFUNDED);
                    store.addRefundToOrder(refund, order);
                    done = true;
                    break;
                  }
                }
              }
              if (done) {
                break;
              }
            }
          } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
            builder.setTitle("Refund Error").
                setMessage("There was an error refunding the payment");
            builder.create().show();
            Log.d(getClass().getName(), "Got refund response of " + response.getCode());
          }
        }

        @Override
        public void onTipAdded(TipAddedMessage message) {

        }

        @Override
        public void onVoidPaymentResponse(VoidPaymentResponse response) {
          if (response.getCode() == "SUCCESS") {
            boolean done = false;
            for (POSOrder order : store.getOrders()) {
              for (POSExchange payment : order.getPayments()) {
                if (payment instanceof POSPayment) {
                  if (payment.getPaymentID().equals(response.getPaymentId())) {
                    ((POSPayment) payment).setPaymentStatus(POSPayment.Status.VOIDED);
                    done = true;
                    break;
                  }
                }
              }
              if (done) {
                break;
              }
            }
          } else {
            Log.d(getClass().getName(), "Got refund response of " + response.getCode());
          }
        }

        @Override
        public void onCaptureCardResponse(CaptureCardResponse response) {
          if ("SUCCESS".equals(response.getCode())) {
            POSCard card = new POSCard();
            card.setFirst6(response.getCard().getFirst6());
            card.setLast4(response.getCard().getLast4());
            card.setName(response.getCard().getFirstName() + response.getCard().getLastName());
            card.setMonth(response.getCard().getExpirationDate().substring(0, 2));
            card.setYear(response.getCard().getExpirationDate().substring(2, 4));
            card.setToken(response.getCard().getToken());

            store.addCard(card);
          }
        }

        @Override
        public void onTransactionState(TxState txState) {

        }
      });
      //cloverConnector.initialize(uri);

      //Toast.makeText(ExamplePOSActivity.this, "Last Exception: " + cloverConnector.getLastException(), Toast.LENGTH_LONG).show();

    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

  }


  @Override
  public void onFragmentInteraction(Uri uri) {

  }

  public void showSettings(MenuItem item) {
    Intent intent = new Intent(this, ExamplePOSSettingsActivity.class);
    startActivityForResult(intent, WS_ENDPOINT_ACTIVITY);
  }

  public void showOrders(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("ORDERS");
    if (fragment == null) {
      fragment = OrdersFragment.newInstance(store, cloverConnector);
      ((OrdersFragment) fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "ORDERS");
    } else {
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commit();
  }

  public void showRegister(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("REGISTER");
    if (fragment == null) {
      fragment = RegisterFragment.newInstance(store, cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "REGISTER");
    } else {
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commit();
  }

  private void hideFragments(FragmentManager fragmentManager, FragmentTransaction fragmentTransaction) {
    Fragment fragment = fragmentManager.findFragmentByTag("ORDERS");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("REGISTER");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("SIGNATURE");
    if (fragment != null) {
      fragmentTransaction.hide(fragment);
    }

  }
}
