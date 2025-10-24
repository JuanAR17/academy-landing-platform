import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';

export interface LeadPayload {
  email: string;
  name: string;
  courses: string[]; // (usa los nombres que espera FastAPI)
}

@Injectable({ providedIn: 'root' })
export class LeadService {
  constructor(private http: HttpClient) {}
  submitLead(payload: LeadPayload) {
    return this.http.post(`${environment.apiUrl}/ingest`, payload);
  }
}

