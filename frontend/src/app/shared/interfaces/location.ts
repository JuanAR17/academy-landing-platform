export interface Country {
  id: number;
  name: string;
  emoji: string;
  nationality: string;
  numeric_code: string;
  phoneCode: string;
  currency: string;
  currency_name: string;
  currency_symbol: string;
}

export interface State {
  id: number;
  name: string;
  country_code: string;
  country_id: number;
}

export interface City {
  id: number;
  name: string;
  country_id: number;
  state_id: number;
  country_code: string;
  state_code: string;
}