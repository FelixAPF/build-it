import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { BusinessService } from '../../services/business.service';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { ToastModule } from 'primeng/toast';
import { MessageService, ConfirmationService } from 'primeng/api'; // <-- ConfirmationService
import { ReviewService } from '../../services/review.service';
import { RatingModule } from 'primeng/rating';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ConfirmDialogModule } from 'primeng/confirmdialog'; // <-- ConfirmDialogModule

@Component({
  selector: 'app-business-dashboard',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, DatePipe, TableModule, ButtonModule, 
    TagModule, DialogModule, InputTextModule, CalendarModule, DropdownModule, 
    InputNumberModule, ToastModule, RatingModule, ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './business-dashboard.component.html'
})
export class BusinessDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private reviewService = inject(ReviewService);
  private businessService = inject(BusinessService);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);

  jobs: any[] = [];
  isLoading: boolean = true;
  showReviewDialog: boolean = false;
  reviewForm!: FormGroup;
  reviewApplicationId: number | null = null;
  isSubmittingReview: boolean = false;
  
  showDialog: boolean = false;
  isSubmitting: boolean = false;
  jobForm!: FormGroup;

  selectedJob: any = null;
  showJobDetailsDialog: boolean = false;
  showApplicantsDialog: boolean = false;
  applicants: any[] = [];
  isLoadingApplicants: boolean = false;

  jobTypes = [
    { label: 'Electrician', value: 'ELECTRICIEN' },
    { label: 'Plumber', value: 'PLOMBIER' },
    { label: 'Floor Layer', value: 'POSEUR_DE_PLANCHER' },
    { label: 'Carpenter', value: 'MENUISIER' },
    { label: 'Laborer', value: 'MANOEUVRE' }
  ];

  ngOnInit() {
    this.loadDashboard();
    this.initForm();
  }

  initForm() {
    this.jobForm = this.fb.group({
      address: ['', Validators.required],
      startDatetime: [null, Validators.required],
      endDatetime: [null, Validators.required],
      requirements: this.fb.array([])
    });
    this.reviewForm = this.fb.group({
      starRating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['']
    });
    this.addRequirement();
  }

  get requirementsFormArray() { return this.jobForm.get('requirements') as FormArray; }

  addRequirement() {
    const reqGroup = this.fb.group({
      jobType: [null, Validators.required],
      hourlyRate: [25, [Validators.required, Validators.min(15)]],
      qtyRequested: [1, [Validators.required, Validators.min(1)]]
    });
    this.requirementsFormArray.push(reqGroup);
  }

  removeRequirement(index: number) { this.requirementsFormArray.removeAt(index); }
  
  loadDashboard() {
    this.isLoading = true;
    this.businessService.getDashboard().subscribe({
      next: (data) => {
        this.jobs = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching dashboard', err);
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    if (this.jobForm.valid) {
      this.isSubmitting = true;
      this.businessService.createJobPosting(this.jobForm.value).subscribe({
        next: (res) => {
          this.isSubmitting = false;
          this.showDialog = false;
          this.messageService.add({ severity: 'success', summary: 'Success', detail: 'Job Posted Successfully!' });
          this.loadDashboard(); 
          this.initForm();      
        },
        error: (err) => {
          this.isSubmitting = false;
          this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to post job.' });
        }
      });
    }
  }

  viewJobDetails(job: any) {
    this.selectedJob = job;
    this.showJobDetailsDialog = true;
  }

  viewApplicants(requirementId: number) {
    this.isLoadingApplicants = true;
    this.showApplicantsDialog = true;
    
    this.businessService.getApplicants(requirementId).subscribe({
      next: (data) => {
        this.applicants = data;
        this.isLoadingApplicants = false;
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to load applicants.' });
        this.isLoadingApplicants = false;
      }
    });
  }

  confirmApprove(app: any) {
    this.confirmationService.confirm({
      message: `Are you sure you want to hire ${app.workerName}? This will lock them into the schedule and notify them immediately.`,
      header: 'Confirm Hire',
      icon: 'pi pi-check-circle',
      acceptIcon: "none",
      rejectIcon: "none",
      rejectButtonStyleClass: "p-button-text",
      accept: () => {
        this.approveWorker(app.applicationId);
      }
    });
  }

  approveWorker(applicationId: number) {
    this.businessService.approveWorker(applicationId).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Hired!', detail: 'Worker approved and scheduled!' });
        this.showApplicantsDialog = false;
        this.showJobDetailsDialog = false;
        this.loadDashboard(); 
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error || 'Failed to approve.' });
      }
    });
  }

  rejectWorker(applicationId: number) {
    this.businessService.rejectWorker(applicationId).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'info', summary: 'Rejected', detail: 'Worker application declined.' });
        this.showApplicantsDialog = false;
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to reject.' });
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getSeverity(status: string): "success" | "secondary" | "info" | "warning" | "danger" | "contrast" {
    switch (status) {
      case 'OPEN': return 'info';
      case 'PARTIALLY_FILLED': return 'warning';
      case 'FULLY_FILLED': return 'success';
      case 'COMPLETED': return 'secondary';
      case 'CANCELLED': return 'danger';
      default: return 'info';
    }
  }

  isJobComplete(endDate: string): boolean {
    return new Date(endDate) < new Date();
  }

  openReview(applicationId: number) {
    this.reviewApplicationId = applicationId;
    this.reviewForm.reset({ starRating: 5, comment: '' });
    this.showReviewDialog = true;
  }

  submitReview() {
    const formValues = this.reviewForm.value;
    if (!formValues.starRating) {
      formValues.starRating = 5;
    }

    if (this.reviewApplicationId) {
      this.isSubmittingReview = true;
      this.reviewService.reviewWorker(this.reviewApplicationId, formValues).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Reviewed!', detail: 'Worker review submitted.' });
          this.showReviewDialog = false;
          this.isSubmittingReview = false;
          
          if (this.selectedJob) {
            this.selectedJob.requirements.forEach((req: any) => {
              const worker = req.assignedWorkers.find((w: any) => w.applicationId === this.reviewApplicationId);
              if (worker) worker.reviewedWorker = true;
            });
          }
        },
        error: (err) => {
          this.messageService.add({ severity: 'error', summary: 'Error', detail: err.error || 'Failed to submit review.' });
          this.isSubmittingReview = false;
        }
      });
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