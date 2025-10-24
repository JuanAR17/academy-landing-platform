import { Component, inject, OnInit, signal } from '@angular/core';
import { Banks } from '../../../shared/interfaces/payment';
import { EpaycoService } from '../../../core/services/epayco-service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pse',
  imports: [FormsModule],
  templateUrl: './pse.html',
  styleUrl: './pse.css'
})
export class Pse implements OnInit{
  private epaycoService = inject(EpaycoService)

  banksArray = signal< Banks[] | null>([]);

  ngOnInit(): void {
    if(localStorage.getItem('epayco_jwt_token')){
      this.getAllBanks()
    } else{
      console.log('No hay token guardado')
    }

  }

  getAllBanks(){
    this.epaycoService.getBanks().subscribe( ( data ) => {
      this.banksArray.set(data.data)
    }) 
  }


}
