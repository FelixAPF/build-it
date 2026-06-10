import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber';
import { CheckboxModule } from 'primeng/checkbox';
import { MultiSelectModule } from 'primeng/multiselect';
import { TabViewModule } from 'primeng/tabview';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { MessageModule } from 'primeng/message'; // <-- CHANGED TO MessageModule
import { DialogModule } from 'primeng/dialog';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterModule, ButtonModule, InputTextModule,
    PasswordModule, DropdownModule, InputNumberModule, CheckboxModule,
    MultiSelectModule, TabViewModule, ToastModule, MessageModule, DialogModule, TranslatePipe // <-- CHANGED TO MessageModule
  ],
  providers: [MessageService],
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private messageService = inject(MessageService);

  activeTabIndex: number = 0;
  workerForm!: FormGroup;
  businessForm!: FormGroup;
  isLoading: boolean = false;
  errorMessage: string = '';
  showTermsDialog: boolean = false;

  tradeOptions: any[] = [];

  ngOnInit() {
    this.authService.getTrades().subscribe({
      next: (res) => this.tradeOptions = res,
      error: (err) => console.error('Failed to load trades', err)
    });

    this.route.queryParams.subscribe(params => {
      if (params['tab'] === 'business') {
        this.activeTabIndex = 1;
      } else {
        this.activeTabIndex = 0;
      }
    });

    this.workerForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phoneNumber: ['', Validators.required],
      yearsExperience: [0, [Validators.required, Validators.min(0)]],
      ccqNumber: [''],
      specialties: [[], Validators.required],
      termsAccepted: [false, Validators.requiredTrue]
    });

    this.businessForm = this.fb.group({
      businessType: ['COMPANY', Validators.required],
      companyName: ['', Validators.required],
      contactName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phoneNumber: ['', Validators.required],
      billingAddress: ['', Validators.required],
      rbqNumber: [''],
      neqNumber: [''],
      termsAccepted: [false, Validators.requiredTrue]
    });

    // Dynamic validation for business types
    this.businessForm.get('businessType')?.valueChanges.subscribe(type => {
      if (type === 'COMPANY') {
        this.businessForm.get('rbqNumber')?.setValidators([Validators.required]);
        this.businessForm.get('neqNumber')?.setValidators([Validators.required]);
      } else {
        this.businessForm.get('rbqNumber')?.clearValidators();
        this.businessForm.get('neqNumber')?.clearValidators();
      }
      this.businessForm.get('rbqNumber')?.updateValueAndValidity();
      this.businessForm.get('neqNumber')?.updateValueAndValidity();
    });
  }

  onWorkerSubmit() {
    if (this.workerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      const payload = { ...this.workerForm.value };
      delete payload.termsAccepted;

      this.authService.registerWorker(payload).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.router.navigate(['/verify-email']);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error || 'Registration failed. Please try again.';
        }
      });
    }
  }

  onBusinessSubmit() {
    if (this.businessForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      const payload = { ...this.businessForm.value };
      delete payload.termsAccepted;

      this.authService.registerBusiness(payload).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.router.navigate(['/verify-email']);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error || 'Registration failed. Please try again.';
        }
      });
    }
  }
}