import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslatePipe],
  templateUrl: './verify-email.component.html'
})
export class VerifyEmailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private authService = inject(AuthService);

  // FIX: Added 'CHECK_INBOX' to the allowed statuses
  status: 'LOADING' | 'SUCCESS' | 'ERROR' | 'CHECK_INBOX' = 'LOADING';
  message: string = 'Verifying your email...';

  ngOnInit() {
    this.authService.logout();

    const token = this.route.snapshot.queryParamMap.get('token');
    
    // FIX: If there is no token, they just registered. Show the Inbox message!
    if (!token) {
      this.status = 'CHECK_INBOX';
      this.message = 'We sent a verification link to your email.';
      return;
    }

    // If there is a token, they clicked the email link. Fire the verification!
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