import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Banks, EpaycoJwtToken} from '../../shared/interfaces/payment';
import { tap } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class Epayco {
  private epaycoUrl = environment.epaycoAPiUrl;

  constructor(private http: HttpClient) {}

  getTokenLogin(){
    const credentials = `${environment.PUBLIC_EPAYCO_KEY}:${environment.PRIVATE_EPAYCO_KEY}`;

    const encodedCredentials = btoa(credentials);

    const httpHeaders = new HttpHeaders({
      "Content-Type": 'application/json',
      'Authorization': `Basic ${encodedCredentials}`})

      return this.http.post<EpaycoJwtToken>(`${this.epaycoUrl}/login`, '', {headers: httpHeaders}).pipe(
        tap(response => {
          this.saveToken(response.token);
      })
    );
  }

  saveToken(token: string): void {
    localStorage.setItem('epayco_jwt_token', token);
    console.log('Token guardado en Local Storage.');
  }

  getAllBanks(){
    return this.http.get<any>(`${this.epaycoUrl}/banks`)
  }

}
