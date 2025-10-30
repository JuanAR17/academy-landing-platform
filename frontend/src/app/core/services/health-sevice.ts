import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class HealthService{
  constructor(private http: HttpClient){}
  private baseUrl = `${environment.apiUrl}`

  getIpUser(){
    return this.http.get<any>(`${this.baseUrl}/ip`)
  }
  
  
}