import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Daviplata } from './daviplata';

describe('Daviplata', () => {
  let component: Daviplata;
  let fixture: ComponentFixture<Daviplata>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Daviplata]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Daviplata);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
