import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { StarBackground } from '../../shared/components/star-background/star-background';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [RouterOutlet, StarBackground],
  templateUrl: './auth.html',
  styleUrls: ['./auth.css']
})
export class Auth {

}

