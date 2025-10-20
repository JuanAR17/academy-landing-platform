export interface DebitCreditCardPayment{
  docType: string,
  docNumber: number,
  name: string,
  lastName: string,
  email: string,
  cellphone: number,
  cardNumber: number,
  cardExpYear: number,
  cardExpMonth: number,
  cardCvv: number,
  titularName: string,
  dues: number,
  value: number,
  _cardTokenId: string,
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

export interface EpaycoJwtToken{
  token: string
}