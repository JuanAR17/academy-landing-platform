import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { CardPaymentRespose, DebitCreditCardPayment } from '../../shared/interfaces/payment';

@Injectable({
  providedIn: 'root'
})
export class EpaycoService {
  constructor(private http: HttpClient) {}
  private baseUrl = `${environment.apiUrl}/epayco`

  paymentCard(data: DebitCreditCardPayment) {
    return this.http.post<CardPaymentRespose>(`${this.baseUrl}/payment`, data)
  }

  generatePaymentUrl() {
    return this.http.get<any>(`${this.baseUrl}/charge`)
  }

  getPaymentMethods() {
    return this.http.get<any>(`${this.baseUrl}/payment-methods`)
  }

  getDocumentTypes() {
    return this.http.get<any>(`${this.baseUrl}/document-types`);
  }

  getBanks() {
    return this.http.get<any>(`${this.baseUrl}/banks`)
  }

}
