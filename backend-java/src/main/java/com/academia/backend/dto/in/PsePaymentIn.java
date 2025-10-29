package com.academia.backend.dto.in;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO para iniciar una transacción PSE en ePayco.
 * NOTA: La IP del cliente NO va aquí; la resuelve el backend con IpResolver.
 */
public class PsePaymentIn {

  // ====== OBLIGATORIOS SEGÚN DOC ======
  @NotBlank
  private String bank;                       // id banco PSE (p.ej. "1022")

  @NotNull
  @DecimalMin("0.01")
  private BigDecimal value;                  // valor de la transacción

  @NotBlank
  @Size(max = 4)
  private String docType;                    // CC, CE, NIT...

  @NotBlank
  @Size(max = 20)
  private String docNumber;

  @NotBlank
  @Size(max = 50)
  private String name;

  @NotBlank
  @Email
  @Size(max = 100)
  private String email;

  @NotBlank
  @Size(min = 7, max = 15)
  private String cellPhone;

  @NotBlank
  @Size(max = 120)
  private String address;

  @NotBlank
  private String urlResponse;                // URL del front adonde ePayco redirige

  // ====== OPCIONALES ÚTILES ======
  @Size(max = 50)
  private String lastName;                   // opcional según doc

  @Size(max = 20)
  private String phone;

  private BigDecimal tax;                    // IVA
  private BigDecimal taxBase;                // Base sin IVA

  @Size(max = 255)
  private String description;                // si es null, define default en servicio

  @Size(max = 60)
  private String invoice;                    // si es null, generar una en servicio

  private String currency = "COP";           // default

  @Min(0)
  @Max(1)
  private Integer typePerson;                // 0 persona, 1 comercio (opcional)

  @Size(max = 255)
  private String urlConfirmation;            // webhook backend (opcional)

  @Pattern(regexp = "GET|POST", message = "methodConfirmation debe ser GET o POST")
  private String methodConfirmation = "GET"; // default válido

  // extras libres
  private String extra1, extra2, extra3, extra4, extra5,
                 extra6, extra7, extra8, extra9, extra10;

  private Boolean testMode;

  // ====== CONSTRUCTORES ======

  /** Requerido por Jackson/Bean Validation. */
  public PsePaymentIn() {}

  /** Constructor completo (mantiene compatibilidad con tu flujo actual). */
  public PsePaymentIn(
      String bank,
      BigDecimal value,
      String docType,
      String docNumber,
      String name,
      String lastName,
      String email,
      String cellPhone,
      String address,
      String phone,
      BigDecimal tax,
      BigDecimal taxBase,
      String description,
      String invoice,
      String currency,
      Integer typePerson,
      String urlResponse,
      String urlConfirmation,
      String methodConfirmation,
      String extra1,
      String extra2,
      String extra3,
      String extra4,
      String extra5,
      String extra6,
      String extra7,
      String extra8,
      String extra9,
      String extra10,
      Boolean testMode
  ) {
    this.bank = bank;
    this.value = value;
    this.docType = docType;
    this.docNumber = docNumber;
    this.name = name;
    this.lastName = lastName;
    this.email = email;
    this.cellPhone = cellPhone;
    this.address = address;
    this.phone = phone;
    this.tax = tax;
    this.taxBase = taxBase;
    this.description = description;
    this.invoice = invoice;
    this.currency = (currency == null || currency.isBlank()) ? "COP" : currency;
    this.typePerson = typePerson;
    this.urlResponse = urlResponse;
    this.urlConfirmation = urlConfirmation;
    this.methodConfirmation = (methodConfirmation == null || methodConfirmation.isBlank())
        ? "GET" : methodConfirmation;
    this.extra1 = extra1;
    this.extra2 = extra2;
    this.extra3 = extra3;
    this.extra4 = extra4;
    this.extra5 = extra5;
    this.extra6 = extra6;
    this.extra7 = extra7;
    this.extra8 = extra8;
    this.extra9 = extra9;
    this.extra10 = extra10;
    this.testMode = testMode;
  }

  // ====== GETTERS ======
  public String getBank() { return bank; }
  public BigDecimal getValue() { return value; }
  public String getDocType() { return docType; }
  public String getDocNumber() { return docNumber; }
  public String getName() { return name; }
  public String getLastName() { return lastName; }
  public String getEmail() { return email; }
  public String getCellPhone() { return cellPhone; }
  public String getAddress() { return address; }
  public String getUrlResponse() { return urlResponse; }
  public String getPhone() { return phone; }
  public BigDecimal getTax() { return tax; }
  public BigDecimal getTaxBase() { return taxBase; }
  public String getDescription() { return description; }
  public String getInvoice() { return invoice; }
  public String getCurrency() { return currency; }
  public Integer getTypePerson() { return typePerson; }
  public String getUrlConfirmation() { return urlConfirmation; }
  public String getMethodConfirmation() { return methodConfirmation; }
  public String getExtra1() { return extra1; }
  public String getExtra2() { return extra2; }
  public String getExtra3() { return extra3; }
  public String getExtra4() { return extra4; }
  public String getExtra5() { return extra5; }
  public String getExtra6() { return extra6; }
  public String getExtra7() { return extra7; }
  public String getExtra8() { return extra8; }
  public String getExtra9() { return extra9; }
  public String getExtra10() { return extra10; }
  public Boolean getTestMode() { return testMode; }

  // ====== SETTERS ======
  public void setBank(String bank) { this.bank = bank; }
  public void setValue(BigDecimal value) { this.value = value; }
  public void setDocType(String docType) { this.docType = docType; }
  public void setDocNumber(String docNumber) { this.docNumber = docNumber; }
  public void setName(String name) { this.name = name; }
  public void setLastName(String lastName) { this.lastName = lastName; }
  public void setEmail(String email) { this.email = email; }
  public void setCellPhone(String cellPhone) { this.cellPhone = cellPhone; }
  public void setAddress(String address) { this.address = address; }
  public void setUrlResponse(String urlResponse) { this.urlResponse = urlResponse; }
  public void setPhone(String phone) { this.phone = phone; }
  public void setTax(BigDecimal tax) { this.tax = tax; }
  public void setTaxBase(BigDecimal taxBase) { this.taxBase = taxBase; }
  public void setDescription(String description) { this.description = description; }
  public void setInvoice(String invoice) { this.invoice = invoice; }
  public void setCurrency(String currency) { this.currency = currency; }
  public void setTypePerson(Integer typePerson) { this.typePerson = typePerson; }
  public void setUrlConfirmation(String urlConfirmation) { this.urlConfirmation = urlConfirmation; }
  public void setMethodConfirmation(String methodConfirmation) { this.methodConfirmation = methodConfirmation; }
  public void setExtra1(String extra1) { this.extra1 = extra1; }
  public void setExtra2(String extra2) { this.extra2 = extra2; }
  public void setExtra3(String extra3) { this.extra3 = extra3; }
  public void setExtra4(String extra4) { this.extra4 = extra4; }
  public void setExtra5(String extra5) { this.extra5 = extra5; }
  public void setExtra6(String extra6) { this.extra6 = extra6; }
  public void setExtra7(String extra7) { this.extra7 = extra7; }
  public void setExtra8(String extra8) { this.extra8 = extra8; }
  public void setExtra9(String extra9) { this.extra9 = extra9; }
  public void setExtra10(String extra10) { this.extra10 = extra10; }
  public void setTestMode(Boolean testMode) { this.testMode = testMode; }
}
