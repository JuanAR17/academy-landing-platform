//Hasta aquí funciona bien

package com.academia.backend.dto.in;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "CardPaymentIn",
        description = "Cuerpo para /payment/process. Primera transacción: datos de tarjeta. " +
                      "Siguientes: cardTokenId (y opcional customerId).")
public class CardPaymentIn {

    // ===== Requeridos siempre =====
    @NotBlank
    @Pattern(regexp = "^\\d+(?:\\.\\d{1,2})?$", message = "value debe tener hasta 2 decimales")
    @Schema(description = "Valor de la transacción (en string, 2 decimales)", example = "10000.00")
    private String value;

    @NotBlank
    @Size(max = 4)
    @Schema(description = "Tipo documento", example = "CC")
    private String docType;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "Número de documento", example = "1098754561")
    private String docNumber;

    @NotBlank @Size(max = 50) @Schema(example = "Julián Eduardo")
    private String name;

    @NotBlank @Size(max = 50) @Schema(example = "Villamizar Peña")
    private String lastName;

    @NotBlank @Email @Size(max = 50) @Schema(example = "fenon214@hotmail.com")
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{10}$")
    @Schema(description = "Celular (10 dígitos, sin +57)", example = "3234323296")
    private String cellPhone;

    @Pattern(regexp = "^\\d{10}$")
    @Schema(description = "Teléfono (10 dígitos)", example = "6076830034")
    private String phone;

    @NotBlank @Schema(description = "Dirección del cliente", example = "Cra 23 # 33-22, Antonia Santos")
    private String address;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{2}$")
    @Schema(description = "País ISO-3166-1 alfa-2", example = "CO")
    private String country;

    @NotBlank @Schema(description = "Ciudad del cliente", example = "Bucaramanga")
    private String city;

    // ===== Solo PRIMERA VEZ (sin token) =====
    @Schema(description = "Número de tarjeta (solo primera vez)", example = "5176400189059206")
    private String cardNumber;

    @Schema(description = "Año (YYYY)", example = "2029")
    private String cardExpYear;

    @Schema(description = "Mes (MM)", example = "08")
    private String cardExpMonth;

    @Schema(description = "CVV (3-4 dígitos)", example = "258")
    private String cardCvc;

    // ===== Siguientes cobros =====
    @Schema(description = "Id del token de la tarjeta", example = "ahYQb8SkKSzyM9cmmvC")
    private String cardTokenId;

    @Schema(description = "Id del cliente en ePayco", example = "cst_9rT5Vq1abcde")
    private String customerId;

    // ===== Opcionales =====
    @Pattern(regexp = "^[A-Z]{3}$")
    @Schema(description = "Moneda ISO-4217. Por defecto COP si no envías", example = "COP")
    private String currency;

    @Pattern(regexp = "^\\d+$", message = "dues debe ser numérico en string")
    @Schema(description = "Número de cuotas (string)", example = "2")
    private String dues;

    @Schema(description = "Transacción de prueba (por defecto false)", example = "true")
    private Boolean testMode;

    @Schema(description = "IP del cliente", example = "110.60.95.38")
    private String ip;

    @Schema(description = "URL a donde redirecciona luego del pago")
    private String urlResponse;

    @Schema(description = "URL de confirmación (server to server)")
    private String urlConfirmation;

    @Schema(description = "Método de confirmación", example = "POST")
    private String methodConfirmation;

    // Extras
    @Schema(example = "extra-1") private String extra1;
    @Schema(example = "extra-2") private String extra2;
    @Schema(example = "extra-3") private String extra3;
    @Schema(example = "extra-4") private String extra4;
    @Schema(example = "extra-5") private String extra5;
    @Schema(example = "extra-6") private String extra6;
    @Schema(example = "extra-7") private String extra7;
    @Schema(example = "extra-8") private String extra8;
    @Schema(example = "extra-9") private String extra9;
    @Schema(example = "extra-10") private String extra10;

    // ===== Getters / setters =====
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public String getDocNumber() { return docNumber; }
    public void setDocNumber(String docNumber) { this.docNumber = docNumber; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCellPhone() { return cellPhone; }
    public void setCellPhone(String cellPhone) { this.cellPhone = cellPhone; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public String getCardExpYear() { return cardExpYear; }
    public void setCardExpYear(String cardExpYear) { this.cardExpYear = cardExpYear; }
    public String getCardExpMonth() { return cardExpMonth; }
    public void setCardExpMonth(String cardExpMonth) { this.cardExpMonth = cardExpMonth; }
    public String getCardCvc() { return cardCvc; }
    public void setCardCvc(String cardCvc) { this.cardCvc = cardCvc; }
    public String getCardTokenId() { return cardTokenId; }
    public void setCardTokenId(String cardTokenId) { this.cardTokenId = cardTokenId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDues() { return dues; }
    public void setDues(String dues) { this.dues = dues; }
    public Boolean getTestMode() { return testMode; }
    public void setTestMode(Boolean testMode) { this.testMode = testMode; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getUrlResponse() { return urlResponse; }
    public void setUrlResponse(String urlResponse) { this.urlResponse = urlResponse; }
    public String getUrlConfirmation() { return urlConfirmation; }
    public void setUrlConfirmation(String urlConfirmation) { this.urlConfirmation = urlConfirmation; }
    public String getMethodConfirmation() { return methodConfirmation; }
    public void setMethodConfirmation(String methodConfirmation) { this.methodConfirmation = methodConfirmation; }
    public String getExtra1() { return extra1; }
    public void setExtra1(String extra1) { this.extra1 = extra1; }
    public String getExtra2() { return extra2; }
    public void setExtra2(String extra2) { this.extra2 = extra2; }
    public String getExtra3() { return extra3; }
    public void setExtra3(String extra3) { this.extra3 = extra3; }
    public String getExtra4() { return extra4; }
    public void setExtra4(String extra4) { this.extra4 = extra4; }
    public String getExtra5() { return extra5; }
    public void setExtra5(String extra5) { this.extra5 = extra5; }
    public String getExtra6() { return extra6; }
    public void setExtra6(String extra6) { this.extra6 = extra6; }
    public String getExtra7() { return extra7; }
    public void setExtra7(String extra7) { this.extra7 = extra7; }
    public String getExtra8() { return extra8; }
    public void setExtra8(String extra8) { this.extra8 = extra8; }
    public String getExtra9() { return extra9; }
    public void setExtra9(String extra9) { this.extra9 = extra9; }
    public String getExtra10() { return extra10; }
    public void setExtra10(String extra10) { this.extra10 = extra10; }
}
