import { Component, inject } from '@angular/core';
import { HomeDataService } from '../../../core/services/home-services';
import { Instructor } from '../../../shared/interfaces/home';

@Component({
  selector: 'app-instructors',
  imports: [],
  templateUrl: './instructors.html',
  styleUrl: './instructors.css'
})
export class Instructors {
  private homeService = inject(HomeDataService);

  instructors:Instructor[] = [];

  constructor() {}

  ngOnInit() {
    this.homeService.getDataInstructors().subscribe((data) => {
      console.log(data);
      this.instructors = data;
    });
  }
}
