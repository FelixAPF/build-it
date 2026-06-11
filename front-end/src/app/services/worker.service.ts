import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environment/environment';

const API_URL = `${environment.apiUrl}`;

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

  // --- NEW SETTINGS ENDPOINTS ---
  getProfile() {
    return this.http.get<any>(`${API_URL}/worker/settings/profile`);
  }

  updateSpecialties(specialties: string[]) {
    return this.http.put<any>(`${API_URL}/worker/settings/specialties`, { specialties });
  }

  deleteAccount() {
    return this.http.delete<any>(`${API_URL}/worker/settings/account`);
  }
}