import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, Observable, switchMap, tap, throwError } from 'rxjs';
import { ApiEnvelope, JwtTokenResponse, LoginRequest, RegisterRequest, UserProfile } from '../models/api.models';

const ACCESS_TOKEN = 'introtech_access_token';
const REFRESH_TOKEN = 'introtech_refresh_token';
const EXPIRES_AT = 'introtech_expires_at';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly accessTokenState = signal<string | null>(localStorage.getItem(ACCESS_TOKEN));
  private readonly profileState = signal<UserProfile | null>(null);
  readonly profile = this.profileState.asReadonly();
  readonly isAuthenticated = computed(() => Boolean(this.accessTokenState()));

  constructor(private readonly http: HttpClient, private readonly router: Router) {}

  get accessToken(): string | null { return this.accessTokenState(); }

  login(request: LoginRequest): Observable<UserProfile> {
    return this.http.post<JwtTokenResponse>('/server/auth/login', request).pipe(
      tap(tokens => this.storeTokens(tokens)),
      switchMap(() => this.loadProfile()),
      catchError(error => throwError(() => this.authError(error)))
    );
  }

  register(request: RegisterRequest): Observable<void> {
    return this.http.post<ApiEnvelope<unknown>>('/server/payroll/register', request).pipe(
      map(response => {
        if (!response.status) throw new Error(response.message || 'Registration failed.');
      }),
      catchError(error => throwError(() => this.authError(error)))
    );
  }

  loadProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>('/server/payroll/api/profile').pipe(
      tap(profile => this.profileState.set(profile))
    );
  }

  updateProfile(input: Pick<UserProfile, 'firstName' | 'lastName' | 'phoneNumber'>): Observable<UserProfile> {
    return this.http.put<UserProfile>('/server/payroll/api/profile', input).pipe(
      tap(profile => this.profileState.set(profile))
    );
  }

  ensureProfile(): void {
    if (this.accessToken && !this.profileState()) {
      this.loadProfile().subscribe({ error: () => this.logout() });
    }
  }

  logout(redirect = true): void {
    localStorage.removeItem(ACCESS_TOKEN);
    localStorage.removeItem(REFRESH_TOKEN);
    localStorage.removeItem(EXPIRES_AT);
    this.accessTokenState.set(null);
    this.profileState.set(null);
    if (redirect) void this.router.navigate(['/login']);
  }

  private storeTokens(tokens: JwtTokenResponse): void {
    localStorage.setItem(ACCESS_TOKEN, tokens.accessToken);
    localStorage.setItem(REFRESH_TOKEN, tokens.refreshToken);
    localStorage.setItem(EXPIRES_AT, String(Date.now() + tokens.expiresIn * 1000));
    this.accessTokenState.set(tokens.accessToken);
  }

  private authError(error: unknown): Error {
    if (error instanceof Error && !(error instanceof HttpErrorResponse)) return error;
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string; detail?: string } | null;
      return new Error(body?.message || body?.detail || (error.status === 401
        ? 'Email or password is incorrect.'
        : 'We could not complete that request.'));
    }
    return new Error('We could not complete that request.');
  }
}
