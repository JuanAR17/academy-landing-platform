import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DebitCreditCard } from './debit-credit-card';

describe('DebitCreditCard', () => {
  let component: DebitCreditCard;
  let fixture: ComponentFixture<DebitCreditCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DebitCreditCard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DebitCreditCard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
