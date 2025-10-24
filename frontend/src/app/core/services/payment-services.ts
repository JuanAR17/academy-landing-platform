import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private localData = environment.localData;

  constructor(private http: HttpClient) {}

  getVibecodingTextsArray(){
    return this.http.get<any>(`${this.localData}/vibecodingTexts.json`)
  }
}
