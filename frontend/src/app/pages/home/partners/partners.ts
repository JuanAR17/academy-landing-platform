import { Component, inject } from '@angular/core';
import { HomeDataService } from '../../../core/services/home-services';
import { Partner } from '../../../shared/interfaces/home';

@Component({
  selector: 'app-partners',
  imports: [],
  templateUrl: './partners.html',
  styleUrl: './partners.css'
})
export class Partners {
  private homeService = inject(HomeDataService);

  partners: Partner[] = [];

  constructor() {}

  ngOnInit(){
    this.homeService.getDataPartners().subscribe((data) => {
      console.log(data);
      this.partners = data;
    });
  }
}
