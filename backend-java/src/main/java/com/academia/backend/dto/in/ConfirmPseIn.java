package com.academia.backend.dto.in;

import jakarta.validation.constraints.NotNull;

/** Input para confirmar transacción PSE en ePayco. */
public class ConfirmPseIn {

  @NotNull
  private Long transactionID; // según doc: integer(11)

  public ConfirmPseIn() {}

  public ConfirmPseIn(Long transactionID) {
    this.transactionID = transactionID;
  }

  public Long getTransactionID() {
    return transactionID;
  }

  public void setTransactionID(Long transactionID) {
    this.transactionID = transactionID;
  }
}
