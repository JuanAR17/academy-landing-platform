import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { DocumentTypes } from '../../shared/interfaces/users';
import { Country, State} from '../../shared/interfaces/location';

@Injectable({
  providedIn: 'root'
})
export class UserServices {
  private localData = environment.localData;
  private apiBase = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getAllCountrys(){
    return this.http.get<[Country]>(`${this.apiBase}/locations/countries`);
  }

  getAllStates(countryId: number){
    return this.http.get<[State]>(`${this.apiBase}/locations/states/countries/${countryId}`)
  }

  getAllCities(stateId: number){
    return this.http.get<[State]>(`${this.apiBase}/locations/cities/states/${stateId}`)
  }
}
