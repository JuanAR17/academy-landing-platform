import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { DataFQS, DataInstructors, DataPartners, DataSuccessStory, Services } from '../../shared/interfaces/home-interfaces';

@Injectable({
  providedIn: 'root'
})
export class HomeData {
  private localData = environment.localData;

  constructor(private http: HttpClient) {}

  getSuccessStories() {
    return this.http.get<DataSuccessStory[]>(`${this.localData}/success-stories.json`);
  }

  getDataInstructors() {
    return this.http.get<DataInstructors[]>(`${this.localData}/instructors.json`);
  }

  getDataFQS() {
    return this.http.get<DataFQS[]>(`${this.localData}/fqs.json`);
  }

  getDataPartners() {
    return this.http.get<DataPartners[]>(`${this.localData}/partners.json`);
  }

  getDataServices() {
    return this.http.get<Services[]>(`${this.localData}/services.json`);
  }
}
