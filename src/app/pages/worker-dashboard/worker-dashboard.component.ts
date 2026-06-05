import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { WorkerService } from '../../services/worker.service';
import { ReviewService } from '../../services/review.service';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { TabViewModule } from 'primeng/tabview';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { RatingModule } from 'primeng/rating';
import { MessageService, ConfirmationService } from 'primeng/api'; // <-- ConfirmationService
import { ConfirmDialogModule } from 'primeng/confirmdialog'; // <-- ConfirmDialogModule

@Component({
  selector: 'app-worker-dashboard',
  standalone: true,
  imports: [
    CommonModule, DatePipe, TableModule, ButtonModule, TagModule, 
    TabViewModule, CardModule, ToastModule, DialogModule, RatingModule,
    ReactiveFormsModule, ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './worker-dashboard.component.html'
})
export class WorkerDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private workerService = inject(WorkerService);
  private reviewService = inject(ReviewService);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);

  mySchedule: any[] = [];
  availableJobs: any[] = [];
  isLoadingSchedule: boolean = true;
  isLoadingFeed: boolean = true;
  isAdminImpersonating: boolean = false;

  showReviewDialog: boolean = false;
  reviewForm!: FormGroup;
  selectedJobId: number | null = null;
  isSubmittingReview: boolean = false;

  ngOnInit() {
    this.isAdminImpersonating = !!localStorage.getItem('admin_token'); // Check if backup token exists
    this.loadData();
    this.initReviewForm();
  }

  initReviewForm() {
    this.reviewForm = this.fb.group({
      starRating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['']
    });
  }

  returnToAdmin() {
    const adminToken = localStorage.getItem('admin_token');
    if (adminToken) {
      localStorage.setItem('jwt_token', adminToken);
      localStorage.setItem('user_role', 'ADMIN');
      localStorage.setItem('user_status', 'ACTIVE');
      localStorage.removeItem('admin_token'); // Clear the backup
      this.router.navigate(['/admin-dashboard']);
    }
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
        // Look how clean this is now! The backend handles all the heavy lifting.
        this.availableJobs = data; 
        this.isLoadingFeed = false;
      },
      error: (err) => console.error(err)
    });
  }

  confirmApply(job: any) {
    this.confirmationService.confirm({
      message: `Are you sure you want to apply for the ${this.formatStatus(job.jobType)} position at ${job.companyName}?`,
      header: 'Confirm Application',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: "none",
      rejectIcon: "none",
      rejectButtonStyleClass: "p-button-text",
      accept: () => {
        this.apply(job.requirementId);
      }
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
          this.loadData(); 
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

  // --- NEW: UI FORMATTING HELPERS ---

  formatStatus(status: string): string {
    if (!status) return '';
    return status.replace(/_/g, ' ')
      .toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  formatDateRange(startStr: string, endStr: string): string {
    if (!startStr || !endStr) return '';
    const start = new Date(startStr);
    const end = new Date(endStr);

    const timeOptions: Intl.DateTimeFormatOptions = { hour: 'numeric', minute: '2-digit' };
    const dateOptions: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric' };

    const startTime = start.toLocaleTimeString([], timeOptions);
    const endTime = end.toLocaleTimeString([], timeOptions);

    const isSameDay = start.getFullYear() === end.getFullYear() &&
                      start.getMonth() === end.getMonth() &&
                      start.getDate() === end.getDate();

    if (isSameDay) {
      return `${startTime} - ${endTime}`;
    } else {
      const endDate = end.toLocaleDateString([], dateOptions);
      return `${startTime} - ${endDate} at ${endTime}`;
    }
  }
}