import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ApiResponse,
  JwtPayload,
  LoginRequest,
  LoginResponseData,
  RegisterRequest,
} from '../models/auth.model';

const TOKEN_KEY = 'carvajal_wishlist_jwt';

@Injectable({ providedIn: 'root' })
export class AuthService {
  /** Señal reactiva con el token actual (o null si no hay sesión) */
  private tokenSignal = signal<string | null>(localStorage.getItem(TOKEN_KEY));

  readonly isAuthenticated = computed(() => !!this.tokenSignal());
  readonly currentUser = computed(() => this.decodeToken(this.tokenSignal()));

  constructor(private http: HttpClient) {}

  login(payload: LoginRequest): Observable<ApiResponse<LoginResponseData>> {
    return this.http
      .post<ApiResponse<LoginResponseData>>(`${environment.authApiUrl}/auth/login`, payload)
      .pipe(
        tap((res) => {
          if (res.data?.jwt) {
            this.setToken(res.data.jwt);
          }
        }),
      );
  }

  register(payload: RegisterRequest) {
    return this.http.post<ApiResponse<unknown>>(`${environment.authApiUrl}/auth/register`, payload);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.tokenSignal.set(null);
  }

  getToken(): string | null {
    return this.tokenSignal();
  }

  private setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
    this.tokenSignal.set(token);
  }

  /** Decodifica el payload del JWT (solo lectura en el cliente, sin validar firma) */
  private decodeToken(token: string | null): JwtPayload | null {
    if (!token) return null;
    try {
      const payloadBase64 = token.split('.')[1];
      const decoded = atob(payloadBase64.replace(/-/g, '+').replace(/_/g, '/'));
      return JSON.parse(decoded) as JwtPayload;
    } catch {
      return null;
    }
  }
}
