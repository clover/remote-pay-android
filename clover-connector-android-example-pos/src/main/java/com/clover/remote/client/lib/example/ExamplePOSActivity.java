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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.clover.remote.InputOption;
import com.clover.remote.ResultStatus;
import com.clover.remote.TxState;
import com.clover.remote.client.CloverConnector;
import com.clover.remote.client.ICloverConnectorListener;
import com.clover.remote.client.MerchantInfo;
import com.clover.remote.client.device.USBCloverDeviceConfiguration;
import com.clover.remote.client.device.WebSocketCloverDeviceConfiguration;
import com.clover.remote.client.lib.example.model.POSCard;
import com.clover.remote.client.lib.example.model.POSDiscount;
import com.clover.remote.client.lib.example.model.POSExchange;
import com.clover.remote.client.lib.example.model.POSItem;
import com.clover.remote.client.lib.example.model.POSNakedRefund;
import com.clover.remote.client.lib.example.model.POSOrder;
import com.clover.remote.client.lib.example.model.POSPayment;
import com.clover.remote.client.lib.example.model.POSRefund;
import com.clover.remote.client.lib.example.model.POSStore;
import com.clover.remote.client.messages.AuthResponse;
import com.clover.remote.client.messages.CaptureAuthResponse;
import com.clover.remote.client.messages.ManualRefundRequest;
import com.clover.remote.client.messages.PreAuthRequest;
import com.clover.remote.client.messages.PreAuthResponse;
import com.clover.remote.client.messages.VaultCardResponse;
import com.clover.remote.client.messages.CloseoutResponse;
import com.clover.remote.client.messages.CloverDeviceErrorEvent;
import com.clover.remote.client.messages.CloverDeviceEvent;
import com.clover.remote.client.messages.ManualRefundResponse;
import com.clover.remote.client.messages.RefundPaymentResponse;
import com.clover.remote.client.messages.SaleResponse;
import com.clover.remote.client.messages.SignatureVerifyRequest;
import com.clover.remote.client.messages.TipAdjustAuthResponse;
import com.clover.remote.client.messages.VoidPaymentResponse;
import com.clover.remote.message.TipAddedMessage;
import com.clover.sdk.v3.payments.CardTransactionType;
import com.clover.sdk.v3.payments.Credit;
import com.clover.sdk.v3.payments.Payment;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class ExamplePOSActivity extends Activity implements CurrentOrderFragment.OnFragmentInteractionListener,
    AvailableItem.OnFragmentInteractionListener, OrdersFragment.OnFragmentInteractionListener,
    RegisterFragment.OnFragmentInteractionListener, SignatureFragment.OnFragmentInteractionListener,
    CardsFragment.OnFragmentInteractionListener, ManualRefundsFragment.OnFragmentInteractionListener, MiscellaneousFragment.OnFragmentInteractionListener,
    ProcessingFragment.OnFragmentInteractionListener, PreAuthFragment.OnFragmentInteractionListener {

  private static final String TAG = "ExamplePOSActivity";
  public static final String EXAMPLE_POS_SERVER_KEY = "clover_device_endpoint";
  public static final int WS_ENDPOINT_ACTIVITY = 123;
  public static final int SVR_ACTIVITY = 456;

  boolean usb = true;

//  private int manualCardEntryMethod = 0;
//  private int swipeCardEntryMethod = 0;
//  private int chipCardEntryMethod = 0;
//  private int contactlessCardEntryMethod = 0;

  String _checksURL = null;

  CloverConnector cloverConnector;

  POSStore store = new POSStore();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_example_pos);

    if (usb || loadBaseURL()) {

      initialize();

    }

    FrameLayout frameLayout = (FrameLayout) findViewById(R.id.contentContainer);

    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    RegisterFragment register = RegisterFragment.newInstance(store, cloverConnector);

    fragmentTransaction.add(R.id.contentContainer, register, "REGISTER");
    fragmentTransaction.commit();


    // initialize store...
    store.addAvailableItem(new POSItem("0", "Chicken Nuggets", 539, true, true));
    store.addAvailableItem(new POSItem("1", "Hamburger", 699, true, true));
    store.addAvailableItem(new POSItem("2", "Cheeseburger", 759, true, true));
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
    store.addAvailableItem(new POSItem("15", "Ice Cream Cone", 189, true, true));
    store.addAvailableItem(new POSItem("16", "$25 Gift Card", 2500, false, false));
    store.addAvailableItem(new POSItem("17", "$50 Gift Card", 5000, false, false));

    store.addAvailableDiscount(new POSDiscount("10% Off", 0.1f));
    store.addAvailableDiscount(new POSDiscount("$5 Off", 500));
    store.addAvailableDiscount(new POSDiscount("None", 0));

    store.createOrder();
  }

  private boolean loadBaseURL() {
    if(usb) {
      return true;
    }
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
      if (!usb) {
        loadBaseURL();
        initialize();
      }
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

  protected int getCardEntryMethods() {
    return CloverConnector.CARD_ENTRY_METHOD_MAG_STRIPE | CloverConnector.CARD_ENTRY_METHOD_ICC_CONTACT | CloverConnector.CARD_ENTRY_METHOD_MAG_STRIPE | CloverConnector.CARD_ENTRY_METHOD_NFC_CONTACTLESS;
  }

  public void onClickWelcome(View view) {
    cloverConnector.showWelcomeScreen();
  }

  public void onClickThankYou(View view) {
    cloverConnector.showThankYouScreen();
  }

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

//      if(cloverConnector == null) {

//      }
//      cloverConnector.addCloverConnectorListener(
          ICloverConnectorListener ccListener = new ICloverConnectorListener() {
        public void onDisconnected() {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText("Disconnected");
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

        public void onReady(final MerchantInfo merchantInfo) {
          runOnUiThread(new Runnable() {
            public void run() {
              Toast.makeText(ExamplePOSActivity.this, "Ready!", Toast.LENGTH_SHORT).show();
              ((TextView) findViewById(R.id.ConnectionStatusLabel)).setText(String.format("Connected: %s (%s)", merchantInfo.getDeviceInfo().getSerial(), merchantInfo.getMerchantName()));
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
              ll.removeAllViews();

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
              ((TextView) findViewById(R.id.DeviceStatus)).setText("");
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
              Payment _payment = response.getPayment();
              POSPayment payment = new POSPayment(_payment.getId(), _payment.getExternalPaymentId(), _payment.getOrder().getId(), "DFLTEMPLYEE", _payment.getAmount(), _payment.getTipAmount() != null ? _payment.getTipAmount() : 0, _payment.getCashbackAmount() != null ? _payment.getCashbackAmount() : 0);
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
        public void onPreAuthResponse(final PreAuthResponse response) {
          runOnUiThread(new Runnable(){
            @Override
            public void run() {
              if ("SUCCESS".equals(response.getCode())) {
                Payment _payment = response.getPayment();
                POSPayment payment = new POSPayment(_payment.getId(), _payment.getExternalPaymentId(), _payment.getOrder().getId(), "DFLTEMPLYEE", _payment.getAmount(), _payment.getTipAmount() != null ? _payment.getTipAmount() : 0,
                    _payment.getCashbackAmount() != null ? _payment.getCashbackAmount() : 0);
                payment.setPaymentStatus(CardTransactionType.PREAUTH.equals(response.getPayment().getCardTransaction().getType()) ?
                    POSPayment.Status.AUTHORIZED :
                    POSPayment.Status.PAID);

                store.addPreAuth(payment);

                cloverConnector.showWelcomeScreen();
                //showRegister(null);
                showPreAuths(null);
              } else {
                Toast.makeText(ExamplePOSActivity.this, "Pre Auth: " + response.getCode(), Toast.LENGTH_SHORT);
              }
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
        public void onPreAuthCaptureResponse(CaptureAuthResponse response) {

          for(final POSPayment payment : store.getPreAuths()) {
            if(payment.getPaymentID().equals(response.getPaymentID())) {
              runOnUiThread(new Runnable(){
                @Override public void run() {
                  store.removePreAuth(payment);
                  store.addPaymentToOrder(payment, store.getCurrentOrder());
                  payment.setPaymentStatus(POSPayment.Status.PAID);
                  store.getCurrentOrder().status = POSOrder.OrderStatus.CLOSED;

                  //TODO: if order isn't fully paid, don't create a new order...
                  store.createOrder();
                  CurrentOrderFragment currentOrderFragment = (CurrentOrderFragment) getFragmentManager().findFragmentById(R.id.PendingOrder);
                  currentOrderFragment.setOrder(store.getCurrentOrder());
                  cloverConnector.showWelcomeScreen();

                  showRegister(null);
                }
              });
              break;
            }
          }
        }

        @Override
        public void onSignatureVerifyRequest(SignatureVerifyRequest request) {

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
          final String msg;
          if(ResultStatus.SUCCESS.toString().equals(response.getCode())) {
            msg = "Closeout is scheduled.";
          } else {
            msg = "Error scheduling closeout. " + response.getReason();
          }
          runOnUiThread(new Runnable(){
            @Override public void run() {
              Toast.makeText(ExamplePOSActivity.this, msg, Toast.LENGTH_SHORT);
            }
          });
        }

        @Override
        public void onSaleResponse(final SaleResponse response) {
          if (response != null && response.getPayment() != null) {
            Payment _payment = response.getPayment();
            POSPayment payment = new POSPayment(_payment.getId(), _payment.getExternalPaymentId(), _payment.getOrder().getId(), "DFLTEMPLYEE", _payment.getAmount(), _payment.getTipAmount() != null ? _payment.getTipAmount() : 0, _payment.getCashbackAmount() != null ? _payment.getCashbackAmount() : 0);
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
              if(ResultStatus.SUCCESS.toString().equals(response.getCode())) {
                Credit credit = response.getCredit();
                POSNakedRefund nakedRefund = new POSNakedRefund(null, credit.getAmount());
                store.addRefund(nakedRefund);
              } else {
                Toast.makeText(ExamplePOSActivity.this, "Manual Refund Failed", Toast.LENGTH_LONG).show();
              }
            }
          });
        }

        @Override
        public void onRefundPaymentResponse(RefundPaymentResponse response) {
          if (ResultStatus.SUCCESS.toString().equals(response.getCode())) {
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
          if (ResultStatus.SUCCESS.toString().equals(response.getCode())) {
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
        public void onVaultCardResponse(final VaultCardResponse response) {
          if (ResultStatus.SUCCESS.toString().equals(response.getCode())) {
            POSCard card = new POSCard();
            card.setFirst6(response.getCard().getFirst6());
            card.setLast4(response.getCard().getLast4());
            card.setName(response.getCard().getCardholderName());
            card.setMonth(response.getCard().getExpirationDate().substring(0, 2));
            card.setYear(response.getCard().getExpirationDate().substring(2, 4));
            card.setToken(response.getCard().getToken());

            store.addCard(card);
          } else {
            runOnUiThread(new Runnable(){
              @Override public void run() {
                Toast.makeText(getBaseContext(), "Error capturing card: " + response.getCode(), Toast.LENGTH_LONG);
              }
            });
          }
        }

        @Override
        public void onTransactionState(TxState txState) {

        }

      };

      if(usb) {
        cloverConnector = new CloverConnector(new USBCloverDeviceConfiguration(this), ccListener);
      } else {
        uri = new URI(_checksURL);
        cloverConnector = new CloverConnector(new WebSocketCloverDeviceConfiguration(uri, 10000, 2000), ccListener);
      }

      updateComponentsWithNewCloverConnector();

    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if(cloverConnector != null) {
      cloverConnector.dispose();
    }
  }

  @Override
  public void onFragmentInteraction(Uri uri) {

  }

  public void showSettings(MenuItem item) {
    if(!usb) {
      Intent intent = new Intent(this, ExamplePOSSettingsActivity.class);
      startActivityForResult(intent, WS_ENDPOINT_ACTIVITY);
    }
  }

  private void updateComponentsWithNewCloverConnector() {
    FragmentManager fragmentManager = getFragmentManager();

    RegisterFragment refFragment = (RegisterFragment)fragmentManager.findFragmentByTag("REGISTER");
    if(refFragment != null) {
      refFragment.setCloverConnector(cloverConnector);
    }
    OrdersFragment ordersFragment = (OrdersFragment)fragmentManager.findFragmentByTag("ORDERS");
    if(ordersFragment != null) {
      ordersFragment.setCloverConnector(cloverConnector);
    }
    ManualRefundsFragment manualRefundsFragment = (ManualRefundsFragment)fragmentManager.findFragmentByTag("REFUNDS");
    if(manualRefundsFragment != null) {
      manualRefundsFragment.setCloverConnector(cloverConnector);
    }
    CardsFragment cardsFragment = (CardsFragment)fragmentManager.findFragmentByTag("CARDS");
    if(cardsFragment != null) {
      cardsFragment.setCloverConnector(cloverConnector);
    }
    MiscellaneousFragment miscFragment = (MiscellaneousFragment)fragmentManager.findFragmentByTag("MISC");
    if(miscFragment != null) {
      miscFragment.setCloverConnector(cloverConnector);
    }
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
      ((OrdersFragment)fragment).setCloverConnector(cloverConnector);
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
      ((RegisterFragment)fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commit();
  }

  public void showRefunds(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("REFUNDS");
    if(fragment == null) {
      fragment = ManualRefundsFragment.newInstance(store, cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "REFUNDS");
    } else {
      ((ManualRefundsFragment)fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commit();
  }

  public void showCards(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("CARDS");
    if(fragment == null) {
      fragment = CardsFragment.newInstance(store, cloverConnector);
      ((CardsFragment)fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "CARDS");
    } else {
      ((CardsFragment)fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }
    fragmentTransaction.commit();
  }

  public void showMisc(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("MISC");

    if(fragment == null) {
      fragment = MiscellaneousFragment.newInstance(store, cloverConnector);
      fragmentTransaction.add(R.id.contentContainer, fragment, "MISC");
    } else {
      ((MiscellaneousFragment)fragment).setCloverConnector(cloverConnector);
      fragmentTransaction.show(fragment);
    }

    fragmentTransaction.commit();
  }

  public void showPreAuths(View view) {
    FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

    hideFragments(fragmentManager, fragmentTransaction);

    Fragment fragment = fragmentManager.findFragmentByTag("PRE_AUTHS");

    if(fragment == null) {
      fragment = PreAuthFragment.newInstance(store, cloverConnector);
      ((PreAuthFragment)fragment).setStore(store);
      fragmentTransaction.add(R.id.contentContainer, fragment, "PRE_AUTHS");
    } else {
      ((PreAuthFragment)fragment).setCloverConnector(cloverConnector);
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
    fragment = fragmentManager.findFragmentByTag("CARDS");
    if(fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("MISC");
    if(fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("REFUNDS");
    if(fragment != null) {
      fragmentTransaction.hide(fragment);
    }
    fragment = fragmentManager.findFragmentByTag("PRE_AUTHS");
    if(fragment != null) {
      fragmentTransaction.hide(fragment);
    }
  }

  public void captureCardClick(View view) {
    cloverConnector.vaultCard(CloverConnector.CARD_ENTRY_METHOD_MAG_STRIPE | CloverConnector.CARD_ENTRY_METHOD_ICC_CONTACT | CloverConnector.CARD_ENTRY_METHOD_MANUAL);
  }

  public void onManualRefundClick(View view) {
    CharSequence val = ((TextView)findViewById(R.id.ManualRefundTextView)).getText();
    try {
      long refundAmount = Long.parseLong(val.toString());
      ManualRefundRequest request = new ManualRefundRequest();
      request.setAmount(refundAmount);
      cloverConnector.manualRefund(request);
    } catch(NumberFormatException nfe) {
      Toast.makeText(getBaseContext(), "Invalid value. Must be an integer.", Toast.LENGTH_LONG);
    }
  }



  public void showMessageClick(View view) {
    cloverConnector.showMessage( ((TextView)findViewById(R.id.ShowMessageText)).getText().toString() );
  }

  public void printTextClick(View view) {
    String[] textLines = ((TextView)findViewById(R.id.PrintTextText)).getText().toString().split("\n");
    List<String> lines = Arrays.asList(textLines);
    cloverConnector.printText(lines);
  }

  public void showWelcomeMessageClick(View view) {
    cloverConnector.showWelcomeScreen();
  }

  public void showThankYouClick(View view) {
    cloverConnector.showThankYouScreen();
  }

  public void onOpenCashDrawerClick(View view) {
    cloverConnector.openCashDrawer("Test");
  }

  public void preauthCardClick(View view) {
    PreAuthRequest request = new PreAuthRequest();
    request.setAmount(5000);

    cloverConnector.preAuth(request);
  }

  public void onClickCloseout(View view) {
    cloverConnector.closeout(false, null);
  }


  public void printImageClick(View view) {
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clover_horizontal);
    cloverConnector.printImage(bitmap);
  }
}
