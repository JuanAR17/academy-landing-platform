package com.academia.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "epayco_transactions")
public class EpaycoTransactionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "ref_payco")
  private Long refPayco;

  private String invoice;
  private String description;

  private BigDecimal amount;     // valorneto
  private String currency;       // moneda

  private BigDecimal tax;        // iva
  private BigDecimal ico;
  @Column(name = "base_tax")
  private BigDecimal baseTax;

  private String bank;
  private String status;         // estado
  private String response;       // respuesta
  private String receipt;        // recibo

  @Column(name = "txn_date")
  private Instant txnDate;       // fecha -> Instant

  private String franchise;      // franquicia
  @Column(name = "code_response")
  private Integer codeResponse;  // cod_respuesta
  @Column(name = "code_error")
  private String codeError;      // cod_error

  private String ip;
  @Column(name = "test_mode")
  private Boolean testMode;

  @Column(name = "doc_type")
  private String docType;
  @Column(name = "doc_number")
  private String docNumber;
  @Column(name = "first_names")
  private String firstNames;
  @Column(name = "last_names")
  private String lastNames;
  private String email;
  private String city;
  private String address;
  @Column(name = "country_iso2")
  private String countryIso2;

  @Column(columnDefinition = "jsonb")
  private String extras;

  @Column(name = "raw_payload", columnDefinition = "jsonb")
  private String rawPayload;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  // getters/setters
  public Long getId() { return id; }
  public UUID getUserId() { return userId; }
  public void setUserId(UUID userId) { this.userId = userId; }
  public Long getRefPayco() { return refPayco; }
  public void setRefPayco(Long refPayco) { this.refPayco = refPayco; }
  public String getInvoice() { return invoice; }
  public void setInvoice(String invoice) { this.invoice = invoice; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }
  public String getCurrency() { return currency; }
  public void setCurrency(String currency) { this.currency = currency; }
  public BigDecimal getTax() { return tax; }
  public void setTax(BigDecimal tax) { this.tax = tax; }
  public BigDecimal getIco() { return ico; }
  public void setIco(BigDecimal ico) { this.ico = ico; }
  public BigDecimal getBaseTax() { return baseTax; }
  public void setBaseTax(BigDecimal baseTax) { this.baseTax = baseTax; }
  public String getBank() { return bank; }
  public void setBank(String bank) { this.bank = bank; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getResponse() { return response; }
  public void setResponse(String response) { this.response = response; }
  public String getReceipt() { return receipt; }
  public void setReceipt(String receipt) { this.receipt = receipt; }
  public Instant getTxnDate() { return txnDate; }
  public void setTxnDate(Instant txnDate) { this.txnDate = txnDate; }
  public String getFranchise() { return franchise; }
  public void setFranchise(String franchise) { this.franchise = franchise; }
  public Integer getCodeResponse() { return codeResponse; }
  public void setCodeResponse(Integer codeResponse) { this.codeResponse = codeResponse; }
  public String getCodeError() { return codeError; }
  public void setCodeError(String codeError) { this.codeError = codeError; }
  public String getIp() { return ip; }
  public void setIp(String ip) { this.ip = ip; }
  public Boolean getTestMode() { return testMode; }
  public void setTestMode(Boolean testMode) { this.testMode = testMode; }
  public String getDocType() { return docType; }
  public void setDocType(String docType) { this.docType = docType; }
  public String getDocNumber() { return docNumber; }
  public void setDocNumber(String docNumber) { this.docNumber = docNumber; }
  public String getFirstNames() { return firstNames; }
  public void setFirstNames(String firstNames) { this.firstNames = firstNames; }
  public String getLastNames() { return lastNames; }
  public void setLastNames(String lastNames) { this.lastNames = lastNames; }
  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
  public String getCity() { return city; }
  public void setCity(String city) { this.city = city; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public String getCountryIso2() { return countryIso2; }
  public void setCountryIso2(String countryIso2) { this.countryIso2 = countryIso2; }
  public String getExtras() { return extras; }
  public void setExtras(String extras) { this.extras = extras; }
  public String getRawPayload() { return rawPayload; }
  public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
  public Instant getCreatedAt() { return createdAt; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
