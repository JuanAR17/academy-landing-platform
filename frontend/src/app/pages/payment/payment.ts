import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { NavBar } from "../../shared/components/nav-bar/nav-bar";
import { DocumentTypes } from '../../shared/interfaces/users';
import { UserServices } from '../../core/services/user-services';
import { City, Country, State } from '../../shared/interfaces/location';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { EpaycoService } from '../../core/services/epayco-service';
import { CommonModule } from '@angular/common';
import { PaymentService } from '../../core/services/payment-services';
import { filter } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-payment',
  imports: [
    CommonModule, 
    NavBar, 
    FormsModule, 
    RouterOutlet, 
    RouterLink,
    ReactiveFormsModule  
  ],
  templateUrl: './payment.html',
  styleUrl: './payment.css'
})
export class Payment implements OnInit{
  private _userService = inject(UserServices)
  private _epaycoService = inject(EpaycoService)
  private _paymentService = inject(PaymentService)
  private _router = inject(Router)
  private fb = inject(FormBuilder)

  documentTypes = signal<DocumentTypes[] | null>([]);

  selectedDocumentType = signal<DocumentTypes | null>(null);
  selectedDocumentTypeComputed = computed(() => {
    if (!this.selectedDocumentType()) {return "Seleccionar";} return this.selectedDocumentType()?.type;
  });

  countrysArray = signal< Country[] | null>([]);
  statesArray = signal< State[] | null>([]);
  citiesArray = signal< State[] | null>([]);

  currentCountry = signal< Country | null >(null);
  currentNationality = computed(() => {
     if (!this.currentCountry()) {return "";} return this.currentCountry()?.nationality;
  });

  currentResidenceCountry = signal< Country | null >(null);
  currentResidenceCountryId = computed(() => {
    return this.currentResidenceCountry()?.id
  });

  currentResidenceState = signal< State | null >(null);
  currentResidenceStateId = computed(() => {
    return this.currentResidenceState()?.id
  });

  currentResidenceCity = signal< City | null >(null);

  activePaymentId: string | null = null;

  textsVibecodingArray = signal<any>([]);

  currentRoute: string = '';

  private navigationEvents = toSignal(this._router.events.pipe(filter((event):event is NavigationEnd => event instanceof NavigationEnd)));

  registerForm = this.fb.group({
    firstName: ['', Validators.required],
    lastname: ['', Validators.required],
    email: ['', Validators.required],
    username: ['', Validators.required],
    phone: [0, Validators.required],
    nationality: ['', Validators.required],
    documentType: ['', Validators.required],
    documentNumber: [0, Validators.required],
    address: this.fb.group({
      livingAddress: ['', Validators.required],
      Country: ['', Validators.required],
      state: ['', Validators.required],
      city: ['', Validators.required],
    }),
    password: ['', Validators.required],
    howDidYouFindUs: ['', Validators.required],
    isAdmin: [false, Validators.required],
    isSuperAdmin: [false, Validators.required]
  })

  constructor(){
    effect( () => {
      const event = this.navigationEvents();

      if(event){
        this.currentRoute = event.urlAfterRedirects;
        console.log('Nueva ruta detectada con effect():', this.currentRoute);
      }
    })
  }

  ngOnInit(){
    this.currentRoute = this._router.url;
    this.getDocumentTypesService();
    this.getCountrysService();
    this.getArrayTextsVibecoding();
  }

  getPaymentTemporalToken(){
    this._epaycoService.getToken().subscribe( ( data ) => {
      this._epaycoService.saveToken(data.token)
    })
  }

  getDocumentTypesService(){
    this._userService.getDocumentTypes().subscribe( (data) => {
      this.documentTypes.set(data)
    })
  }

  getCountrysService(){
    this._userService.getAllCountrys().subscribe( (data) => {
      this.countrysArray.set(data)
    })
  }

  getStatesService(country_id: number | null){
    if(country_id === null){
      console.log("el id es null")
    }else{
      this._userService.getAllStates(country_id).subscribe( (data) => {
        this.statesArray.set(data)
    })
    }
  }

  getCitysService(state_id: number | null){
    if(state_id === null){
      console.log("el id es null")
    }else{
      this._userService.getAllCities(state_id).subscribe( (data) => {
        this.citiesArray.set(data)
    })
    }
  }

  selectType(type: DocumentTypes | null): void {
    this.selectedDocumentType.set(type);
  }

  selectPayment(id: string): void {
    this.activePaymentId = id;
  }

  getArrayTextsVibecoding(){
    this._paymentService.getVibecodingTextsArray().subscribe( (data) => {
      this.textsVibecodingArray.set(data)
    })
  }

  onSubmit(){
    
  }

}
 