import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ButtonModule } from 'primeng/button';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonModule],
  templateUrl: './landing.component.html'
})
export class LandingComponent {
  public authService = inject(AuthService); // <-- Ensure this is public

  // Quick helper to route them to the correct dashboard if they are already logged in
  getDashboardLink(): string {
    const role = localStorage.getItem('user_role');
    if (role === 'ADMIN') return '/admin-dashboard';
    return role === 'WORKER' ? '/worker-dashboard' : '/business-dashboard';
  }
}