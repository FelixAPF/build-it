import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

// PrimeNG Modules
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { TabViewModule } from 'primeng/tabview';
import { InputNumberModule } from 'primeng/inputnumber';
import { PasswordModule } from 'primeng/password';
import { CheckboxModule } from 'primeng/checkbox';
import { DialogModule } from 'primeng/dialog';
import { MultiSelectModule } from 'primeng/multiselect';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, CardModule, InputTextModule, ButtonModule, 
    MessageModule, TabViewModule, InputNumberModule, PasswordModule, 
    CheckboxModule, DialogModule, RouterLink, MultiSelectModule
  ],
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  errorMessage: string = '';
  isLoading: boolean = false;
  showTermsDialog: boolean = false;
  
  activeTabIndex: number = 0; // 0 = Worker, 1 = Business

  workerForm!: FormGroup;
  businessForm!: FormGroup;

  tradeOptions = [
    { label: 'Electrician', value: 'ELECTRICIEN' },
    { label: 'Plumber', value: 'PLOMBIER' },
    { label: 'Floor Layer', value: 'POSEUR_DE_PLANCHER' },
    { label: 'Carpenter', value: 'MENUISIER' },
    { label: 'Laborer', value: 'MANOEUVRE' }
  ];

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      this.activeTabIndex = params['tab'] === 'business' ? 1 : 0;
    });

    this.initForms();
    this.setupDynamicValidators();
  }

  initForms() {
    this.workerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      yearsExperience: [0, [Validators.required, Validators.min(0)]],
      ccqNumber: ['', Validators.required],
      specialties: [[], Validators.required],
      termsAccepted: [false, Validators.requiredTrue] 
    });

    this.businessForm = this.fb.group({
      businessType: ['PRIVATE', Validators.required], // Defaults to Private Individual
      companyName: ['', Validators.required],
      contactName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      phoneNumber: ['', Validators.required],
      billingAddress: ['', Validators.required],
      rbqNumber: [''], // Validations applied dynamically
      neqNumber: [''], // Validations applied dynamically
      termsAccepted: [false, Validators.requiredTrue] 
    });
  }

  setupDynamicValidators() {
    const businessTypeControl = this.businessForm.get('businessType');
    const rbqControl = this.businessForm.get('rbqNumber');
    const neqControl = this.businessForm.get('neqNumber');

    businessTypeControl?.valueChanges.subscribe((type) => {
      if (type === 'COMPANY') {
        rbqControl?.setValidators([Validators.required]);
        neqControl?.setValidators([Validators.required]);
      } else {
        rbqControl?.clearValidators();
        neqControl?.clearValidators();
      }
      rbqControl?.updateValueAndValidity();
      neqControl?.updateValueAndValidity();
    });
  }
onWorkerSubmit() {
    if (this.workerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const formValues = this.workerForm.value;
      const payload = {
        email: formValues.email,
        password: formValues.password,
        fullName: `${formValues.firstName} ${formValues.lastName}`.trim(),
        phoneNumber: formValues.phoneNumber,
        yearsExperience: formValues.yearsExperience,
        ccqNumber: formValues.ccqNumber,
        specialties: formValues.specialties
      };

      this.authService.registerWorker(payload).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.router.navigate(['/pending'], { queryParams: { status: 'UNVERIFIED', email: payload.email } });
        },
        error: (err) => {
          this.isLoading = false;
          console.error(err);

          // Safety net: If the backend actually succeeded but Angular choked on parsing
          if (err.status === 200) {
            this.router.navigate(['/pending'], { queryParams: { status: 'UNVERIFIED', email: payload.email } });
            return;
          }

          // Smart error extraction to prevent [object Object]
          if (typeof err.error === 'string') {
            this.errorMessage = err.error;
          } else if (err.error && typeof err.error.message === 'string') {
            this.errorMessage = err.error.message;
          } else {
            this.errorMessage = 'Registration failed. Please check your details.';
          }
        }
      });
    }
  }
onBusinessSubmit() {
    if (this.businessForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      
const payload = { ...this.businessForm.value };
      
      if (payload.businessType === 'PRIVATE') {
        payload.rbqNumber = null;
        payload.neqNumber = null; // <-- Swapped from ccqNumber
      }

      this.authService.registerBusiness(payload).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.router.navigate(['/pending'], { queryParams: { status: 'UNVERIFIED', email: payload.email } });
        },
        error: (err) => {
          this.isLoading = false;
          console.error(err);

          // Safety net: If the backend actually succeeded but Angular choked on parsing
          if (err.status === 200) {
            this.router.navigate(['/pending'], { queryParams: { status: 'UNVERIFIED', email: payload.email } });
            return;
          }

          // Smart error extraction to prevent [object Object]
          if (typeof err.error === 'string') {
            this.errorMessage = err.error;
          } else if (err.error && typeof err.error.message === 'string') {
            this.errorMessage = err.error.message;
          } else {
            this.errorMessage = 'Registration failed. Please check your details.';
          }
        }
      });
    }
  }
}