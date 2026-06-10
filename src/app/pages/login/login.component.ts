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
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-login',
  standalone: true,
imports: [
    ReactiveFormsModule, CardModule, InputTextModule, ButtonModule, 
    MessageModule, RouterLink, PasswordModule, DialogModule, ToastModule, CommonModule, FormsModule, TranslatePipe // <-- ADDED
  ],  providers: [MessageService],
  templateUrl: './login.component.html'
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private messageService = inject(MessageService);
  private route = inject(ActivatedRoute);
  private translate = inject(TranslateService); 

  showForgotDialog: boolean = false;
  forgotEmail: string = '';
  isSendingReset: boolean = false;

  // Track current language
  public currentLang = localStorage.getItem('buildit_lang') || 'en'; 

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
          
          // Add PENDING_UPLOAD to the trap block
          if (status === 'UNVERIFIED' || status === 'PENDING_VERIFICATION' || status === 'PENDING_UPLOAD') {
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

          // Deep string resolution for server objects to prevent [object Object] fallbacks
          let backendErrorMessage = 'Invalid email or password.';
          
          if (typeof err.error === 'string') {
            backendErrorMessage = err.error;
          } else if (err.error && typeof err.error.message === 'string') {
            backendErrorMessage = err.error.message;
          } else if (err.message) {
            backendErrorMessage = err.message;
          }

          if (backendErrorMessage.includes('verify your email')) {
            this.router.navigate(['/pending'], { 
              queryParams: { status: 'UNVERIFIED', email: this.loginForm.value.email } 
            });
          } else {
            this.errorMessage = backendErrorMessage;
          }
        }
      });
    }
  }
  switchLanguage() {
    this.currentLang = this.currentLang === 'en' ? 'fr' : 'en';
    this.translate.use(this.currentLang);
    localStorage.setItem('buildit_lang', this.currentLang);
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