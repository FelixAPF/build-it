import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080/api/admin';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);

  baseUrl: string = 'http://localhost:8080/api';
  adminUrl: string = `${this.baseUrl}/admin`;
  tradeQuestionUrl: string = `${this.baseUrl}/trade-questions`

  getPendingUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminUrl}/users/pending`);
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminUrl}/users/all`);
  }

  getAuditLogs(): Observable<any[]> { // <-- NEW
    return this.http.get<any[]>(`${this.adminUrl}/logs`);
  }

  verifyUser(userId: number): Observable<string> {
    return this.http.put(`${this.adminUrl}/users/${userId}/verify`, {}, { responseType: 'text' });
  }

  impersonateUser(userId: number): Observable<any> {
    return this.http.post(`${this.adminUrl}/users/${userId}/impersonate`, {});
  }

  getUserLogs(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.adminUrl}/users/${userId}/logs`);
  }

  getDocumentAsBlob(documentUrl: string): Observable<Blob> {
    // We fetch it as a blob so we can inject the JWT securely
    return this.http.get(`http://localhost:8080${documentUrl}`, { responseType: 'blob' });
  }

  getTradeQuestions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.tradeQuestionUrl}`);
  }
  addTradeQuestion(data: any): Observable<any> {
    return this.http.post<any>(`${this.adminUrl}/trade-questions`, data);
  }
  deleteTradeQuestion(id: number): Observable<any> {
    return this.http.delete<any>(`${this.adminUrl}/trade-questions/${id}`);
  }

  addTrade(trade: any): Observable<any> {
    return this.http.post<any>(`${this.adminUrl}/trades`, trade);
  }

  deleteTrade(id: number): Observable<any> {
    return this.http.delete<any>(`${this.adminUrl}/trades/${id}`);
  }
  getMetrics(): Observable<any> {
    return this.http.get<any>(`${this.adminUrl}/metrics`);
  }
  suspendUser(userId: number): Observable<any> {
    return this.http.put<any>(`${this.adminUrl}/users/${userId}/suspend`, {});
  }
}