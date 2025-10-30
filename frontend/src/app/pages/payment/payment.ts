import { Component, computed, effect, inject, OnDestroy, OnInit, signal, WritableSignal } from '@angular/core';
import { NavBar } from "../../shared/components/nav-bar/nav-bar";
import { DocumentTypes, Users } from '../../shared/interfaces/users';
import { UserServices } from '../../core/services/user-services';
import { Country, State } from '../../shared/interfaces/location';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { EpaycoService } from '../../core/services/epayco-service';
import { CommonModule } from '@angular/common';
import { PaymentService } from '../../core/services/payment-services';
import { filter, from, Subscription } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { passwordMatchValidator, passwordStrengthValidator } from '../../shared/validators/password-validator';
import { AuthService } from '../../core/services/auth-services';
import { CardPaymentRespose, DebitCreditCardPayment } from '../../shared/interfaces/payment';

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
export class Payment implements OnInit, OnDestroy{
  private _userService = inject(UserServices)
  private _epaycoService = inject(EpaycoService)
  private _paymentService = inject(PaymentService)
  private _authService = inject(AuthService)
  private _router = inject(Router)
  private fb = inject(FormBuilder)

  documentTypes = signal<DocumentTypes[] | null>([]);

  selectedDocumentType = signal<DocumentTypes | null>(null);
  selectedDocumentTypeComputed = computed(() => {
    if (!this.selectedDocumentType()) {return "Seleccionar";} return this.selectedDocumentType()?.name;
  });

  countrysArray = signal< Country[] | null>([]);

  statesArray = signal< State[] | null>([]);
  citiesArray = signal< State[] | null>([]);

  currentCountry = signal< Country | null >(null);
  currentNationality = computed(() => {
     if (!this.currentCountry()) {return "";} return this.currentCountry()?.nationality;
  });

  activePaymentId: string | null = null;

  textsVibecodingArray = signal<any>([]);

  currentRoute: string = '';
  
  countryTouched = false;

  private navigationEvents = toSignal(this._router.events.pipe(filter((event):event is NavigationEnd => event instanceof NavigationEnd)));

  childPaymentForm: WritableSignal<FormGroup | null> = signal(null);

  registerPaymentForm = this.fb.group({
    firstName: [null, Validators.required],
    lastName: [null, Validators.required],
    email: [null, [Validators.required, Validators.email]],
    username: [null, Validators.required],
    phone: [null, [Validators.required, Validators.maxLength(10), Validators.minLength(10)]],
    cellphone: this.fb.group({
      extension : ['+57', Validators.required],
      phoneNumber: [null, [Validators.required, Validators.maxLength(10), Validators.minLength(10)]]
    }),
    nationality: null,
    documentType: [null, Validators.required],
    documentNumber: [null, Validators.required],
    address: this.fb.group({
      street: [null, Validators.required],
      country: [null, Validators.required],
      state: [null, Validators.required],
      city: [null, Validators.required],
    }),
    role: "STUDENT",
    password: [null, [Validators.required, Validators.minLength(8), Validators.maxLength(72), passwordStrengthValidator]],
    confirmPassword: [null, Validators.required],
    howDidYouFindUs: [null, Validators.required],
    isAdmin: false,
    isSuperAdmin: false,
    paymentMethod: [null, Validators.required],
    paymentDetails : new FormControl(null),
  }, {validators: passwordMatchValidator});

  private formSubscription = new Subscription();

  constructor(){
    effect( () => {
      const event = this.navigationEvents();

      if(event){
        this.currentRoute = event.urlAfterRedirects;
        console.log(this.currentRoute)
      }

      const selectedCountry = this.currentCountry();
        const nationalityControl = this.registerPaymentForm.get('nationality') as FormControl<string | null>;
        
        if (selectedCountry && selectedCountry.nationality) {

            nationalityControl.setValue(selectedCountry.nationality, { emitEvent: false });
            nationalityControl.markAsDirty();
        } else {
            nationalityControl.setValue(null, { emitEvent: false });
        }

    })
  }

  ngOnInit(){
    this.currentRoute = this._router.url;
    this.getDocumentTypesService();
    this.getCountrysService();
    this.getArrayTextsVibecoding();

    this.setupCascadingLocation();
  }

  ngOnDestroy(): void {
    this.formSubscription.unsubscribe();
  }

  getDocumentTypesService(){
    this._epaycoService.getDocumentTypes().subscribe( (data) => {
      this.documentTypes.set(data.data)
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
    const paymentControl = this.registerPaymentForm.get('paymentMethod') as FormControl<string | null>;
    paymentControl.patchValue(id)
    this.activePaymentId = id;
  }

  setPaymentForm(form: FormGroup): void {
    this.childPaymentForm.set(form);
  }

  getArrayTextsVibecoding(){
    this._paymentService.getVibecodingTextsArray().subscribe( (data) => {
      this.textsVibecodingArray.set(data)
    })
  }

  onCountrySelectTouch(): void {
    this.countryTouched = true;
  }

  setupCascadingLocation(): void {
    const addressGroup = this.registerPaymentForm.get('address') as FormGroup;
    
    const countrySub = addressGroup.get('country')?.valueChanges.subscribe((selectedCountry: any) => {
      addressGroup.get('state')?.reset(null, { emitEvent: false }); 
      addressGroup.get('city')?.reset(null, { emitEvent: false });
      this.statesArray.set([]); 
      this.citiesArray.set([]); 
      
      if (selectedCountry && selectedCountry.id) {
        this.getStatesService(selectedCountry.id);
      }
    });

    const stateSub = addressGroup.get('state')?.valueChanges.subscribe((selectedState: any) => {
      addressGroup.get('city')?.reset(null, { emitEvent: false }); 
    
      if (selectedState && selectedState.id) {
        this.getCitysService(selectedState.id);
      }
    });

    if (countrySub) this.formSubscription.add(countrySub);
    if (stateSub) this.formSubscription.add(stateSub);
  }

  onSubmit(){
    if (this.registerPaymentForm.invalid) {
      this.registerPaymentForm.markAllAsTouched();
      this.childPaymentForm()?.markAllAsTouched();
      console.log('Formulario Inválido. Revise los campos.');
      console.log('Datos del formulario hasta ahora:', this.registerPaymentForm.getRawValue());
      return;
    }
    
    const formValue = this.registerPaymentForm.getRawValue();
    console.log('VALOR DEL CAMPO paymentDetails:', formValue.paymentDetails);
    
    const newUser: Users = {
      firstName: formValue.firstName! as string,
      lastName: formValue.lastName! as string,
      correo: formValue.email! as string,
      username: formValue.username! as string,
      phone: (formValue.cellphone?.phoneNumber!) as string,
      nationality: formValue.nationality! as string,
      address: {
        address: formValue.address?.street! as string,
        country: (formValue.address?.country as any)?.name! as string,
        state: (formValue.address?.state as any)?.name! as string,
        city: (formValue.address?.city as any)?.name! as string,
      },
      password: formValue.password! as string,
      role: formValue.role! as string,
      howDidYouFindUs: formValue.howDidYouFindUs! as string,
      isAdmin: formValue.isAdmin! as boolean,
      isSuperAdmin: formValue.isSuperAdmin! as boolean
    };

    console.log('Registro de nuevo usuario = ', newUser);

    this._authService.postNewUser(newUser).subscribe({
      next: (response) => {

        if (this.activePaymentId === 'cards' && this.childPaymentForm()) {
          const cardFormValue = this.childPaymentForm()?.value;
          
          const [cardExpMonth, cardExpYear] = cardFormValue.expiryDate!.split('/');
          const fullCardExpYear = `20${cardExpYear}`;

          const paymentData: DebitCreditCardPayment = {
              value: '20000.00',
              currency: 'COP',
              testMode: false,
              docType: formValue.documentType!, 
              docNumber: (formValue.documentNumber! as string).toString(), 
              name: formValue.firstName! as string,
              lastName: formValue.lastName! as string,
              email: formValue.email! as string,
              cellPhone: formValue.cellphone?.phoneNumber! as string,
              phone: formValue?.phone! as string, 
              address: formValue.address?.street! as string,
              country: 'CO',
              city: (formValue.address?.city as any)?.name! as string,
              cardNumber: (cardFormValue.cardNumber! as string).toString(),
              cardExpYear: fullCardExpYear, 
              cardExpMonth: cardExpMonth!,
              cardCvc: (cardFormValue.cvc! as string).toString(),
              dues: cardFormValue.dues?.toString()!,
              ip: '127.0.0.1'
          };

          console.log('----------------------------------------------------');
          console.log('DATOS DEL PAGO CON TARJETA A ENVIAR:');

          this._epaycoService.paymentCard(paymentData).subscribe({
            next: (paymentResponse: CardPaymentRespose) => {
              console.log('Pago exitoso! Respuesta del servicio de pago:', paymentResponse);
              
              const transactionStatus = paymentResponse.data.transaction.data.estado;
              const transactionReceipt = paymentResponse.data.transaction.data.recibo;
              const cardToken = paymentResponse.data.tokenCard.cardTokenId;
              
              if (paymentResponse.success && transactionStatus === 'Aceptada') {
                 alert('Usuario registrado y pago aceptado! Recibo: ' + transactionReceipt);
              } else {
                 alert('Usuario registrado, pero el pago tiene estado: ' + transactionStatus);
              }
            },
            error: (paymentError) => {
              console.error('Ocurrió un error al procesar el pago:', paymentError);
              alert('Ocurrió un error al procesar el pago.');
            }
          });

        } else {
            console.log('El método de pago seleccionado no es tarjeta de débito/crédito o el formulario de tarjeta no fue inicializado.');
        }

      },
      error: (error) => {
        console.log('Ocurrio un error al resgistrar el usuario = ', error)
      }
    })
  }
}
