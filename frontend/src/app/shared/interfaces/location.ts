export interface Country {
  id: number;
  name: string;
  emoji: string;
  nationality: string;
  numericCode: string;
  phoneCode: string;
  currency: string;
  currencyName: string;
  currencySymbol: string;
}

export interface State {
  id: number;
  name: string;
}

export interface City {
  id: number;
  name: string;
}