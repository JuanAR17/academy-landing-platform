export interface Login {
  identifier: string,
  password: string
}

export interface AuthResponse{
  accessToken: string,
  tokenType: string,
  csrfToken: string,
  userId: string,
}