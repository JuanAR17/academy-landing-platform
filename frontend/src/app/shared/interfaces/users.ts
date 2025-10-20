export interface Users{
  id: number,
  name: string,
  lastname: string,
  email: string,
  username: string,
  cellphone: number,
  country: string,
  nationality: string,
  documentType: string,
  documentNumber: number,
  address: {
    livingAddress: string,
    residenceCountry: string,
    department: string,
    city: string,
  },
  isAdmin: boolean,
  password: string,
  whereYouSeeUs: string,
}

export interface UsersLogin{
  emailUsername: string,
  password: string,
}

export interface DocumentTypes{
  id: number,
  type: string,
  value: string,
}