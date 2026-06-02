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

  verifyUser(userId: number): Observable<string> {
    return this.http.put(`${API_URL}/users/${userId}/verify`, {}, { responseType: 'text' });
  }
}