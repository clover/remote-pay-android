package com.clover.remote.client.messages;

import com.clover.common2.payments.PayIntent;
import com.clover.sdk.v3.payments.VaultedCard;

import java.util.Map;

public abstract class BaseTransactionRequest extends BaseRequest {
  private Boolean disablePrinting = null;
  private Boolean cardNotPresent = null;
  private Boolean disableRestartTransactionOnFail = null;
  private long amount;
  private Integer cardEntryMethods = null;
  private VaultedCard vaultedCard = null;
  private String externalId = null;
  private PayIntent.TransactionType type = null;
  private Boolean disableDuplicateChecking = null;
  private Boolean disableReceiptSelection = null;
  private Boolean autoAcceptPaymentConfirmations = null;
  private Map<String, String> extras = null;
  private Map<String, String> regionalExtras = null;
  private String externalReferenceId = null;
  private Boolean allowPartialAuth = null;

  public BaseTransactionRequest(long amount, String externalId) {
    if(externalId == null || externalId.length() > 32) {
      throw new IllegalArgumentException("The externalId must be provided.  The maximum length is 32 characters.");
    }
    this.amount = amount;
    this.externalId = externalId;
  }

  /**
   * Get the field value
   *
   * @return Do not print
   */
  public Boolean getDisablePrinting() {
    return disablePrinting;
  }

  /**
   * Set the field value
   *
   * @param disablePrinting Do not print
   */
  public void setDisablePrinting(Boolean disablePrinting) {
    this.disablePrinting = disablePrinting;
  }

  /**
   * Get the field value
   *
   * @return If true then card not present is accepted
   */
  public Boolean getCardNotPresent() {
    return cardNotPresent;
  }

  /**
   * Set the field value
   *
   * @param cardNotPresent If true then card not present is accepted
   */
  public void setCardNotPresent(Boolean cardNotPresent) {
    this.cardNotPresent = cardNotPresent;
  }

  /**
   * Get the field value
   *
   * @return If the transaction times out or fails because of decline, do not restart it
   */
  public Boolean getDisableRestartTransactionOnFail() {
    return disableRestartTransactionOnFail;
  }

  /**
   * Set the field value
   *
   * @param disableRestartTransactionOnFail If the transaction times out or fails because of decline, do not restart it
   */
  public void setDisableRestartTransactionOnFail(Boolean disableRestartTransactionOnFail) {
    this.disableRestartTransactionOnFail = disableRestartTransactionOnFail;
  }

  /**
   * Get the field value
   *
   * @return Total amount paid
   */
  public long getAmount() {
    return amount;
  }

  /**
   * Set the field value
   *
   * @param amount Total amount paid
   */
  public void setAmount(long amount) {
    this.amount = amount;
  }

  /**
   * Get the field value
   *
   * @return Allowed entry methods
   */
  public Integer getCardEntryMethods() {
    return cardEntryMethods;
  }

  /**
   * Set the field value
   *
   * @param cardEntryMethods Allowed entry methods
   */
  public void setCardEntryMethods(Integer cardEntryMethods) {
    this.cardEntryMethods = cardEntryMethods;
  }

  /**
   * Get the field value
   *
   * @return A saved card
   */
  public VaultedCard getVaultedCard() {
    return vaultedCard;
  }

  /**
   * Set the field value
   *
   * @param vaultedCard A saved card
   */
  public void setVaultedCard(VaultedCard vaultedCard) {
    this.vaultedCard = vaultedCard;
  }

  /**
   * Get the field value
   *
   * @return An id that will be persisted with transactions.
   */
  public String getExternalId() {
    return externalId;
  }

  /**
   * Set the field value
   *
   * @param externalId An id that will be persisted with transactions.
   */
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  /**
   * Get the field value
   *
   * @return The type of the transaction.
   */
  abstract public PayIntent.TransactionType getType();

  /**
   * Get the field value
   *
   * @return The disable duplicate transaction validation settings overrides
   */
  public Boolean getDisableDuplicateChecking() {
    return disableDuplicateChecking;
  }

  /**
   * Set the field value
   *
   * @param disableDuplicateChecking The disable duplicate transaction validation settings overrides
   */
  public void setDisableDuplicateChecking(Boolean disableDuplicateChecking) {
    this.disableDuplicateChecking = disableDuplicateChecking;
  }

  /**
   * Set the field value
   *
   * @param disableReceiptSelection The disable receipt options screen settings overrides
   */
  public void setDisableReceiptSelection(Boolean disableReceiptSelection) {
    this.disableReceiptSelection = disableReceiptSelection;
  }

  /**
   * Get the field value
   *
   * @return The disable receipt options screen settings overrides
   */
  public Boolean getDisableReceiptSelection() {
    return this.disableReceiptSelection;
  }

  /**
   * Set the field value
   *
   * @param autoAcceptPaymentConfirmations The automatically accept payment confirmations settings override
   */
  public void setAutoAcceptPaymentConfirmations(Boolean autoAcceptPaymentConfirmations) {
    this.autoAcceptPaymentConfirmations = autoAcceptPaymentConfirmations;
  }

  /**
   * Get the field value
   *
   * @return The automatically accept payment confirmations settings override
   */
  public Boolean getAutoAcceptPaymentConfirmations() {
    return this.autoAcceptPaymentConfirmations;
  }

  /**
   * Get the field value
   * @return Extra pass-through data used by external systems.
   */
  public Map<String, String> getExtras() {
    return extras;
  }

  /**
   * Set the field value
   * @param extras Extra pass-through data used by external systems.
   */
  public void setExtras(Map<String, String> extras) {
    this.extras = extras;
  }

  /**
   * Get the field value
   * @return A map of all the regionalExtras that have been provided
   */
  public Map<String, String> getRegionalExtras() {
    return regionalExtras;
  }

  /**
   * Set the field value
   * @param regionalExtras any extra region specific data.  Keys are referenced in RegionalExtras.java
   */
  public void setRegionalExtras(Map<String, String> regionalExtras) {
    this.regionalExtras = regionalExtras;
  }

  /**
   * Get the field value
   * @return An id that can be passed to the merchant's gateway, and ultimately appear in settlement records.
   */
  public String getExternalReferenceId() {
    return externalReferenceId;
  }

  /**
   * Set the field value
   * @param externalReferenceId An id that can be passed to the merchant's gateway, and ultimately appear in settlement records.
   */
  public void setExternalReferenceId(String externalReferenceId) {
    this.externalReferenceId = externalReferenceId;
  }

  /**
   * Get the field value
   *
   * @return Allow partial auth
   */
  public Boolean getAllowPartialAuth() {
    return allowPartialAuth;
  }

  /**
   * Set the field value
   *
   * @param allowPartialAuth Allow partial auth
   */
  public void setAllowPartialAuth(Boolean allowPartialAuth) {
    this.allowPartialAuth = allowPartialAuth;
  }

}
