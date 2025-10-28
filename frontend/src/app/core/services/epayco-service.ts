import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { DebitCreditCardPayment, EpaycoToken } from '../../shared/interfaces/payment';
import { DocumentTypes } from '../../shared/interfaces/users';

@Injectable({
  providedIn: 'root'
})
export class EpaycoService {
  constructor(private http: HttpClient) {}
  private baseUrl = `${environment.apiUrl}/epayco`

  paymentCard(data: DebitCreditCardPayment) {
    return this.http.post<any>(`${this.baseUrl}/payment`, data)
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
