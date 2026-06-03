import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
import { MultiSelectModule } from 'primeng/multiselect'; // <-- NEW IMPORT

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule, CardModule, InputTextModule, ButtonModule, 
    MessageModule, TabViewModule, InputNumberModule, PasswordModule, 
    CheckboxModule, DialogModule, RouterLink, MultiSelectModule // <-- ADDED
  ],
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute); // <-- Inject ActivatedRoute

  errorMessage: string = '';
  isLoading: boolean = false;
  showTermsDialog: boolean = false;
  activeTabIndex: number = 0; // <-- 0 = Worker, 1 = Business

  ngOnInit() {
      this.route.queryParams.subscribe(params => {
        if (params['tab'] === 'business') {
          this.activeTabIndex = 1;
        } else {
          this.activeTabIndex = 0;
        }
      });
  }

  // Options for the MultiSelect dropdown
  tradeOptions = [
    { label: 'Electrician', value: 'ELECTRICIEN' },
    { label: 'Plumber', value: 'PLOMBIER' },
    { label: 'Floor Layer', value: 'POSEUR_DE_PLANCHER' },
    { label: 'Carpenter', value: 'MENUISIER' },
    { label: 'Laborer', value: 'MANOEUVRE' }
  ];

  // WORKER FORM - Added ccqNumber and specialties
  workerForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    phoneNumber: ['', Validators.required],
    yearsExperience: [0, [Validators.required, Validators.min(0)]],
    ccqNumber: ['', Validators.required], // <-- NEW
    specialties: [[], Validators.required], // <-- NEW (Array)
    termsAccepted: [false, Validators.requiredTrue] 
  });

  // BUSINESS FORM - Added rbqNumber
  businessForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    companyName: ['', Validators.required],
    contactName: ['', Validators.required],
    phoneNumber: ['', Validators.required],
    rbqNumber: ['', Validators.required],
    billingAddress: ['', Validators.required], // <-- NEW FIELD
    termsAccepted: [false, Validators.requiredTrue] 
  });

  onWorkerSubmit() {
    if (this.workerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const formValues = this.workerForm.value;
      
      // MAP THE ANGULAR FORM TO MATCH SPRING BOOT'S EXPECTED DTO EXACTLY
      const payload = {
        email: formValues.email,
        password: formValues.password,
        fullName: `${formValues.firstName} ${formValues.lastName}`.trim(), // Combines first & last
        phoneNumber: formValues.phoneNumber,
        yearsExperience: formValues.yearsExperience,
        ccqNumber: formValues.ccqNumber,
        specialties: formValues.specialties
      };

      this.authService.registerWorker(payload).subscribe({
        next: () => {
          this.isLoading = false;
          this.router.navigate(['/worker-dashboard']);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Registration failed. Please check your details.';
          console.error(err);
        }
      });
    }
  }

  onBusinessSubmit() {
    if (this.businessForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.authService.registerBusiness(this.businessForm.value).subscribe({
        next: () => {
          this.isLoading = false;
          this.router.navigate(['/business-dashboard']);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Registration failed. Please check your details.';
          console.error(err);
        }
      });
    }
  }
}