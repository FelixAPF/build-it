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
          localStorage.setItem('user_status', response.status); // <-- ADD THIS LINE
        }
      })
    );
  }

  getBusinessProfile(): Observable<any> {
    return this.http.get<any>(`${API_URL}/business/profile`);
  }

registerWorker(data: any): Observable<any> {
    return this.http.post(`${API_URL}/register/worker`, data, { responseType: 'text' });
  }

uploadDocuments(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    
    // FIX: Bypassed the local API_URL constant to point exactly to the UserController
    return this.http.post('http://localhost:8080/api/users/upload-documents', formData, { responseType: 'text' });
  }

  registerBusiness(data: any): Observable<any> {
    return this.http.post(`${API_URL}/register/business`, data, { responseType: 'text' });
  }

  verifyEmail(token: string): Observable<string> {
    return this.http.get(`${API_URL}/verify-email?token=${encodeURIComponent(token)}`, { responseType: 'text' });
  }

logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_role');
    localStorage.removeItem('user_email');
    localStorage.removeItem('user_status');
    localStorage.removeItem('admin_token');
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

private saveToken(response: any) {
    if (response && response.token) {
      localStorage.setItem('jwt_token', response.token);
      localStorage.setItem('user_role', response.role);
      localStorage.setItem('user_email', response.email);
      localStorage.setItem('user_status', response.status); 
    }
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${API_URL}/forgot-password?email=${encodeURIComponent(email)}`, {}, { responseType: 'text' });
  }

  resetPassword(token: string, newPassword: string): Observable<string> {
    return this.http.post(`${API_URL}/reset-password?token=${encodeURIComponent(token)}&newPassword=${encodeURIComponent(newPassword)}`, {}, { responseType: 'text' });
  }

  // Fetches dynamic trades from the database for global use (Register, Dashboards)
getTrades(): Observable<any[]> {
    return this.http.get<any[]>('http://localhost:8080/api/trades');
  }
}