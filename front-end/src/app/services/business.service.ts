import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environment/environment';

const API_URL = `${environment.apiUrl}`;

@Injectable({
  providedIn: 'root'
})
export class BusinessService {
  private http = inject(HttpClient);

  getDashboard(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/dashboard/business`);
  }
  getSingleJob(jobPostingId: number){
    return this.http.get<any>(`${API_URL}/dashboard/business/${jobPostingId}`);
  }

  getTradeQuestions(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/trade-questions`);
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
createJobPosting(jobData: any, frontendUrl: string): Observable<any> {
  const encodedUrl = encodeURIComponent(frontendUrl);
  return this.http.post(`${API_URL}/jobs?frontendUrl=${encodedUrl}`, jobData);
}
  // New endpoint to confirm payment
  confirmPayment(jobId: number): Observable<any> {
    return this.http.post(`${API_URL}/payments/success/${jobId}`, {});
  }

  payForExistingJob(jobId: number, frontendUrl: string): Observable<any> {
      const encodedUrl = encodeURIComponent(frontendUrl);

    return this.http.post(`${API_URL}/jobs/${jobId}/pay?frontendUrl=${encodedUrl}`, {});
  }
}