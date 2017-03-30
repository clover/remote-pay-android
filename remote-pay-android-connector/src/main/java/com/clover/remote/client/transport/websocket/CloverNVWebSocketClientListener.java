package com.clover.remote.client.transport.websocket;

public interface CloverNVWebSocketClientListener {


  public void onOpen(CloverNVWebSocketClient ws);

  public void onNotResponding(CloverNVWebSocketClient ws);

  public void onPingResponding(CloverNVWebSocketClient ws);

  public void onClose(CloverNVWebSocketClient ws, int code, String reason, boolean remote);

  public void onMessage(CloverNVWebSocketClient ws, String message);

  public void connectionError(CloverNVWebSocketClient cloverNVWebSocketClient);

  public void onSendError(String payloadText);
}
