import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class BusinessService {
  private http = inject(HttpClient);

  getDashboard(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/dashboard/business`);
  }

  getTradeQuestions(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/trade-questions`);
  }

  createJobPosting(jobData: any): Observable<string> {
    return this.http.post(`${API_URL}/jobs`, jobData, { responseType: 'text' });
  }

  // --- NEW METHODS FOR HIRING ---
  getApplicants(requirementId: number): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/applications/business/requirements/${requirementId}/applications`);
  }

  approveWorker(applicationId: number): Observable<string> {
    return this.http.post(`${API_URL}/applications/business/${applicationId}/approve`, {}, { responseType: 'text' });
  }

  rejectWorker(applicationId: number): Observable<string> {
    return this.http.post(`${API_URL}/applications/business/${applicationId}/reject`, {}, { responseType: 'text' });
  }
}