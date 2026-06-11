import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080/api/reviews';

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private http = inject(HttpClient);

  reviewWorker(applicationId: number, data: any): Observable<string> {
    return this.http.post(`${API_URL}/worker/${applicationId}`, data, { responseType: 'text' });
  }

  reviewBusiness(jobId: number, data: any): Observable<string> {
    return this.http.post(`${API_URL}/business/${jobId}`, data, { responseType: 'text' });
  }
}