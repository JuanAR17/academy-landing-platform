export interface Login {
  identifier: string,
  password: string
}

export interface LoginResponse{
  accessToken: string,
  tokenType: string,
  csrfToken: string,
  userId: string,
}