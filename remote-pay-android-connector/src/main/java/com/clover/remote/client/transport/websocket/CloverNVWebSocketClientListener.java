package com.clover.remote.client.transport.websocket;

public interface CloverNVWebSocketClientListener {

  void onOpen(CloverNVWebSocketClient ws);

  void onNotResponding(CloverNVWebSocketClient ws);

  void onPingResponding(CloverNVWebSocketClient ws);

  void onPong(CloverNVWebSocketClient ws);

  void onClose(CloverNVWebSocketClient ws, int code, String reason, boolean remote);

  void onMessage(CloverNVWebSocketClient ws, String message);

  void connectionError(CloverNVWebSocketClient cloverNVWebSocketClient);

  void onSendError(String payloadText);
}
