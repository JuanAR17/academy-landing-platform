export interface DebitCreditCardPayment{
  value: string,
  docType: string,
  docNumber: string,
  name: string,
  lastName: string,
  email: string,
  cellPhone: string,
  phone: string,
  address: string,
  country: string,
  city: string,
  currency: string,
  cardNumber: string,
  cardExpYear: string,
  cardExpMonth: string,
  cardCvc: string,
  dues: string,
  testMode: boolean,
  ip: string
}

export interface CardPaymentRespose{
  success: boolean,
  titleResponse: string,
  data: {
    transaction: {
      status: boolean,
      object: string,
      data: {
        ref_payco: number,
        factura: string,
        valor: number,
        estado: string,
        respuesta: string,
        recibo: string
      }
    },
    tokenCard: {
      email: string,
      cardTokenId: string
    }
  }
}

export interface PsePayment{
  bankName: string,
  value: number,
  docType: string,
  docNumber: number,
  name: string,
  lastName: string,
  email: string,
  cellphone: number,
  ip: string,
  urlResponse: string, 
  tax: number,
  taxBase: number,
  description: string,
  invoice: number,
  currency: string,
  typePerson: number,
  address: string,
  urlConfirmation: string,
  methodConfimation: string,
  extra1: string,
  extra2: string,
  extra3: string,
}

export interface daviplataPayment{
  docType: string,
  document: number,
  name: string,
  lastName:string,
  email: string,
  indCountry: string,
  phone: number,
  country: string,
  city: string,
  address: string,
  ip: number,
  currency: string,
  description: string,
  value: number,
  tax: number,
  taxBase: number,
  methodConfirmation: string,
}

export interface NequiPayment{
  nequiNumber: number,
}

export interface responsePayment{
  channel: string,
  value: number,
  refEpayco: string,
  currentStatusTransaction: string,
  statusTransaction: string,
  authorizationCode: number,
}

export interface Banks{
  id: number,
  name: string,
}

export interface EpaycoToken{
  token: string
}
