import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, throwError } from 'rxjs';
import { ApiEnvelope } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ApiClientService {
  private readonly baseUrl = '/server/payroll/api';

  constructor(private readonly http: HttpClient) {}

  get<T>(path: string, params?: Record<string, string | number | boolean | undefined>): Observable<T> {
    let httpParams = new HttpParams();
    Object.entries(params ?? {}).forEach(([key, value]) => {
      if (value !== undefined) httpParams = httpParams.set(key, String(value));
    });
    return this.http.get<T | ApiEnvelope<T>>(`${this.baseUrl}${path}`, { params: httpParams }).pipe(
      map(response => this.unwrap(response)),
      catchError(error => throwError(() => this.toError(error)))
    );
  }

  post<T>(path: string, body: unknown = {}): Observable<T> {
    return this.http.post<T | ApiEnvelope<T>>(`${this.baseUrl}${path}`, body).pipe(
      map(response => this.unwrap(response)),
      catchError(error => throwError(() => this.toError(error)))
    );
  }

  put<T>(path: string, body: unknown): Observable<T> {
    return this.http.put<T | ApiEnvelope<T>>(`${this.baseUrl}${path}`, body).pipe(
      map(response => this.unwrap(response)),
      catchError(error => throwError(() => this.toError(error)))
    );
  }

  patch<T>(path: string, body: unknown = {}): Observable<T> {
    return this.http.patch<T | ApiEnvelope<T>>(`${this.baseUrl}${path}`, body).pipe(
      map(response => this.unwrap(response)),
      catchError(error => throwError(() => this.toError(error)))
    );
  }

  delete<T>(path: string): Observable<T> {
    return this.http.delete<T | ApiEnvelope<T>>(`${this.baseUrl}${path}`).pipe(
      map(response => this.unwrap(response)),
      catchError(error => throwError(() => this.toError(error)))
    );
  }

  private unwrap<T>(response: T | ApiEnvelope<T>): T {
    if (response && typeof response === 'object' && 'status' in response && 'message' in response) {
      const envelope = response as ApiEnvelope<T>;
      if (!envelope.status) throw new Error(envelope.message || 'The request could not be completed.');
      return envelope.data as T;
    }
    return response as T;
  }

  private toError(error: unknown): Error {
    if (error instanceof Error && !(error instanceof HttpErrorResponse)) return error;
    if (error instanceof HttpErrorResponse) {
      const body = error.error as { message?: string; detail?: string; error?: string } | string | null;
      if (typeof body === 'string' && body.trim()) return new Error(body);
      const details = typeof body === 'object' ? body : null;
      const message = details?.message || details?.detail || details?.error;
      return new Error(message || (error.status === 0 ? 'Unable to reach the server.' : 'Something went wrong.'));
    }
    return new Error('Something went wrong.');
  }
}
