import { Routes, Router } from '@angular/router';
import { inject } from '@angular/core';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { WorkerDashboardComponent } from './pages/worker-dashboard/worker-dashboard.component';
import { BusinessDashboardComponent } from './pages/business-dashboard/business-dashboard.component';
import { LandingComponent } from './pages/landing/landing.component'; // <-- Import Landing
import { authGuard } from './guards/auth.guard';
import { AuthService } from './services/auth.service';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { PendingComponent } from './pages/pending/pending.component';

const guestGuard = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  
  if (authService.getToken()) {
    const role = localStorage.getItem('user_role');
    if (role === 'WORKER') return router.parseUrl('/worker-dashboard');
    if (role === 'BUSINESS') return router.parseUrl('/business-dashboard');
    if (role === 'ADMIN') return router.parseUrl('/admin-dashboard');
  }
  return true; 
};

export const routes: Routes = [
  { 
    path: '', 
    pathMatch: 'full', 
    redirectTo: () => {
      const authService = inject(AuthService);
      if (authService.getToken()) {
        const role = localStorage.getItem('user_role');
        if (role === 'WORKER') return '/worker-dashboard';
        if (role === 'BUSINESS') return '/business-dashboard';
        if (role === 'ADMIN') return '/admin-dashboard';
      }
      return '/landing'; 
    }
  },
  { path: 'landing', component: LandingComponent }, // Moved landing to its own path
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },
  { path: 'pending', component: PendingComponent },
  
  { path: 'worker-dashboard', component: WorkerDashboardComponent, canActivate: [authGuard] },
  { path: 'business-dashboard', component: BusinessDashboardComponent, canActivate: [authGuard] },
  { path: 'admin-dashboard', component: AdminDashboardComponent, canActivate: [authGuard] }
];