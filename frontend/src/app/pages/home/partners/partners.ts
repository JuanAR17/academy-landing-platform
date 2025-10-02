import { Component, inject } from '@angular/core';
import { HomeData } from '../../../core/services/home-data';
import { DataPartners } from '../../../shared/interfaces/home-interfaces';

@Component({
  selector: 'app-partners',
  imports: [],
  templateUrl: './partners.html',
  styleUrl: './partners.css'
})
export class Partners {
  private homeService = inject(HomeData);

  partners: DataPartners[] = [];

  constructor() {}

  ngOnInit(){
    this.homeService.getDataPartners().subscribe((data) => {
      console.log(data);
      this.partners = data;
    });
  }
}
