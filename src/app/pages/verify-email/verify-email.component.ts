import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify-email.component.html'
})
export class VerifyEmailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  status: 'LOADING' | 'SUCCESS' | 'ERROR' = 'LOADING';
  message: string = 'Verifying your email...';

  ngOnInit() {
    // FIX: Clear out any lingering token data from registration 
    // so it doesn't mess with our next login attempt
    this.authService.logout();

    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token) {
      this.status = 'ERROR';
      this.message = 'Invalid verification link.';
      return;
    }

    // Fire the API call directly to Spring Boot
    this.authService.verifyEmail(token).subscribe({
      next: (res) => {
        this.status = 'SUCCESS';
        this.message = res;
      },
      error: (err) => {
        this.status = 'ERROR';
        this.message = err.error || 'Verification failed. Link may be expired.';
      }
    });
  }
}