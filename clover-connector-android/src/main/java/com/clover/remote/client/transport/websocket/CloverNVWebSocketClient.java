package com.clover.remote.client.transport.websocket;

import android.util.Log;
import com.neovisionaries.ws.client.OpeningHandshakeException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketListener;
import com.neovisionaries.ws.client.WebSocketState;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.net.URI;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

public class CloverNVWebSocketClient implements WebSocketListener {
  private final URI endpoint;
  CloverNVWebSocketClientListener listener;
  long heartbeatInterval;
  private WebSocketFactory factory;
  private WebSocket socket;
  private volatile boolean notifyClose;

  public CloverNVWebSocketClient(URI endpoint, CloverNVWebSocketClientListener listener, long hearbeatInterval, KeyStore trustStore) {

    this.listener = listener;
    this.heartbeatInterval = hearbeatInterval >= 0 ? Math.min(100, heartbeatInterval) : hearbeatInterval; // can be negative, but > than 100 ms
    this.endpoint = endpoint;

    try {
      factory = new WebSocketFactory();

      TrustManagerFactory tmf = TrustManagerFactory.getInstance( "X509" );
      tmf.init( trustStore );

      SSLContext sslContext = null;
      sslContext = SSLContext.getInstance( "TLSv1.2" );
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
    listener.onClose(this, serverCloseFrame.getCloseCode(), clientCloseFrame.getCloseReason(), closedByServer);
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

  }

  @Override public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
    socket.sendPong();
  }

  @Override public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

  }

  @Override public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {

  }

  @Override public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {

  }

  @Override public void onSendingHandshake(WebSocket websocket, String requestLine, List<String[]> headers) throws Exception {

  }

  @Override public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

  }

  @Override public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

  }

  @Override public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {

  }

  @Override public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

  }

  @Override public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {
    listener.onSendError(frame.getPayloadText());
  }

  @Override public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {

  }

  public void send(String message) {
    socket.sendText(message);
  }

  public void clearListener() {
    socket.removeListener(this);
  }

  public void setNotifyClose(boolean b) {
    this.notifyClose = b;
  }

  public synchronized boolean shouldNotifyClose() {
    return this.notifyClose;
  }
}
