import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

// PrimeNG Modules
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { TabViewModule } from 'primeng/tabview';
import { InputNumberModule } from 'primeng/inputnumber';
import { PasswordModule } from 'primeng/password';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule, CardModule, InputTextModule, ButtonModule, 
    MessageModule, TabViewModule, InputNumberModule, PasswordModule, RouterLink
  ],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  errorMessage: string = '';
  isLoading: boolean = false;

  // WORKER FORM
  workerForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    phoneNumber: ['', Validators.required],
    yearsExperience: [0, [Validators.required, Validators.min(0)]]
  });

  // BUSINESS FORM
  businessForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    companyName: ['', Validators.required],
    contactName: ['', Validators.required],
    phoneNumber: ['', Validators.required]
  });

  onWorkerSubmit() {
    if (this.workerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.authService.registerWorker(this.workerForm.value).subscribe({
        next: () => {
          this.isLoading = false;
          this.router.navigate(['/worker-dashboard']);
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Registration failed. Email might already be in use.';
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
          this.errorMessage = 'Registration failed. Email might already be in use.';
          console.error(err);
        }
      });
    }
  }
}