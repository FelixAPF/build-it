import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.getToken()) {
    const status = localStorage.getItem('user_status');
    
    // TRAP: Block pending users from accessing active dashboards
    if (status === 'PENDING_VERIFICATION') {
      router.navigate(['/pending']);
      return false;
    }
    return true;
  }

  router.navigate(['/login']);
  return false;
};