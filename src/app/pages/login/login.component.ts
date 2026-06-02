import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

// PrimeNG Modules
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageModule } from 'primeng/message';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, CardModule, InputTextModule, ButtonModule, MessageModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

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
          const status = localStorage.getItem('user_status'); // <-- Grab the status
          
          // Route them to the correct screen
          if (status === 'PENDING_VERIFICATION') {
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
          this.errorMessage = 'Invalid email or password.';
          console.error(err);
        }
      });
    }
  }
}