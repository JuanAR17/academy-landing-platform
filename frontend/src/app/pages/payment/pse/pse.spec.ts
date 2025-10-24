import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Pse } from './pse';

describe('Pse', () => {
  let component: Pse;
  let fixture: ComponentFixture<Pse>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Pse]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Pse);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
