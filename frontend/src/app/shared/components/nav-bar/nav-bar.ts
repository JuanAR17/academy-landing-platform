import { Component, HostListener } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-nav-bar',
  imports: [RouterLink],
  templateUrl: './nav-bar.html',
  styleUrl: './nav-bar.css'
})
export class NavBar {
  public scrolled: boolean = false;

  @HostListener('window:scroll', [])
  onWindowScroll () {
    this.scrolled = window.scrollY > 50;
  }
}

