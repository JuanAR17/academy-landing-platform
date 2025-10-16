import { Component, inject } from '@angular/core';
import { HomeData } from '../../../core/services/home-data';
import { DataFQS } from '../../../shared/interfaces/home-interfaces';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-fqs',
  imports: [CommonModule,],
  templateUrl: './fqs.html',
  styleUrl: './fqs.css'
})
export class Fqs {
  private homeService = inject(HomeData);

  fqs:DataFQS[] = [];
  wrapped: number | null  = null;

  constructor() {}

  ngOnInit() {
    this.homeService.getDataFQS().subscribe((data) => {
      console.log(data);
      this.fqs = data;
    });
  }

  wrapper(elementId:number){
    this.wrapped = elementId === this.wrapped ? null : elementId;
  }
}
