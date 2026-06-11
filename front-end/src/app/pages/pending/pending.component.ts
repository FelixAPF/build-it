import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-pending',
  standalone: true,
  imports: [CommonModule, ButtonModule, ToastModule, TranslatePipe],
  providers: [MessageService],
  templateUrl: './pending.component.html'
})
export class PendingComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private messageService = inject(MessageService);

  status: string = '';
  email: string = '';
  isAdminImpersonating: boolean = false;
  selectedFile: File | null = null;
  isUploading: boolean = false;

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

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.messageService.add({ severity: 'info', summary: 'File Selected', detail: file.name });
    }
  }

  submitDocuments() {
    if (!this.selectedFile) return;
    
    this.isUploading = true;
    this.authService.uploadDocuments(this.selectedFile).subscribe({
      next: (res) => {
        this.isUploading = false;
        this.status = 'PENDING_VERIFICATION';
        localStorage.setItem('user_status', 'PENDING_VERIFICATION'); // Instantly upgrade their UI
        this.messageService.add({ severity: 'success', summary: 'Uploaded!', detail: 'Documents submitted for review.' });
      },
      error: (err) => {
        this.isUploading = false;
        this.messageService.add({ severity: 'error', summary: 'Error', detail: 'Failed to upload documents.' });
      }
    });
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