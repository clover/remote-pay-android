package com.clover.remote.client.lib.example;

import com.clover.remote.client.ICloverConnector;
import com.clover.remote.client.lib.example.adapter.PayloadMessageAdapter;
import com.clover.remote.client.messages.CustomActivityRequest;
import com.clover.remote.client.messages.MessageFromActivity;
import com.clover.remote.client.messages.MessageToActivity;
import com.clover.remote.message.ByteArrayToBase64TypeAdapter;
import com.clover.remote.message.CloverJSONifiableTypeAdapter;
import com.clover.sdk.JSONifiable;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CustomActivitiesFragment extends Fragment {

  private WeakReference<ICloverConnector> cloverConnectorWeakReference;
  private TextView otherCustom, cloverCustom;
  private View view;
  private LinearLayout initialLayout, finalLayout, sendPayloadLayout, messagesLayout;
  private TextView initialPayload,finalPayload;
  private EditText customActionName, activityPayload, initialPayloadContent;
  private ListView messageList;
  private Button sendPayload, startActivity;
  private static final Gson GSON;

  private List<PayloadMessage> messages;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeHierarchyAdapter(JSONifiable.class, new CloverJSONifiableTypeAdapter());
    builder.registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());
    GSON = builder.create();
  }


  public static CustomActivitiesFragment newInstance(ICloverConnector cloverConnector){
    CustomActivitiesFragment fragment = new CustomActivitiesFragment();
    Bundle args = new Bundle();
    fragment.setCloverConnector(cloverConnector);
    fragment.setArguments(args);
    return fragment;
  }

  public CustomActivitiesFragment(){

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_custom_activities, container, false);
    messages = new ArrayList<PayloadMessage>();
    initialLayout = (LinearLayout) view.findViewById(R.id.InitialPayloadLayout);
    sendPayloadLayout = (LinearLayout) view.findViewById(R.id.SendPayloadLayout);
    initialPayload = (TextView) view.findViewById(R.id.InitialPayload);
    initialPayloadContent = (EditText) view.findViewById(R.id.InitialPayloadContent);
    finalLayout = (LinearLayout) view.findViewById(R.id.FinalPayloadLayout);
    finalPayload = (TextView) view.findViewById(R.id.FinalPayload);
    customActionName = (EditText) view.findViewById(R.id.CustomActionName);
    activityPayload = (EditText) view.findViewById(R.id.ActivityPayload);
    startActivity = (Button) view.findViewById(R.id.StartCustomActivity);
    sendPayload = (Button) view.findViewById(R.id.sendPayloadToCustomActivity);
    messagesLayout = (LinearLayout) view.findViewById(R.id.MessagesLayout);
    messageList = (ListView) view.findViewById(R.id.MessagesListView);

    PayloadMessageAdapter payloadMessageAdapter = new PayloadMessageAdapter(getActivity().getBaseContext(), R.id.MessagesListView, messages);
    messageList.setAdapter(payloadMessageAdapter);

    startActivity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity();
      }
    });
    sendPayload.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        sendMessageToActivity();
      }
    });
    View[] toDisable = {sendPayloadLayout, sendPayload};
    for(View view : toDisable){
      disableView(view);
    }
    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    return view;
  }

  public void enableView (View view){
    view.setEnabled(true);
    view.setAlpha(1);
  }

  public void disableView (View view){
    view.setEnabled(false);
    view.setAlpha((float)0.4);
  }

  public void updateMessages(){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        PayloadMessageAdapter payloadMessageAdapter = new PayloadMessageAdapter(getActivity().getBaseContext(), R.id.MessagesListView, messages);
        messageList.setAdapter(payloadMessageAdapter);
      }
    });
  }

  public void startActivity() {
    String activityId = customActionName.getText().toString();
    String payload = initialPayloadContent.getText().toString();

    CustomActivityRequest car = new CustomActivityRequest(activityId);
    car.setPayload(payload);
    boolean nonBlocking = ((Switch) view.findViewById(R.id.customActivityBlocking)).isChecked();
    car.setNonBlocking(nonBlocking);
    initialPayload.setText(payload);

    View [] toDisable = {customActionName, initialPayloadContent, startActivity};
    for(View view : toDisable){
      disableView(view);
    }
    View[] toEnable = {sendPayloadLayout, sendPayload, messagesLayout};
    for(View view : toEnable){
      enableView(view);
    }
    messages = new ArrayList<PayloadMessage>();
    updateMessages();
    initialPayloadContent.setText("");
    finalPayload.setText("");

    getCloverConnector().startCustomActivity(car);
  }

  public void sendMessageToActivity() {
    String activityId = customActionName.getText().toString();
    String payload = activityPayload.getText().toString();
    MessageToActivity messageRequest = new MessageToActivity(activityId, toJsonString(payload));
    getCloverConnector().sendMessageToActivity(messageRequest);
    messages.add(new PayloadMessage(payload, true));
    activityPayload.setText("");
    updateMessages();
  }

  public void addMessage(MessageFromActivity message){
    messages.add(new PayloadMessage(message.getPayload(), false));
    updateMessages();
  }

  public void onFinalMessage(final String responsePayload){
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        finalPayload.setText(responsePayload);
        View[] toDisable = {messagesLayout, sendPayloadLayout, sendPayload};
        for(View view : toDisable){
          disableView(view);
        }
        View [] toEnable = {customActionName, initialPayloadContent, startActivity};
        for(View view : toEnable){
          enableView(view);
        }
      }
    });
  }

  public String toJsonString(String message) {
    return GSON.toJson(message);
  }


  public void setCloverConnector(ICloverConnector cloverConnector) {
    cloverConnectorWeakReference = new WeakReference<ICloverConnector>(cloverConnector);
  }

  public ICloverConnector getCloverConnector(){
    return cloverConnectorWeakReference.get();
  }

}
