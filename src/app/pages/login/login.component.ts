import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { Router, RouterLink, ActivatedRoute } from '@angular/router'; 
import { AuthService } from '../../services/auth.service';
import { PasswordModule } from 'primeng/password';

// PrimeNG Modules
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';
import { MessageService } from 'primeng/api';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
imports: [
    ReactiveFormsModule, CardModule, InputTextModule, ButtonModule, 
    MessageModule, RouterLink, PasswordModule, DialogModule, ToastModule, CommonModule, FormsModule // <-- ADDED
  ],  providers: [MessageService],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private messageService = inject(MessageService);
  private route = inject(ActivatedRoute);

  showForgotDialog: boolean = false;
  forgotEmail: string = '';
  isSendingReset: boolean = false;

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  ngOnInit(): void {
      this.route.queryParams.subscribe(params => {
      if (params['message']) {
        this.messageService.add({ severity: 'success', summary: 'Check Your Email', detail: params['message'], sticky: true });
      }
    });
  }
  errorMessage: string = '';
  isLoading: boolean = false;

onSubmit() {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      
      this.authService.login(this.loginForm.value).subscribe({
        next: (res) => {
          this.isLoading = false;
          
          const role = localStorage.getItem('user_role');
          const status = localStorage.getItem('user_status');
          
          // INTELLIGENT LOGIN ROUTING
          if (status === 'UNVERIFIED' || status === 'PENDING_VERIFICATION') {
            this.router.navigate(['/pending']);
          } else if (role === 'ADMIN') {
            this.router.navigate(['/admin-dashboard']);
          } else if (role === 'BUSINESS') {
            this.router.navigate(['/business-dashboard']);
          } else {
            this.router.navigate(['/worker-dashboard']);
          }
        },
        error: (err) => {
          this.isLoading = false;
          console.error(err);

          const backendErrorMessage = err.error || '';

          // If the backend blocked them for email reasons, route them back to the hold screen
          if (backendErrorMessage.includes('verify your email')) {
            this.router.navigate(['/pending'], { 
              queryParams: { status: 'UNVERIFIED', email: this.loginForm.value.email } 
            });
          } else {
            this.errorMessage = typeof err.error === 'string' ? err.error : 'Invalid email or password.';
          }
        }
      });
    }
  }

  submitForgotPassword() {
    if (!this.forgotEmail) return;
    
    this.isSendingReset = true;
    this.authService.forgotPassword(this.forgotEmail).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Sent', detail: res });
        this.isSendingReset = false;
        this.showForgotDialog = false;
        this.forgotEmail = '';
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to process request.' });
        this.isSendingReset = false;
      }
    });
  }
}