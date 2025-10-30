export interface Users{
  firstName: string,
  lastName: string,
  correo: string,
  username: string,
  phone: string,
  nationality: string,
  address: {
    address: string,
    country: string,
    state: string,
    city: string,
  },
  password: string,
  role: string,
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