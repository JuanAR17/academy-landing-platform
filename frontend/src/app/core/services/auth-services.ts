import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Users } from '../../shared/interfaces/users';
import { AuthResponse } from '../../shared/interfaces/auth';
import { Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import * as CryptoJS from 'crypto-js'

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(private http: HttpClient, private router: Router){this.loadInitialState();}
  private baseUrl = `${environment.apiUrl}/auth`
  private currentUserCredentials = signal<AuthResponse | null>(null);
  private isAuthenticated = signal<boolean>(false);
  private ENCRYPTION_KEY = ''

    private encrypt(data: string): string {
    return CryptoJS.AES.encrypt(data, this.ENCRYPTION_KEY).toString();
  }

  private decrypt(encryptedData: string): string | null {
    try {
      const bytes = CryptoJS.AES.decrypt(encryptedData, this.ENCRYPTION_KEY);
      const decryptedData = bytes.toString(CryptoJS.enc.Utf8);
      
      if (!decryptedData) {
        throw new Error('El token descifrado estaba vacío.');
      }
      return decryptedData;
    } catch (error) {
      console.error('Error FATAL al descifrar el token. El token o la clave podrían ser incorrectos:', error);
      return null;
    }
  }

  private loadInitialState(): void {
    const encryptedAccessToken = localStorage.getItem('access_token_enc'); 
    const storedCsrfToken = localStorage.getItem('csrf_token'); 
    const storedUserId = localStorage.getItem('user_id');

    if (encryptedAccessToken && storedCsrfToken && storedUserId) {
        console.log('Tokens persistentes encontrados. Intentando rehidratar sesión...');
        
        const decryptedToken = this.decrypt(encryptedAccessToken);

        if (decryptedToken) {
            const savedTokenData: AuthResponse = {
                userId: storedUserId,
                accessToken: decryptedToken,
                tokenType: 'Bearer', 
                csrfToken: storedCsrfToken,
            } as AuthResponse; 

            this.handleAuthentication(savedTokenData, false); 
            console.log('Sesión rehidratada exitosamente desde almacenamiento cifrado (AES real).');
        } else {
            console.error('Fallo al descifrar el token. Eliminando credenciales.');
            this.logout();
        }

    } else {
        console.log('No hay tokens persistentes. Sesión inactiva.');
    }
  }

  private handleAuthentication(response: AuthResponse, persist: boolean = true): void {
    this.currentUserCredentials.set(response);
    this.isAuthenticated.set(true);

    if (persist) {
        const encryptedToken = this.encrypt(response.accessToken);
        
        localStorage.setItem('access_token_enc', encryptedToken);
        localStorage.setItem('csrf_token', response.csrfToken);
        localStorage.setItem('user_id', response.userId);

        console.log('Tokens y userId guardados en memoria del AuthService. Token cifrado AES y CSRF guardados en localStorage.');
    }
  }

  public getAccessToken(): string | null {
    return this.currentUserCredentials()?.accessToken ?? null;
  }

  public getCsrfToken(): string | null {
    return this.currentUserCredentials()?.csrfToken ?? null;
  }
  
  public getUserId(): string | null {
    return this.currentUserCredentials()?.userId ?? null;
  }

  postNewUser(newUser: Users): Observable<AuthResponse> {
    const registerUrl = `${this.baseUrl}/register`;
    
    return this.http.post<AuthResponse>(registerUrl, newUser).pipe(
      tap(response => {
        this.handleAuthentication(response);
      })
    );
  }

  logout(): void {
    this.currentUserCredentials.set(null);
    this.isAuthenticated.set(false);
    
    localStorage.removeItem('access_token_enc');
    localStorage.removeItem('csrf_token');
    localStorage.removeItem('user_id');
    
    this.router.navigate(['/login']); 
  }

}
