import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

// Update this if you host your backend somewhere else later
const API_URL = 'http://localhost:8080/api/auth';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);

  login(credentials: any): Observable<any> {
    return this.http.post(`${API_URL}/login`, credentials).pipe(
      tap((response: any) => {
        if (response && response.token) {
          localStorage.setItem('jwt_token', response.token);
          localStorage.setItem('user_role', response.role);
          localStorage.setItem('user_email', response.email);
        }
      })
    );
  }

  registerWorker(data: any): Observable<any> {
    return this.http.post(`${API_URL}/register/worker`, data).pipe(
      tap((response: any) => this.saveToken(response))
    );
  }

  registerBusiness(data: any): Observable<any> {
    return this.http.post(`${API_URL}/register/business`, data).pipe(
      tap((response: any) => this.saveToken(response))
    );
  }

  logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_role');
    localStorage.removeItem('user_email');
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  private saveToken(response: any) {
    if (response && response.token) {
      localStorage.setItem('jwt_token', response.token);
      localStorage.setItem('user_role', response.role);
      localStorage.setItem('user_email', response.email);
    }
  }
}