import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { StarBackground } from '../../shared/components/star-background/star-background';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [RouterOutlet, StarBackground, RouterLink],
  templateUrl: './auth.html',
  styleUrls: ['./auth.css']
})
export class Auth {

}

