import { AfterViewInit, Component, computed, ElementRef, inject, OnInit, QueryList, signal, ViewChildren } from '@angular/core';
import { NavBar } from "../../shared/components/nav-bar/nav-bar";
import { DocumentTypes, Users } from '../../shared/interfaces/users';
import { UserServices } from '../../core/services/userServices';
import { City, Country, State } from '../../shared/interfaces/location';
import { FormsModule } from '@angular/forms';
import { Epayco } from '../../core/services/epaycoService';
import { Banks } from '../../shared/interfaces/payment';

@Component({
  selector: 'app-payment',
  imports: [NavBar, FormsModule],
  templateUrl: './payment.html',
  styleUrl: './payment.css'
})
export class Payment implements OnInit, AfterViewInit{
  private userService = inject(UserServices)
  private epaycoService = inject(Epayco)
  isDropdownOpen = signal(false);
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
  })

  currentResidenceCountry = signal< Country | null >(null);
  currentResidenceCountryId = computed(() => {
    return this.currentResidenceCountry()?.id
  })

  currentResidenceState = signal< State | null >(null);
  currentResidenceStateId = computed(() => {
    return this.currentResidenceState()?.id
  })

  currentResidenceCity = signal< City | null >(null);

  @ViewChildren('paymentButton')
  private paymentButtons!: QueryList<ElementRef<HTMLButtonElement>>;

  @ViewChildren('paymentView')
  private paymentViews!: QueryList<ElementRef<HTMLElement>>;

  ngOnInit(){
    this.getDocumentTypesService();
    this.getCountrysService();
    this.tokenLogin();
  }

  ngAfterViewInit() {
    const defaultButton = this.paymentButtons.find(b => b.nativeElement.getAttribute('data-view') === 'view-pse');

    if (defaultButton) {
      defaultButton.nativeElement.classList.add('active-button-style');
    }
  }

  getDocumentTypesService(){
    this.userService.getDocumentTypes().subscribe( (data) => {
      console.log("DocumentTypes: ", data);
      this.documentTypes.set(data)
    })
  }

  getCountrysService(){
    this.userService.getAllCountrys().subscribe( (data) => {
      this.countrysArray.set(data)
      console.log("Countrys", this.countrysArray())
    })
  }

  getStatesService(country_id: number | null){
    if(country_id === null){
      console.log("el id es null")
    }else{
      this.userService.getAllStates(country_id).subscribe( (data) => {
        this.statesArray.set(data)
        console.log(data)
    })
    }
  }

  getCitysService(state_id: number | null){
    if(state_id === null){
      console.log("el id es null")
    }else{
      this.userService.getAllCities(state_id).subscribe( (data) => {
        this.citiesArray.set(data)
        console.log(data)
    })
    }
  }

  toggleDropdown(): void {
    this.isDropdownOpen.update(current => !current);
  }

  selectType(type: DocumentTypes | null): void {
    this.selectedDocumentType.set(type);
    this.isDropdownOpen.set(false);
  }

  showView(viewId: string, event: Event){
    event.preventDefault();

    this.paymentViews.forEach(view => {
        view.nativeElement.classList.remove('active');
    });

    const targetView = this.paymentViews.find(view => view.nativeElement.id === viewId);
    if (targetView) {
        targetView.nativeElement.classList.add('active');
    }

    this.removeActiveClassFromButtons();
    (event.currentTarget as HTMLElement).classList.add('active-button-style');
  }


  removeActiveClassFromButtons(): void {
    this.paymentButtons.forEach(btn => {
        btn.nativeElement.classList.remove('active-button-style');
    });
  }

  // ------------------------------- epayco -----------------------------------------

  banksArray = signal< Banks[] | null>([]);

  tokenLogin(){
    this.epaycoService.getTokenLogin().subscribe( (data) => {
      console.log(data)
    })
  }

  getBanks(){
    this.epaycoService.getAllBanks().subscribe( (data) => {
      this.banksArray.set(data)
      console.log("Bancos", data)
    })
  }







}
 