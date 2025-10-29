// backend-java/src/main/java/com/academia/backend/dto/PsePaymentOut.java
package com.academia.backend.dto;

import java.time.Instant;
import com.academia.backend.domain.EpaycoTransactionEntity;
import com.fasterxml.jackson.databind.JsonNode;

public class PsePaymentOut {
  public String redirectUrl; // urlbanco
  public Long refPayco;
  public String invoice;
  public String status;      // "Pendiente"
  public String response;    // "Redireccionando al banco"
  public String receipt;     // recibo
  public String authorizationOrTxnId; // transactionID/autorization
  public String ticketId;
  public Instant txnDate;
  public String lookupToken; // (opcional) para consulta segura luego

  /** Construye la respuesta para el front a partir de lo persistido. */
  public static PsePaymentOut from(EpaycoTransactionEntity tx, String lookupTokenIfAny) {
    PsePaymentOut out = new PsePaymentOut();
    out.refPayco  = tx.getRefPayco();
    out.invoice   = tx.getInvoice();
    out.status    = tx.getStatus();
    out.response  = tx.getResponse();
    out.receipt   = tx.getReceipt();
    out.txnDate   = tx.getTxnDate();
    out.lookupToken = lookupTokenIfAny;

    JsonNode ex = tx.getExtras();
    if (ex != null) {
      // usa las claves que guardaste en extras al crear la PSE
      out.redirectUrl          = ex.path("urlBanco").asText(null);
      out.authorizationOrTxnId = ex.path("transactionID").asText(null);
      out.ticketId             = ex.path("ticketId").asText(null);
    }
    return out;
  }
}
