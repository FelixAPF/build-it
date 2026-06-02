import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class WorkerService {
  private http = inject(HttpClient);

  // Gets the worker's current applications and schedule
  getDashboard(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/dashboard/worker`);
  }

  // Gets the open jobs that match the worker's CCQ trades
  getAvailableFeed(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/worker/feed`);
  }

  // Applies for a specific job requirement
  applyForJob(requirementId: number): Observable<string> {
    return this.http.post(`${API_URL}/applications/worker/${requirementId}/apply`, {}, { responseType: 'text' });
  }
}