import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { DebitCreditCardPayment, EpaycoToken } from '../../shared/interfaces/payment';

@Injectable({
  providedIn: 'root'
})
export class EpaycoService {
  constructor(private http: HttpClient) {}
  private baseUrl = `${environment.apiUrl}/epayco`

  getToken() {
    return this.http.get<EpaycoToken>(`${this.baseUrl}/token`)
  }

  saveToken(token: string): void {
    localStorage.setItem('epayco_jwt_token', token);
    console.log('Token guardado en Local Storage.');
  }

  paymentCard(data: DebitCreditCardPayment) {
    return this.http.post<any>(`${this.baseUrl}/payment`, data)
  }

  generatePaymentUrl() {
    return this.http.get<any>(`${this.baseUrl}/charge`)
  }

  getPaymentMethods() {
    return this.http.get<any>(`${this.baseUrl}/payment-methods`)
  }

  getBanks() {
    return this.http.get<any>(`${this.baseUrl}/banks`)
  }

}
