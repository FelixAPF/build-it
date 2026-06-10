import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
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
import { MessageService, ConfirmationService } from 'primeng/api'; 
import { ConfirmDialogModule } from 'primeng/confirmdialog'; 
import { MultiSelectModule } from 'primeng/multiselect'; // <-- NEW
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-worker-dashboard',
  standalone: true,
  imports: [
    CommonModule, DatePipe, TableModule, ButtonModule, TagModule, 
    TabViewModule, CardModule, ToastModule, DialogModule, RatingModule,
    ReactiveFormsModule, FormsModule, ConfirmDialogModule, MultiSelectModule, TranslatePipe
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './worker-dashboard.component.html'
})
export class WorkerDashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private workerService = inject(WorkerService);
  private reviewService = inject(ReviewService);
  public translate = inject(TranslateService);
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

  // NEW SETTINGS VARIABLES
  showSettingsDialog: boolean = false;
  isSavingSettings: boolean = false;
  isDeletingAccount: boolean = false;
  workerProfile: any = null;
  selectedSpecialties: string[] = [];
  public currentLang = localStorage.getItem('buildit_lang') || 'en';

  // 2. Add this method anywhere in your class
  switchLanguage() {
    this.currentLang = this.currentLang === 'en' ? 'fr' : 'en';
    this.translate.use(this.currentLang);
    localStorage.setItem('buildit_lang', this.currentLang);
  }
  jobTypes = [
    { label: 'Electrician', value: 'ELECTRICIEN' },
    { label: 'Plumber', value: 'PLOMBIER' },
    { label: 'Floor Layer', value: 'POSEUR_DE_PLANCHER' },
    { label: 'Carpenter', value: 'MENUISIER' },
    { label: 'Laborer', value: 'MANOEUVRE' }
  ];

  ngOnInit() {
    this.isAdminImpersonating = !!localStorage.getItem('admin_token'); 
    this.loadData();
    this.initReviewForm();
  }

  initReviewForm() {
    this.reviewForm = this.fb.group({
      starRating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['']
    });
  }

  // --- NEW SETTINGS LOGIC ---
  openSettings() {
    this.workerService.getProfile().subscribe(res => {
      this.workerProfile = res;
      this.selectedSpecialties = res.specialties || [];
      this.showSettingsDialog = true;
    });
  }

  saveSettings() {
    this.isSavingSettings = true;
    this.workerService.updateSpecialties(this.selectedSpecialties).subscribe({
      next: () => {
        this.messageService.add({severity:'success', summary:'Updated', detail:'Your trade specialties have been updated!'});
        this.isSavingSettings = false;
        this.showSettingsDialog = false;
        this.loadData(); // Reload the feed with the new trades
      },
      error: () => {
        this.messageService.add({severity:'error', summary:'Error', detail:'Failed to update settings.'});
        this.isSavingSettings = false;
      }
    });
  }

  confirmDeleteAccount() {
    this.confirmationService.confirm({
      message: 'Are you absolutely sure you want to permanently delete your account? You will lose all your shift history and this action cannot be undone.',
      header: 'Delete Account',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: "none",
      rejectIcon: "none",
      acceptButtonStyleClass: "p-button-danger",
      rejectButtonStyleClass: "p-button-text p-button-secondary",
      accept: () => {
        this.isDeletingAccount = true;
        this.workerService.deleteAccount().subscribe({
          next: () => {
             this.authService.logout();
             this.router.navigate(['/']);
          },
          error: (err) => {
             this.messageService.add({severity:'error', summary:'Error', detail:'Failed to delete account. Please contact support.'});
             this.isDeletingAccount = false;
          }
        });
      }
    });
  }

  returnToAdmin() {
    const adminToken = localStorage.getItem('admin_token');
    if (adminToken) {
      localStorage.setItem('jwt_token', adminToken);
      localStorage.setItem('user_role', 'ADMIN');
      localStorage.setItem('user_status', 'ACTIVE');
      localStorage.removeItem('admin_token'); 
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
        this.availableJobs = data; 
        this.isLoadingFeed = false;
      },
      error: (err) => console.error(err)
    });
  }

  confirmApply(job: any) {
    const translatedJobType = this.formatStatus(job.jobType);
    this.confirmationService.confirm({
    message: this.translate.instant('DIALOGS.CONFIRM_APPLICATION_MSG', { 
        jobType: translatedJobType, 
        companyName: job.companyName 
      }),
      header: this.translate.instant('DIALOGS.CONFIRM_APPLICATION_TITLE'),
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

  addToCalendar(app: any) {
    const startDate = new Date(app.startDatetime);
    const endDate = new Date(app.endDatetime);

    const pad = (n: number): string => n < 10 ? '0' + n : n.toString();
    
    const formatICSDate = (date: Date) => {
      return `${date.getUTCFullYear()}${pad(date.getUTCMonth() + 1)}${pad(date.getUTCDate())}T${pad(date.getUTCHours())}${pad(date.getUTCMinutes())}${pad(date.getUTCSeconds())}Z`;
    };

    let description = `Job Type: ${this.formatStatus(app.jobType)}\\n`;
    description += `Pay: ${this.formatPay(app.payRate, app.paymentType)}\\n`;
    description += `Phone: ${app.companyPhone}\\n`;
    if (app.providesSupplyChain) description += `\\n--- Logistic Notes ---\\nSupply Chain Provided\\n`;
    
    if (app.supplyChainItems && app.supplyChainItems.length > 0) {
      description += `Required Materials: ${app.supplyChainItems.join(', ')}\\n`;
    }

    if (app.specificTools && app.specificTools.length > 0) {
      description += `Required Tools: ${app.specificTools.join(', ')}\\n`;
    }

    const icsData = [
      'BEGIN:VCALENDAR',
      'VERSION:2.0',
      'PRODID:-//BuildIt//EN',
      'BEGIN:VEVENT',
      `DTSTART:${formatICSDate(startDate)}`,
      `DTEND:${formatICSDate(endDate)}`,
      `SUMMARY:Shift at ${app.companyName}`,
      `LOCATION:${app.address}`,
      `DESCRIPTION:${description}`,
      'END:VEVENT',
      'END:VCALENDAR'
    ].join('\n');

    const blob = new Blob([icsData], { type: 'text/calendar;charset=utf-8' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Shift_${app.companyName.replace(/\s+/g, '_')}.ics`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    
    this.messageService.add({ severity: 'success', summary: 'Added', detail: 'Event downloaded for your calendar!' });
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

  formatStatus(status: string): string {
    if (!status) return '';
    return status.replace(/_/g, ' ')
      .toLowerCase()
      .split(' ')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

formatDateRange(startStr: string, endStr: string, isTimeFlexible: boolean = false): string {
    if (!startStr || !endStr) return '';
    const start = new Date(startStr);
    const end = new Date(endStr);

    const timeOptions: Intl.DateTimeFormatOptions = { hour: 'numeric', minute: '2-digit' };
    const dateOptions: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric' };

    const isSameDay = start.getFullYear() === end.getFullYear() &&
                      start.getMonth() === end.getMonth() &&
                      start.getDate() === end.getDate();

    if (isTimeFlexible) {
      return isSameDay 
        ? this.translate.instant('JOB_FORMAT.FLEXIBLE') 
        : `${this.translate.instant('JOB_FORMAT.UNTIL')} ${end.toLocaleDateString([], dateOptions)}`;
    }

    const startTime = start.toLocaleTimeString([], timeOptions);
    const endTime = end.toLocaleTimeString([], timeOptions);

    if (isSameDay) {
      return `${startTime} - ${endTime}`;
    } else {
      const endDate = end.toLocaleDateString([], dateOptions);
      return `${startTime} - ${endDate} ${this.translate.instant('JOB_FORMAT.AT')} ${endTime}`;
    }
  }

formatPay(rate: number, type: string): string {
    if (!rate) return '';
    switch(type) {
      case 'HOURLY': return `$${rate}/${this.translate.instant('JOB_FORMAT.HOURLY_SHORT') || 'hr'}`;
      case 'FIXED': return `$${rate} ${this.translate.instant('JOB_FORMAT.FIXED_LABEL')}`;
      case 'PER_SQFT': return `$${rate}/${this.translate.instant('JOB_FORMAT.PER_SQFT_LABEL')}`;
      default: return `$${rate}`;
    }
  }
}