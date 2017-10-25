package com.clover.remote.client.transport.websocket;

import android.util.Log;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketCloseCode;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.net.URI;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

public class CloverNVWebSocketClient implements WebSocketListener {

  private static final int MISSED_PONG = 4001;

  private final URI endpoint;
  private final CloverNVWebSocketClientListener listener;
  private WebSocketFactory factory;
  private WebSocket socket;
  private volatile boolean notifyClose;

  public CloverNVWebSocketClient(URI endpoint, CloverNVWebSocketClientListener listener, KeyStore trustStore) {

    this.listener = listener;
    this.endpoint = endpoint;

    try {
      factory = new WebSocketFactory();

      TrustManagerFactory tmf = TrustManagerFactory.getInstance( "X509" );
      tmf.init( trustStore );

      SSLContext sslContext = null;
      sslContext = SSLContext.getInstance( "TLS" );
      sslContext.init( /*kmf.getKeyManagers()*/null, tmf.getTrustManagers(), null );

      factory.setSSLContext(sslContext);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public void connect() {
    if(socket != null) {
      throw new RuntimeException("Socket already created. Must create a new CloverNVWebSocketClient");
    }
    try {
      socket = factory.createSocket(endpoint, 5000);
      socket.setAutoFlush(true);
      socket.addListener(this);
      socket.connect();
    } catch(Throwable e) {
      e.printStackTrace();
      listener.connectionError(this);
    }
  }
  public void close() {
    socket.sendClose();
  }

  public boolean isConnecting() {
    return socket.getState() == WebSocketState.CONNECTING;
  }

  public boolean isOpen() {
    return socket.isOpen();
  }

  public boolean isClosing() {
    return socket.getState() == WebSocketState.CLOSING;
  }

  public boolean isClosed() {
    return socket.getState() == WebSocketState.CLOSED;
  }

  @Override public void onTextMessage(WebSocket websocket, String text) throws Exception {
    listener.onMessage(this, text);
  }

  @Override public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {

  }

  @Override public void onSendingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

  }

  @Override public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {

  }

  @Override public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {
    Log.d(getClass().getSimpleName(), String.format("Frame Unsent frame, %s", frame));
  }

  @Override public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
    listener.onOpen(this);
  }

  @Override public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {
    Log.w(getClass().getSimpleName(), cause);
    listener.connectionError(this);
  }

  @Override public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer)
      throws Exception {
    int closeCode = serverCloseFrame != null ? serverCloseFrame.getCloseCode() : clientCloseFrame != null ? clientCloseFrame.getCloseCode() : WebSocketCloseCode.NONE;
    String closeReason = clientCloseFrame != null ? clientCloseFrame.getCloseReason() : serverCloseFrame != null ? serverCloseFrame.getCloseReason() : "";
    listener.onClose(this, closeCode, closeReason, closedByServer);
  }

  @Override public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

  }

  @Override public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

  }

  @Override public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

  }

  @Override public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

  }

  @Override public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
    listener.onClose(this, frame.getCloseCode(), frame.getCloseReason(), true);
  }

  @Override public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
    if (!isClosing() && !isClosed()) {
      Log.e(getClass().getSimpleName(), "Error", cause);
    }
  }

  @Override public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
    socket.sendPong();
  }

  @Override public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
    listener.onPong(this);
  }

  @Override public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {
  }

  @Override public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
    Log.e(getClass().getSimpleName(), "Error in callback", cause);
  }

  @Override public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {
  }

  @Override public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
    if (!isClosing() && !isClosed()) {
      Log.e(getClass().getSimpleName(), String.format("Error in frame, %s", frame), cause);
    }
  }

  @Override public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {
    if (!isClosing() && !isClosed()) {
      Log.e(getClass().getSimpleName(), String.format("Error in message, %s", frames), cause);
    }
  }

  @Override public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
    if (!isClosing() && !isClosed()) {
      Log.e(getClass().getSimpleName(), "Error in message decompression", cause);
    }
  }

  @Override public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {
    if (!isClosing() && !isClosed()) {
      Log.e(getClass().getSimpleName(), "Error in test message", cause);
    }
  }

  @Override public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
    if (!isClosing() && !isClosed()) {
      Log.e(getClass().getSimpleName(), String.format("Error in send, %s", frame), cause);
    }
    listener.onSendError(frame.getPayloadText());
  }

  @Override public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
    Log.e(getClass().getSimpleName(), "Unexpected error", cause);
  }

  public void send(String message) {
    socket.sendText(message);
  }

  public void sendPing() {
    if (isOpen()) {
      socket.sendPing();
    }
  }

  public void disconnect() {
    socket.disconnect(1000, null, 0);
  }

  public void disconnectMissedPong() {
    socket.disconnect(MISSED_PONG, "Missed pong", 0);
  }

  public void clearListener() {
    if (!isClosing() && !isClosed()) {
      Log.w(getClass().getSimpleName(), "Listener cleared");
    }
    socket.removeListener(this);
  }

  public void setNotifyClose(boolean b) {
    this.notifyClose = b;
  }

  public synchronized boolean shouldNotifyClose() {
    return this.notifyClose;
  }
}
