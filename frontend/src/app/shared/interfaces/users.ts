export interface Users{
  firstName: string,
  lastname: string,
  email: string,
  username: string,
  phone: number,
  nationality: string,
  documentType: string,
  documentNumber: number,
  address: {
    livingAddress: string,
    Country: string,
    state: string,
    city: string,
  },
  password: string,
  howDidYouFindUs: string,
  isAdmin: boolean,
  isSuperAdmin: boolean
}

export interface UsersLogin{
  emailUsername: string,
  password: string,
}

export interface DocumentTypes{
  id: number,
  name: string,
  cod: string,
  description: string,
}