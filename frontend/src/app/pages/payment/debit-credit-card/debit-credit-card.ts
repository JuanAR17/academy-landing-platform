import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Payment } from '../payment';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-debit-credit-card',
  imports: [FormsModule, ReactiveFormsModule, CommonModule],
  templateUrl: './debit-credit-card.html',
  styleUrl: './debit-credit-card.css'
})
export class DebitCreditCard implements OnInit{
  private fb = inject(FormBuilder);
  private parentComponent = inject(Payment);
  
  cardForm!: FormGroup;

  startNumber = 1;
  arrayForLoop: number[] = Array.from({ length: 12 }, (_, i) => i + this.startNumber);

  ngOnInit(): void {
    this.cardForm = this.fb.group({
      cardNumber: [null, [Validators.required, Validators.pattern(/^\d{16}$/)]],
      cardHolderName: [null, Validators.required],
      expiryDate: [null, [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/\d{2}$/)]],
      cvc: [null, [Validators.required, Validators.pattern(/^\d{3,4}$/)]], 
      dues: [null, Validators.required],
    });

    setTimeout(() => {
        this.parentComponent.setPaymentForm(this.cardForm);
    }, 0); 
  }
}
