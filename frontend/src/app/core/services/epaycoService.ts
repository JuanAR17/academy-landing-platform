import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Banks } from '../../shared/interfaces/payment';

@Injectable({
  providedIn: 'root'
})
export class Epayco {
  private epaycoUrl = environment.epaycoAPiUrl;

  constructor(private http: HttpClient) {}

  getTokenLogin(){
    const httpHeaders = new HttpHeaders({
      "Content-Type": 'application/json',
      "Username": environment.PUBLIC_EPAYCO_KEY, 
      "Password": environment.PRIVATE_EPAYCO_KEY})

    return this.http.post<{}>(`${this.epaycoUrl}/login`, '', {headers: httpHeaders})
  }

  getAllBanks(){
    const httpHeaders = new HttpHeaders({
      'Content-Type': 'application/json',
    })
    return this.http.get<[Banks]>(`${this.epaycoUrl}/banks`, {headers: httpHeaders})
  }

}
