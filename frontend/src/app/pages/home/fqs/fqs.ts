import { Component, inject } from '@angular/core';
import { HomeDataService } from '../../../core/services/home-services';
import { FQS } from '../../../shared/interfaces/home';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-fqs',
  imports: [CommonModule,],
  templateUrl: './fqs.html',
  styleUrl: './fqs.css'
})
export class Fqs {
  private homeService = inject(HomeDataService);

  fqs:FQS[] = [];
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
