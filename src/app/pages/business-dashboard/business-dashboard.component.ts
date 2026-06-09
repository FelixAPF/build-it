import { Component, OnInit, inject, NgZone, ViewChild, ElementRef, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { BusinessService } from '../../services/business.service';
import { ReviewService } from '../../services/review.service';

import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { ToastModule } from 'primeng/toast';
import { MessageService, ConfirmationService } from 'primeng/api'; 
import { RatingModule } from 'primeng/rating';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ConfirmDialogModule } from 'primeng/confirmdialog'; 
import { CheckboxModule } from 'primeng/checkbox'; 
import { InputSwitchModule } from 'primeng/inputswitch';
import { MultiSelectModule } from 'primeng/multiselect'; 

declare var google: any;

@Component({
  selector: 'app-business-dashboard',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule, DatePipe, TableModule, ButtonModule, 
    TagModule, DialogModule, InputTextModule, CalendarModule, DropdownModule, 
    InputNumberModule, ToastModule, RatingModule, ConfirmDialogModule, CheckboxModule, InputSwitchModule, MultiSelectModule
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
  private ngZone = inject(NgZone);
  private cdr = inject(ChangeDetectorRef);

  @ViewChild('autocompleteContainer') autocompleteContainerRef!: ElementRef;
  jobs: any[] = [];
  isLoading: boolean = true;
  showReviewDialog: boolean = false;
  reviewForm!: FormGroup;
  reviewApplicationId: number | null = null;
  isSubmittingReview: boolean = false;
  businessType: 'COMPANY' | 'PRIVATE' = 'COMPANY';
  isAdminImpersonating: boolean = false;
  
  showDialog: boolean = false;
  isSubmitting: boolean = false;
  jobForm!: FormGroup;
  useSpecificTime: boolean = false;

  selectedJob: any = null;
  showJobDetailsDialog: boolean = false;
  showApplicantsDialog: boolean = false;
  applicants: any[] = [];
  allTradeQuestions: any[] = [];
  isLoadingApplicants: boolean = false;

  // FIX: Start empty so we can fetch the dynamic trades from the database!
  jobTypes: any[] = [];

  paymentTypes = [
    { label: 'Hourly /hr', value: 'HOURLY' },
    { label: 'Fixed Salary', value: 'FIXED' },
    { label: 'Per Sq. Ft.', value: 'PER_SQFT' }
  ];

  ngOnInit() {
    this.loadDashboard();
    this.isAdminImpersonating = !!localStorage.getItem('admin_token'); 

    // FIX: Load the exact dynamic trades from the DB so the values match the questions!
    this.authService.getTrades().subscribe(res => this.jobTypes = res);
    
    // Load the questions
    this.businessService.getTradeQuestions().subscribe(res => this.allTradeQuestions = res);
    
    this.initForm();
  }

  updateTools(value: any){
    if(!this.specificToolsFormArray.length && value){
      this.addTool();
    }
  }

  updateSupplyItems(value: any) {
    if(!this.supplyChainItemsFormArray.length && value) {
      this.addSupplyItem();
    }
  }
  
  onTradeChange(event: any, reqIndex: number) {
    const reqGroup = this.requirementsFormArray.at(reqIndex) as FormGroup;
    const selectedTrade = event.value; // Now this will perfectly match "FLOOR_LAYER"
    const questions = this.allTradeQuestions.filter(q => q.jobType === selectedTrade);

    const questionsArray = this.fb.array(
      questions.map(q => this.fb.group({
        question: [q.questionText],
        answers: this.fb.array([this.fb.control('', Validators.required)]) 
      }))
    );
    
    reqGroup.setControl('tradeQuestions', questionsArray);
  }

  getTradeQuestionsArray(reqIndex: number): FormArray {
    return this.requirementsFormArray.at(reqIndex).get('tradeQuestions') as FormArray;
  }

  getAnswersForQuestion(reqIndex: number, qIndex: number): FormArray {
    return this.getTradeQuestionsArray(reqIndex).at(qIndex).get('answers') as FormArray;
  }

  addAnswerToQuestion(reqIndex: number, qIndex: number) {
    this.getAnswersForQuestion(reqIndex, qIndex).push(this.fb.control('', Validators.required));
  }

  removeAnswerFromQuestion(reqIndex: number, qIndex: number, aIndex: number) {
    this.getAnswersForQuestion(reqIndex, qIndex).removeAt(aIndex);
  }

  initForm() {
    this.useSpecificTime = false;
    this.jobForm = this.fb.group({
      address: ['', Validators.required],
      city: ['', Validators.required],
      province: ['', Validators.required],
      postalCode: ['', Validators.required],
      startDatetime: [null, Validators.required],
      endDatetime: [null, Validators.required],
      providesSupplyChain: [false], 
      needSpecificTools: [false], 
      specificTools: this.fb.array([]), 
      needSupplyChainItems: [false], 
      supplyChainItems: this.fb.array([]), 
      requirements: this.fb.array([])
    });
    this.reviewForm = this.fb.group({
      starRating: [5, [Validators.required, Validators.min(1), Validators.max(5)]],
      comment: ['']
    });
    this.addRequirement();
  }

  get specificToolsFormArray() { return this.jobForm.get('specificTools') as FormArray; }
  addTool() { this.specificToolsFormArray.push(this.fb.control('', Validators.required)); }
  removeTool(index: number) { this.specificToolsFormArray.removeAt(index); }

  get supplyChainItemsFormArray() { return this.jobForm.get('supplyChainItems') as FormArray; }
  addSupplyItem() { this.supplyChainItemsFormArray.push(this.fb.control('', Validators.required)); }
  removeSupplyItem(index: number) { this.supplyChainItemsFormArray.removeAt(index); }

  get requirementsFormArray() { return this.jobForm.get('requirements') as FormArray; }

  addRequirement() {
    const reqGroup = this.fb.group({
      jobType: [null, Validators.required],
      paymentType: ['HOURLY', Validators.required],
      payRate: [25, [Validators.required, Validators.min(1)]],
      tradeQuestions: this.fb.array([]), 
      qtyRequested: [1, [Validators.required, Validators.min(1)]]
    });
    this.requirementsFormArray.push(reqGroup);
  }

  removeRequirement(index: number) { this.requirementsFormArray.removeAt(index); }

  initAutocomplete() {
    setTimeout(() => {
      if (!this.autocompleteContainerRef?.nativeElement) return;

      const container = this.autocompleteContainerRef.nativeElement;
      container.innerHTML = '';

      const input = document.createElement('input');
      input.placeholder = 'Search for an address...';
      input.style.cssText = 'width:100%; padding:12px; border:none; outline:none; background:transparent; font-size:14px; box-sizing:border-box;';
      container.appendChild(input);

      const dropdown = document.createElement('ul');
      dropdown.style.cssText = `
        position:fixed; z-index:99999;
        background:white; border:1px solid #e2e8f0; border-radius:8px;
        box-shadow:0 4px 20px rgba(0,0,0,0.15); list-style:none;
        margin:0; padding:4px 0; display:none; min-width:200px;
      `;
      document.body.appendChild(dropdown);

      const positionDropdown = () => {
        const rect = input.getBoundingClientRect();
        dropdown.style.top = `${rect.bottom + 4}px`;
        dropdown.style.left = `${rect.left}px`;
        dropdown.style.width = `${rect.width}px`;
      };

      let debounceTimer: any;
      let sessionToken = new google.maps.places.AutocompleteSessionToken();

      input.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        const query = input.value.trim();
        if (query.length < 3) { dropdown.style.display = 'none'; return; }
        positionDropdown();
        debounceTimer = setTimeout(() => this.fetchSuggestions(query, dropdown, input, sessionToken, () => {
          sessionToken = new google.maps.places.AutocompleteSessionToken();
        }), 300);
      });

      const observer = new MutationObserver(() => {
        if (!document.body.contains(container)) {
          dropdown.remove();
          observer.disconnect();
        }
      });
      observer.observe(document.body, { childList: true, subtree: true });

      document.addEventListener('click', (e) => {
        if (!container.contains(e.target as Node) && !dropdown.contains(e.target as Node)) {
          dropdown.style.display = 'none';
        }
      });

    }, 500);
  }

  async fetchSuggestions(query: string, dropdown: HTMLElement, input: HTMLInputElement, sessionToken: any, onPicked: () => void) {
    try {
      const request = {
        input: query,
        sessionToken,
        includedRegionCodes: ['ca'],
        language: 'en'
      };

      const { suggestions } = await google.maps.places.AutocompleteSuggestion.fetchAutocompleteSuggestions(request);

      dropdown.innerHTML = '';

      if (!suggestions?.length) {
        dropdown.style.display = 'none';
        return;
      }

      suggestions.forEach((suggestion: any) => {
        const placePrediction = suggestion.placePrediction;
        const li = document.createElement('li');
        li.style.cssText = 'padding:10px 14px; cursor:pointer; font-size:13px; color:#334155; border-bottom:1px solid #f1f5f9;';
        li.textContent = placePrediction.text.toString();

        li.addEventListener('mouseenter', () => li.style.background = '#fef2f2'); 
        li.addEventListener('mouseleave', () => li.style.background = 'white');

        li.addEventListener('click', async () => {
          input.value = placePrediction.text.toString();
          dropdown.style.display = 'none';

          try {
            const place = placePrediction.toPlace();
            await place.fetchFields({ fields: ['addressComponents'] });

            this.ngZone.run(() => {
              this.fillInAddressFromComponents(place.addressComponents ?? []);
            });

            onPicked(); 
          } catch (err) {
            console.error('fetchFields error:', err);
          }
        });

        dropdown.appendChild(li);
      });

      dropdown.style.display = 'block';

    } catch (err) {
      console.error('fetchAutocompleteSuggestions error:', err);
    }
  }

  fillInAddressFromComponents(components: any[]) {
    let streetNumber = '', route = '', city = '', province = '', postalCode = '';

    for (const component of components) {
      const types: string[] = component.types ?? [];
      const longText: string = component.longText ?? component.long_name ?? '';
      const shortText: string = component.shortText ?? component.short_name ?? '';

      if (types.includes('street_number')) streetNumber = longText;
      if (types.includes('route')) route = longText;
      if (types.includes('locality') || types.includes('sublocality') || types.includes('postal_town')) city = longText;
      if (types.includes('administrative_area_level_1')) province = shortText;
      if (types.includes('postal_code')) postalCode = longText;
    }

    this.jobForm.patchValue({ address: `${streetNumber} ${route}`.trim(), city, province, postalCode });
    this.cdr.detectChanges();
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
  
  loadDashboard() {
    this.isLoading = true;
    this.authService.getBusinessProfile().subscribe({
      next: (profile) => {
        this.businessType = profile.businessType || 'COMPANY';
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
      },
      error: (err) => {
        console.error('Could not load account profile details', err);
        this.isLoading = false;
      }
    });
  }

  onSubmit() {
    if (this.jobForm.valid) {
      this.isSubmitting = true;
      
      const payload = JSON.parse(JSON.stringify(this.jobForm.value));

      payload.isTimeFlexible = !this.useSpecificTime;

      if (!payload.needSpecificTools) payload.specificTools = [];
      if (!payload.needSupplyChainItems) payload.supplyChainItems = [];

      if (!this.useSpecificTime) {
        if (payload.startDatetime) {
          const startDate = new Date(payload.startDatetime);
          startDate.setHours(0, 0, 0, 0); 
          payload.startDatetime = startDate;
        }
        if (payload.endDatetime) {
          const endDate = new Date(payload.endDatetime);
          endDate.setHours(0, 0, 0, 0); 
          payload.endDatetime = endDate;
        }
      }

      if (payload.requirements) {
        payload.requirements = payload.requirements.map((req: any) => {
          if (req.tradeQuestions) {
            req.answers = req.tradeQuestions.map((tq: any) => {
              return {
                question: tq.question,
                answer: tq.answers.filter((a: string) => a.trim().length > 0).join(', ')
              };
            });
            delete req.tradeQuestions;
          }
          return req;
        });
      }

      this.businessService.createJobPosting(payload).subscribe({
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
      return isSameDay ? 'Flexible schedule / TBD' : `Until ${end.toLocaleDateString([], dateOptions)}`;
    }

    const startTime = start.toLocaleTimeString([], timeOptions);
    const endTime = end.toLocaleTimeString([], timeOptions);

    if (isSameDay) {
      return `${startTime} - ${endTime}`;
    } else {
      const endDate = end.toLocaleDateString([], dateOptions);
      return `${startTime} - ${endDate} at ${endTime}`;
    }
  }

  formatPay(rate: number, type: string): string {
    if (!rate) return '';
    switch(type) {
      case 'HOURLY': return `$${rate}/hr`;
      case 'FIXED': return `$${rate} Fixed`;
      case 'PER_SQFT': return `$${rate}/sqft`;
      default: return `$${rate}`;
    }
  }
}