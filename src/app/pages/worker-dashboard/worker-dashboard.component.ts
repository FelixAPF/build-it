import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms'; // <-- ADDED ReactiveFormsModule
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { WorkerService } from '../../services/worker.service';
import { ReviewService } from '../../services/review.service'; // <-- ADDED ReviewService

// PrimeNG imports
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { TabViewModule } from 'primeng/tabview';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';   // <-- ADDED DialogModule
import { RatingModule } from 'primeng/rating';   // <-- ADDED RatingModule
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-worker-dashboard',
  standalone: true,
  imports: [
    CommonModule, DatePipe, TableModule, ButtonModule, TagModule, 
    TabViewModule, CardModule, ToastModule, DialogModule, RatingModule, // <-- ADDED PrimeNG modules
    ReactiveFormsModule // <-- ADDED ReactiveFormsModule
  ],
  providers: [MessageService],
  templateUrl: './worker-dashboard.component.html'
})
export class WorkerDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private workerService = inject(WorkerService);
  private reviewService = inject(ReviewService); // <-- INJECTED ReviewService
  private router = inject(Router);
  private fb = inject(FormBuilder);              // <-- INJECTED FormBuilder
  private messageService = inject(MessageService);

  mySchedule: any[] = [];
  availableJobs: any[] = [];
  isLoadingSchedule: boolean = true;
  isLoadingFeed: boolean = true;

  // --- NEW: Review State Management ---
  showReviewDialog: boolean = false;
  reviewForm!: FormGroup;
  selectedJobId: number | null = null;
  isSubmittingReview: boolean = false;

  ngOnInit() {
    this.loadData();
    this.initReviewForm();
  }

  initReviewForm() {
    this.reviewForm = this.fb.group({
      starRating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['']
    });
  }

  loadData() {
    this.isLoadingSchedule = true;
    this.isLoadingFeed = true;

    this.workerService.getDashboard().subscribe({
      next: (data) => {
        this.mySchedule = data;
        this.isLoadingSchedule = false;
      },
      error: (err) => console.error(err)
    });

    this.workerService.getAvailableFeed().subscribe({
      next: (data) => {
        this.availableJobs = data;
        this.isLoadingFeed = false;
      },
      error: (err) => console.error(err)
    });
  }

  apply(requirementId: number) {
    this.workerService.applyForJob(requirementId).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Application sent!' });
        this.loadData();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error || 'Failed to apply.' });
      }
    });
  }

  // --- NEW: Review Management Methods ---
  isJobComplete(endDate: string): boolean {
    return new Date(endDate) < new Date();
  }

  openReview(jobId: number) {
    this.selectedJobId = jobId;
    this.reviewForm.reset({ starRating: 5, comment: '' });
    this.showReviewDialog = true;
  }

  submitReview() {
    const formValues = this.reviewForm.value;
    
    // Fallback if PrimeNG star selection evaluates to null
    if (!formValues.starRating) {
      formValues.starRating = 5;
    }

    if (this.selectedJobId) {
      this.isSubmittingReview = true;
      this.reviewService.reviewBusiness(this.selectedJobId, formValues).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Contractor review submitted!' });
          this.showReviewDialog = false;
          this.isSubmittingReview = false;
          this.loadData(); // Re-fetches the entire schedule list and updates flags instantly!
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error || 'Failed to submit review.' });
          this.isSubmittingReview = false;
        }
      });
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getAppSeverity(status: string): "success" | "secondary" | "info" | "warning" | "danger" | "contrast" {
    switch (status) {
      case 'PENDING': return 'warning';
      case 'SELECTED': return 'success';
      case 'REJECTED': return 'danger';
      case 'AUTO_CANCELLED': return 'secondary';
      default: return 'info';
    }
  }
}