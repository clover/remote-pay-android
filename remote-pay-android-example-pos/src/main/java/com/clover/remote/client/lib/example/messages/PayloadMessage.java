/*
 * Copyright (C) 2017 Clover Network, Inc.
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

package com.clover.remote.client.lib.example.messages;

import com.clover.remote.message.ByteArrayToBase64TypeAdapter;
import com.clover.remote.message.CloverJSONifiableTypeAdapter;
import com.clover.sdk.JSONifiable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PayloadMessage {
  public final String payloadClassName;
  public final MessageType messageType;
  private static final Gson GSON;

  static {
    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeHierarchyAdapter(JSONifiable.class, new CloverJSONifiableTypeAdapter());
    builder.registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64TypeAdapter());
    GSON = builder.create();
  }

  private static final JsonParser PARSER = new JsonParser();

  public PayloadMessage(String payloadClassName, MessageType messageType) {
    if (payloadClassName == null || payloadClassName.isEmpty()) {
      this.payloadClassName = "PayloadMessage";
    } else {
      this.payloadClassName = payloadClassName;
    }
    this.messageType = messageType;
  }

  public String toJsonString() {
    return GSON.toJson(this, this.getClass());
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + toJsonString();
  }

  public static PayloadMessage fromJsonString(String m) {
    JsonElement je = PARSER.parse(m);
    JsonObject jo = je.getAsJsonObject();
    String payloadClassName = jo.get("payloadClassName").getAsString();
    Class<? extends PayloadMessage> cls = null;
    try {
      cls = (Class<? extends PayloadMessage>)Class.forName("com.clover.remote.client.lib.example.model." + payloadClassName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return GSON.fromJson(jo, cls);
  }

}
