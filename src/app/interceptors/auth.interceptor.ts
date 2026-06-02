import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  let clonedRequest = req;

  // 1. Attach the token if we have one
  if (token) {
    clonedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // 2. Send the request and intercept any errors returning from Spring Boot
  return next(clonedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      // If the backend rejects the token (expired, tampered, or missing)
      if (error.status === 401 || error.status === 403) {
        console.warn('Session expired or unauthorized. Redirecting to login.');
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};