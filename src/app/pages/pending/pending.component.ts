import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ButtonModule } from 'primeng/button';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pending',
  standalone: true,
  imports: [ButtonModule, CommonModule],
  templateUrl: './pending.component.html'
})
export class PendingComponent implements OnInit {
  private authService = inject(AuthService);
  private router = inject(Router);
  isAdminImpersonating: boolean = false;

  ngOnInit(): void {
    this.isAdminImpersonating = !!localStorage.getItem('admin_token');
  }
  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
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