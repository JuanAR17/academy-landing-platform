import { Component } from '@angular/core';

@Component({
  selector: 'app-debit-credit-card',
  imports: [],
  templateUrl: './debit-credit-card.html',
  styleUrl: './debit-credit-card.css'
})
export class DebitCreditCard {
  startNumber = 2;
  arrayForLoop: number[] = Array.from({ length: 10 }, (_, i) => i + this.startNumber);
}
