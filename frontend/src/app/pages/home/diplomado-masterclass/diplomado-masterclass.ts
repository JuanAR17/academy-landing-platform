import { Component, inject } from '@angular/core';
import { TiltCardDirective } from '../../../shared/directives/tilt-card-directive';
import { CommonModule } from '@angular/common';
import { Services } from '../../../shared/interfaces/home-interfaces';
import { HomeData } from '../../../core/services/home-data';


@Component({
  selector: 'app-diplomado-masterclass',
  imports: [TiltCardDirective, CommonModule],
  templateUrl: './diplomado-masterclass.html',
  styleUrl: './diplomado-masterclass.css'
})
export class DiplomadoMasterclass {
  private homeService = inject(HomeData)

  services:Services[] =[]

  constructor() {}

  ngOnInit() {
    this.homeService.getDataServices().subscribe((data) => {
      console.log(data);
      this.services = data;
    });
  }
}

