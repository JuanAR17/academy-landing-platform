import { Component, inject } from '@angular/core';
import { TiltCardDirective } from '../../../shared/directives/tilt-card-directive';
import { CommonModule } from '@angular/common';
import { Service } from '../../../shared/interfaces/home';
import { HomeDataService } from '../../../core/services/home-services';


@Component({
  selector: 'app-diplomado-masterclass',
  imports: [TiltCardDirective, CommonModule],
  templateUrl: './diplomado-masterclass.html',
  styleUrl: './diplomado-masterclass.css'
})
export class DiplomadoMasterclass {
  private homeService = inject(HomeDataService)

  services:Service[] =[]

  constructor() {}

  ngOnInit() {
    this.homeService.getDataServices().subscribe((data) => {
      console.log(data);
      this.services = data;
    });
  }
}

