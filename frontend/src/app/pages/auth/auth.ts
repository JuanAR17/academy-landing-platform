import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { StarBackground } from '../../shared/components/star-background/star-background';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [StarBackground, RouterLink],
  templateUrl: './auth.html',
  styleUrls: ['./auth.css']
})
export class Auth {

}

