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

package com.clover.remote.client.messages;

public class ProtocolMessages {

    /*
    public static class RemoteMessage
    {
        public String id;
        public Method method;
        public MessageTypes type;
        public String payload;
        public String packageName;

        public static Gson gson = new Gson();

        public static RemoteMessage createMessage(Methods meth, Method method, com.clover.remote.protocol.message.Message payload, String packageName)
        {
            RemoteMessage msg = new RemoteMessage();
            msg.method = method;
            msg.type = msgType;
            if (null == payload)
            {
                payload = new com.clover.remote.protocol.message.Message(method){};
            }
            msg.payload = gson.toJson(payload);
            msg.packageName = packageName;

            return msg;
        }
    }

    public static class Message
    {
        public final int version = 1;
        public final Methods method;

        public Message(Methods method)
        {
            this.method = method;
        }
    }

    public static class ShowWelcomeScreenMessage extends Message {
        public ShowWelcomeScreenMessage() {
            super(Methods.SHOW_WELCOME_SCREEN);
        }
    }

    public static class ShowThankYouScreen extends Message {
        public ShowThankYouScreen() {
            super(Methods.SHOW_THANK_YOU_SCREEN);
        }
    }

    public static class DiscoveryRequestMessage extends Message
    {
        public final boolean supportsOrderModification;

        public DiscoveryRequestMessage(boolean supportsOrderModification)
        {
            super(Methods.DISCOVERY_REQUEST);
            this.supportsOrderModification = supportsOrderModification;
        }
    }

    public class DiscoveryResponseMessage extends Message
    {
        public final String merchantId;
        public final String name;
        public final String serial;
        public final String model;
        public final boolean ready;

        public DiscoveryResponseMessage(String merchantId, String name, String serial, String model, boolean ready)
        {
            super(Methods.DISCOVERY_RESPONSE);
            this.merchantId = merchantId;
            this.name = name;
            this.serial = serial;
            this.model = model;
            this.ready = ready;
        }
    }


    public class ReceiptMessage extends Message
    {
        public ReceiptMessage()
        {
            super(Methods.SHOW_RECEIPT_SCREEN)
        }
    }

    public class TextPrintMessage extends Message
    {
        public List<String> textLines;

        public TextPrintMessage()
        {
            super(Methods.PRINT_TEXT)
            textLines = new List<String>();
        }
    }

    public class ImagePrintMessage extends Message
    {
        public ImagePrintMessage()
        {
            super(Methods.PRINT_IMAGE)
        }

        public String png;
    }

    public class FinishCancelMessage extends Message
    {
        public FinishCancelMessage()
        {
            super(Methods.FINISH_CANCEL)
        }
    }

    public class FinishOkMessage extends Message
    {
        public Payment paymentObj;
        public String payment;
        public Credit creditObj;
        public String credit;
        public Signature2 signature;

        public FinishOkMessage()
        {
            super(Methods.FINISH_OK)
        }
    }

    public class TxStartRequestMessage extends Message
    {
        public final PayIntent payIntent;
        public final Order order;

        public TxStartRequestMessage(PayIntent payIntent, Order order)
        {
            super(Methods.TX_START)
            this.payIntent = payIntent;
            this.order = order;
        }
    }

    public class VerifySignatureMessage extends Message
    {

        public String payment;
        public Payment paymentObj;
        public Signature2 signature;

        public VerifySignatureMessage()
        {
            super(Methods.VERIFY_SIGNATURE)
        }
    }

    public class TxStateMessage extends Message
    {
        public final TxState txState;

        public TxStateMessage(TxState txState)
        {
            super(Methods.TX_STATE)
            this.txState = txState;
        }
    }

    public class PartialAuthMessage extends Message
    {
        public long partialAuthAmount;

        public PartialAuthMessage() super(Methods.PARTIAL_AUTH) { }

        public PartialAuthMessage(long partialAuth)
        {
            super(Methods.PARTIAL_AUTH)
            this.partialAuthAmount = partialAuth;
        }
    }

    public class CashbackSelectedMessage extends Message
    {
        public long cashbackAmount;

        public CashbackSelectedMessage() super(Methods.CASHBACK_SELECTED) { }

        public CashbackSelectedMessage(long cashback)
        {
            super(Methods.CASHBACK_SELECTED)
            this.cashbackAmount = cashback;
        }
    }

    public class TipAddedMessage extends Message
    {
        public long tipAmount;

        public TipAddedMessage() super(Methods.TIP_ADDED) { }

        public TipAddedMessage(long tip)
        {
            super(Methods.TIP_ADDED)
            this.tipAmount = tip;
        }
    }

    public class RefundRequestMessage extends Message
    {
        public String orderId;
        public String paymentId;
        public long amount;

        public RefundRequestMessage() super(Methods.REFUND_REQUEST) { }

        public RefundRequestMessage(String oid, String pid, long amt )
        {
            super(Methods.REFUND_REQUEST)
            this.orderId = oid;
            this.paymentId = pid;
            this.amount = amt;
        }
    }

    public class TipAdjustAuthMessage extends Message
    {
        public String orderId;
        public String paymentId;
        public long amount;

        public TipAdjustAuthMessage(String orderId, String paymentId, long amount) super(Methods.TIP_ADJUST)
        {
            this.orderId = orderId;
            this.paymentId = paymentId;
            this.amount = amount;
        }
    }

    /// <summary>
    /// RefundResponseMessage is used when there is a refund for a payment. It is not used when doing a manual refund
    /// </summary>
    public class RefundResponseMessage extends Message
    {
        public String orderId;
        public String paymentId;
        public String refund;
        public TxState code;

        public RefundResponseMessage() super(Methods.REFUND_RESPONSE) { }

        public RefundResponseMessage(String oid, String pid, Refund refund, TxState state)
        {
            super(Methods.REFUND_RESPONSE)
            this.orderId = oid;
            this.paymentId = pid;
            this.refund = JsonUtils.serialize(refund);
            this.code = state;
        }
    }
    public class UiStateMessage extends Message
    {
        public final UiState uiState;
        public final String uiText;
        public final UiDirection uiDirection;
        public final InputOption[] inputOptions;

        public UiStateMessage(UiState uiState, String uiText, UiDirection uiDirection, InputOption[] inputOptions)
        {
            super(Methods.UI_STATE)
            this.uiState = uiState;
            this.uiText = uiText;
            this.uiDirection = uiDirection;
            this.inputOptions = inputOptions;
        }
    }

    /// <summary>
    /// Message used to indicate that a payment should be voided.
    /// </summary>
    public class VoidPaymentMessage extends Message
    {
        public String payment;
        public VoidReason voidReason;

        public VoidPaymentMessage(Payment payment, VoidReason voidReason)
        {
            super(Methods.VOID_PAYMENT)
            this.payment = JsonUtils.serialize(payment);
            this.voidReason = voidReason;
        }
    }
*

    public class TerminalMessage extends Message
    {
        public final String text;

        public TerminalMessage(String text)
        {
            super(Methods.TERMINAL_MESSAGE)
            this.text = text;
        }
    }

    public class OpenCashDrawerMessage extends Message
    {
        public String reason;

        public OpenCashDrawerMessage() super(Methods.OPEN_CASH_DRAWER)
        {

        }

        public OpenCashDrawerMessage(String reaseon) super(Methods.OPEN_CASH_DRAWER)
        {
            this.reason = reason;
        }
    }

    public class CloseoutMessage extends Message
    {
        public CloseoutMessage() super(Methods.CLOSEOUT)
        {
        }
    }

    public class KeyPressMessage extends Message
    {
        public KeyPress keyPress;

        public KeyPressMessage() super(Methods.KEY_PRESS) { }

        public KeyPressMessage(KeyPress keyPre)
        {
            super(Methods.KEY_PRESS)
            this.keyPress = keyPre;
        }
    }


    public class SignatureVerifiedMessage extends Message
    {

        public String payment;
        public boolean verified;

        public SignatureVerifiedMessage(Payment paymentObj, boolean verified)
        {
            super(Methods.SIGNATURE_VERIFIED)
            this.payment = JsonUtils.serialize(paymentObj);
            this.verified = verified;
        }
    }

    public class OrderUpdateMessage extends Message
    {
        public String order;
        public String lineItemsAddedOperation;
        public String lineItemsDeletedOperation;
        public String discountsAddedOperation;
        public String discountsDeletedOperation;
        public String orderDeletedOperation;


        public OrderUpdateMessage() super(Methods.SHOW_ORDER_SCREEN)
        {

        }

        public OrderUpdateMessage(DisplayOrder displayOrder) super(Methods.SHOW_ORDER_SCREEN)
        {
            this.order = JsonUtils.serialize(displayOrder);
            //System.Console.WriteLine("Serialized Order:" + this.order );
        }

    public void setOperation(DisplayOperation operation)
    {
        if (null == operation) { return; }

        if (operation is LineItemsAddedOperation)
        {
            this.lineItemsAddedOperation = JsonUtils.serialize(operation);
        }
        else if (operation is LineItemsDeletedOperation)
        {
            this.lineItemsDeletedOperation = JsonUtils.serialize(operation);
        }
        else if (operation is DiscountsDeletedOperation)
        {
            this.discountsDeletedOperation = JsonUtils.serialize(operation);
        }
        else if (operation is DiscountsAddedOperation)
        {
            this.discountsAddedOperation = JsonUtils.serialize(operation);
        }
        else if (operation is OrderDeletedOperation)
        {
            this.orderDeletedOperation = JsonUtils.serialize(operation);
        }
    }
}

/// <summary>
/// The top level protocol message
/// </summary
public class RemoteMessage
{
    public String id;
    public Methods method;
    public MessageTypes type;
    public String payload;
    public String packageName;

    public static RemoteMessage createMessage(Methods meth, MessageTypes msgType, Message payload, String packageName)
    {
        RemoteMessage msg = new RemoteMessage();
        msg.method = meth;
        msg.type = msgType;
        if (null == payload)
        {
            payload = new Message(meth);
        }
        msg.payload = JsonUtils.serialize(payload);
        msg.packageName = packageName;

        return msg;
    }
}*/
}
