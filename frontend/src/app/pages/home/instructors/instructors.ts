import { Component, inject } from '@angular/core';
import { HomeData } from '../../../core/services/home-data';
import { DataInstructors } from '../../../shared/interfaces/home-interfaces';

@Component({
  selector: 'app-instructors',
  imports: [],
  templateUrl: './instructors.html',
  styleUrl: './instructors.css'
})
export class Instructors {
  private homeService = inject(HomeData);

  instructors:DataInstructors[] = [];

  constructor() {}

  ngOnInit() {
    this.homeService.getDataInstructors().subscribe((data) => {
      console.log(data);
      this.instructors = data;
    });
  }
}
