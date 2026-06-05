import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080/api/admin';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);

  getPendingUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/users/pending`);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/users/all`);
  }

  getAuditLogs(): Observable<any[]> { // <-- NEW
    return this.http.get<any[]>(`${API_URL}/logs`);
  }

  verifyUser(userId: number): Observable<string> {
    return this.http.put(`${API_URL}/users/${userId}/verify`, {}, { responseType: 'text' });
  }

  impersonateUser(userId: number): Observable<any> {
    return this.http.post(`${API_URL}/users/${userId}/impersonate`, {});
  }

  getUserLogs(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/users/${userId}/logs`);
  }

  getDocumentAsBlob(documentUrl: string): Observable<Blob> {
    // We fetch it as a blob so we can inject the JWT securely
    return this.http.get(`http://localhost:8080${documentUrl}`, { responseType: 'blob' });
  }
}