import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { FQS, Instructor, Partner, SuccessStory, Service } from '../../shared/interfaces/home';

@Injectable({
  providedIn: 'root'
})
export class HomeDataService {
  private localData = environment.localData;

  constructor(private http: HttpClient) {}

  getSuccessStories() {
    return this.http.get<SuccessStory[]>(`${this.localData}/success-stories.json`);
  }

  getDataInstructors() {
    return this.http.get<Instructor[]>(`${this.localData}/instructors.json`);
  }

  getDataFQS() {
    return this.http.get<FQS[]>(`${this.localData}/fqs.json`);
  }

  getDataPartners() {
    return this.http.get<Partner[]>(`${this.localData}/partners.json`);
  }

  getDataServices() {
    return this.http.get<Service[]>(`${this.localData}/services.json`);
  }
}
