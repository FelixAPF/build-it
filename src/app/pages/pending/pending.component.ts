import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-pending',
  standalone: true,
  imports: [CommonModule, ButtonModule, RouterLink],
  templateUrl: './pending.component.html'
})
export class PendingComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);

  status: string = '';
  email: string = '';
  isAdminImpersonating: boolean = false;

  ngOnInit() {
    // 1. Check if we just came from the Registration page (URL Params)
    const queryStatus = this.route.snapshot.queryParamMap.get('status');
    const queryEmail = this.route.snapshot.queryParamMap.get('email');
    this.isAdminImpersonating = !!localStorage.getItem('admin_token'); // Check if backup token exists


    if (queryStatus) {
      this.status = queryStatus;
      this.email = queryEmail || '';
    } else {
      // 2. Fallback to LocalStorage if they hit this page via Login
      const storedStatus = localStorage.getItem('user_status');
      if (storedStatus) {
        this.status = storedStatus;
        this.email = localStorage.getItem('user_email') || '';
      } else {
        // If they have no status, they shouldn't be here
        this.router.navigate(['/login']);
      }
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

    returnToAdmin() {
    const adminToken = localStorage.getItem('admin_token');
    if (adminToken) {
      localStorage.setItem('jwt_token', adminToken);
      localStorage.setItem('user_role', 'ADMIN');
      localStorage.setItem('user_status', 'ACTIVE');
      localStorage.removeItem('admin_token'); // Clear the backup
      this.router.navigate(['/admin-dashboard']);
    }
  }
}